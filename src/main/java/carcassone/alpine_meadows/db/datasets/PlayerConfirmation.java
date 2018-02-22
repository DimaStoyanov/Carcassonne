package carcassone.alpine_meadows.db.datasets;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * Created by Dmitrii Stoianov
 */

@Entity
public class PlayerConfirmation implements Serializable {

    @Column(unique = true, length = 1024, nullable = false)
    private String token;

    // The same id as in player table. So it fast to find player by playerConfirmation and conversely
    @Id
    @Column(unique = true, nullable = true, updatable = false)
    private long id;

    protected PlayerConfirmation() {

    }

    public PlayerConfirmation(long id, String token) {
        this.id = id;
        this.token = token;
    }

    public long getId() {
        return id;
    }

    public String getToken() {
        return token;
    }



    public void setToken(String token) {
        this.token = token;
    }
}
