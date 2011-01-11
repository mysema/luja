package com.mysema.luja.mapping;

import org.apache.lucene.document.Fieldable;

public class TypeHandler {

    public Fieldable visit(String name, Field annotation, Class<String> fieldType, Object fieldValue) {
        return new org.apache.lucene.document.Field(
                name, 
                (String) (fieldValue != null ? fieldValue : ""),
                annotation.store(), 
                annotation.index());
    }

}
