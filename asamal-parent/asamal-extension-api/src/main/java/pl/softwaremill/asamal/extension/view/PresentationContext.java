package pl.softwaremill.asamal.extension.view;

public interface PresentationContext {

    void put(String key, Object value);

    Object get(String key);
}
