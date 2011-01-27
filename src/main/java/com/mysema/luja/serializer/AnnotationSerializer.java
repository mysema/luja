package com.mysema.luja.serializer;

import java.lang.reflect.AnnotatedElement;
import java.util.Date;

import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Field.Index;
import org.joda.time.LocalDate;
import org.joda.time.ReadableInstant;

import com.mysema.luja.annotations.DateResolution;
import com.mysema.luja.annotations.Field;
import com.mysema.query.QueryException;
import com.mysema.query.lucene.LuceneSerializer;
import com.mysema.query.types.Path;

/**
 * Lucene query serializer that understands Luja annotations and quides the
 * Lucene serialiser
 * 
 * @author laimw
 * 
 */
public class AnnotationSerializer extends LuceneSerializer {

    public AnnotationSerializer() {
        super(true, true);
    }

    @Override
    protected boolean isTokenized(Path<?> path) {
        if (path.getAnnotatedElement().isAnnotationPresent(Field.class)) {
            Field fieldAnnotation = path.getAnnotatedElement().getAnnotation(Field.class);
            if (fieldAnnotation.index() == Index.ANALYZED
                || fieldAnnotation.index() == Index.ANALYZED_NO_NORMS) {
                return true;
            }
            return false;
        }
        return super.isTokenized(path);
    }

    @Override
    protected String normalize(Path<?> path, String s) {
        // Do not normalize not tokenized path
        if (!isTokenized(path)) {
            return s;
        }
        return super.normalize(path, s);
    }

    @Override
    protected String convert(Path<?> path, Object value) {
        System.out.println("Här " + value);
        if (path.getAnnotatedElement().isAnnotationPresent(DateResolution.class)) {
            DateResolution resolutionAnnotation = path.getAnnotatedElement().getAnnotation(DateResolution.class);
            
            Long instant = null;
            if (value instanceof Date) {
                instant = ((Date)value).getTime();
            } else if (value instanceof LocalDate) {
                instant = ((LocalDate)value).toDateTimeAtStartOfDay().getMillis();
            } else if (value instanceof ReadableInstant) {
                instant = ((ReadableInstant)value).getMillis();
            }
            if (instant == null) {
                throw new QueryException("Unsupported value type: " + value.getClass());
            }
            
            return DateTools.timeToString(instant, resolutionAnnotation.value().asLuceneResolution());
            
        }
        return super.convert(path, value);
    }

}
