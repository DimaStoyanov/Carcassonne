package servlets;

import cashe.SessionCache;
import com.google.gson.Gson;
import dbService.dataSets.PlayersDataSet;
import dbService.exceptions.DBException;
import dbService.services.DBServicesContainer;
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import messages.ErrorMsg;
import messages.TokenMsg;
import utils.RandomHash;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by Dmitrii Stoianov
 */


public class SignInServlet extends HttpServlet {

    private final DBServicesContainer dbServices;
    private final SessionCache sessionCache;
    private final Gson gson;

    public SignInServlet() {
        dbServices = DBServicesContainer.getInstance();
        sessionCache = SessionCache.getInstance();
        gson = new Gson();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html;charset=utf-8");
        String username = req.getParameter("username");
        String password = req.getParameter("password");

        if (username == null || password == null) {
            resp.getWriter().write(gson.toJson(new ErrorMsg("Some parameters are missed", 300)));
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        try {
            PlayersDataSet player = dbServices.getPlayersDBService()
                    .getPlayerByUsername(username);
            Argon2 argon2 = Argon2Factory.create();

            if (player == null || !argon2.verify(player.getPasswordHash(), password)) {
                resp.getWriter().write(gson.toJson(
                        new ErrorMsg("Incorrect username/password", 306)
                ));
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            if (!player.isConfirmedEmail()) {
                resp.getWriter().write(gson.toJson(
                        new ErrorMsg("Email address does not confirmed", 307)
                ));
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }


            String token = RandomHash.nextHash(16);
            sessionCache.addSession(token, username);
            resp.getWriter().write(gson.toJson(
                    new TokenMsg(token)
            ));
            resp.setStatus(HttpServletResponse.SC_OK);

        } catch (DBException e) {
            e.printStackTrace();
            resp.getWriter().write(gson.toJson(
                    new ErrorMsg("Internal server error", 304)
            ));
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
