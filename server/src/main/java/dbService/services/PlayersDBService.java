package dbService.services;

import dbService.dataSets.PlayersDataSet;
import dbService.exceptions.DBException;

/**
 * Created by Dmitrii Stoianov
 */


public class PlayersDBService extends AbstractDBService {


    PlayersDBService() {
        super(PlayersDataSet.class);
    }

    public void addPlayer(String username, String password, String email) throws DBException {
        super.addNote(new PlayersDataSet(username, password, email));
    }


    public PlayersDataSet getPlayerByUsername(String username) throws DBException {
        return super.getNote("username", username);
    }

    public PlayersDataSet getPlayerByEmail(String email) throws DBException {
        return super.getNote("email", email);
    }

    public boolean isUsernameUnique(String username) throws DBException {
        return getPlayerByUsername(username) == null;
    }

    public boolean isEmailUnique(String email) throws DBException {
        return getPlayerByEmail(email) == null;
    }

}
