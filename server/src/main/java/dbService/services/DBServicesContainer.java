package dbService.services;

/**
 * Created by Dmitrii Stoianov
 */


public class DBServicesContainer {
    private static DBServicesContainer ourInstance = new DBServicesContainer();

    public static DBServicesContainer getInstance() {
        return ourInstance;
    }


    private final PlayersDBService playersDBService;
    private final ConfirmationDBService confirmationDBService;

    private DBServicesContainer() {
        this.confirmationDBService = new ConfirmationDBService();
        this.playersDBService = new PlayersDBService();
    }

    public ConfirmationDBService getConfirmationDBService() {
        return confirmationDBService;
    }

    public PlayersDBService getPlayersDBService() {
        return playersDBService;
    }

    public void printConnectInfo() {
        playersDBService.printConnectInfo();
    }
}
