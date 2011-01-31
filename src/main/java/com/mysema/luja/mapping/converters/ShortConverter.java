package com.mysema.luja.mapping.converters;

import static java.util.Collections.singletonList;

import java.util.List;

import org.apache.lucene.document.Fieldable;
import org.apache.lucene.document.NumericField;

public class ShortConverter extends com.mysema.converters.ShortConverter implements
        LuceneConverter<Short>, NumericSetter {

    private FieldableFactory factory;

    public ShortConverter(FieldableFactory factory) {
        this.factory = factory;
    }

    @Override
    public List<Fieldable> fromObject(FieldMapping mapping, Object value) {
        return singletonList(factory.getNumericField(mapping, value, this));
    }

    @Override
    public void setValue(NumericField field, Object value) {
        field.setIntValue((Short) value);
    }

    @Override
    public Short fromField(Fieldable field) {
        // TODO Auto-generated method stub
        return null;
    }

}