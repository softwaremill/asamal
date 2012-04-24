package pl.softwaremill.asamal.controller.cdi;

import pl.softwaremill.asamal.controller.annotation.Download;

import java.lang.annotation.Annotation;

/**
 * Type of the request
 *
 * User: szimano
 */
public enum RequestType {

    GET(pl.softwaremill.asamal.controller.annotation.Get.class),

    JSON(pl.softwaremill.asamal.controller.annotation.Json.class),

    POST(pl.softwaremill.asamal.controller.annotation.Post.class),

    DOWNLOAD(Download.class);

    private final Class<? extends Annotation> requestAnnotation;

    public Class<? extends Annotation> getRequestAnnotation() {
        return requestAnnotation;
    }

    private RequestType(Class<? extends Annotation> requestAnnotation) {
        this.requestAnnotation = requestAnnotation;
    }
}
