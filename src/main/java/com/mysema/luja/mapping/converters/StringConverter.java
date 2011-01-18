package com.mysema.luja.mapping.converters;

import static java.util.Collections.singletonList;

import java.util.List;

import org.apache.lucene.document.Fieldable;

import com.mysema.converters.AbstractConverter;

public class StringConverter extends AbstractConverter<String> implements LuceneConverter<String> {

    private FieldableFactory factory;

    public StringConverter(FieldableFactory factory) {
        this.factory = factory;
    }

    @Override
    public List<Fieldable> fromObject(FieldMapping mapping, Object value) {
        return singletonList(factory.getField(mapping, (String) value, this));
    }

    @Override
    public String fromField(Fieldable field) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Class<String> getJavaType() {
        return String.class;
    }

    @Override
    public String fromString(String str) {
        return str;
    }

}
