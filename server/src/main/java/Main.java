import dbService.services.DBServicesContainer;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import servlets.authorisation.*;

/**
 * Created by Dmitrii Stoianov
 */


public class Main {


    public static void main(String[] args) {

        System.setProperty("user.timezone", "GMT+3");
        DBServicesContainer dbServices = DBServicesContainer.getInstance();
        dbServices.printConnectInfo();

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);

        context.addServlet(new ServletHolder(new SignUpServlet()), "/sign_up");
        context.addServlet(new ServletHolder(new SignInServlet()), "/sign_in");
        context.addServlet(new ServletHolder(new VerifyAccountServlet()), "/verify");
        context.addServlet(new ServletHolder(new SetPasswordServlet()), "/set_password");
        context.addServlet(new ServletHolder(new ResetPasswordServlet()), "/reset_password");
        context.addServlet(new ServletHolder(new SendEmailServlet()), "/send_email");
        context.addServlet(new ServletHolder(new CheckUniqueServlet()), "/check_unique");

        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[]{context});

        Server server = new Server(8080);
        server.setHandler(handlers);

        try {
            server.setStopTimeout(5000);
            server.start();


            server.join();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
