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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
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

        List<Leasable> leaseCalls = new ArrayList<Leasable>();
   
        Map<Leasable, Integer> leases = new HashMap<Leasable, Integer>();

        public CountingSessionFactory(Directory directory) {
            super(directory);
        }

        @Override
        public boolean lease(Leasable leasable) {
            boolean success = super.lease(leasable);
            // Don't count failed leases
            if (!success)
                return success;

            count(true, leasable);
            return success;
        }
        
        public void assertAllLeasesReleased() throws IOException {
            Leasable oneAllowedOpen = null;
            for (int i = 0; i < leaseCalls.size(); i++) {
                Leasable l = leaseCalls.get(i);

                // One reader should be left open

                assertNotNull("Not found leasable " + l + " in index " + i, leases.get(l));
                if (leases.get(l) > 0) {
                    if (l instanceof FileLockingWriter) {
                        fail("Found open writer " + l + " with ref count "
                                + leases.get(0));
                    }
                    if (oneAllowedOpen == null || l == oneAllowedOpen) {
                        System.out.println("Found allowed open in index " + i + " " + l);
                        oneAllowedOpen = l;
                        continue;
                    }
                    if (l == oneAllowedOpen) {
                        
                    }

                }
                assertEquals("For leaseable " + l + " the ref count is "
                        + leases.get(l) + " in the index " + i, 0,
                        (int) leases.get(l));
                
                assertLeasableIsClosed(l);
            }

            if (oneAllowedOpen == null) {
                fail("There should be one reader left open");
            }
        }

        private synchronized void count(boolean lease, Leasable leasable) {
            if (lease) {
                //System.out.println("leasing " + leasable);
                
                leaseCalls.add(leasable);
                if (!leases.containsKey(leasable)) {
                    leases.put(leasable, 0);
                }
                
                leases.put(leasable, leases.get(leasable) + 1);
            }
            else {
                if (!leases.containsKey(leasable) ) 
                    throw new RuntimeException("Trying to release not leased leasable " + leasable);
                if (leases.get(leasable) == 0)
                    throw new RuntimeException("Trying to release already released leasable " + leasable);
                leases.put(leasable, leases.get(leasable) - 1);
            }
        }

        @Override
        public void release(Leasable leasable) {
            count(false, leasable);
            super.release(leasable);
        }

    }
    
    @Test
    public void ResourcesAreReleased() throws IOException {

        CountingSessionFactory sessionFactory = new CountingSessionFactory(directory);

        LuceneSession session = sessionFactory.openSession();

        String curTitle = "Resource release test";
        // Lease writer, leases +1
        session.beginAppend().addDocument(createDocument("1", curTitle, "", "", 0, 0));
        session.commit();

        // Lease searcher 1, leases +2 
        LuceneQuery query = session.createQuery();
        assertEquals(1, query.where(title.eq(curTitle)).count());

        //Using the same writer
        session.beginAppend().addDocument(createDocument("2", curTitle, "", "", 0, 0));
        // Release searcher 1
        session.commit();

        // Lease searcher 2, leases +2
        query = session.createQuery();
        assertEquals(2, query.where(title.eq(curTitle)).count());

        // Release searcher 2 and writer
        session.close();

        // Second session
        session = sessionFactory.openReadOnlySession();
        // Lease searcher 3, leases +2, as the session.close has second commit
        // which dirties the searcher even there was not changes.
        query = session.createQuery();
        assertEquals(2, query.where(title.eq(curTitle)).count());
        // Release searcher 3
        session.close();

        // Third session
        session = sessionFactory.openReadOnlySession();
        // Lease searcher 4, leases +1 as there is no changes
        query = session.createQuery();
        assertEquals(2, query.where(title.eq(curTitle)).count());
        // Release searcher 4
        session.close();

        assertEquals(1 + 2 + 2 + 2 + 1, sessionFactory.leaseCalls.size());
        // 1 writer, 3 unique searchers
        assertEquals(1 + 3, sessionFactory.leases.size());
        sessionFactory.assertAllLeasesReleased();
    }

    private class Releases implements Runnable {

        private LuceneSessionFactory sessionFactory;
        private int loops;

        public Releases(LuceneSessionFactory sessionFactory, int loops) {
            this.sessionFactory = sessionFactory;
            this.loops = loops;
        }

        @Override
        public void run() {
            System.out.println("Started running on thread "
                    + Thread.currentThread());

            String curTitle = Thread.currentThread().toString();

            for (int i = 1; i <= loops; i++) {

                LuceneSession session = sessionFactory.openSession();
                // Lease one writer
                session.beginAppend().addDocument(
                        createDocument("" + i, curTitle, "", "", 0, 0));
                session.commit();

                // Lease searcher 1
                LuceneQuery query = session.createQuery();
                assertEquals(i, query.where(title.eq(curTitle)).count());

                // Release searcher and writer
                session.close();

                // Second session
                session = sessionFactory.openReadOnlySession();
                // Lease searcher 2
                query = session.createQuery();
                assertEquals(i, query.where(title.eq(curTitle)).count());
                // Release searcher 3
                session.close();
            }

            System.out.println("End running on thread "
                    + Thread.currentThread());
        }
    }
    
    @Test
    public void ResourcesAreReleasedOnTwoThreads() throws Exception {
        
        CountingSessionFactory sessionFactory = new CountingSessionFactory(directory);
        sessionFactory.setDefaultLockTimeout(10000);
        
        int numOfThreads = 2;
        int loops = 100;
        ExecutorService threads = Executors.newFixedThreadPool(numOfThreads);
        Future<?> f1 = threads.submit(new Releases(sessionFactory, loops));
        Future<?> f2 = threads.submit(new Releases(sessionFactory, loops));
        
        f1.get(150, TimeUnit.SECONDS);
        f2.get(150, TimeUnit.SECONDS);
        threads.shutdown();
        
        //Amount of leaseCalls 
        System.out.println("Amount of leaseCalls is " + sessionFactory.leaseCalls.size());
        sessionFactory.assertAllLeasesReleased();
    }

    private void assertLeasableIsClosed(Leasable leasable) throws IOException {

        if (leasable instanceof LuceneSearcher) {
            IndexSearcher searcher = ((LuceneSearcher) leasable)
                    .getIndexSearcer();
            try {
                searcher.getIndexReader().flush();
                fail("Indexreader was not closed");
            } catch (AlreadyClosedException e) {
                // Nothing
            }
        } else {
            IndexWriter writer = ((FileLockingWriter) leasable).writer;
            try {
                writer.commit();
                fail("Indexwriter was not closed");
            } catch (AlreadyClosedException e) {
                // Nothing
            }
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
