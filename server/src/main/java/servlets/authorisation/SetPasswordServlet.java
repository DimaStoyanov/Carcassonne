package servlets.authorisation;

import dbService.dataSets.PlayersDataSet;
import dbService.dataSets.ResetAccountDataSet;
import dbService.exceptions.DBException;
import messages.DefaultMsg;
import messages.EmailMsg;
import servlets.AbstractHttpServlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by Dmitrii Stoianov
 */


public class SetPasswordServlet extends AbstractHttpServlet {


    public SetPasswordServlet() {
        super();
    }

    @Override
    protected void processPostRequest(HttpServletRequest req, HttpServletResponse resp)
            throws DBException, IOException {
        String token = req.getParameter("token");
        String password = req.getParameter("password");

        if (token == null || password == null) {
            sendCallback(resp, new DefaultMsg("Some parameters are missed", MSG_PARAM_MISSED));
            return;
        }


        ResetAccountDataSet resetAccountDataSet = dbServices.getResetAccountDBService()
                .getNoteByToken(token);

        if (resetAccountDataSet == null) {
            sendCallback(resp, new DefaultMsg("Incorrect token", MSG_INCORRECT_TOKEN));
            return;
        }

        PlayersDataSet player = dbServices.getPlayersDBService().getPlayerByUsername
                (resetAccountDataSet.getUsername());
        dbServices.getResetAccountDBService().deleteNote(resetAccountDataSet);

        if (!player.isConfirmedEmail()) {
            sendCallback(resp, new EmailMsg(player.getEmail(),
                    "Account does not verified. " +
                            "First of all verify your email address and then " +
                            "you can reset password.", MSG_EMAIL_NOT_VERIFIED));
            return;
        }

        player.setPasswordHash(argon2.hash(2, 65536, 1, password));
        dbServices.getPlayersDBService().updateNote(player);
        log.info(String.format("Player %s change password", player.getUsername()));
        sendCallback(resp, new DefaultMsg("Password successfully changed", MSG_CHANGED_PASSWORD));

    }

}
