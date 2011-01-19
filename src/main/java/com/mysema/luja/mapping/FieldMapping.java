package com.mysema.luja.mapping;

import org.apache.lucene.document.Fieldable;

public interface FieldMapping<T> {

    String getPath();
    
    Fieldable fromObject(Object root);
    
    T fromFieldable(Fieldable field);
}
