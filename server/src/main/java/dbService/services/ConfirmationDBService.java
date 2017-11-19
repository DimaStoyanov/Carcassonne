package dbService.services;

import dbService.dataSets.ConfirmationDataSet;
import dbService.exceptions.DBException;

/**
 * Created by Dmitrii Stoianov
 */


public class ConfirmationDBService extends AbstractDBService {
    ConfirmationDBService() {
        super(ConfirmationDataSet.class);
    }

    public void addNote(String confirmationKey, String username) throws DBException {
        super.addNote(new ConfirmationDataSet(confirmationKey, username));
    }

    public ConfirmationDataSet getNoteByConfirmationKey(String confirmationKey) throws DBException {
        return super.getNote("confirmationKey", confirmationKey);
    }

    public ConfirmationDataSet getNoteByUsername(String username) throws DBException {
        return super.getNote("username", username);
    }
}
