package pl.softwaremill.asamal.plugin.velocity;

import org.apache.velocity.app.Velocity;
import pl.softwaremill.asamal.extension.AsamalExtension;
import pl.softwaremill.asamal.extension.exception.ResourceNotFoundException;
import pl.softwaremill.asamal.extension.view.PresentationContext;
import pl.softwaremill.asamal.extension.view.PresentationExtension;
import pl.softwaremill.asamal.extension.view.ResourceResolver;
import pl.softwaremill.asamal.plugin.velocity.context.VelocityPresentationContext;
import pl.softwaremill.asamal.plugin.velocity.extensions.LayoutDirective;

import java.io.StringWriter;
import java.util.Properties;

/**
 * Presentation extension for Asamal using velocity
 */
@AsamalExtension
public class AsamalVelocityExtension implements PresentationExtension {

    private static final String LOG_TAG = "velocity";

    public final static String VELOCITY_EXTENSION = "vm";

    public AsamalVelocityExtension() {
        // init velocity - this bean is a singleton actually, so it's fine to do it here
        Properties velocityProps = new Properties();
        velocityProps.setProperty("userdirective",
                "pl.softwaremill.asamal.plugin.velocity.extensions.RegionDirective," +
                        "pl.softwaremill.asamal.plugin.velocity.extensions.LayoutDirective," +
                        "pl.softwaremill.asamal.plugin.velocity.extensions.IncludeRegionDirective," +
                        "pl.softwaremill.asamal.plugin.velocity.extensions.RenderPartialDirective");
        Velocity.init(velocityProps);
    }

    @Override
    public String getExtension() {
        return VELOCITY_EXTENSION;
    }

    @Override
    public PresentationContext createNewPresentationContext() {
        return new VelocityPresentationContext();
    }

    @Override
    public String evaluateTemplate(PresentationContext context, ResourceResolver resourceResolver,
                                   String template) {
        VelocityPresentationContext velocityContext = (VelocityPresentationContext) context;

        StringWriter w = new StringWriter();
        Velocity.setProperty("input.encoding", "UTF-8");
        Velocity.setProperty("output.encoding", "UTF-8");
        Velocity.evaluate(velocityContext.getVelocityContext(), w, LOG_TAG, template);

        String layout;
        while ((layout = (String) context.get(LayoutDirective.LAYOUT)) != null) {
            // clear the layout
            context.put(LayoutDirective.LAYOUT, null);

            w = new StringWriter();
            try {
                template = resourceResolver.resolveTemplate("layout", layout, getExtension());
            } catch (ResourceNotFoundException e) {
                throw new RuntimeException("Cannot find layout: " + layout);
            }
            Velocity.evaluate(velocityContext.getVelocityContext(), w, LOG_TAG, template);
        }

        return w.toString();
    }
}
