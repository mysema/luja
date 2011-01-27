package com.mysema.luja.serializer;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.lucene.document.Document;
import org.apache.lucene.store.RAMDirectory;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Before;
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

    LocalDate d1 = new LocalDate(2009, 12, 30);

    LocalDate d2 = new LocalDate(2009, 12, 31);

    LocalDate d3 = new LocalDate(2010, 1, 1);

    DateTime t1 = new DateTime(2010, 1, 1, 12, 1, 1, 1);

    DateTime t2 = new DateTime(2010, 1, 1, 12, 1, 1, 2);

    DateTime t3 = new DateTime(2010, 1, 1, 12, 1, 1, 3);

    LocalDateTime lt1 = new LocalDateTime(t1);

    LocalDateTime lt2 = new LocalDateTime(t2);

    LocalDateTime lt3 = new LocalDateTime(t3);

    Date jd1 = new Date(d1.toDateTimeAtStartOfDay(DateTimeZone.UTC).getMillis());

    Date jd2 = new Date(d2.toDateTimeAtStartOfDay(DateTimeZone.UTC).getMillis());

    Date jd3 = new Date(d3.toDateTimeAtStartOfDay(DateTimeZone.UTC).getMillis());

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
    public void LocalDateSort() {
        LuceneSession session = sessionFactory.openSession(true);
        List<FieldAnnotated> results =
            convertList(session.createQuery().orderBy(d.date.desc()).list());
        assertEquals(data.get(2).getIntNumber(), results.get(0).getIntNumber());
        assertEquals(data.get(1).getIntNumber(), results.get(1).getIntNumber());
        assertEquals(data.get(0).getIntNumber(), results.get(2).getIntNumber());

        results = convertList(session.createQuery().orderBy(d.date.asc()).list());
        assertEquals(data.get(0).getIntNumber(), results.get(0).getIntNumber());
        assertEquals(data.get(1).getIntNumber(), results.get(1).getIntNumber());
        assertEquals(data.get(2).getIntNumber(), results.get(2).getIntNumber());

    }

    @Test
    public void LocalDateSearch() {
        LocalDate date = new LocalDate(2010, 1, 1);
        assertQuery(d.date.eq(date), data.get(2));
        assertQuery(d.date.after(date.minusYears(1)), data.get(0), data.get(1), data.get(2));
        assertQuery(d.date.between(date.minusDays(1), date), data.get(1), data.get(2));
    }

    @Test
    public void DateSort() {
        LuceneSession session = sessionFactory.openSession(true);
        List<FieldAnnotated> results =
            convertList(session.createQuery().orderBy(d.javaDate.desc()).list());
        assertEquals(data.get(2).getIntNumber(), results.get(0).getIntNumber());
        assertEquals(data.get(1).getIntNumber(), results.get(1).getIntNumber());
        assertEquals(data.get(0).getIntNumber(), results.get(2).getIntNumber());

        results = convertList(session.createQuery().orderBy(d.javaDate.asc()).list());
        assertEquals(data.get(0).getIntNumber(), results.get(0).getIntNumber());
        assertEquals(data.get(1).getIntNumber(), results.get(1).getIntNumber());
        assertEquals(data.get(2).getIntNumber(), results.get(2).getIntNumber());

    }

    @Test
    public void DateSearch() {
        assertQuery(d.javaDate.eq(jd1), data.get(0));
        assertQuery(
                d.javaDate.after(new Date(d1.minusYears(1).toDateTimeAtStartOfDay().getMillis())),
                data.get(0),
                data.get(1),
                data.get(2));
        assertQuery(d.javaDate.between(jd1, jd2), data.get(0), data.get(1));
    }

    @Test
    public void DateTimeSort() {
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

    @Test
    public void DateTimeSearch() {
        DateTime t1 = new DateTime(2010, 1, 1, 12, 1, 1, 1);
        assertQuery(d.time.eq(t1), data.get(0));
        assertQuery(d.time.after(t1), data.get(1), data.get(2));
        assertQuery(d.time.between(t1, t1.plusMillis(1)), data.get(0), data.get(1));
    }

    @Test
    public void LocalDateTimeSort() {
        LuceneSession session = sessionFactory.openSession(true);
        List<FieldAnnotated> results =
            convertList(session.createQuery().orderBy(d.localTime.desc()).list());
        assertEquals(data.get(2).getIntNumber(), results.get(0).getIntNumber());
        assertEquals(data.get(1).getIntNumber(), results.get(1).getIntNumber());
        assertEquals(data.get(0).getIntNumber(), results.get(2).getIntNumber());

        results = convertList(session.createQuery().orderBy(d.localTime.asc()).list());
        assertEquals(data.get(0).getIntNumber(), results.get(0).getIntNumber());
        assertEquals(data.get(1).getIntNumber(), results.get(1).getIntNumber());
        assertEquals(data.get(2).getIntNumber(), results.get(2).getIntNumber());
    }

    @Test
    public void LocalDateTimeSearch() {
        assertQuery(d.localTime.eq(lt1), data.get(0));
        assertQuery(d.localTime.after(lt1), data.get(1), data.get(2));
        assertQuery(d.localTime.between(lt1, lt1.plusMillis(1)), data.get(0), data.get(1));
    }

    @Test
    public void DatesAndTimesRoundtripWorks() {
        LuceneSession session = sessionFactory.openSession(true);
        List<FieldAnnotated> results = convertList(session.createQuery().list());
        assertEquals(3, results.size());
        assertEquals(d1, results.get(0).getDate());
        assertEquals(d2, results.get(1).getDate());
        assertEquals(d3, results.get(2).getDate());
        assertEquals(t1, results.get(0).getTime());
        assertEquals(t2, results.get(1).getTime());
        assertEquals(t3, results.get(2).getTime());
        assertEquals(lt1, results.get(0).getLocalTime());
        assertEquals(lt2, results.get(1).getLocalTime());
        assertEquals(lt3, results.get(2).getLocalTime());
        assertEquals(jd1, results.get(0).getJavaDate());
        assertEquals(jd2, results.get(1).getJavaDate());
        assertEquals(jd3, results.get(2).getJavaDate());
        session.close();
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
            assertEquals("Invalid results size", 0, results.size());
        }
        assertEquals("Invalid results size", expects.length, results.size());
        int i = 0;
        for (FieldAnnotated expect : expects) {
            assertEquals(
                    "Expected id does not match",
                    expect.getIntNumber(),
                    new FieldAnnotated(results.get(i++)).getIntNumber());
        }
        session.close();
    }

    private void createTestData() {

        Locale fi = new Locale("fi", "FI");
        Locale enUs = new Locale("en", "US");
        Locale enUk = new Locale("en", "UK");

        data.add(new FieldAnnotated(1, d1, t1, lt1, jd1, "ABC-123", "Aapeli Aakkonen",
                                    "Java C++ Scala Ruby", fi));
        data.add(new FieldAnnotated(2, d2, t2, lt2, jd2, "ABC-123 B", "Aapeli Ukkonen",
                                    "Java C++ Groove Ruby", enUs));

        data.add(new FieldAnnotated(3, d3, t3, lt3, jd3, "ABC-123 D", "Esko Aakkonen",
                                    "PHP C Scala Ruby", enUk));

        LuceneSession session = sessionFactory.openSession(false);
        session.beginReset().addDocument(data.get(0).toDocument())
                .addDocument(data.get(1).toDocument()).addDocument(data.get(2).toDocument());
        session.close();

    }
}
