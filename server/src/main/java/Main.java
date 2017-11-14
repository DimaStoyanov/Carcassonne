import dbService.services.DBServicesContainer;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import servlets.ConfirmEmailServlet;
import servlets.SignInServlet;
import servlets.SignUpServlet;

/**
 * Created by Dmitrii Stoianov
 */


public class Main {


    public static void main(String[] args) {

        DBServicesContainer dbServices = DBServicesContainer.getInstance();
        dbServices.printConnectInfo();


        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.addServlet(new ServletHolder(new SignUpServlet()), "/signup");
        context.addServlet(new ServletHolder(new SignInServlet()), "/signin");
        context.addServlet(new ServletHolder(new ConfirmEmailServlet()), "/confirmation");
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
