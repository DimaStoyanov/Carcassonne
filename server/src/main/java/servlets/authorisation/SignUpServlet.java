package servlets.authorisation;

import dbService.exceptions.DBException;
import messages.DefaultMsg;
import servlets.AbstractHttpServlet;
import utils.RandomHash;
import utils.SendEmail;

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


public class SignUpServlet extends AbstractHttpServlet {


    public SignUpServlet() {
        super();
    }

    @Override
    protected void processPostRequest(HttpServletRequest req, HttpServletResponse resp)
            throws DBException, IOException {
        resp.setContentType("text/html;charset=utf-8");
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        String email = req.getParameter("email");


        if (username == null || password == null || email == null) {
            sendCallback(resp, new DefaultMsg("Some parameters are missed", MSG_PARAM_MISSED));
            return;
        }


        // Start generating hash asynchronously
        FutureTask<String> hashTask = new FutureTask<>(() -> {
            // Generate unique hash
            String confirmationKey;
            do {
                confirmationKey = RandomHash.nextHash(32);
            } while (dbServices.getConfirmationDBService().getNoteByConfirmationKey(confirmationKey) != null);
            return confirmationKey;
        });
        new Thread(hashTask).start();

        if (!dbServices.getPlayersDBService().isUsernameUnique(username)) {
            sendCallback(resp, new DefaultMsg("Not unique username", MSG_DUPLICATE_USERNAME));
            return;
        }

        if (!dbServices.getPlayersDBService().isEmailUnique(email)) {
            sendCallback(resp, new DefaultMsg("Not unique email address", MSG_DUPLICATE_EMAIL));
            return;
        }

        String passwordHash = argon2.hash(2, 65536, 1, password);
        if (!argon2.verify(passwordHash, password)) {
            log.error(String.format("Argon2 generated hash error: " +
                    "password=%s passwordHash=%s", password, passwordHash));
            sendServerInternalErrorCallback(resp);
            return;
        }

        String confirmationKey;
        try {
            confirmationKey = hashTask.get(5, TimeUnit.SECONDS);
        } catch (ExecutionException | InterruptedException e) {
            log.error("Generating hash exception", e);
            sendServerInternalErrorCallback(resp);
            return;
        } catch (TimeoutException e) {
            log.error("Generate hash too slow", e);
            sendServerInternalErrorCallback(resp);
            return;
        }


        SendEmail.sendSignUpLetter(email, username, confirmationKey);
        dbServices.getConfirmationDBService().addNote(confirmationKey, username);
        dbServices.getPlayersDBService().addPlayer(username, passwordHash, email);
        log.info(String.format("Player %s signed up", username));
        sendCallback(resp, new DefaultMsg("Need confirm email: " + email, MSG_NEED_CONFIRMATION));

    }


}
