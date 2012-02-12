package pl.softwaremill.cdiexample.filters;

import pl.softwaremill.cdiexample.logic.auth.LoginBean;
import pl.softwaremill.cdiweb.controller.CDIWebContext;
import pl.softwaremill.cdiweb.controller.CDIWebFilter;

import javax.inject.Inject;

/**
 * Expects user to be logged in
 *
 * User: szimano
 */
public class AuthorizationFilter implements CDIWebFilter{

    public static final String PREVIOUS_URI = "security.previous.uri";

    private CDIWebContext context;

    private LoginBean loginBean;

    @Inject
    public AuthorizationFilter(CDIWebContext context, LoginBean loginBean) {
        this.context = context;
        this.loginBean = loginBean;
    }

    public void doFilter() {
        if (!loginBean.isLoggedIn()) {
            context.addObjectToFlash(PREVIOUS_URI, context.getCurrentLink());
            context.redirect("home", "login");
        }
    }
}
