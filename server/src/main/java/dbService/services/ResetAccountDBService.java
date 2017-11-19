package dbService.services;

import dbService.dataSets.ResetAccountDataSet;
import dbService.exceptions.DBException;

/**
 * Created by Dmitrii Stoianov
 */


public class ResetAccountDBService extends AbstractDBService {

    ResetAccountDBService() {
        super(ResetAccountDataSet.class);
    }

    public void addNote(String token, String username) throws DBException {
        super.addNote(new ResetAccountDataSet(token, username));
    }

    public ResetAccountDataSet getNoteByToken(String token) throws DBException {
        return super.getNote("token", token);
    }

    public ResetAccountDataSet getNoteByUsername(String username) throws DBException {
        return super.getNote("username", username);
    }
}
