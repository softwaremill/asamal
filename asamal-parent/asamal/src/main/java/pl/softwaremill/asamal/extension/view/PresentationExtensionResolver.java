package pl.softwaremill.asamal.extension.view;

import pl.softwaremill.asamal.controller.cdi.AsamalAnnotationScanner;
import pl.softwaremill.asamal.exception.NoViewFoundException;
import pl.softwaremill.asamal.extension.exception.ResourceNotFoundException;
import pl.softwaremill.common.util.dependency.D;

import javax.inject.Inject;

public class PresentationExtensionResolver {

    @Inject
    private AsamalAnnotationScanner asamalAnnotationScanner;

    public PresentationExtension resolvePresentationExtension(ResourceResolver resourceResolver,
                                                                      String controller, String view)
            throws NoViewFoundException {
        for (Class<? extends PresentationExtension> extensionClass :
                asamalAnnotationScanner.getPresentationExtensions()) {
            try {
                PresentationExtension extension = D.inject(extensionClass);

                resourceResolver.resolveTemplate(controller, view,
                        extension.getExtension());

                return extension;
            } catch (ResourceNotFoundException e) {
                // do nothing, try next one
            }
        }

        // this means there's no view available with any knows extension die
        throw new NoViewFoundException("There is no file for controller "+controller+" and view "+view);
    }


}

