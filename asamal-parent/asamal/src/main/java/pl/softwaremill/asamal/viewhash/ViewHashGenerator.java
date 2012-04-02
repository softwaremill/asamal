package pl.softwaremill.asamal.viewhash;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Class that handles view hashes used for XSS prevention
 *
 * User: szimano
 */
public class ViewHashGenerator {

    public static final String VIEWHASH = "asamalViewHash";
    public static final String VIEWHASH_MAP = VIEWHASH + "Map";
    
    private Instance<HttpServletRequest> request;

    @Inject
    public ViewHashGenerator(Instance<HttpServletRequest> request) {
        this.request = request;
    }

    public String createNewViewHash(String controller, String view) {
        ViewDescriptor viewDescriptor = new ViewDescriptor(controller, view);

        String viewHash = UUID.randomUUID().toString();

        Map<String, ViewDescriptor> viewHashMap = getViewHashMap();

        viewHashMap.put(viewHash, viewDescriptor);

        return viewHash;
    }

    public Map<String, ViewDescriptor> getViewHashMap() {
        HttpServletRequest httpServletRequest = request.get();

        Map<String, ViewDescriptor> viewHashMap = (Map<String, ViewDescriptor>) httpServletRequest.getSession()
                .getAttribute(VIEWHASH_MAP);

        if (viewHashMap == null) {
            // the hash map is not yet defined
            // the map is synchronized, because one user might actually perform simultaneous requests
            httpServletRequest.getSession().setAttribute(VIEWHASH_MAP, viewHashMap =
                    Collections.synchronizedMap(new HashMap<String, ViewDescriptor>()));
        }

        return viewHashMap;
    }
}
