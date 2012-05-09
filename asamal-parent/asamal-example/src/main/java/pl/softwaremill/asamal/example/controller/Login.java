package pl.softwaremill.asamal.example.controller;

import pl.softwaremill.asamal.controller.AsamalContext;
import pl.softwaremill.asamal.controller.ControllerBean;
import pl.softwaremill.asamal.controller.annotation.Controller;
import pl.softwaremill.asamal.controller.annotation.Get;
import pl.softwaremill.asamal.controller.annotation.Post;
import pl.softwaremill.asamal.controller.annotation.RequestParameter;
import pl.softwaremill.asamal.example.logic.auth.LoginBean;
import pl.softwaremill.asamal.example.model.security.User;
import pl.softwaremill.asamal.example.service.email.EmailService;
import pl.softwaremill.common.cdi.transaction.Transactional;

import javax.inject.Inject;
import java.io.Serializable;

/**
 * Home page controller
 * <p/>
 * User: szimano
 */
@Controller("login")
public class Login extends ControllerBean implements Serializable {

    private User user = new User();

    @Inject
    private LoginBean loginBean;

    @Inject
    private EmailService emailService;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Get
    public void login() {
    }

    @Get
    public void logout() {
        loginBean.logout();

        redirect("home", "index");
    }

    @Get
    public void forgot() {
    }

    @Post(skipViewHash = true)
    @Transactional
    public void doForgot(@RequestParameter("login") String login) {
        String newPassword = loginBean.resetPassword(login);

        if (newPassword != null) {
            emailService.sendForgotEmail(login, newPassword);
            addMessageToFlash("Password reset. Please wait for email.", AsamalContext.MessageSeverity.SUCCESS);

            redirect("forgot");
        } else {
            addMessageToFlash("Cannot reset password. Wrong login?", AsamalContext.MessageSeverity.ERR);

            putInContext("username", login);

            includeView("forgot");
        }
    }

    @Post(skipViewHash = true)
    @Transactional
    public void doLogin() {
        if (loginBean.doLogin(getParameter("login"), getParameter("password"))) {
            if (loginBean.getUser().getPassReset()) {
                // we need to set his password
                redirect("login", "changePassword");
            }
            else {
                redirect("home", "index");
            }
        }
        else {
            addMessageToFlash("Wrong username/password", AsamalContext.MessageSeverity.ERR);

            includeView("login");
        }
    }

    @Get
    public void changePassword() {
    }

    @Post
    @Transactional
    public void doChangePassword(@RequestParameter("password") String password,
                                 @RequestParameter("password2") String password2) {
        if (!password.equals(password2)) {
            addMessageToFlash(getFromMessageBundle("register.password.notmatch"), AsamalContext.MessageSeverity.ERR);

            redirect("changePassword");
        } else {
            loginBean.changePassword(password);

            addMessageToFlash("Password reset!", AsamalContext.MessageSeverity.SUCCESS);

            redirect("home", "index");
        }
    }
}
