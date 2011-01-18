package com.mysema.luja.mapping.converters;

import static java.util.Collections.singletonList;

import java.util.List;

import org.apache.lucene.document.Fieldable;

public class CharacterConverter extends com.mysema.converters.CharacterConverter implements
        LuceneConverter<Character> {

    private FieldableFactory factory;

    public CharacterConverter(FieldableFactory factory) {
        this.factory = factory;
    }

    @Override
    public List<Fieldable> fromObject(FieldMapping mapping, Object value) {
        return singletonList(factory.getField(mapping, (Character) value, this));
    }

    @Override
    public Character fromField(Fieldable field) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Class<Character> getJavaType() {
        return Character.class;
    }

}
