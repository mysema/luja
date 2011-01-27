package com.mysema.luja.serializer;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Field.Index;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.ReadableInstant;

import com.mysema.luja.annotations.DateResolution;
import com.mysema.luja.annotations.Field;
import com.mysema.query.QueryException;
import com.mysema.query.lucene.LuceneSerializer;
import com.mysema.query.types.Path;

/**
 * Lucene query serializer that understands Luja annotations and quides the
 * Lucene serialiser. Supports LocalDate, LocalDateTime, ReadableInstant and java.util.Date.
 * 
 * @author laimw
 * 
 */
public class AnnotationSerializer extends LuceneSerializer {

    public AnnotationSerializer() {
        super(true, true);
    }

    @Override
    protected String[] convert(Path<?> leftSide, Object rightSide) {

        String str = rightSide.toString();

        if (leftSide.getAnnotatedElement().isAnnotationPresent(DateResolution.class)) {
            DateResolution resolutionAnnotation =
                leftSide.getAnnotatedElement().getAnnotation(DateResolution.class);

            Long instant = null;
            if (rightSide instanceof Date) {
                instant = ((Date) rightSide).getTime();
            } else if (rightSide instanceof LocalDate) {
                instant =
                    ((LocalDate) rightSide).toDateTimeAtStartOfDay(DateTimeZone.UTC).getMillis();
            } else if (rightSide instanceof LocalDateTime) {
                instant = ((LocalDateTime) rightSide).toDateTime(DateTimeZone.UTC).getMillis();
            } else if (rightSide instanceof ReadableInstant) {
                instant = ((ReadableInstant) rightSide).getMillis();
            }
            if (instant == null) {
                throw new QueryException("Unsupported value type: " + rightSide.getClass());
            }

            String timeString =
                DateTools.timeToString(instant, resolutionAnnotation.value().asLuceneResolution());
            // System.out.println("timeToString: " + timeString);
            return new String[] { timeString };

        }

        if (isAnalyzed(leftSide)) {
            str = str.toLowerCase();
            if (!str.equals("")) {
                return StringUtils.split(str);
            }
        }

        return new String[] { str };
    }

    private boolean isAnalyzed(Path<?> path) {
        if (path.getAnnotatedElement().isAnnotationPresent(Field.class)) {
            Field fieldAnnotation = path.getAnnotatedElement().getAnnotation(Field.class);
            if (fieldAnnotation.index() == Index.ANALYZED
                || fieldAnnotation.index() == Index.ANALYZED_NO_NORMS) {
                return true;
            }
        }
        return false;
    }

}
