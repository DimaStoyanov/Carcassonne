package servlets;

import dbService.dataSets.PlayersDataSet;
import dbService.exceptions.DBException;
import messages.DefaultMsg;
import messages.EmailMsg;
import utils.RandomHash;
import utils.SendEmail;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by Dmitrii Stoianov
 */


public class ResetPasswordServlet extends AbstractHttpServlet {


    public ResetPasswordServlet() {
        super();
    }

    @Override
    protected void processPostRequest(HttpServletRequest req, HttpServletResponse resp)
            throws DBException, IOException {
        String username = req.getParameter("username");
        String email = req.getParameter("email");

        if (username == null && email == null) {
            sendCallback(resp, new DefaultMsg("Some parameters are missed", MSG_PARAM_MISSED));
            return;
        }

        PlayersDataSet player;
        player = username == null ?
                dbServices.getPlayersDBService().getPlayerByEmail(email) :
                dbServices.getPlayersDBService().getPlayerByUsername(username);


        if (player == null) {
            sendCallback(resp, new DefaultMsg(
                    "Player with specified username/email does not exist", MSG_PLAYER_DOES_NOT_EXIST));
            return;
        }


        username = player.getUsername();
        email = player.getEmail();

        if (!player.isConfirmedEmail()) {
            sendCallback(resp, new EmailMsg(email, "Account does not verified. " +
                    "First of all verify your email address and then " +
                    "you can reset password.", MSG_EMAIL_NOT_VERIFIED));
            return;
        }

        // Generate unique restoreToken
        String restoreToken;
        int iterations = 0;
        do {
            iterations++;
            if (iterations > 100) {
                log.error("Can't generate restoreToken. Done over 100 iterations");
                sendServerInternalErrorCallback(resp);
            }
            restoreToken = RandomHash.nextHash(64);
        } while (dbServices.getResetAccountDBService().getNoteByToken(restoreToken) != null);


        try {
            if (!SendEmail.sendLostPasswordLetter(email, username, restoreToken)) {
                log.error(String.format("Invalid email address %s was written in database 'users'", email));
                sendServerInternalErrorCallback(resp);
                return;
            }

            if (redisService.isUserAlreadyRequested(username)) {
                sendCallback(resp, new EmailMsg(email, "A letter with instructions " +
                        "for resetting the password has already been sent to your mail. " +
                        "If you did not receive the letter, please wait 30 seconds and try again",
                        MSG_TOO_MUCH_LETTER_REQUESTS));
                return;
            }


            dbServices.getResetAccountDBService().addNote(restoreToken, player.getUsername());
            log.info(String.format("Player %s requested reset password", username));
            sendCallback(resp, new EmailMsg(email, "Letter successfully sent", MSG_LETTER_SENT));

        } catch (MessagingException e) {
            log.error(String.format("Error occurred while sending letter to %s", email), e);
            sendServerInternalErrorCallback(resp);
        }


    }


}
