package servlets.authorisation;

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

        // Start generating hash asynchronously
        FutureTask<String> hashTask = new FutureTask<>(() -> {
            // Generate unique hash
            String hash;
            do {
                hash = RandomHash.nextHash(32);
            } while (dbServices.getResetAccountDBService().getNoteByToken(hash) != null);
            return hash;
        });
        new Thread(hashTask).start();


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

        if (redisService.isUserAlreadyRequested(username)) {
            sendCallback(resp, new EmailMsg(email, "A letter with instructions " +
                    "for resetting the password has already been sent to your mail. " +
                    "If you did not receive the letter, please wait 30 seconds and try again",
                    MSG_TOO_MUCH_LETTER_REQUESTS));
            return;
        }
        redisService.addEmailRequest(username);


        // Get hash from task
        String restoreToken;
        try {
            restoreToken = hashTask.get(5, TimeUnit.SECONDS);
        } catch (ExecutionException | InterruptedException e) {
            log.error("Generating restoreToken exception", e);
            sendServerInternalErrorCallback(resp);
            return;
        } catch (TimeoutException e) {
            log.error("Generate session hash too slow", e);
            sendServerInternalErrorCallback(resp);
            return;
        }


        SendEmail.sendLostPasswordLetter(email, username, restoreToken);

        ResetAccountDataSet dataSet = dbServices.getResetAccountDBService().getNoteByUsername(username);
        if (dataSet == null) {
            dataSet = new ResetAccountDataSet(restoreToken, username);
        } else {
            dataSet.setToken(restoreToken);
        }
        dbServices.getResetAccountDBService().addOrUpdateNote(dataSet);

        log.info(String.format("Player %s requested reset password", username));
        sendCallback(resp, new EmailMsg(email, "Letter sent", MSG_LETTER_SENT));


    }


}
