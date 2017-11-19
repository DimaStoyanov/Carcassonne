package servlets;

import cashe.RedisService;
import com.google.gson.Gson;
import dbService.exceptions.DBException;
import dbService.services.DBServicesContainer;
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import messages.DefaultMsg;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by Dmitrii Stoianov
 */


public abstract class AbstractHttpServlet extends HttpServlet {

    final DBServicesContainer dbServices;
    final RedisService redisService;
    final Argon2 argon2;
    // TODO: it's may be a problem to create new instance
    // of logger for every servlet
    // so https://logging.apache.org/log4j/2.x/manual/webapp.html
    private final Gson gson;
    static Logger log;

    final static int MSG_NEED_CONFIRMATION = 100;
    final static int MSG_VERIFIED = 101;
    final static int MSG_CHANGED_PASSWORD = 102;
    final static int MSG_LETTER_SENT = 103;
    final static int MSG_TOKEN = 200;
    final static int MSG_PARAM_MISSED = 300;
    final static int MSG_DUPLICATE_USERNAME = 301;
    final static int MSG_DUPLICATE_EMAIL = 302;
    final static int MSG_INVALID_EMAIL = 303;
    final static int MSG_INCORRECT_CONFIRMATION_KEY = 304;
    final static int MSG_INCORRECT_LOGIN = 305;
    final static int MSG_INCORRECT_TOKEN = 306;
    final static int MSG_PLAYER_DOES_NOT_EXIST = 307;
    final static int MSG_ALREADY_VERIFIED = 308;
    final static int MSG_EMAIL_NOT_VERIFIED = 309;
    final static int MSG_TOO_MUCH_LETTER_REQUESTS = 310;
    final static int MSG_INCORRECT_PARAM_TYPE = 311;
    private final static int MSG_LIMIT_REQUESTS_PER_SECOND = 313;
    private final static int MSG_INTERNAL_SERVER_ERROR = 314;

    private final static String ISE_JSON = new Gson().toJson(new DefaultMsg(
            "Internal server error", MSG_INTERNAL_SERVER_ERROR
    ));

    AbstractHttpServlet() {
        dbServices = DBServicesContainer.getInstance();
        redisService = RedisService.getInstance();
        gson = new Gson();
        argon2 = Argon2Factory.create();
        log = Logger.getLogger(this.getClass());
    }


    void sendCallback(HttpServletResponse resp, DefaultMsg msg) throws IOException {
        String json = gson.toJson(msg);
        resp.getWriter().write(json);
        log.info("Send response " + json);
        if (msg.getCode() <= MSG_TOKEN) {
            resp.setStatus(HttpServletResponse.SC_OK);
        } else if (msg.getCode() < MSG_INTERNAL_SERVER_ERROR) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } else {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    void sendServerInternalErrorCallback(HttpServletResponse resp) throws IOException {
        resp.getWriter().write(ISE_JSON);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html;charset=utf-8");

        if (redisService.getAndUpdateCountIPRequests(req.getRemoteAddr()) > 3) {
            sendCallback(resp, new DefaultMsg("Too much request per second",
                    MSG_LIMIT_REQUESTS_PER_SECOND));
            return;
        }

        try {
            processPostRequest(req, resp);
        } catch (DBException e) {
            log.error("Error occurred in database", e);
            sendServerInternalErrorCallback(resp);
        }
    }

    abstract void processPostRequest(HttpServletRequest req, HttpServletResponse resp)
            throws DBException, IOException;


}
