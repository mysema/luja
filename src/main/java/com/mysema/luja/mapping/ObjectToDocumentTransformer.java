package com.mysema.luja.mapping;

import org.apache.commons.collections15.Transformer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.document.NumericField;

import com.mysema.commons.lang.Assert;
import com.mysema.luja.annotations.Field;
import com.mysema.luja.annotations.Indexed;
import com.mysema.query.QueryException;

public class ObjectToDocumentTransformer implements Transformer<Object, Document> {

    // private TypeHandler typeHandler;

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

                addFieldsForType(document, name, value, field.getType(), fieldAnnotation);
            }
        }

        return document;

    }

    private void addFieldsForType(Document doc,
            String name,
            Object value,
            Class<?> fieldType,
            Field fieldAnnotation) {

        // Handling null, this is the same for all types.
        // Null values are indexed in the different field and the
        // searcher must use this field when user is querying null
        // values
        if (value == null) {
            doc.add(new org.apache.lucene.document.Field(name + Constants.NULL_FIELD_POSTFIX,
                                                        Constants.NULL_FIELD_VALUE, Store.NO,
                                                        Index.NOT_ANALYZED));
            return;
        }

        // Handling numbers

        // Integer and shorter
        if (fieldType == Byte.TYPE || fieldType == Short.TYPE || fieldType == Integer.TYPE
            || fieldType == Byte.class || fieldType == Short.class || fieldType == Integer.class) {
            NumericField numField = getNumericField(name, fieldAnnotation);
            doc.add(numField.setIntValue(((Number) value).intValue()));
            return;
        }

        // Long
        if (fieldType == Long.TYPE || fieldType == Long.class) {
            NumericField numField = getNumericField(name, fieldAnnotation);
            doc.add(numField.setLongValue(((Long) value).longValue()));
            return;
        }

        // Float
        if (fieldType == Float.TYPE || fieldType == Float.class) {
            NumericField numField = getNumericField(name, fieldAnnotation);
            doc.add(numField.setFloatValue(((Float) value).floatValue()));
            return;
        }

        // Double
        if (fieldType == Double.TYPE || fieldType == Double.class) {
            NumericField numField = getNumericField(name, fieldAnnotation);
            doc.add(numField.setDoubleValue(((Double) value).doubleValue()));
            return;
        }

//        // Array
//        if (fieldType.isArray()) {
//            // Length
//            Fieldable lengthField =
//                new NumericField(name + ".lenght", Store.NO, true)
//                        .setIntValue(((Object[]) value).length);
//            // Collect indexed data
//            Fieldable indexedField = createFieldFromIterable(name, (Iterable<?>) value);
////            // Store values as json
////            Fieldable jsonField = createJsonField(name, value);
//        }

        // Handle other types
        doc.add(new org.apache.lucene.document.Field(name, value.toString(),
                                                    fieldAnnotation.store(),
                                                    fieldAnnotation.index()));

    }

    private Fieldable createFieldFromIterable(String name, Iterable<?> values) {
        // TODO How the more complex cases should be handled
        // We probably have to understand more about the nature of the actual
        // type in the
        // list
        // One thing would be to create json tokenizer, which would skip all the
        // json data and
        // let json handle all the deep tostring handling

        StringBuilder builder = new StringBuilder();
        for (Object value : values) {
            builder.append(value.toString()).append(' ');
        }
        return new org.apache.lucene.document.Field(name, builder.toString(), Store.NO,
                                                    Index.ANALYZED);
    }

    private NumericField getNumericField(String name, Field fieldAnnotation) {
        return new NumericField(name, fieldAnnotation.store(),
                                fieldAnnotation.index() == Index.NO ? true : false);
    }
}
