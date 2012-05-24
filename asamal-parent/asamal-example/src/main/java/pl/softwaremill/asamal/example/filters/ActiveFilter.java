package pl.softwaremill.asamal.example.filters;

import pl.softwaremill.asamal.controller.AsamalContext;
import pl.softwaremill.asamal.controller.AsamalFilter;
import pl.softwaremill.asamal.example.logic.auth.LoginBean;
import pl.softwaremill.asamal.example.logic.conf.ConfigurationBean;
import pl.softwaremill.asamal.example.model.conf.Conf;

import javax.inject.Inject;

public class ActiveFilter implements AsamalFilter{

    private AsamalContext asamalContext;

    private ConfigurationBean configurationBean;

    private LoginBean loginBean;

    @Inject
    public ActiveFilter(AsamalContext asamalContext, ConfigurationBean configurationBean, LoginBean loginBean) {
        this.asamalContext = asamalContext;
        this.configurationBean = configurationBean;
        this.loginBean = loginBean;
    }

    @Override
    public void doFilter() {
        // if the selling is not activated, and user is not admin, he should be redirected
        // to some "not yet" screen
        if (!configurationBean.getBooleanProperty(Conf.ACTIVE) && !loginBean.isAdmin()) {
            asamalContext.redirect("come", "back", null);
        }
    }
}
