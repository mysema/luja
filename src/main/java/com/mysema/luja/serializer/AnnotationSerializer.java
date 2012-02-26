package com.mysema.luja.serializer;

import java.util.Date;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Field.Index;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.ReadableInstant;

import com.mysema.luja.annotations.DateResolution;
import com.mysema.luja.annotations.Field;
import com.mysema.luja.annotations.Resolution;
import com.mysema.query.lucene.LuceneSerializer;
import com.mysema.query.types.Path;

/**
 * Lucene query serializer that understands Luja annotations and quides the
 * Lucene serializer. Supports LocalDate, LocalDateTime, ReadableInstant and
 * java.util.Date.
 * 
 * @author laimw
 * 
 */
public class AnnotationSerializer extends LuceneSerializer {

    public AnnotationSerializer(Locale sortLocale) {
        super(true, true, sortLocale);
    }

    @Override
    protected String[] convert(Path<?> leftSide, Object rightSide) {

        String str = handleDates(leftSide, rightSide);
        if (str != null) {
            return new String[] { str };
        }

        str = rightSide.toString();

        //Split and lowercase analyzed cases
        if (isAnalyzed(leftSide)) {
            str = str.toLowerCase();
            if (!str.equals("")) {
                return StringUtils.split(str);
            }
        }

        return new String[] { str };
    }

    private String handleDates(Path<?> leftSide, Object rightSide) {

        Long instant = null;
        Resolution resolution = null;

        if (rightSide instanceof Date) {
            instant = ((Date) rightSide).getTime();
            resolution = Resolution.MILLISECOND;
        } else if (rightSide instanceof LocalDate) {
            instant = ((LocalDate) rightSide).toDateTimeAtStartOfDay(DateTimeZone.UTC).getMillis();
            resolution = Resolution.DAY;
        } else if (rightSide instanceof LocalDateTime) {
            instant = ((LocalDateTime) rightSide).toDateTime(DateTimeZone.UTC).getMillis();
            resolution = Resolution.MILLISECOND;
        } else if (rightSide instanceof ReadableInstant) {
            instant = ((ReadableInstant) rightSide).getMillis();
            resolution = Resolution.MILLISECOND;
        }

        if (instant == null) {
            return null;
        }

        if (leftSide.getAnnotatedElement().isAnnotationPresent(DateResolution.class)) {
            resolution = leftSide.getAnnotatedElement().getAnnotation(DateResolution.class).value();

        }

        return DateTools.timeToString(instant, resolution.asLuceneResolution());

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
