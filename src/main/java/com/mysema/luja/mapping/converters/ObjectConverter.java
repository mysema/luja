package com.mysema.luja.mapping.converters;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.document.Fieldable;

import com.mysema.converters.AbstractConverter;

public class ObjectConverter extends AbstractConverter<Object> implements LuceneConverter<Object> {

    private FieldableFactory factory;
    
    private static final LuceneConverters CONVERTERS = new LuceneConverters();

    public ObjectConverter(FieldableFactory factory) {
        this.factory = factory;
    }

    @Override
    public List<Fieldable> fromObject(FieldMapping mapping, Object value) {
//        FieldMapping context = mappingContext;
//        if (context == null) {
//            context = new FieldMapping(this);
//        }
        
        //JOs tähän tullaan mapping != null, niin sitten pitää 
        //mennä siihen sisään, otetaan fieldin arvo ja siitä tulee uusi
        //konteksti
        
        List<FieldMapping> mappings = new ArrayList<FieldMapping>();
        for (Field field : value.getClass().getDeclaredFields()) {
            mappings.add(new FieldMapping(field));
        }
        
        List<Fieldable> fieldables = new ArrayList<Fieldable>();
        for(FieldMapping m: mappings) {
            if (m.isTransientField()) {
                continue;
            }
            fieldables.addAll(m.fromObject(value));
        }
        
       return fieldables;
    }

    @Override
    public Object fromField(Fieldable field) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Class<Object> getJavaType() {
        throw new UnsupportedOperationException("Object converter is not supporting any java type");
    }

    @Override
    public Object fromString(String str) {
        return str;
    }

}
