package pl.softwaremill.asamal.plugin.velocity.context;

import org.apache.velocity.context.Context;
import org.apache.velocity.tools.ToolContext;
import org.apache.velocity.tools.ToolManager;
import pl.softwaremill.asamal.extension.view.PresentationContext;

public class VelocityPresentationContext implements PresentationContext {

    private final ToolContext context;

    public VelocityPresentationContext() {
        ToolManager toolManager = new ToolManager(true, true);
        context = toolManager.createContext();
    }

    @Override
    public void put(String key, Object value) {
        context.put(key, value);
    }

    @Override
    public Object get(String key) {
        return context.get(key);
    }

    public Context getVelocityContext() {
        return context;
    }
}
