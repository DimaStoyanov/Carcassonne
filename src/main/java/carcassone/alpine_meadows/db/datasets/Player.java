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
public class Player implements Serializable {

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private boolean emailConfirmed;

    @Column(unique = true, length = 1024)
    private String confirmationToken;

    @Column(unique = true, length = 1024)
    private String resetToken;

    @Id
    @GeneratedValue
    private long id;

    protected Player() {
    }

    public Player(String username, String password, String email, String confirmationToken) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.confirmationToken = confirmationToken;
        emailConfirmed = false;
    }

    public long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    public boolean isEmailConfirmed() {
        return emailConfirmed;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setEmailConfirmed(boolean emailConfirmed) {
        this.emailConfirmed = emailConfirmed;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getConfirmationToken() {
        return confirmationToken;
    }

    public void setConfirmationToken(String confirmationToken) {
        this.confirmationToken = confirmationToken;
    }

    public String getResetToken() {
        return resetToken;
    }

    public void setResetToken(String resetToken) {
        this.resetToken = resetToken;
    }

    @Override
    public String toString() {
        return String.format("Player{username=%s,email=%s,confirmed=%s",
                username, email, String.valueOf(emailConfirmed));
    }
}
