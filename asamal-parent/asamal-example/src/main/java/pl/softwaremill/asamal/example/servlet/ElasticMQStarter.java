package pl.softwaremill.asamal.example.servlet;

import org.elasticmq.Node;
import org.elasticmq.NodeAddress;
import org.elasticmq.NodeBuilder;
import org.elasticmq.rest.RestServer;
import org.elasticmq.rest.sqs.SQSRestServerFactory;
import org.elasticmq.storage.inmemory.InMemoryStorage;
import org.jboss.netty.logging.InternalLoggerFactory;
import org.jboss.netty.logging.Slf4JLoggerFactory;
import pl.softwaremill.asamal.example.service.email.EmailSendingBean;

import javax.inject.Inject;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class ElasticMQStarter implements ServletContextListener{

    private Node node;
    private RestServer server;

    @Inject
    EmailSendingBean emailSendingBean;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory());

        // First we need to create a Node
//        node = NodeBuilder.withStorage(new InMemoryStorage());

        // Then we can expose the native client using the SQS REST interface
//        server = SQSRestServerFactory.start(node.nativeClient(), 8855, new NodeAddress("http://localhost:8855"));

        // start email sending
//        emailSendingBean.startTimer(10000);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        emailSendingBean.destroyTimer();
        server.stop();
        node.shutdown();
    }
}
