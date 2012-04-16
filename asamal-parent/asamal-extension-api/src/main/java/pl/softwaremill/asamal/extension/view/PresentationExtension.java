package pl.softwaremill.asamal.extension.view;

/**
 * Extension used to generate views
 */
public interface PresentationExtension {
    String getExtension();

    PresentationContext createNewPresentationContext();

    String evaluateTemplate(PresentationContext context, ResourceResolver resourceResolver,
                            String template);
}
