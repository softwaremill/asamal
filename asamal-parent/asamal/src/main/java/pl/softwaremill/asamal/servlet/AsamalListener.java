package pl.softwaremill.asamal.servlet;

import org.apache.velocity.app.Velocity;
import pl.softwaremill.asamal.resource.ResourceResolver;
import pl.softwaremill.common.util.dependency.BeanManagerDependencyProvider;
import pl.softwaremill.common.util.dependency.D;
import pl.softwaremill.common.util.dependency.DependencyProvider;

import javax.enterprise.inject.spi.BeanManager;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.util.Properties;

/**
 * Listener that sets the correct D providers
 * <p/>
 * User: szimano
 */
@WebListener
public class AsamalListener implements ServletContextListener {

    public static final String BEAN_MANAGER = "BeanManager";

    private DependencyProvider registeredDependencyProvider;

    public void contextInitialized(ServletContextEvent sce) {
        // Setting a dependency provider with the right bean manager
        BeanManager bm;
        try {
            bm = (BeanManager) new InitialContext().lookup("java:comp/BeanManager");
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
        registeredDependencyProvider = new BeanManagerDependencyProvider(bm);
        D.register(registeredDependencyProvider);

        sce.getServletContext().setAttribute(BEAN_MANAGER, bm);

        // init velocity
        Properties velocityProps = new Properties();
        velocityProps.setProperty("userdirective",
                        "pl.softwaremill.asamal.velocity.RegionDirective," +
                        "pl.softwaremill.asamal.velocity.LayoutDirective," +
                        "pl.softwaremill.asamal.velocity.IncludeRegionDirective,"+
                        "pl.softwaremill.asamal.velocity.RenderPartialDirective");
        Velocity.init(velocityProps);

        // warn about dev mode
        if (System.getProperty(ResourceResolver.ASAMAL_DEV_DIR) != null) {
            System.out.println("****************************");
            System.out.println("****************************");
            System.out.println("*    Running in DEV mode   *");
            System.out.println("* DO NOT use on production *");
            System.out.println("****************************");
            System.out.println("****************************");
        }
    }

    public void contextDestroyed(ServletContextEvent sce) {
        // Clearing up
        if (registeredDependencyProvider != null) {
            D.unregister(registeredDependencyProvider);
        }
    }
}
