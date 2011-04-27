package com.mysema.luja.impl;

import static com.mysema.luja.QueryTestHelper.addData;
import static com.mysema.luja.QueryTestHelper.createDocument;
import static com.mysema.luja.QueryTestHelper.createDocuments;
import static com.mysema.luja.QueryTestHelper.getDocument;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.AlreadyClosedException;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Before;
import org.junit.Test;

import com.mysema.luja.LuceneSession;
import com.mysema.luja.LuceneSessionFactory;
import com.mysema.luja.SessionClosedException;
import com.mysema.luja.SessionNotBoundException;
import com.mysema.luja.SessionReadOnlyException;
import com.mysema.luja.mapping.domain.QMovie;
import com.mysema.query.QueryException;
import com.mysema.query.lucene.LuceneQuery;
import com.mysema.query.types.path.NumberPath;
import com.mysema.query.types.path.StringPath;

public class LuceneSessionFactoryTest {

    private LuceneSessionFactory sessionFactory;

    private Directory directory;

    private StringPath title;

    private StringPath id;

    private NumberPath<Integer> year;

    private QMovie path = new QMovie("doc");

    @Before
    public void before() throws IOException {
        directory = new RAMDirectory();
        sessionFactory = new LuceneSessionFactoryImpl(directory);

        title = path.title;
        year = path.year;
        id = path.id;
    }

    @Test
    public void BasicQuery() {

        addData(sessionFactory);

        LuceneSession session = sessionFactory.openReadOnlySession();
        // Testing the queries work through session
        LuceneQuery query = session.createQuery();

        List<Document> results = query.where(title.eq("Jurassic Park")).list();
        assertEquals(1, results.size());
        assertEquals("Jurassic Park", results.get(0).getField("title").stringValue());

        results = session.createQuery().where(title.startsWith("Jurassic")).list();
        assertEquals(1, results.size());

        results = session.createQuery().where(title.startsWith("Jurassic P")).list();
        assertEquals(1, results.size());

        results = session.createQuery().where(id.eq("1")).list();
        assertEquals(1, results.size());

        results = session.createQuery().where(id.eq("2 A")).list();
        assertEquals(1, results.size());

        results = session.createQuery().where(id.eq("3_B")).list();
        assertEquals(1, results.size());

        long count = session.createQuery().where(title.startsWith("Nummi")).count();
        assertEquals(1, count);

        session.close();
    }

    @Test
    public void Flush() {
        LuceneSession session = sessionFactory.openSession();
        createDocuments(session);
        session.commit();

        // Now we will see the four documents
        LuceneQuery query = session.createQuery();
        assertEquals(4, query.where(year.gt(1800)).count());

        // Adding new document
        session.beginAppend().addDocument(createDocument("1", "title", "author", "", 2010, 1));

        // New query will not see the addition
        query = session.createQuery();
        assertEquals(4, query.where(year.gt(1800)).count());

        session.commit();

        // This will see the addition
        LuceneQuery query1 = session.createQuery();
        assertEquals(5, query1.where(year.gt(1800)).count());

        // The old query throws exception as it's closed on commit
        try {
        	query.count();
        	fail("Query is closed and there should have been exception");
        } catch(AlreadyClosedException e) {
        	//Nothing
        }

        session.close();
    }

    @Test(expected = SessionNotBoundException.class)
    public void CurrentSession() {
        sessionFactory.getCurrentSession();
    }

    @Test(expected = SessionReadOnlyException.class)
    public void Readonly() {
        LuceneSession session = sessionFactory.openReadOnlySession();
        session.beginReset();
    }

    @Test(expected = SessionClosedException.class)
    public void SessionClosedCreate() {
        LuceneSession session = sessionFactory.openSession();
        session.close();
        session.createQuery();
    }

    @Test(expected = SessionClosedException.class)
    public void SessionClosedAppend() {
        LuceneSession session = sessionFactory.openSession();
        session.close();
        session.beginAppend();
    }

    @Test(expected = SessionClosedException.class)
    public void SessionClosedFlush() {
        LuceneSession session = sessionFactory.openSession();
        session.close();
        session.commit();
    }

    @Test
    public void SessionClosedClosed() {
        LuceneSession session = sessionFactory.openSession();
        session.close();
        //It's fine to call close many times
        session.close();
    }

    @Test(expected = SessionClosedException.class)
    public void SessionClosedOverwrite() {
        LuceneSession session = sessionFactory.openSession();
        session.close();
        session.beginReset();
    }
    
   @Test(expected = AlreadyClosedException.class)
   public void PreviousQueryIsClosedAfterCommit() {
	   addData(sessionFactory);
	   
	   LuceneSession session = sessionFactory.openSession();
	   //make change, that next commit invalidates current readers
	   createDocuments(session);
	   LuceneQuery query1 = session.createQuery();
	   assertEquals(4, query1.count());
       
	   session.commit();
       
       //This will close the query1
       session.createQuery();
       
       //IndexReader in query1 should be closed now
       query1.where(year.gt(1800)).count();
   }

    @Test
    public void Reset() {
        addData(sessionFactory);

        LuceneSession session = sessionFactory.openReadOnlySession();
        assertEquals(4, session.createQuery().count());
        session.close();

        session = sessionFactory.openSession();

        assertEquals(4, session.createQuery().count());
        session.beginReset().addDocument(getDocument());
        session.commit();
        assertEquals(1, session.createQuery().count());
        session.close();

        session = sessionFactory.openReadOnlySession();
        assertEquals(1, session.createQuery().count());
        session.close();
    }

    private class CountingSessionFactory extends LuceneSessionFactoryImpl {

        List<Leasable> leasables = new ArrayList<Leasable>();

        Map<Leasable, Integer> leases = new HashMap<Leasable, Integer>();

        Map<Leasable, Integer> releases = new HashMap<Leasable, Integer>();

        public CountingSessionFactory(Directory directory) {
            super(directory);
        }

        @Override
        public boolean lease(Leasable leasable) {
        	boolean success = super.lease(leasable);
        	//Don't count failed leases
        	if (!success) return success;
        	
            if (!leasables.contains(leasable)) {
                leasables.add(leasable);
            }
            if (!leases.containsKey(leasable)) {
                leases.put(leasable, 0);
            }
            leases.put(leasable, leases.get(leasable) + 1);
            return success;
        }

        @Override
        public void release(Leasable leasable) {
            if (!leasables.contains(leasable)) {
                leasables.add(leasable);
            }
            if (!releases.containsKey(leasable)) {
                releases.put(leasable, 0);
            }
            releases.put(leasable, releases.get(leasable) + 1);
            super.release(leasable);
        }

    }

    @Test
    public void ResourcesAreReleased() throws IOException {

        CountingSessionFactory sessionFactory = new CountingSessionFactory(directory);

        LuceneSession session = sessionFactory.openSession();

        // Lease one writer
        session.beginAppend().addDocument(getDocument());
        session.commit();

        // Lease searcher 1
        LuceneQuery query = session.createQuery();
        assertEquals(1, query.where(year.gt(1800)).count());

        session.beginAppend().addDocument(getDocument());
        // Release searcher 1
        session.commit();

        // Lease searcher 2
        query = session.createQuery();
        assertEquals(2, query.where(year.gt(1800)).count());

        // Release searcher 2 and writer
        session.close();

        // Second session
        session = sessionFactory.openSession(true);
        // Lease searcher 3
        query = session.createQuery();
        assertEquals(2, query.where(year.gt(1800)).count());
        // Release searcher 3
        session.close();

        assertEquals(4, sessionFactory.leasables.size());
        assertEquals(4, sessionFactory.leases.size());
        assertEquals(4, sessionFactory.releases.size());

        // First one should be writer
        assertTrue(sessionFactory.leasables.get(0) instanceof FileLockingWriter);
        assertTrue(sessionFactory.leasables.get(1) instanceof LuceneSearcher);
        assertTrue(sessionFactory.leasables.get(2) instanceof LuceneSearcher);
        assertTrue(sessionFactory.leasables.get(3) instanceof LuceneSearcher);

        // The writer should be closed
        assertEquals(1, (int) sessionFactory.leases.get(sessionFactory.leasables.get(0)));
        assertEquals(1, (int) sessionFactory.releases.get(sessionFactory.leasables.get(0)));

        // First and second searchers should be released totally
        assertEquals(2, (int) sessionFactory.leases.get(sessionFactory.leasables.get(1)));
        assertEquals(2, (int) sessionFactory.releases.get(sessionFactory.leasables.get(1)));
        assertIndexReaderIsClosed(sessionFactory.leasables.get(1));

        assertEquals(2, (int) sessionFactory.leases.get(sessionFactory.leasables.get(2)));
        assertEquals(2, (int) sessionFactory.releases.get(sessionFactory.leasables.get(2)));
        assertIndexReaderIsClosed(sessionFactory.leasables.get(2));

        // Third searcher leaves it as current
        assertEquals(2, (int) sessionFactory.leases.get(sessionFactory.leasables.get(3)));
        assertEquals(1, (int) sessionFactory.releases.get(sessionFactory.leasables.get(3)));

    }

	private void assertIndexReaderIsClosed(Leasable leasable)
			throws IOException {

		IndexSearcher searcher = ((LuceneSearcher) leasable).getIndexSearcer();
		try {
			searcher.getIndexReader().flush();
			fail("Indexreader was not closed");
		} catch (AlreadyClosedException e) {
			// Nothing
		}
	}

	@Test
    public void StringPathCreationWorks() throws IOException {
        sessionFactory = new LuceneSessionFactoryImpl("target/stringpathtest");
        LuceneSession session = sessionFactory.openSession();
        session.beginReset().addDocument(getDocument());
        session.commit();
        assertEquals(1, session.createQuery().where(year.gt(1800)).count());
        session.close();
    }

    @Test(expected = QueryException.class)
    public void GetsQueryException() throws IOException {
        String path = "target/exceptiontest";
        sessionFactory = new LuceneSessionFactoryImpl(path);
        LuceneSession session = sessionFactory.openSession();
        session.beginAppend().addDocument(getDocument());
        FileUtils.deleteDirectory(new File(path));
        session.close();
    }
    
    @Test
    public void BasicRollback() {
        LuceneSession session = sessionFactory.openSession();
        session.beginAppend().addDocument(getDocument());
        session.close();
        
        assertDocumentCount(1, sessionFactory);
        
        session = sessionFactory.openSession();
        session.beginAppend().addDocument(getDocument());
        session.rollback();
        assertEquals(true, session.isClosed());
        
        assertDocumentCount(1, sessionFactory);
        
    }

    private void assertDocumentCount(int count, LuceneSessionFactory sf) {
        LuceneSession session = sf.openReadOnlySession();
        try {
            assertEquals(count, session.createQuery().count());
        } finally {
            session.close();
        }
    }

}
