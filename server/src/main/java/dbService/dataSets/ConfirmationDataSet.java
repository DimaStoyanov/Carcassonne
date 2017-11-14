package dbService.dataSets;

import javax.persistence.*;

/**
 * Created by Dmitrii Stoianov
 */

@Entity
@Table(name = "confirmation")
public class ConfirmationDataSet extends AbstractDataSet {
    @SuppressWarnings("unused")
    protected final static long serialVersionUID = -124235630102216L;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "confirmationKey", updatable = false)
    private String confirmationKey;

    @Column(name = "username", updatable = false, unique = true)
    private String username;

    @SuppressWarnings("unused")
    public ConfirmationDataSet() {
        super();
    }

    public ConfirmationDataSet(String confirmationKey, String username) {
        this.confirmationKey = confirmationKey;
        this.username = username;
    }


    @SuppressWarnings("unused")
    public long getId() {
        return id;
    }

    @SuppressWarnings("unused")
    public void setId(long id) {
        this.id = id;
    }


    @SuppressWarnings("unused")
    public String getUsername() {
        return username;
    }

    @SuppressWarnings("unused")
    public String getConfirmationKey() {
        return confirmationKey;
    }

    @SuppressWarnings("unused")
    public void setUsername(String username) {
        this.username = username;
    }

    @SuppressWarnings("unused")
    public void setConfirmationKey(String confirmationKey) {
        this.confirmationKey = confirmationKey;
    }

    @Override
    public String toString() {
        return super.toString() +
                String.format("{id=\'%d\', confirmationKey=\'%s\', username=\'%s\'}", id, confirmationKey, username);
    }
}
