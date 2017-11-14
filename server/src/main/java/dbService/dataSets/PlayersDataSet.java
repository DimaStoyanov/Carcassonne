package dbService.dataSets;

import javax.persistence.*;

/**
 * Created by Dmitrii Stoianov
 */

@Entity
@Table(name = "players")
public class PlayersDataSet extends AbstractDataSet {
    private final static long serialVersionUID = -345423563214523565L;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "username", unique = true)
    private String username;

    @Column(name = "password")
    private String passwordHash;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "confirmedEmail")
    private boolean confirmedEmail;

    @SuppressWarnings("unused")
    public PlayersDataSet() {
        super();
    }

    public PlayersDataSet(String username, String passwordHash, String email) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.email = email;
        confirmedEmail = false;
    }

    @SuppressWarnings("unused")
    public long getId() {
        return id;
    }

    @SuppressWarnings("unused")
    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    @SuppressWarnings("unused")
    public String getEmail() {
        return email;
    }

    @SuppressWarnings("unused")
    public boolean isConfirmedEmail() {
        return confirmedEmail;
    }

    public void setConfirmedEmail(boolean confirmedEmail) {
        this.confirmedEmail = confirmedEmail;
    }

    @SuppressWarnings("unused")
    public void setId(long id) {
        this.id = id;
    }

    @SuppressWarnings("unused")
    public void setUsername(String username) {
        this.username = username;
    }

    @SuppressWarnings("unused")
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    @SuppressWarnings("unused")
    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return super.toString() +
                String.format("{id=%d, login=\'%s\', password=\'%s\', email=\'%s\', confirmed=\'%s\'}",
                        id, username, passwordHash, email, confirmedEmail);
    }
}
