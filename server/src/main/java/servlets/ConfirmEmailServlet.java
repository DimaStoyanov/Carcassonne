package servlets;

import com.google.gson.Gson;
import dbService.dataSets.ConfirmationDataSet;
import dbService.dataSets.PlayersDataSet;
import dbService.exceptions.DBException;
import dbService.services.DBServicesContainer;
import messages.ErrorMsg;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by Dmitrii Stoianov
 */


public class ConfirmEmailServlet extends HttpServlet {

    private final DBServicesContainer dbServices;
    private final Gson gson;

    public ConfirmEmailServlet() {
        dbServices = DBServicesContainer.getInstance();
        gson = new Gson();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String confirmationKey = req.getParameter("confirmationKey");

        if (confirmationKey == null) {
            resp.getWriter().write(gson.toJson(
                    new ErrorMsg("Parameter confirmationKey is missed", 300)));
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }


        try {
            ConfirmationDataSet dataSet = dbServices.getConfirmationDBService().getNote(confirmationKey);

            if (dataSet == null) {
//                resp.getWriter().write(gson.toJson(
//                        new ErrorMsg("Invalid confirmationKey", 305)));
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            PlayersDataSet player = dbServices.getPlayersDBService().getPlayerByUsername(dataSet.getUsername());
            dbServices.getConfirmationDBService().deleteNote(dataSet);

            if (player == null) {
//                resp.getWriter().write(gson.toJson(
//                        new ErrorMsg("Internal server error. Try to registration again", 304)
//                ));
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }

            player.setConfirmedEmail(true);
            dbServices.getPlayersDBService().updateNote(player);
//            resp.getWriter().write(gson.toJson(
//                    new OKMsg("Account confirmed.", 202)
//            ));
            resp.getWriter().println("Account successfully confirmed!\n");
            resp.setStatus(HttpServletResponse.SC_OK);

        } catch (DBException e) {
            e.printStackTrace();
//            resp.getWriter().write(gson.toJson(
//                    new ErrorMsg("Internal server error", 304)
//            ));
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
