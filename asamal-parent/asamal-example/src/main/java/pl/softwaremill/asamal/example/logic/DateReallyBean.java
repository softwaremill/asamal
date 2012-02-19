package pl.softwaremill.asamal.example.logic;

import javax.inject.Named;
import java.io.Serializable;
import java.util.Date;

/**
 * User: szimano
 */
@Named("dateReally")
public class DateReallyBean implements Serializable {

    Date now = new Date();

    public Date getNow() {
        return now;
    }
}
