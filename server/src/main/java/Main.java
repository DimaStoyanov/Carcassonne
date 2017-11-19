import dbService.services.DBServicesContainer;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import servlets.*;

/**
 * Created by Dmitrii Stoianov
 */


public class Main {


    public static void main(String[] args) {

        DBServicesContainer dbServices = DBServicesContainer.getInstance();
        dbServices.printConnectInfo();



        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);

        context.addServlet(new ServletHolder(new SignUpServlet()), "/sign_up");
        context.addServlet(new ServletHolder(new SignInServlet()), "/sign_in");
        context.addServlet(new ServletHolder(new VerifyAccountServlet()), "/verify");
        context.addServlet(new ServletHolder(new SetPasswordServlet()), "/set_password");
        context.addServlet(new ServletHolder(new ResetPasswordServlet()), "/reset_password");
        context.addServlet(new ServletHolder(new SendEmailServlet()), "/send_email");

        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[]{context});

        Server server = new Server(8080);
        server.setHandler(handlers);

        try {
            server.start();
            server.join();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
