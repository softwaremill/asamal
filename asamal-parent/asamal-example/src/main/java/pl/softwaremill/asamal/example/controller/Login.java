package pl.softwaremill.asamal.example.controller;

import pl.softwaremill.asamal.controller.ControllerBean;
import pl.softwaremill.asamal.controller.annotation.Controller;
import pl.softwaremill.asamal.controller.annotation.Get;
import pl.softwaremill.asamal.controller.annotation.Post;
import pl.softwaremill.asamal.example.logic.auth.LoginBean;
import pl.softwaremill.asamal.example.model.security.User;
import pl.softwaremill.common.cdi.transaction.Transactional;

import javax.inject.Inject;
import java.io.Serializable;

/**
 * Home page controller
 *
 * User: szimano
 */
@Controller("login")
public class Login extends ControllerBean implements Serializable {

    private User user = new User();

    @Inject
    private LoginBean loginBean;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Get
    public void login() {}
    
    @Get
    public void logout() {
        loginBean.logout();

        redirect("home", "index");
    }

    @Post(skipViewHash = true)
    @Transactional
    public void doLogin() {
        loginBean.doLogin(getParameter("login"), getParameter("password"));

        redirect("home", "index");
    }
}
