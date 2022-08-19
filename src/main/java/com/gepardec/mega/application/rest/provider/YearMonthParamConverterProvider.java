package com.gepardec.mega.application.rest.provider;

import com.gepardec.mega.rest.mapper.YearMonthParamConverter;

import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import javax.ws.rs.ext.Provider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.YearMonth;

@Provider
public class YearMonthParamConverterProvider implements ParamConverterProvider {
    @Override
    public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType,
                                              Annotation[] annotations) {
        if (rawType.equals(YearMonth.class)) {
            return (ParamConverter<T>) new YearMonthParamConverter();
        }
        return null;
    }
}
