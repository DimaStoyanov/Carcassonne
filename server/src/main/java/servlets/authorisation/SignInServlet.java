package servlets.authorisation;

import dbService.dataSets.PlayersDataSet;
import dbService.exceptions.DBException;
import messages.DefaultMsg;
import messages.TokenMsg;
import servlets.AbstractHttpServlet;
import utils.RandomHash;

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


public class SignInServlet extends AbstractHttpServlet {


    public SignInServlet() {
        super();
    }

    @Override
    protected void processPostRequest(HttpServletRequest req, HttpServletResponse resp) throws DBException, IOException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");


        if (username == null || password == null) {
            sendCallback(resp, new DefaultMsg("Some parameters are missed", MSG_PARAM_MISSED));
            return;
        }

        // Start generating hash asynchronously
        FutureTask<String> hashTask = new FutureTask<>(() -> {
            // Generate unique hash
            String token;
            do {
                token = RandomHash.nextHash(32);
            } while (redisService.getUsernameBySession(token) != null);
            return token;
        });
        new Thread(hashTask).start();

        PlayersDataSet player = dbServices.getPlayersDBService().getPlayerByUsername(username);

        if (player == null || !argon2.verify(player.getPasswordHash(), password)) {
            sendCallback(resp, new DefaultMsg("Incorrect username/password", MSG_INCORRECT_LOGIN));
            return;
        }

        if (!player.isConfirmedEmail()) {
            sendCallback(resp,
                    new DefaultMsg("Email address does not confirmed", MSG_EMAIL_NOT_VERIFIED));
            return;
        }

        // Generate unique token
        String token;
        try {
            token = hashTask.get(5, TimeUnit.SECONDS);
        } catch (ExecutionException | InterruptedException e) {
            log.error("Generating session hash exception", e);
            sendServerInternalErrorCallback(resp);
            return;
        } catch (TimeoutException e) {
            log.error("Generate session hash too slow", e);
            sendServerInternalErrorCallback(resp);
            return;
        }

        redisService.addSession(token, username);
        log.info(String.format("Player %s signed in", username));
        sendCallback(resp, new TokenMsg(token, MSG_TOKEN));


    }
}
