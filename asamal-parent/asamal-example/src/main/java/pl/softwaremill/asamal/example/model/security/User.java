package pl.softwaremill.asamal.example.model.security;

import org.hibernate.validator.constraints.Email;
import pl.softwaremill.asamal.example.model.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.Size;

/**
 * Entity for storing all system users
 */
@Entity
@Table(name = "USER")
public class User extends BaseEntity {

    @Column(unique = true, name = "username", nullable = false, length = 30)
    @Size(min = 3, max = 30)
    @Email
    private String username;

    @Column(name = "password", nullable = false)
    @Size(min = 3, message = "Password has to be at least 6 characters")
    private String password;

    @Column(name = "admin", nullable = false)
    private boolean admin = false;

    public User(String username, String password, boolean admin) {
        this.username = username;
        this.password = password;
        this.admin = admin;
    }

    public User() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;

        User user = (User) o;

        if (admin != user.admin) return false;
        if (password != null ? !password.equals(user.password) : user.password != null) return false;
        if (username != null ? !username.equals(user.username) : user.username != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = username != null ? username.hashCode() : 0;
        result = 31 * result + (password != null ? password.hashCode() : 0);
        result = 31 * result + (admin ? 1 : 0);
        return result;
    }
}
