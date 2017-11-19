package dbService.dataSets;

import javax.persistence.*;

/**
 * Created by Dmitrii Stoianov
 */

@Entity
@Table(name = "reset_account")
public class ResetAccountDataSet extends AbstractDataSet {
    private final static long serialVersionUID = -2782307612543135L;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "token", unique = true)
    private String token;

    @Column(name = "username", unique = true)
    private String username;

    @SuppressWarnings("unused")
    public ResetAccountDataSet() {

    }

    public ResetAccountDataSet(String token, String username) {
        this.token = token;
        this.username = username;
    }

    public long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getToken() {
        return token;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return super.toString() +
                String.format("{id=\'%s\', token=\'%s\', username=\'%s\'}",
                        id, token, username);
    }
}
