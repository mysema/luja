package com.mysema.luja.mapping.converters;

import java.lang.reflect.Field;
import java.util.List;

import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Fieldable;

import com.mysema.luja.annotations.Transient;
import com.mysema.query.QueryException;

public final class FieldMapping {

    private final String name;

    private final Store store;

    private final Index index;

    private final boolean transientField;

    private final Field field;

    private final LuceneConverter<?> converter;

    private static final LuceneConverters CONVERTERS = new LuceneConverters();

    public FieldMapping(LuceneConverter<?> converter) {
        name = "ROOT";
        store = null;
        index = null;
        transientField = true;
        field = null;
        this.converter = converter;
    }

    public FieldMapping(Field field) {

        this.field = field;
        if (field.isAnnotationPresent(Transient.class)) {
            this.name = null;
            this.store = null;
            this.index = null;
            this.converter = null;
            this.transientField = true;
            return;
        }

        Store localStore = Store.YES;
        Index localIndex = Index.ANALYZED;
        String localName = field.getName();

        if (field.isAnnotationPresent(com.mysema.luja.annotations.Field.class)) {
            com.mysema.luja.annotations.Field fieldAnnotation =
                field.getAnnotation(com.mysema.luja.annotations.Field.class);
            localStore = fieldAnnotation.store();
            localIndex = fieldAnnotation.index();
            if (!fieldAnnotation.name().equals("")) {
                localName = fieldAnnotation.name();
            }
        }
        this.store = localStore;
        this.index = localIndex;
        this.name = localName;
        this.transientField = store == Store.NO && index == Index.NO;
        this.converter = CONVERTERS.getConverter(field.getType());
    }

    public String getName() {
        return name;
    }

    public Store getStore() {
        return store;
    }

    public Index getIndex() {
        return index;
    }

    public boolean isTransientField() {
        return transientField;
    }

    public Object getValue(Object input) {
        try {
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            return field.get(input);
        } catch (Exception e) {
            throw new QueryException(e);
        }
    }

    public Class<?> getType() {
        return field.getType();
    }

    public List<Fieldable> fromObject(Object input) {
        return converter.fromObject(this, getValue(input));
    }

}
