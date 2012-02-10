package pl.softwaremill.cdiweb.servlet;

import org.apache.velocity.app.Velocity;
import pl.softwaremill.cdiweb.jaxrs.JAXPostHandler;
import pl.softwaremill.common.util.dependency.BeanManagerDependencyProvider;
import pl.softwaremill.common.util.dependency.D;
import pl.softwaremill.common.util.dependency.DependencyProvider;

import javax.enterprise.inject.spi.BeanManager;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Listener that sets the correct D providers
 *
 * User: szimano
 */
public class CDIWebListener implements ServletContextListener {
    
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
        Velocity.init();

        // warn about dev mode
        if (System.getProperty(JAXPostHandler.CDIWEB_DEV_DIR) != null) {
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
