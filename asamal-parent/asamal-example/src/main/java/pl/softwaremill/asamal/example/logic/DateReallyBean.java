package pl.softwaremill.asamal.example.logic;

import pl.softwaremill.asamal.controller.annotation.Web;

import java.io.Serializable;
import java.util.Date;

/**
 * User: szimano
 */
@Web("dateReally")
public class DateReallyBean implements Serializable {

    Date now = new Date();

    public Date getNow() {
        return now;
    }
}
