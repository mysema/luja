package com.mysema.luja.mapping;

import org.apache.commons.collections15.Transformer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Fieldable;

import com.mysema.commons.lang.Assert;
import com.mysema.luja.mapping.converters.LuceneConverter;
import com.mysema.luja.mapping.converters.LuceneConverters;

public class ObjectToDocumentTransformer implements Transformer<Object, Document> {

    // TODOThread safe mapping cache
    // private final Map<Class<?>, List<FieldMapping>> classToMapping = new
    // ConcurrentHashMap<Class<?>, List<FieldMapping>>();

    private static final LuceneConverters CONVERTERS = new LuceneConverters();

    @Override
    public Document transform(Object input) {

        Assert.notNull(input, "Transformed input should not be null");

        LuceneConverter<?> root = CONVERTERS.getConverter(input.getClass());

        Document document = new Document();
        for (Fieldable field : root.fromObject(null, input)) {
            document.add(field);
        }

        return document;

    }

}
