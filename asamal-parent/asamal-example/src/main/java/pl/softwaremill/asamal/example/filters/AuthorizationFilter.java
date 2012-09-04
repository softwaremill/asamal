package pl.softwaremill.asamal.example.filters;

import pl.softwaremill.asamal.controller.AsamalContext;
import pl.softwaremill.asamal.controller.AsamalFilter;
import pl.softwaremill.asamal.example.logic.auth.LoginBean;

import javax.inject.Inject;

/**
 * Expects user to be logged in
 *
 * User: szimano
 */
public class AuthorizationFilter implements AsamalFilter {

    private AsamalContext context;

    private LoginBean loginBean;

    @Inject
    public AuthorizationFilter(AsamalContext context, LoginBean loginBean) {
        this.context = context;
        this.loginBean = loginBean;
    }

    public void doFilter() {
        if (!loginBean.isLoggedIn()) {
            context.redirect("login", "login", null);
        }
    }
}
