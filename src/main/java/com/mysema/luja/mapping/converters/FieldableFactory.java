package com.mysema.luja.mapping.converters;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.document.NumericField;

public class FieldableFactory {

    public Fieldable getNullField(FieldMapping mapping) {
        return new Field(mapping.getName() + Constants.NULL_FIELD,
                         Constants.NULL_FIELD_VALUE, Store.NO, Index.NOT_ANALYZED);
    }

    public <T> Fieldable getField(FieldMapping mapping, T value, LuceneConverter<T> converter) {
        if (value == null) {
            return getNullField(mapping);
        }
        return new Field(mapping.getName(), converter.toString(value), mapping.getStore(),
                         mapping.getIndex());
    }

    public Fieldable getNumericField(FieldMapping mapping, Object value, NumericSetter setter) {
        if (value == null) {
            return getNullField(mapping);
        }
        NumericField field =
            new NumericField(mapping.getName(), mapping.getStore(), mapping.getIndex() != Index.NO);
        setter.setValue(field, value);
        return field;
    }

}
