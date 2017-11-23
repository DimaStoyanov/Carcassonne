package servlets.authorisation;

import dbService.exceptions.DBException;
import messages.DefaultMsg;
import servlets.AbstractHttpServlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by Dmitrii Stoianov
 */


public class CheckUniqueServlet extends AbstractHttpServlet {


    @Override
    protected void processPostRequest(HttpServletRequest req, HttpServletResponse resp) throws DBException, IOException {

        String type = req.getParameter("type");
        String data = req.getParameter("data");

        if (type == null || data == null) {
            sendCallback(resp, new DefaultMsg("Some parameters are missed", MSG_PARAM_MISSED));
            return;
        }

        switch (type) {
            case "username":
                boolean unique = dbServices.getPlayersDBService().isUsernameUnique(data);
                if (unique) {
                    sendCallback(resp, new DefaultMsg("Unique username", MSG_UNIQUE_USERNAME));
                } else {
                    sendCallback(resp, new DefaultMsg("Not unique username", MSG_DUPLICATE_USERNAME));
                }
                break;
            case "email":
                unique = dbServices.getPlayersDBService().isEmailUnique(data);
                if (unique) {
                    sendCallback(resp, new DefaultMsg("Unique email", MSG_UNIQUE_EMAIL));
                } else {
                    sendCallback(resp, new DefaultMsg("Not unique email", MSG_DUPLICATE_EMAIL));
                }
                break;
            default:
                sendCallback(resp, new DefaultMsg("Incorrect parameter type", MSG_INCORRECT_PARAM_TYPE));
        }


    }
}
