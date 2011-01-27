package com.mysema.luja.mapping.domain;

import java.text.ParseException;
import java.util.Locale;

import org.apache.commons.lang.LocaleUtils;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.NumericField;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import com.mysema.luja.annotations.DateResolution;
import com.mysema.luja.annotations.Field;
import com.mysema.luja.annotations.Resolution;
import com.mysema.query.annotations.QueryEntity;

@QueryEntity
public class FieldAnnotated {

    @Field
    private int intNumber;

    @Field(index = Index.NOT_ANALYZED)
    @DateResolution(Resolution.DAY)
    private LocalDate date;

    @Field(index = Index.NOT_ANALYZED)
    @DateResolution(Resolution.MILLISECOND)
    private DateTime time;
    
    @Field(index = Index.NOT_ANALYZED)
    private String code;

    @Field(index = Index.NOT_ANALYZED)
    private String name;

    @Field
    private String tokenized;

    @Field(index = Index.NOT_ANALYZED)
    private Locale locale;

    public FieldAnnotated() {
    }

    public FieldAnnotated(int intNumber, LocalDate date, DateTime time, String code, String name,
                          String tokenized, Locale locale) {
        this.intNumber = intNumber;
        this.date = date;
        this.time = time;
        this.code = code;
        this.name = name;
        this.tokenized = tokenized;
        this.locale = locale;
    }

    public FieldAnnotated(Document document) {
        // System.out.println(document.getFields());

        intNumber = Integer.parseInt(document.getField("intNumber").stringValue());
        try {
            date =
                new LocalDate(DateTools.stringToTime(document.getFieldable("date").stringValue()));
            time = new DateTime(DateTools.stringToTime(document.getFieldable("date").stringValue()));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        code = document.getFieldable("code").stringValue();
        name = document.getFieldable("name").stringValue();
        tokenized = document.getFieldable("tokenized").stringValue();
        locale = LocaleUtils.toLocale(document.getFieldable("locale").stringValue());
    }

    public Document toDocument() {
        Document document = new Document();
        document.add(new NumericField("intNumber", Store.YES, true).setIntValue(intNumber));
        // document.add(new NumericField("date", Store.YES,
        // true).setLongValue(date.getTime()));
        String dateAsString =
            DateTools.timeToString(date.toDateTimeAtStartOfDay().getMillis(), Resolution.DAY.asLuceneResolution());
        document.add(new org.apache.lucene.document.Field("date", dateAsString, Store.YES,
                                                          Index.NOT_ANALYZED));
        String timeAsString =
            DateTools.timeToString(time.getMillis(), Resolution.MILLISECOND.asLuceneResolution());
        document.add(new org.apache.lucene.document.Field("time", timeAsString, Store.YES,
                                                          Index.NOT_ANALYZED));
        document.add(new org.apache.lucene.document.Field("code", code, Store.YES,
                                                          Index.NOT_ANALYZED));
        document.add(new org.apache.lucene.document.Field("name", name, Store.YES,
                                                          Index.NOT_ANALYZED));
        document.add(new org.apache.lucene.document.Field("tokenized", tokenized, Store.YES,
                                                          Index.ANALYZED));
        document.add(new org.apache.lucene.document.Field("locale", locale.toString(), Store.YES,
                                                          Index.NOT_ANALYZED));

        return document;
    }

    public int getIntNumber() {
        return intNumber;
    }

    public void setIntNumber(int intNumber) {
        this.intNumber = intNumber;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate localDate) {
        this.date = localDate;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTokenized() {
        return tokenized;
    }

    public void setTokenized(String tokenized) {
        this.tokenized = tokenized;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

}
