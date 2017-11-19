package servlets;

import dbService.dataSets.PlayersDataSet;
import dbService.exceptions.DBException;
import messages.DefaultMsg;
import messages.TokenMsg;
import utils.RandomHash;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

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
        int iterations = 0;
        do {
            iterations++;
            token = RandomHash.nextHash(32);
            if (iterations > 100) {
                log.error("Can't generate unique hash. Done over 100 iterations");
                sendServerInternalErrorCallback(resp);
                return;
            }
        } while (redisService.getUsernameBySession(token) != null);

        redisService.addSession(token, username);
        log.info(String.format("Player %s signed in", username));
        sendCallback(resp, new TokenMsg(token, MSG_TOKEN));


    }
}
