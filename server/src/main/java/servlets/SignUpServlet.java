package servlets;

import com.google.gson.Gson;
import dbService.exceptions.DBException;
import dbService.services.DBServicesContainer;
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import messages.AbstractMsg;
import messages.ErrorMsg;
import messages.OKMsg;
import utils.RandomHash;
import utils.SendEmail;

import javax.mail.MessagingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by Dmitrii Stoianov
 */


public class SignUpServlet extends HttpServlet {

    private final DBServicesContainer dbServices;
    private final Gson gson;
    private final String hostIP = "178.70.217.88";

    public SignUpServlet() {
        dbServices = DBServicesContainer.getInstance();
        gson = new Gson();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html;charset=utf-8");
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        String email = req.getParameter("email");


        if (username == null || password == null || email == null) {
            sendCallback(resp, new ErrorMsg("Some parameters are missed", 300));
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }


        try {

            if (!dbServices.getPlayersDBService().isUsernameUnique(username)) {
                sendCallback(resp, new ErrorMsg("Not unique username", 301));
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            if (!dbServices.getPlayersDBService().isEmailUnique(email)) {
                sendCallback(resp, new ErrorMsg("Not unique email address", 302));
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            Argon2 argon2 = Argon2Factory.create();
            String passwordHash = argon2.hash(2, 65536, 1, password);
            if (!argon2.verify(passwordHash, password)) {
                sendCallback(resp, new ErrorMsg("Server internal error", 304));
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }

            String confirmationKey = RandomHash.nextHash(16);
            String header = "Confirmation your Carcassonne account";
            String body = " thanks for creating a Carcassonne account. We are happy you found us.\n" +
                    "To confirm you account, please follow this link.";
            String hostname = hostIP + ":8080";
            String path = "/confirmation";

            try {

                if (!SendEmail.sendMsg(email, header,
                        String.format("Dear %s,%s\n%s%s?confirmationKey=%s",
                                username, body, hostname, path, confirmationKey))) {
                    sendCallback(resp, new ErrorMsg("Invalid email address", 303));
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }

            } catch (MessagingException e) {
                e.printStackTrace();
                sendCallback(resp,
                        new ErrorMsg("Can't send email. It can be internal server error", 304));
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }

            dbServices.getConfirmationDBService().addNote(confirmationKey, username);
            dbServices.getPlayersDBService().addPlayer(username, passwordHash, email);
            sendCallback(resp, new OKMsg("Need confirm email: " + email, 100));
            resp.setStatus(HttpServletResponse.SC_OK);
        } catch (DBException e) {
            e.printStackTrace();
            sendCallback(resp, new ErrorMsg("Internal server error.", 304));
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void sendCallback(HttpServletResponse resp, AbstractMsg msg) throws IOException {
        resp.getWriter().write(gson.toJson(msg));
    }
}
