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

    @SuppressWarnings("UnusedReturnValue")
    public long addPlayer(String username, String password, String email) throws DBException {
        return super.addNote(new PlayersDataSet(username, password, email));
    }


    public PlayersDataSet getPlayerByUsername(String username) throws DBException {
        return super.getNote("username", username);
    }

    @SuppressWarnings("WeakerAccess")
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
