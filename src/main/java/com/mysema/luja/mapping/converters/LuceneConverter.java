package com.mysema.luja.mapping.converters;

import java.util.List;

import org.apache.lucene.document.Fieldable;

import com.mysema.converters.Converter;

public interface LuceneConverter<T> extends Converter<T> {

    List<Fieldable> fromObject(FieldMapping mapping, Object input);
    
    T fromField(Fieldable field);
    
}
