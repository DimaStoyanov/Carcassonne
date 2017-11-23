package servlets.authorisation;

import dbService.dataSets.ConfirmationDataSet;
import dbService.dataSets.PlayersDataSet;
import dbService.dataSets.ResetAccountDataSet;
import dbService.exceptions.DBException;
import messages.DefaultMsg;
import messages.EmailMsg;
import servlets.AbstractHttpServlet;
import utils.RandomHash;
import utils.SendEmail;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
        String username = req.getParameter("data");
        String email = req.getParameter("email");

        if (type == null || username == null && email == null) {
            sendCallback(resp, new DefaultMsg("Some parameters are missed", MSG_PARAM_MISSED));
            return;
        }

        if (!type.equals("sign_up") && !type.equals("reset_password")) {
            sendCallback(resp, new DefaultMsg("Incorrect parameter type", MSG_INCORRECT_PARAM_TYPE));
            return;
        }

        // Start generating hash asynchronously
        FutureTask<String> hashTask = new FutureTask<>(() -> {
            // Generate unique hash
            String hash;
            do {
                hash = RandomHash.nextHash(32);
            } while (type.equals("sign_up") ?
                    dbServices.getConfirmationDBService().getNoteByConfirmationKey(hash) != null :
                    dbServices.getResetAccountDBService().getNoteByToken(hash) != null);
            return hash;
        });
        new Thread(hashTask).start();


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

                // Get hash from task
                String newConfirmationKey;
                try {
                    newConfirmationKey = hashTask.get(5, TimeUnit.SECONDS);
                } catch (ExecutionException | InterruptedException e) {
                    log.error("Generating confirmationKey exception", e);
                    sendServerInternalErrorCallback(resp);
                    return;
                } catch (TimeoutException e) {
                    log.error("Generate hash too slow", e);
                    sendServerInternalErrorCallback(resp);
                    return;
                }

                confirmationDataSet.setConfirmationKey(newConfirmationKey);
                dbServices.getConfirmationDBService().updateNote(confirmationDataSet);

                SendEmail.sendSignUpLetter(email, username, newConfirmationKey);

                log.info(String.format("Player %s re-requested sign up letter", username));
                sendCallback(resp, new EmailMsg(email, "Letter sent", MSG_LETTER_SENT));
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
                try {
                    restoreToken = hashTask.get(5, TimeUnit.SECONDS);
                } catch (ExecutionException | InterruptedException e) {
                    log.error("Generating restoreToken exception", e);
                    sendServerInternalErrorCallback(resp);
                    return;
                } catch (TimeoutException e) {
                    log.error("Generate hash too slow", e);
                    sendServerInternalErrorCallback(resp);
                    return;
                }

                ResetAccountDataSet resetAccountDataSet =
                        dbServices.getResetAccountDBService().getNoteByUsername(username);
                if (resetAccountDataSet == null) {
                    resetAccountDataSet = new ResetAccountDataSet(restoreToken, username);
                }
                resetAccountDataSet.setToken(restoreToken);
                dbServices.getResetAccountDBService().addOrUpdateNote(resetAccountDataSet);

                SendEmail.sendLostPasswordLetter(email, username, restoreToken);

                log.info(String.format("Player %s (re-)requested reset password", username));
                sendCallback(resp, new EmailMsg(email, "Letter sent", MSG_LETTER_SENT));
        }


    }
}
