package pl.softwaremill.asamal.example.logic;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import java.io.Serializable;

import java.util.Date;

/**
 * User: szimano
 */
@Named("date")
@SessionScoped
public class DateBean implements Serializable {

    Date now = new Date();

    public Date getNow() {
        return now;
    }
}
