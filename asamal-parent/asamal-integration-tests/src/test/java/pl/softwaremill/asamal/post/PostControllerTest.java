package pl.softwaremill.asamal.post;

import org.jboss.arquillian.api.Deployment;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.jboss.resteasy.util.GenericType;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.testng.annotations.Test;
import pl.softwaremill.asamal.ControllerTest;
import pl.softwaremill.asamal.common.TestRecorder;
import pl.softwaremill.asamal.common.TestResourceResolver;
import pl.softwaremill.asamal.exception.HttpErrorException;
import pl.softwaremill.asamal.jaxrs.JAXPostHandler;
import pl.softwaremill.asamal.resource.ResourceResolver;
import pl.softwaremill.common.util.dependency.D;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * User: szimano
 */
public class PostControllerTest extends ControllerTest {

    @Deployment
    public static JavaArchive createDeployment() {
        return ShrinkWrap.create(JavaArchive.class, "test.jar")
                .addClass(PostTestController.class)
                .addClass(ResourceResolver.Factory.class)
                .addClass(ResourceResolver.class)
                .addPackage(TestResourceResolver.class.getPackage());
    }

    @Test
    public void shouldRunCorrectPostMethod() throws HttpErrorException {

        // given
        JAXPostHandler postHandler = getPostHandler();

        // when
        String output = postHandler.handlePost(req, resp, "post", "doPost", null, null);

        // then
        assertThat(D.inject(TestRecorder.class).getMethodsCalled()).containsOnly("doPost");

        // by default post doesn't return anythings
        assertThat(output).isEqualTo(null);
    }

    @Test
    public void shouldRunCorrectPostFormDataMethod() throws HttpErrorException {

        // given
        JAXPostHandler postHandler = getPostHandler();

        MultipartFormDataInput dataInput = mock(MultipartFormDataInput.class);
        Map<String, List<InputPart>> formValues = mock(Map.class);
        when(dataInput.getFormDataMap()).thenReturn(formValues);

        Set<Map.Entry<String, List<InputPart>>> entries = new HashSet<Map.Entry<String, List<InputPart>>>();
        entries.add(new EntryImpl<String, List<InputPart>>("a",
                input(new InputPartImpl("a", MediaType.TEXT_PLAIN_TYPE))));
        entries.add(new EntryImpl<String, List<InputPart>>("b",
                input(new InputPartImpl("b", MediaType.TEXT_PLAIN_TYPE),
                      new InputPartImpl("c", MediaType.TEXT_PLAIN_TYPE))));

        when(formValues.entrySet()).thenReturn(entries);

        // when
        String output = postHandler.handlePostFormData(req, resp, "post", "doFormDataPost", null, dataInput);

        // then
        assertThat(D.inject(TestRecorder.class).getMethodsCalled()).containsOnly("doFormDataPost");

        // by default post doesn't return anythings
        assertThat(output).isEqualTo(null);
    }

    public List<InputPart> input(InputPart... part) {
        return Arrays.asList(part);
    }
}

class EntryImpl<K, V> implements Map.Entry<K, V> {
    private K key;
    private V value;

    EntryImpl(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public void setKey(K key) {
        this.key = key;
    }

    public V getValue() {
        return value;
    }

    public V setValue(V value) {
        return this.value = value;
    }
}

class InputPartImpl implements InputPart {
    
    String body;

    MediaType mediaType;

    InputPartImpl(String body, MediaType mediaType) {
        this.body = body;
        this.mediaType = mediaType;
    }

    public MultivaluedMap<String, String> getHeaders() {
        return null;
    }

    public String getBodyAsString() throws IOException {
        return body;
    }

    public <T> T getBody(Class<T> type, Type genericType) throws IOException {
        return (T)body;
    }

    public <T> T getBody(GenericType<T> type) throws IOException {
        return (T)body;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public boolean isContentTypeFromMessage() {
        return false;
    }
}