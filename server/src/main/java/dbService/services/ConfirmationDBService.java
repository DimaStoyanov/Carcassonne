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

    @SuppressWarnings("UnusedReturnValue")
    public long addNote(String confirmationKey, String username) throws DBException {
        return super.addNote(new ConfirmationDataSet(confirmationKey, username));
    }

    public ConfirmationDataSet getNote(String confirmationKey) throws DBException {
        return super.getNote("confirmationKey", confirmationKey);
    }
}
