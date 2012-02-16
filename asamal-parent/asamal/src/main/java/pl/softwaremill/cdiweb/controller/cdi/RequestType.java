package pl.softwaremill.cdiweb.controller.cdi;

import java.lang.annotation.Annotation;

/**
 * Type of the request
 *
 * User: szimano
 */
public enum RequestType {

    GET(pl.softwaremill.cdiweb.controller.annotation.Get.class),

    JSON(pl.softwaremill.cdiweb.controller.annotation.Json.class),

    POST(pl.softwaremill.cdiweb.controller.annotation.Post.class);

    private final Class<? extends Annotation> requestAnnotation;

    public Class<? extends Annotation> getRequestAnnotation() {
        return requestAnnotation;
    }

    private RequestType(Class<? extends Annotation> requestAnnotation) {
        this.requestAnnotation = requestAnnotation;
    }
}
