package pl.softwaremill.asamal.i18n;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.fest.assertions.Assertions.assertThat;

public class MessagesTest {

    @Test(dataProvider = "messages")
    public void shouldFormatProperly(String key, String expected, Object... params) {
        // given
        Messages m = new Messages();
        
        // when
        String result = m.getFromBundle(key, params);
        
        // then
        assertThat(result).isEqualTo(expected);
    }
    
    @DataProvider(name = "messages")
    public Object[][] getMessages() {
        return new Object[][] {
                new Object[]{"test1", "This is test", null},
                new Object[]{"test1", "This is test", new Object[]{1}},
                new Object[]{"test2", "This is 1 test", new Object[]{1}},
                new Object[]{"test3", "This is 1 test 2", new Object[]{1, "2"}},
                new Object[]{"test4", "This is 2 test 1", new Object[]{1, "2"}},
        };
    }
}
