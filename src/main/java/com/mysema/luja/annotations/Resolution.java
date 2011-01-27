package com.mysema.luja.annotations;

public enum Resolution {
    MILLISECOND, SECOND, MINUTE, HOUR, DAY, MONTH, YEAR;

    public org.apache.lucene.document.DateTools.Resolution asLuceneResolution() {
        switch (this) {
        case MILLISECOND:
            return org.apache.lucene.document.DateTools.Resolution.MILLISECOND;
        case SECOND:
            return org.apache.lucene.document.DateTools.Resolution.SECOND;
        case MINUTE:
            return org.apache.lucene.document.DateTools.Resolution.MINUTE;
        case HOUR:
            return org.apache.lucene.document.DateTools.Resolution.HOUR;
        case DAY:
            return org.apache.lucene.document.DateTools.Resolution.DAY;
        case MONTH:
            return org.apache.lucene.document.DateTools.Resolution.MONTH;
        case YEAR:
            return org.apache.lucene.document.DateTools.Resolution.YEAR;
        }
        throw new RuntimeException("Unsupported conversion, this should not never happen");
    }
}
