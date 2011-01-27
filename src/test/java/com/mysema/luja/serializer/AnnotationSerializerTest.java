package com.mysema.luja.serializer;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.lucene.document.Document;
import org.apache.lucene.store.RAMDirectory;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.mysema.luja.LuceneSession;
import com.mysema.luja.LuceneSessionFactory;
import com.mysema.luja.impl.LuceneSessionFactoryImpl;
import com.mysema.luja.mapping.domain.FieldAnnotated;
import com.mysema.luja.mapping.domain.QFieldAnnotated;
import com.mysema.query.lucene.LuceneQuery;
import com.mysema.query.types.Predicate;

public class AnnotationSerializerTest {

    private QFieldAnnotated d = new QFieldAnnotated("d");

    private List<FieldAnnotated> data = new ArrayList<FieldAnnotated>();

    private LuceneSessionFactory sessionFactory;

    @Before
    public void before() {
        sessionFactory = new LuceneSessionFactoryImpl(new RAMDirectory());
        createTestData();
    }

    @Test
    public void NotAnalyzedAnnotation() {
        assertQuery(d.intNumber.eq(1), data.get(0));

        assertQuery(d.code.eq("ABC-123"), data.get(0));
        assertQuery(d.code.eq("ABC-123 B"), data.get(1));

        assertQuery(d.code.contains("ABC"), data.get(0), data.get(1), data.get(2));
        assertQuery(d.code.contains("abc"));

        // not analyzed does not support ignorecase
        assertQuery(d.code.equalsIgnoreCase("abc"));

        assertQuery(d.name.in("Aapeli", "Aakkonen"));
        assertQuery(d.name.in("Aapeli Aakkonen", "Esko Aakkonen"), data.get(0), data.get(2));

        assertQuery(d.locale.eq(new Locale("fi", "FI")), data.get(0));
        assertQuery(d.locale.eq(new Locale("fi", "SV")));
        assertQuery(d.locale.eq(new Locale("en", "US")), data.get(1));

    }

    @Test
    public void AnalyzedAnnotation() {
        assertQuery(d.tokenized.contains("java"), data.get(0), data.get(1));
    }

    @Test
    public void DateFieldSort() {
        LuceneSession session = sessionFactory.openSession(true);
        List<FieldAnnotated> results =
            convertList(session.createQuery().orderBy(d.date.asc()).list());
        assertEquals(data.get(2).getIntNumber(), results.get(0).getIntNumber());
        assertEquals(data.get(1).getIntNumber(), results.get(1).getIntNumber());
        assertEquals(data.get(0).getIntNumber(), results.get(2).getIntNumber());

        results = convertList(session.createQuery().orderBy(d.date.desc()).list());
        assertEquals(data.get(0).getIntNumber(), results.get(0).getIntNumber());
        assertEquals(data.get(1).getIntNumber(), results.get(1).getIntNumber());
        assertEquals(data.get(2).getIntNumber(), results.get(2).getIntNumber());

    }

    @Test
    @Ignore("This should work")
    public void DateSearch() {
        
        assertQuery(d.date.eq(new LocalDate(2010,1,1)), data.get(0));
        
    }
    
    @Test
    public void TimeFieldSort() {
        LuceneSession session = sessionFactory.openSession(true);
        List<FieldAnnotated> results =
            convertList(session.createQuery().orderBy(d.time.desc()).list());
        assertEquals(data.get(2).getIntNumber(), results.get(0).getIntNumber());
        assertEquals(data.get(1).getIntNumber(), results.get(1).getIntNumber());
        assertEquals(data.get(0).getIntNumber(), results.get(2).getIntNumber());

        results = convertList(session.createQuery().orderBy(d.time.asc()).list());
        assertEquals(data.get(0).getIntNumber(), results.get(0).getIntNumber());
        assertEquals(data.get(1).getIntNumber(), results.get(1).getIntNumber());
        assertEquals(data.get(2).getIntNumber(), results.get(2).getIntNumber());
    }

    private List<FieldAnnotated> convertList(List<Document> list) {
        List<FieldAnnotated> results = new ArrayList<FieldAnnotated>();
        for (Document document : list) {
            results.add(new FieldAnnotated(document));
        }
        return results;
    }

    private void assertQuery(Predicate where, FieldAnnotated... expects) {
        LuceneSession session = sessionFactory.openSession(true);
        LuceneQuery query = session.createQuery().where(where).orderBy(d.intNumber.asc());
        System.out.println(query);
        List<Document> results = query.list();
        if (expects == null && results.size() > 0) {
            assertEquals(0, results.size());
        }
        assertEquals(expects.length, results.size());
        int i = 0;
        for (FieldAnnotated expect : expects) {
            assertEquals(expect.getIntNumber(), new FieldAnnotated(results.get(i++)).getIntNumber());
        }
        session.close();
    }

    private void createTestData() {

        Locale fi = new Locale("fi", "FI");
        Locale enUs = new Locale("en", "US");
        Locale enUk = new Locale("en", "UK");
        data.add(new FieldAnnotated(1, new LocalDate(2010, 1, 1), new DateTime(2010, 1, 1, 12, 1,
                                                                               1, 1), "ABC-123",
                                    "Aapeli Aakkonen", "Java C++ Scala Ruby", fi));
        data.add(new FieldAnnotated(2, new LocalDate(2009, 12, 30), new DateTime(2010, 1, 1, 12, 1,
                                                                                 1, 2),
                                    "ABC-123 B", "Aapeli Ukkonen", "Java C++ Groove Ruby", enUs));

        data.add(new FieldAnnotated(3, new LocalDate(2009, 12, 29), new DateTime(2010, 1, 1, 12, 1,
                                                                                 1, 3),
                                    "ABC-123 D", "Esko Aakkonen", "PHP C Scala Ruby", enUk));

        LuceneSession session = sessionFactory.openSession(false);
        session.beginReset().addDocument(data.get(0).toDocument())
                .addDocument(data.get(1).toDocument()).addDocument(data.get(2).toDocument());
        session.close();

        session = sessionFactory.openSession(true);
        assertEquals(3, session.createQuery().count());
        session.close();

    }
}
