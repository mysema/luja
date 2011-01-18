package com.mysema.luja.mapping.converters;

import static java.util.Collections.singletonList;

import java.util.List;

import org.apache.lucene.document.Fieldable;
import org.apache.lucene.document.NumericField;

public class LongConverter extends com.mysema.converters.LongConverter implements
        LuceneConverter<Long>, NumericSetter {

    private FieldableFactory factory;

    public LongConverter(FieldableFactory factory) {
        this.factory = factory;
    }

    @Override
    public List<Fieldable> fromObject(FieldMapping mapping, Object value) {
        return singletonList(factory.getNumericField(mapping, value, this));
    }

    @Override
    public void setValue(NumericField field, Object value) {
        field.setLongValue((Long) value);
    }

    @Override
    public Long fromField(Fieldable field) {
        // TODO Auto-generated method stub
        return null;
    }

}
