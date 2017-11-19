package utils;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Date;
import java.util.Properties;

/**
 * Created by Dmitrii Stoianov
 */


public class SendEmail {


    private static final String CLIENT_URL = "DimaStoyanov.github.io/Carcassonne/client";


    private static boolean sendMsg(String to, String subject, String body) throws MessagingException {

        final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
        Properties props = System.getProperties();
        props.setProperty("mail.smtp.host", "smtp.gmail.com");
        props.setProperty("mail.smtp.socketFactory.class", SSL_FACTORY);
        props.setProperty("mail.smtp.socketFactory.fallback", "false");
        props.setProperty("mail.smtp.port", "465");
        props.setProperty("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.auth", "true");
        props.put("mail.store.protocol", "pop3");
        props.put("mail.transport.protocol", "smtp");
        final String username = "carcassone.game@gmail.com";
        final String password = "ConcurentHashMap";


        try {


            final Session session = Session.getDefaultInstance(props,
                    new Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(username, password);
                        }
                    });

            Message msg = new MimeMessage(session);

            msg.setFrom(new InternetAddress("carcassonne.game@gmail.com"));
            msg.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(to, false));
            msg.setSubject(subject);
            msg.setContent(body, "text/html");
            msg.saveChanges();
            msg.setSentDate(new Date());
            Transport.send(msg);
        } catch (SendFailedException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    public static boolean sendSignUpLetter(String to, String username, String token)
            throws MessagingException {
        String header = "Carcassonne Game account verification for " + username;
        String body = String.format("Hello %s!<br><br>" +
                        "" +
                        "Thanks you for joining Carcassonne Game." +
                        " We are happy you found us.<br><br>" +
                        "" +
                        "Verify your email address and start playing Carcassonne here:<br>" +
                        "" +
                        "<a href=\"http://%s/verify?confirmationKey=%s\">Confirm account</a><br><br>" +
                        "" +
                        "Thanks,<br>" +
                        "The Carcassonne Game Team<br>",
                username, CLIENT_URL, token);
        return sendMsg(to, header, body);
    }

    public static boolean sendLostPasswordLetter(String to, String username, String token)
            throws MessagingException {
        String header = "Password Reset Request - Carcassonne Game";
        String body = String.format("Hi %s,<br><br>" +
                "" +
                "Someone, probably you, is having trouble logging in to Carcassonne Game account " +
                "and has requested help.<br><br>" +
                "" +
                "Your username is: %s<br>" +
                "To reset your password, please use the link: " +
                "<a href=\"http://%s/lostpassword?token=%s\">Reset password</a><br><br>" +
                "" +
                "If you didn't request this information, there's nothing to worry about - " +
                "simply ignore this email.<br><br>" +
                "" +
                "Thanks,<br>" +
                "Carcassonne Game Team<br>", username, username, CLIENT_URL, token);
        return sendMsg(to, header, body);
    }

}