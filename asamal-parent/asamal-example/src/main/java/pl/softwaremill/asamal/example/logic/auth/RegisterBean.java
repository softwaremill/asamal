package pl.softwaremill.asamal.example.logic.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.softwaremill.asamal.controller.ControllerBean;
import pl.softwaremill.asamal.example.model.security.User;
import pl.softwaremill.asamal.example.service.hash.StringHasher;
import pl.softwaremill.asamal.example.service.user.UserService;
import pl.softwaremill.asamal.example.service.user.exception.UserExistsException;

import javax.inject.Inject;

import static pl.softwaremill.asamal.controller.AsamalContext.MessageSeverity.ERR;

public class RegisterBean {

    @Inject
    private StringHasher stringHasher;

    @Inject
    private UserService userService;

    @Inject
    private LoginBean loginBean;

    private static final Logger log = LoggerFactory.getLogger(RegisterBean.class);

    public boolean registerUser(ControllerBean bean) {
        User user = new User();

        String login = bean.getParameter("user.username");

        user.setUsername(login);

        bean.putInContext("username", login);

        String password = bean.getParameter("password");
        if (password.equals(bean.getParameter("password2"))) {
            user.setPassword(password);

            if (bean.validateBean("user", user)) {
                // now encode the passport
                user.setPassword(stringHasher.encode(password));

                if (userService.loadUser(login) != null) {
                    bean.addMessageToFlash("user.username",
                            bean.getFromMessageBundle("register.username.taken"), ERR);
                }
                else {
                    try {
                        userService.createNewUser(user);

                        // login the user
                        loginBean.doLogin(login, password);

                        return true;
                    } catch (UserExistsException e) {
                        log.error("User exists", e);
                        bean.addMessageToFlash("user.username",
                                bean.getFromMessageBundle("register.username.taken"), ERR);
                    }
                }
            }
        }
        else {
            bean.addMessageToFlash("user.password",
                    bean.getFromMessageBundle("register.password.notmatch"), ERR);
        }

        return false;
    }
}
