package com.mysema.luja.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Field {

    Index index() default Index.NOT_ANALYZED;
    
    Store store() default Store.YES;
    
}
