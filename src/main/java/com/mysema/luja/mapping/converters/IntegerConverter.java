package com.mysema.luja.mapping.converters;

import static java.util.Collections.singletonList;

import java.util.List;

import org.apache.lucene.document.Fieldable;
import org.apache.lucene.document.NumericField;

public class IntegerConverter extends com.mysema.converters.IntegerConverter implements
        LuceneConverter<Integer>, NumericSetter {

    private FieldableFactory factory;

    public IntegerConverter(FieldableFactory factory) {
        this.factory = factory;
    }

    @Override
    public List<Fieldable> fromObject(FieldMapping mapping, Object value) {
        return singletonList(factory.getNumericField(mapping, value, this));
    }

    @Override
    public void setValue(NumericField field, Object value) {
        field.setIntValue((Integer) value);
    }

    @Override
    public Integer fromField(Fieldable field) {
        // TODO Auto-generated method stub
        return null;
    }

}
