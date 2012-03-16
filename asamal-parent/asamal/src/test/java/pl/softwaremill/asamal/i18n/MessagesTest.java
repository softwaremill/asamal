package pl.softwaremill.asamal.i18n;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.fest.assertions.Assertions.assertThat;

@RunWith(value = Parameterized.class)
public class MessagesTest {

    private String key;
    private String expected;
    private Object[] params;
    
    public MessagesTest(String key, String expected, Object[] params) {
        this.params = params;
        this.expected = expected;
        this.key = key;
    }

    @Test
    public void shouldFormatProperly() {
        // given
        Messages m = new Messages();
        
        // when
        String result = m.getFromBundle(key, params);
        
        // then
        assertThat(result).isEqualTo(expected);
    }
    
    @Parameterized.Parameters
    public static Collection<Object[]> getMessages() {
        return Arrays.asList(new Object[][] {
                new Object[]{"test1", "This is test", null},
                new Object[]{"test1", "This is test", new Object[]{1}},
                new Object[]{"test2", "This is 1 test", new Object[]{1}},
                new Object[]{"test3", "This is 1 test 2", new Object[]{1, "2"}},
                new Object[]{"test4", "This is 2 test 1", new Object[]{1, "2"}},
        });
    }
}
