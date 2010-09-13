package com.mysema.query.lucene;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

import com.mysema.query.DefaultQueryMetadata;
import com.mysema.query.QueryMetadata;
import com.mysema.query.types.path.StringPath;

public class TermElementTest {

    @Test
    public void test(){
        StringPath title = new StringPath("title");
        LuceneSerializer serializer = new LuceneSerializer(false,true);
        QueryMetadata metadata = new DefaultQueryMetadata();
        assertEquals("title:\"Hello World\"", serializer.toQuery(title.eq("Hello World"), metadata).toString());
        assertEquals("title:Hello World", serializer.toQuery(title.eq(new TermElement("Hello World")), metadata).toString());
    }

    @Test
    public void testEqualsAndHashCode(){
    TermElement el1 = new TermElement("x"), el2 = new TermElement("x"), el3 = new TermElement("y");
        assertEquals(el1, el2);
        assertFalse(el1.equals(el3));
        assertEquals(el1.hashCode(), el2.hashCode());
    }

}
