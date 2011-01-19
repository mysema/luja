package com.mysema.luja.mapping.converters;

import static java.util.Collections.singletonList;

import java.util.Collection;
import java.util.List;

import org.apache.lucene.document.Fieldable;

import com.mysema.converters.AbstractConverter;

@SuppressWarnings("rawtypes")
public class CollectionConverter extends AbstractConverter<Collection> implements LuceneConverter<Collection> {

    private FieldableFactory factory;

    public CollectionConverter(FieldableFactory factory) {
        this.factory = factory;
    }

    @Override
    public List<Fieldable> fromObject(FieldMapping mapping, Object value) {
        //return singletonList(factory.getField(mapping, (String) value, this));
        
        System.out.println(value);
        
        return null;
    }

    @Override
    public Collection<?> fromField(Fieldable field) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Class<Collection> getJavaType() {
        return Collection.class;
    }

    @Override
    public Collection fromString(String str) {
        return null;
    }

}
