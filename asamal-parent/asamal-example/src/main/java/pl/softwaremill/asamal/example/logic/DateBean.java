package pl.softwaremill.asamal.example.logic;

import pl.softwaremill.asamal.controller.annotation.Web;

import javax.enterprise.context.SessionScoped;
import java.io.Serializable;

import java.util.Date;

/**
 * User: szimano
 */
@Web("date")
@SessionScoped
public class DateBean implements Serializable {

    Date now = new Date();

    public Date getNow() {
        return now;
    }
}
