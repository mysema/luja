package com.mysema.luja.serializer;

import com.mysema.query.lucene.LuceneSerializer;

/**
 * Lucene query serializer that understands Luja annotations and quides the Lucene serialiser
 * @author laimw
 *
 */
public class AnnotationSerializer extends LuceneSerializer {

    public AnnotationSerializer() {
        super(true, true);
    }
    
    public AnnotationSerializer(boolean lowerCase, boolean splitTerms) {
        super(lowerCase, splitTerms);
    }

    
    
    
}
