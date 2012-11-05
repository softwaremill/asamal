
package pl.softwaremill.asamal.post;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.jboss.resteasy.specimpl.MultivaluedMapImpl;
import org.jboss.resteasy.util.GenericType;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import pl.softwaremill.asamal.ControllerTest;
import pl.softwaremill.asamal.MockAsamalProducers;
import pl.softwaremill.asamal.common.TestRecorder;
import pl.softwaremill.asamal.common.TestResourceResolver;
import pl.softwaremill.asamal.controller.cdi.AsamalAnnotationScanner;
import pl.softwaremill.asamal.exception.HttpErrorException;
import pl.softwaremill.asamal.extension.view.PresentationExtensionResolver;
import pl.softwaremill.asamal.extension.view.ResourceResolver;
import pl.softwaremill.asamal.helper.AsamalHelper;
import pl.softwaremill.asamal.i18n.Messages;
import pl.softwaremill.asamal.plugin.velocity.AsamalVelocityExtension;
import pl.softwaremill.asamal.plugin.velocity.context.VelocityPresentationContext;
import pl.softwaremill.asamal.request.AsamalViewHandler;
import pl.softwaremill.asamal.request.http.PostHandler;
import pl.softwaremill.asamal.viewhash.ViewHashGenerator;
import pl.softwaremill.common.util.dependency.D;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * User: szimano
 */
@RunWith(Arquillian.class)
public class PostControllerTest extends ControllerTest {

    @Deployment
    public static JavaArchive createDeployment() {
        return ShrinkWrap.create(JavaArchive.class, "test.jar")
                .addClass(PostTestController.class)
                .addClass(ResourceResolver.Factory.class)
                .addClass(ResourceResolver.class)
                .addClass(Messages.class)
                .addClass(MockAsamalProducers.class)
                .addClass(ViewHashGenerator.class)
                .addClass(AsamalViewHandler.class)
                .addClass(AsamalHelper.class)
                .addClass(PresentationExtensionResolver.class)
                .addClass(AsamalVelocityExtension.class)
                .addClass(VelocityPresentationContext.class)
                .addPackage(TestResourceResolver.class.getPackage())
                .addAsServiceProviderAndClasses(AsamalAnnotationScanner.class);
    }

    @Test
    public void shouldRunCorrectPostMethod() throws HttpErrorException {

        // given
        PostHandler postHandler = getPostHandler();
        addViewHash("view-hash", "post", "doPost");

        // when
        Response output = postHandler.handlePost(req, resp, "post", "doPost", null, new MultivaluedMapImpl<String, String>(){
            {
                put(ViewHashGenerator.VIEWHASH, Arrays.asList("view-hash"));
            }
        });

        // then
        assertThat(D.inject(TestRecorder.class).getMethodsCalled()).containsOnly("doPost");

        // by default post doesn't return anythings
        assertThat(output.getEntity()).isNull();
    }

    @Test
    public void shouldRunCorrectPostFormDataMethod() throws HttpErrorException {

        // given
        PostHandler postHandler = getPostHandler();
        addViewHash("view-hash", "post", "doFormDataPost");

        MultipartFormDataInput dataInput = mock(MultipartFormDataInput.class);
        Map<String, List<InputPart>> formValues = mock(Map.class);
        when(dataInput.getFormDataMap()).thenReturn(formValues);

        Set<Map.Entry<String, List<InputPart>>> entries = new HashSet<Map.Entry<String, List<InputPart>>>();
        entries.add(new EntryImpl<String, List<InputPart>>("a",
                input(new InputPartImpl("a", MediaType.TEXT_PLAIN_TYPE))));
        entries.add(new EntryImpl<String, List<InputPart>>("b",
                input(new InputPartImpl("b", MediaType.TEXT_PLAIN_TYPE),
                      new InputPartImpl("c", MediaType.TEXT_PLAIN_TYPE))));
        entries.add(new EntryImpl<String, List<InputPart>>(ViewHashGenerator.VIEWHASH,
                input(new InputPartImpl("view-hash", MediaType.TEXT_PLAIN_TYPE))));

        when(formValues.entrySet()).thenReturn(entries);

        // when
        Response output = postHandler.handlePostFormData(req, resp, "post", "doFormDataPost", null, dataInput);

        // then
        assertThat(D.inject(TestRecorder.class).getMethodsCalled()).containsOnly("doFormDataPost");

        // by default post doesn't return anythings
        assertThat(output.getEntity()).isNull();
    }

    @Test(expected = RuntimeException.class)
            //FIXME expectedExceptionsMessageRegExp = ".*There is no viewHash send for this post query")
    public void shouldFailWhenNoViewHash() throws HttpErrorException {
        // given
        PostHandler postHandler = getPostHandler();
        addViewHash("view-hash", "post", "doFormDataPost");

        // when
        String output = postHandler.handlePost(req, resp, "post", "doPost", null,
                new MultivaluedMapImpl<String, String>()).getEntity().toString();
    }

    @Test
    public void shouldPassWhenNoViewHashWithViewHashSkipped() throws HttpErrorException {
        // given
        PostHandler postHandler = getPostHandler();
        addViewHash("view-hash", "post", "doPost");

        // when
        Response output = postHandler.handlePost(req, resp, "post", "doPostWithoutViewHashCheck", null,
                new MultivaluedMapImpl<String, String>());

        // then
        assertThat(D.inject(TestRecorder.class).getMethodsCalled()).containsOnly("doPostWithoutViewHashCheck");
    }

    @Test
    public void shouldPassWithAnnotatedParams() throws HttpErrorException {
        // given
        PostHandler postHandler = getPostHandler();
        addViewHash("view-hash", "post", "postWithParams");

        // when
        MultivaluedMapImpl<String, String> formValues = new MultivaluedMapImpl<String, String>();
        formValues.putSingle("name", "postParam");
        formValues.putSingle(ViewHashGenerator.VIEWHASH, "view-hash");

        Response output = postHandler.handlePost(req, resp, "post", "postWithParams", "10",
                formValues);

        // then
        assertThat(D.inject(TestRecorder.class).getMethodsCalled()).
                containsOnly("postWithParams id = 10 name = postParam");
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