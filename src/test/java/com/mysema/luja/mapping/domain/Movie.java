package com.mysema.luja.mapping.domain;

import org.apache.lucene.document.Field.Index;

import com.mysema.luja.annotations.Field;
import com.mysema.query.annotations.QueryEntity;

@QueryEntity
public class Movie {

    private String id;
    
    private String title;
    
    private String author;

    @Field(index=Index.ANALYZED)
    private String text;
    
    private int year;
    
    private int gross;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getGross() {
        return gross;
    }

    public void setGross(int gross) {
        this.gross = gross;
    }
    
      
    
}
