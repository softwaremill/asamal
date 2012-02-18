package pl.softwaremill.asamal.viewhash;

import java.io.Serializable;

/**
 * Descriptor that sits in the session, and stores the controller/view pair
 *
 * User: szimano
 */
public class ViewDescriptor implements Serializable {

    private final String controller;
    private final String view;

    public ViewDescriptor(String controller, String view) {
        this.controller = controller;
        this.view = view;
    }

    public String getController() {
        return controller;
    }

    public String getView() {
        return view;
    }
}
