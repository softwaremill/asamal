#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.controller;

import org.testng.annotations.Test;

import java.util.List;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * User: szimano
 */
public class HomeControllerTest extends Home {

    @Test
    public void shouldSetTheListInContext() {
        // given

        // not much to initiate

        // when
        index();

        // then
        assertThat((List)getParams().get("list")).containsExactly("One", "Two", "Three");
    }
}
