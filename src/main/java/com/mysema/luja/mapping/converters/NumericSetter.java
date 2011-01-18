package com.mysema.luja.mapping.converters;

import org.apache.lucene.document.NumericField;

public interface NumericSetter {
    void setValue(NumericField field, Object value);
}
