package servlets;

import dbService.exceptions.DBException;
import messages.DefaultMsg;
import utils.RandomHash;
import utils.SendEmail;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by Dmitrii Stoianov
 */


public class SignUpServlet extends AbstractHttpServlet {


    public SignUpServlet() {
        super();
    }

    @Override
    protected void processPostRequest(HttpServletRequest req, HttpServletResponse resp)
            throws DBException, IOException {
        resp.setContentType("text/html;charset=utf-8");
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        String email = req.getParameter("email");


        if (username == null || password == null || email == null) {
            sendCallback(resp, new DefaultMsg("Some parameters are missed", MSG_PARAM_MISSED));
            return;
        }



            if (!dbServices.getPlayersDBService().isUsernameUnique(username)) {
                sendCallback(resp, new DefaultMsg("Not unique username", MSG_DUPLICATE_USERNAME));
                return;
            }

            if (!dbServices.getPlayersDBService().isEmailUnique(email)) {
                sendCallback(resp, new DefaultMsg("Not unique email address", MSG_DUPLICATE_EMAIL));
                return;
            }

            String passwordHash = argon2.hash(2, 65536, 1, password);
            if (!argon2.verify(passwordHash, password)) {
                log.error(String.format("Argon2 generated hash error: " +
                        "password=%s passwordHash=%s", password, passwordHash));
                sendServerInternalErrorCallback(resp);
                return;
            }

        // Generate unique hash
        String confirmationKey;
        int iterations = 0;
        do {
            iterations++;
            if (iterations > 100) {
                log.error("Can't generate unique hash. Done over 100 iterations");
                sendServerInternalErrorCallback(resp);
                return;
            }
            confirmationKey = RandomHash.nextHash(32);
        } while (dbServices.getConfirmationDBService().getNoteByConfirmationKey(confirmationKey) != null);

            try {
                if (!SendEmail.sendSignUpLetter(email, username, confirmationKey)) {
                    log.debug(String.format("Email address %s does not exist", email));
                    sendCallback(resp, new DefaultMsg("Invalid email address", MSG_INVALID_EMAIL));
                    return;
                }

            } catch (MessagingException e) {
                log.error(String.format("An error occurred while sending the message to %s", email), e);
                sendServerInternalErrorCallback(resp);
                return;
            }

            dbServices.getConfirmationDBService().addNote(confirmationKey, username);
            dbServices.getPlayersDBService().addPlayer(username, passwordHash, email);
        log.info(String.format("Player %s signed up", username));
        sendCallback(resp, new DefaultMsg("Need confirm email: " + email, MSG_NEED_CONFIRMATION));

    }


}
