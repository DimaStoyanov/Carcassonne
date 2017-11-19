package servlets;

import dbService.dataSets.ConfirmationDataSet;
import dbService.dataSets.PlayersDataSet;
import dbService.exceptions.DBException;
import messages.DefaultMsg;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by Dmitrii Stoianov
 */


public class VerifyAccountServlet extends AbstractHttpServlet {


    public VerifyAccountServlet() {
        super();
    }

    @Override
    protected void processPostRequest(HttpServletRequest req, HttpServletResponse resp)
            throws DBException, IOException {

        System.out.println(req.getRemoteAddr());
        String confirmationKey = req.getParameter("confirmationKey");


        if (confirmationKey == null) {
            sendCallback(resp, new DefaultMsg("Parameter confirmationKey is missed", MSG_PARAM_MISSED));
            return;
        }


        ConfirmationDataSet dataSet =
                dbServices.getConfirmationDBService().getNoteByConfirmationKey(confirmationKey);

        if (dataSet == null) {
            sendCallback(resp,
                    new DefaultMsg("Incorrect confirmationKey", MSG_INCORRECT_CONFIRMATION_KEY));
            return;
        }

        PlayersDataSet player = dbServices.getPlayersDBService().getPlayerByUsername(dataSet.getUsername());
        dbServices.getConfirmationDBService().deleteNote(dataSet);

        if (player == null) {
            log.error(String.format("Player %s exist in db \'confirmation\', but doesn't exist in " +
                    "db \'players\'", dataSet.getUsername()));
            sendServerInternalErrorCallback(resp);
            return;
        }

        player.setConfirmedEmail(true);
        dbServices.getPlayersDBService().updateNote(player);
        log.info(String.format("Player %s verified account", player.getUsername()));
        sendCallback(resp, new DefaultMsg("Account successfully confirmed", MSG_VERIFIED));

    }
}
