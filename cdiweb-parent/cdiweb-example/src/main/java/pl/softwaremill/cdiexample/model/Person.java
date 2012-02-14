package pl.softwaremill.cdiexample.model;

import java.io.InputStream;
import java.util.List;

/**
 * User: szimano
 */
public class Person {
    
    private String name;
    
    private String lastName;

    private List<String> addresses;
    
    private InputStream file;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public List<String> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<String> addresses) {
        this.addresses = addresses;
    }

    public InputStream getFile() {
        return file;
    }

    public void setFile(InputStream file) {
        this.file = file;
    }

    @Override
    public String toString() {
        return "Person{" +
                "name='" + name + '\'' +
                ", lastName='" + lastName + '\'' +
                ", addresses=" + addresses +
                '}';
    }
}
