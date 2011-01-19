package com.mysema.luja.mapping;

import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Fieldable;

public interface ContainerMapping<T> {

    String getPath();
    
    List<Fieldable> fromObject(Object root);
    
    T fromFields(Document document);
}
