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
public class PlayerReset implements Serializable{

    // The same id as in player table. So it fast to find player by playerReset and conversely
    @Id
    @Column(unique = true, updatable = false, nullable = false)
    private long id;

    @Column(unique = true, length = 1024, nullable = false)
    private String token;


    protected PlayerReset(){

    }

    public PlayerReset(long id, String token){
        this.token = token;
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public long getId() {
        return id;
    }

    public void setToken(String token) {
        this.token = token;
    }

}
