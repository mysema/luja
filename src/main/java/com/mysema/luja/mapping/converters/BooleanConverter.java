package com.mysema.luja.mapping.converters;

import static java.util.Collections.singletonList;

import java.util.List;

import org.apache.lucene.document.Fieldable;

public class BooleanConverter extends com.mysema.converters.BooleanConverter implements
        LuceneConverter<Boolean> {

    private FieldableFactory factory;

    public BooleanConverter(FieldableFactory factory) {
        this.factory = factory;
    }

    @Override
    public List<Fieldable> fromObject(FieldMapping mapping, Object value) {
        return singletonList(factory.getField(mapping, (Boolean) value, this));
    }

    @Override
    public Boolean fromField(Fieldable field) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Class<Boolean> getJavaType() {
        return Boolean.class;
    }

}
