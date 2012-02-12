package pl.softwaremill.cdiexample.logic.auth;

import pl.softwaremill.cdiweb.controller.annotation.Web;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Bean for authenticating the user
 *
 * User: szimano
 */
@SessionScoped
@Web("login")
@Named("login")
public class LoginBean implements Serializable {
    
    private static final Map<String, String> users = new HashMap<String, String>();
    
    static {
        users.put("admin", "admin");
        users.put("test", "test");
    }

    private User user;

    public boolean isLoggedIn() {
        return user != null;
    }


    public boolean doLogin(String login, String password) {
        if (password == null) {
            return false;
        }
        
        if (password.equals(users.get(login))) {
            user = new User(login, password);
        }
        
        return isLoggedIn();
    }

    public User getUser() {
        return user;
    }

    public void logout() {
        user = null;
    }
}