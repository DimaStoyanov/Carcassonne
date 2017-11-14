package dbService.dataSets;

import java.io.Serializable;

/**
 * Created by Dmitrii Stoianov
 */


public abstract class AbstractDataSet implements Serializable {

    @SuppressWarnings("unused")
    AbstractDataSet() {
    }


    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
}
