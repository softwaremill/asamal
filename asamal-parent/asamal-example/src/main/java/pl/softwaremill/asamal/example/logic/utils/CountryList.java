package pl.softwaremill.asamal.example.logic.utils;

import javax.annotation.PostConstruct;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

/**
 * Bean that provides list of countries.
 *
 * Taken from http://www.java2s.com/Code/Java/I18N/Getalistofcountrynames.htm
 */
@Singleton
@Named("countries")
public class CountryList {
    
    private Set<Country> countries;

    @PostConstruct
    public void initCountries() {
        countries = new TreeSet<Country>();

        Locale[] locales = Locale.getAvailableLocales();
        for (Locale locale : locales) {
            String iso = locale.getISO3Country();
            String code = locale.getCountry();
            String name = locale.getDisplayCountry();

            if (!"".equals(iso) && !"".equals(code) && !"".equals(name)) {
                countries.add(new Country(iso, code, name));
            }
        }
    }

    public Set<Country> getCountries() {
        return countries;
    }

    public void setCountries(Set<Country> countries) {
        this.countries = countries;
    }
}
