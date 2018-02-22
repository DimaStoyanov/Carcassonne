package carcassone.alpine_meadows.utils;

import org.apache.log4j.Logger;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.persistence.CascadeType;
import javax.persistence.OneToMany;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Dmitrii Stoianov
 */


public final class SendEmail {

    private static ExecutorService executorService = Executors.newSingleThreadExecutor();


    private static final String CLIENT_URL = "carcassonne-alpine-meadows.herokuapp.com";


    private SendEmail() {
    }

    @OneToMany(cascade = CascadeType.REFRESH)
    public static void sendSignUpLetter(String to, String username, String token) {
        String header = "Carcassonne Game account verification for " + username;
        String body = String.format("Hello %s!<br><br>" +
                        "" +
                        "Thanks you for joining Carcassonne Game." +
                        " We are happy you found us.<br><br>" +
                        "" +
                        "Verify your email address and start playing Carcassonne here:<br>" +
                        "" +
                        "<a href=\"https://%s/verify?token=%s\">Confirm account</a><br><br>" +
                        "" +
                        "Thanks,<br>" +
                        "The Carcassonne Game Team<br>",
                username, CLIENT_URL, token);
        executorService.execute(new SendEmailRunnable(to, header, body));
    }

    public static void sendLostPasswordLetter(String to, String username, String token) {
        String header = "Password Reset Request - Carcassonne Game";
        String body = String.format("Hi %s,<br><br>" +
                "" +
                "Someone, probably you, is having trouble logging in to Carcassonne Game account " +
                "and has requested help.<br><br>" +
                "" +
                "Your username is: %s<br>" +
                "To reset your password, please use the link: " +
                "<a href=\"https://%s/set_password?token=%s\">Reset password</a><br><br>" +
                "" +
                "If you didn't request this information, there's nothing to worry about - " +
                "simply ignore this email.<br><br>" +
                "" +
                "Thanks,<br>" +
                "Carcassonne Game Team<br>", username, username, CLIENT_URL, token);
        executorService.execute(new SendEmailRunnable(to, header, body));
    }


    private static class SendEmailRunnable implements Runnable {


        private static final Logger log = Logger.getLogger(SendEmail.class);

        private static final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";

        private static final Properties props = System.getProperties();

        private static final String username = "carcassone.game@gmail.com";
        private static final String password = "ConcurentHashMap";

        static {
            props.setProperty("mail.smtp.host", "smtp.gmail.com");
            props.setProperty("mail.smtp.socketFactory.class", SSL_FACTORY);
            props.setProperty("mail.smtp.socketFactory.fallback", "false");
            props.setProperty("mail.smtp.port", "465");
            props.setProperty("mail.smtp.socketFactory.port", "465");
            props.put("mail.smtp.auth", "true");
            props.put("mail.store.protocol", "pop3");
            props.put("mail.transport.protocol", "smtp");
        }

        private final String to;
        private final String header;
        private final String body;

        SendEmailRunnable(String to, String header, String body) {
            this.to = to;
            this.header = header;
            this.body = body;
        }


        @Override
        public void run() {

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
                msg.setSubject(header);
                msg.setContent(body, "text/html");
                msg.saveChanges();
                msg.setSentDate(new Date());
                Transport.send(msg);
            } catch (MessagingException e) {
                log.error("Error sending email", e);
            }
        }

    }
}