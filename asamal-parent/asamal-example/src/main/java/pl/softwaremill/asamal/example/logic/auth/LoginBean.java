package pl.softwaremill.asamal.example.logic.auth;

import pl.softwaremill.asamal.example.model.security.User;
import pl.softwaremill.asamal.example.service.user.UserService;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;

/**
 * Bean for authenticating the user
 *
 * User: szimano
 */
@SessionScoped
@Named("login")
public class LoginBean implements Serializable {

    @Inject
    private UserService userService;

    private User user;

    public boolean isLoggedIn() {
        return user != null;
    }

    public boolean isAdmin() {
        return user != null && user.isAdmin();
    }

    public boolean doLogin(String username, String password) {
        user = userService.authenticate(username, password);
        
        return isLoggedIn();
    }

    public User getUser() {
        return user;
    }

    public void logout() {
        user = null;
    }

    public String resetPassword(String login) {
        return userService.resetPassword(login);
    }

    public void changePassword(String password) {
        user = userService.changePassword(user, password);
    }
}
