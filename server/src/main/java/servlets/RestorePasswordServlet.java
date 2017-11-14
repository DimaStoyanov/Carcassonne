package servlets;

import com.google.gson.Gson;
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


public class RestorePasswordServlet extends HttpServlet {

    private final DBServicesContainer dbServices;
    private final Gson gson;
    private final String hostIP = "178.70.217.88";


    public RestorePasswordServlet() {
        dbServices = DBServicesContainer.getInstance();
        gson = new Gson();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = req.getParameter("username");
        String email = req.getParameter("email");

        if (username == null && email == null) {
            resp.getWriter().write(gson.toJson(
                    new ErrorMsg("Some parameters are missed", 300)
            ));
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {

            if (username != null) {
                PlayersDataSet player = dbServices.getPlayersDBService().getPlayerByUsername(username);

                if (player == null) {
                    resp.getWriter().write(gson.toJson(
                            new ErrorMsg("Player with username " + username + " not found", 307)
                    ));
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                }


            }
        } catch (DBException e) {
            e.printStackTrace();
        }
    }
}
