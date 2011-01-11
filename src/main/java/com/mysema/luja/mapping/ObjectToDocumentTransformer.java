package com.mysema.luja.mapping;

import org.apache.commons.collections15.Transformer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.document.NumericField;

import com.mysema.commons.lang.Assert;
import com.mysema.query.QueryException;

public class ObjectToDocumentTransformer implements Transformer<Object, Document> {

    //private TypeHandler typeHandler;

    /**
     * Tips: - pienet fieldit ennen suuria ja sitten niiden latauksia voi
     * määrittää
     */

    @Override
    public Document transform(Object input) {

        Assert.notNull(input, "Transformed input should not be null");

        if (!input.getClass().isAnnotationPresent(Indexed.class)) {
            throw new TypeNotMappedException("Input object does not have Indexed annotation");
        }

        Document document = new Document();

        for (java.lang.reflect.Field field : input.getClass().getDeclaredFields()) {

            if (field.isAnnotationPresent(Field.class)) {
                Field fieldAnnotation = field.getAnnotation(Field.class);
                String name =
                    fieldAnnotation.name().equals("") ? field.getName() : fieldAnnotation.name();

                Object value;
                try {
                    if (!field.isAccessible()) {
                        field.setAccessible(true);
                    }
                    value = field.get(input);
                } catch (Exception e) {
                    throw new QueryException(e);
                }
                
                document.add(createField(name, value, field.getType(), fieldAnnotation));
            }
        }

        return document;

    }

    private Fieldable createField(String name, Object value, Class<?> fieldType, Field fieldAnnotation) {
        
        //Handling null, this is the same for all types.
        //Null values are indexed in the different field and the
        //searcher must use this field when user is querying null
        //values
        if (value == null) {
            return new org.apache.lucene.document.Field(
                    name + Constants.NULL_FIELD_POSTFIX, 
                    Constants.NULL_FIELD_VALUE,
                    Store.NO,
                    Index.NOT_ANALYZED);
        }
        
        //Handling numbers

        // Integer and shorter
        if (fieldType == Byte.TYPE || fieldType == Short.TYPE || fieldType == Integer.TYPE
            || fieldType == Byte.class || fieldType == Short.class || fieldType == Integer.class) {
            NumericField numField = getNumericField(name, fieldAnnotation);
            return numField.setIntValue(((Number) value).intValue());
        }

        // Long
        if (fieldType == Long.TYPE || fieldType == Long.class) {
            NumericField numField = getNumericField(name, fieldAnnotation);
            return numField.setLongValue(((Long) value).longValue());
        }

        // Float
        if (fieldType == Float.TYPE || fieldType == Float.class) {
            NumericField numField = getNumericField(name, fieldAnnotation);
            return numField.setFloatValue(((Float) value).floatValue());
        }

        // Double
        if (fieldType == Double.TYPE || fieldType == Double.class) {
            NumericField numField = getNumericField(name, fieldAnnotation);
            return numField.setDoubleValue(((Double) value).doubleValue());
        }
        
        // Array
        if (fieldType.isArray()) {
            
        }


        // Handle other types
        return new org.apache.lucene.document.Field(name, value.toString(),
                                                    fieldAnnotation.store(),
                                                    fieldAnnotation.index());

    }

    private NumericField getNumericField(String name, Field fieldAnnotation) {
        return new NumericField(name, fieldAnnotation.store(),
                                fieldAnnotation.index() == Index.NO ? true : false);
    }
}
