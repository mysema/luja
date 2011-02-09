package com.mysema.luja.mapping.domain;

import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.lang.LocaleUtils;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.NumericField;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import com.mysema.luja.annotations.DateResolution;
import com.mysema.luja.annotations.Field;
import com.mysema.luja.annotations.Resolution;
import com.mysema.query.annotations.QueryEntity;

@QueryEntity
public class FieldAnnotated {

    private int intNumber;

    @DateResolution(Resolution.DAY)
    private LocalDate date;

    @DateResolution(Resolution.MILLISECOND)
    private DateTime time;

    private LocalDateTime localTime;

    @DateResolution(Resolution.DAY)
    private Date javaDate;

    private String code;

    private String name;

    @Field(index = Index.ANALYZED)
    private String tokenized;

    private Locale locale;

    public FieldAnnotated() {
    }

    public FieldAnnotated(int id, String name) {
        this(id, new LocalDate(), new DateTime(), new LocalDateTime(), new Date(), "", name, "",
             Locale.ENGLISH);
    }

    public FieldAnnotated(int intNumber, LocalDate date, DateTime time, LocalDateTime localTime,
                          Date javaDate, String code, String name, String tokenized, Locale locale) {
        this.intNumber = intNumber;
        this.date = date;
        this.time = time;
        this.localTime = localTime;
        this.javaDate = javaDate;
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
                new LocalDate(DateTools.stringToTime(document.getFieldable("date").stringValue()),
                              DateTimeZone.UTC);
            time =
                new DateTime(DateTools.stringToTime(document.getFieldable("time").stringValue()));
            localTime =
                new LocalDateTime(DateTools.stringToTime(document.getFieldable("localTime")
                        .stringValue()), DateTimeZone.UTC);
            javaDate =
                new Date(DateTools.stringToTime(document.getFieldable("javaDate").stringValue()));
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

        document.add(new org.apache.lucene.document.Field("date", DateTools.timeToString(
                date.toDateTimeAtStartOfDay(DateTimeZone.UTC).getMillis(),
                Resolution.DAY.asLuceneResolution()), Store.YES, Index.NOT_ANALYZED));

        document.add(new org.apache.lucene.document.Field("time", DateTools.timeToString(
                time.getMillis(),
                Resolution.MILLISECOND.asLuceneResolution()), Store.YES, Index.NOT_ANALYZED));

        document.add(new org.apache.lucene.document.Field("localTime", DateTools.timeToString(
                localTime.toDateTime(DateTimeZone.UTC).getMillis(),
                Resolution.MILLISECOND.asLuceneResolution()), Store.YES, Index.NOT_ANALYZED));

        document.add(new org.apache.lucene.document.Field("javaDate", DateTools.timeToString(
                javaDate.getTime(),
                Resolution.DAY.asLuceneResolution()), Store.YES, Index.NOT_ANALYZED));

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

    public DateTime getTime() {
        return time;
    }

    public void setTime(DateTime time) {
        this.time = time;
    }

    public Date getJavaDate() {
        return javaDate;
    }

    public void setJavaDate(Date javaDate) {
        this.javaDate = javaDate;
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

    public LocalDateTime getLocalTime() {
        return localTime;
    }

    public void setLocalTime(LocalDateTime localTime) {
        this.localTime = localTime;
    }

}
