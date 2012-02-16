package pl.softwaremill.asamal.controller;

/**
 * Interface for asamal filters, to be run just before each action-view invoke
 *
 * The filter is allowed to perform redirect or include
 *
 * User: szimano
 */
public interface AsamalFilter {

    void doFilter();
}
