package com.mysema.luja.mapping.converters;

import static java.util.Collections.singletonList;

import java.util.List;

import org.apache.lucene.document.Fieldable;
import org.apache.lucene.document.NumericField;

public class ByteConverter extends com.mysema.converters.ByteConverter implements
        LuceneConverter<Byte>, NumericSetter {

    private FieldableFactory factory;

    public ByteConverter(FieldableFactory factory) {
        this.factory = factory;
    }

    @Override
    public List<Fieldable> fromObject(FieldMapping mapping, Object value) {
        return singletonList(factory.getNumericField(mapping, value, this));
    }

    @Override
    public void setValue(NumericField field, Object value) {
        field.setIntValue((Byte) value);
    }

    @Override
    public Byte fromField(Fieldable field) {
        // TODO Auto-generated method stub
        return null;
    }

}
