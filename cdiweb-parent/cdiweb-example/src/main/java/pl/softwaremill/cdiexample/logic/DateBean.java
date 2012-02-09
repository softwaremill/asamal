package pl.softwaremill.cdiexample.logic;

import pl.softwaremill.cdiweb.controller.annotation.Web;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
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
