package servlets;

import dbService.dataSets.ConfirmationDataSet;
import dbService.dataSets.PlayersDataSet;
import dbService.dataSets.ResetAccountDataSet;
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


public class SendEmailServlet extends AbstractHttpServlet {


    public SendEmailServlet() {
        super();
    }

    @Override
    protected void processPostRequest(HttpServletRequest req, HttpServletResponse resp)
            throws DBException, IOException {
        String type = req.getParameter("type");
        String username = req.getParameter("username");
        String email = req.getParameter("email");

        if (type == null || username == null && email == null) {
            sendCallback(resp, new DefaultMsg("Some parameters are missed", MSG_PARAM_MISSED));
            return;
        }

        try {

            PlayersDataSet player = username == null ?
                    dbServices.getPlayersDBService().getPlayerByEmail(email) :
                    dbServices.getPlayersDBService().getPlayerByUsername(username);


            if (player == null) {
                sendCallback(resp,
                        new DefaultMsg("Player with specified email or username does not exist",
                                MSG_PLAYER_DOES_NOT_EXIST));
                return;
            }

            username = player.getUsername();
            email = player.getEmail();

            if (redisService.isUserAlreadyRequested(username)) {
                sendCallback(resp, new EmailMsg(email, "Letter with instructions " +
                        "has already sent. If you did not receive the letter " +
                        "try again after 30 seconds",
                        MSG_TOO_MUCH_LETTER_REQUESTS));
                return;
            }
            redisService.addEmailRequest(username);


            switch (type) {
                case "sign_up":

                    if (player.isConfirmedEmail()) {
                        sendCallback(resp, new DefaultMsg("Account has already been verified",
                                MSG_ALREADY_VERIFIED));
                        return;
                    }

                    ConfirmationDataSet confirmationDataSet = dbServices.getConfirmationDBService()
                            .getNoteByUsername(username);

                    // Generate new unique confirmation key
                    String newConfirmationKey;
                    int iterations = 0;
                    do {
                        iterations++;
                        if (iterations > 100) {
                            log.error("Can't generate new unique confirmationKey. " +
                                    "Done over 100 iterations");
                            sendServerInternalErrorCallback(resp);
                            return;
                        }
                        newConfirmationKey = RandomHash.nextHash(32);
                    }
                    while (dbServices.getConfirmationDBService().getNoteByConfirmationKey(newConfirmationKey) != null);

                    confirmationDataSet.setConfirmationKey(newConfirmationKey);
                    dbServices.getConfirmationDBService().updateNote(confirmationDataSet);

                    if (!SendEmail.sendSignUpLetter(email, username, newConfirmationKey)) {
                        sendServerInternalErrorCallback(resp);
                        log.error(String.format("Wrong email address %s was written in " +
                                "database \'confirmation\'", email));
                        return;
                    }

                    log.info(String.format("Player %s re-requested sign up letter", username));
                    sendCallback(resp, new EmailMsg(email, "Letter successfully sent", MSG_LETTER_SENT));
                    break;

                case "reset_password":


                    if (!player.isConfirmedEmail()) {
                        sendCallback(resp, new EmailMsg(email, "Account does not verified. " +
                                "First of all verify your email address and then " +
                                "you can reset password.", MSG_EMAIL_NOT_VERIFIED));
                        return;
                    }

                    // Generate new unique restoreToken
                    String restoreToken;
                    iterations = 0;
                    do {
                        iterations++;
                        if (iterations > 100) {
                            log.error("Can't generate new unique restoreToken. Done over 100 iterations");
                            sendServerInternalErrorCallback(resp);
                            return;
                        }
                        log.debug("Generate new restore token");
                        restoreToken = RandomHash.nextHash(64);
                    } while (dbServices.getResetAccountDBService().getNoteByToken(restoreToken) != null);

                    ResetAccountDataSet resetAccountDataSet =
                            dbServices.getResetAccountDBService().getNoteByUsername(username);
                    if (resetAccountDataSet == null) {
                        resetAccountDataSet = new ResetAccountDataSet(restoreToken, username);
                    }
                    resetAccountDataSet.setToken(restoreToken);
                    dbServices.getResetAccountDBService().addOrUpdateNote(resetAccountDataSet);

                    if (!SendEmail.sendLostPasswordLetter(email, username, restoreToken)) {
                        log.error(String.format("Wrong email address %s was written in " +
                                "database 'reset_account'", email));
                        sendServerInternalErrorCallback(resp);
                        return;
                    }

                    log.info(String.format("Player %s (re-)requested reset password", username));
                    sendCallback(resp, new EmailMsg(email, "Letter successfully sent", MSG_LETTER_SENT));
                    break;
                default:
                    sendCallback(resp, new DefaultMsg("Incorrect parameter type", MSG_INCORRECT_PARAM_TYPE));
            }


        } catch (MessagingException e) {
            log.error(String.format("Error occurred while sending letter to %s", email), e);
            sendServerInternalErrorCallback(resp);
        }
    }
}
