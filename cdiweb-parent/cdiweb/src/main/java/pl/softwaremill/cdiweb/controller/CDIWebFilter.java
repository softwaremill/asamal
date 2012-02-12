package pl.softwaremill.cdiweb.controller;

/**
 * Interface for cdiweb filters, to be run just before each action-view invoke
 *
 * The filter is allowed to perform redirect or include
 *
 * User: szimano
 */
public interface CDIWebFilter {

    void doFilter();
}
