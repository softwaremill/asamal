package pl.softwaremill.asamal.example.converter;

import org.apache.commons.beanutils.converters.AbstractConverter;

public class EnumConverter extends AbstractConverter {
    @Override
    protected Object convertToType(Class aClass, Object o) throws Throwable {
        return Enum.valueOf(aClass, o.toString());
    }

    @Override
    protected Class getDefaultType() {
        return Enum.class;
    }
}
