package pl.softwaremill.asamal.example.controller;

import pl.softwaremill.asamal.controller.ControllerBean;
import pl.softwaremill.asamal.controller.annotation.Controller;
import pl.softwaremill.asamal.controller.annotation.Get;
import pl.softwaremill.asamal.controller.annotation.Post;
import pl.softwaremill.asamal.example.logic.auth.LoginBean;
import pl.softwaremill.asamal.example.model.security.User;
import pl.softwaremill.asamal.example.service.hash.StringHasher;
import pl.softwaremill.asamal.example.service.user.UserService;

import javax.inject.Inject;
import java.io.Serializable;

import static pl.softwaremill.asamal.controller.AsamalContext.MessageSeverity.ERR;
import static pl.softwaremill.asamal.controller.AsamalContext.MessageSeverity.SUCCESS;

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
    
    @Inject
    private StringHasher stringHasher;

    @Inject
    private UserService userService;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Get
    public void register() {

    }

    @Get
    public void login() {}
    
    @Get
    public void logout() {
        loginBean.logout();

        redirect("home", "index");
    }

    @Post(skipViewHash = true)
    public void doLogin() {
        loginBean.doLogin(getParameter("login"), getParameter("password"));

        redirect("home", "index");
    }

    @Post(skipViewHash = true)
    public void doRegister() {
        String login = getParameter("user.username");
        user.setUsername(login);

        String password = getParameter("password");
        if (password.equals(getParameter("password2"))) {
            user.setPassword(password);

            if (validateBean("user", user)) {
                // now encode the passport
                user.setPassword(stringHasher.encode(password));
                userService.createNewUser(user);

                addMessageToFlash(getFromMessageBundle("register.created"), SUCCESS);

                // login the user
                loginBean.doLogin(login, password);
                redirect("home", "index");
            } else {
                addMessageToFlash("Validation errors ", ERR);

                includeView("register");
            }
        }
        else {
            addMessageToFlash("user.password", "Passwords do not match", ERR);

            includeView("register");
        }
    }
}
