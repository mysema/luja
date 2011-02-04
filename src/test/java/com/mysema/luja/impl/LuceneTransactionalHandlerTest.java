package com.mysema.luja.impl;

import static com.mysema.luja.QueryTestHelper.createDocument;
import static com.mysema.luja.QueryTestHelper.createDocuments;
import static com.mysema.luja.QueryTestHelper.getDocument;
import static org.junit.Assert.assertEquals;

import org.apache.lucene.store.RAMDirectory;
import org.junit.Before;
import org.junit.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

import com.mysema.luja.LuceneSession;
import com.mysema.luja.LuceneSessionFactory;
import com.mysema.luja.LuceneTransactional;
import com.mysema.luja.SessionNotBoundException;
import com.mysema.luja.SessionReadOnlyException;
import com.mysema.luja.mapping.domain.QMovie;
import com.mysema.query.lucene.LuceneQuery;

public class LuceneTransactionalHandlerTest {

    private LuceneSessionFactory sessionFactory;
    
    private final LuceneTransactionHandler handler = new LuceneTransactionHandler();

    private TestDao testDao;
    
    private QMovie path = new QMovie("doc");

    @Before
    public void before() {

        sessionFactory = new LuceneSessionFactoryImpl(new RAMDirectory());

        AspectJProxyFactory factory = new AspectJProxyFactory(new TestDaoImpl(sessionFactory));
        factory.addAspect(handler);

        testDao = factory.getProxy();
    }

    @Test
    public void Empty() {
        testDao.empty();
        assertEquals(1, testDao.count());
    }

    @Test(expected = SessionNotBoundException.class)
    public void NoAnnotation() {
        testDao.noAnnotation();
    }

    @Test
    public void Annotation() {
        testDao.annotation();
        assertEquals(1, testDao.count());
    }
    
    @Test(expected = SessionReadOnlyException.class)
    public void ReadOnly() {
        testDao.readOnly();
    }
    
    @Test
    public void Writing() {
        testDao.writing();
        
        LuceneQuery q = sessionFactory.openReadOnlySession().createQuery();
        assertEquals(4, q.where(path.title.like("*")).count());
    }
    
    @Test
    public void Multifactories() {
        
        LuceneSessionFactory sf1 = new LuceneSessionFactoryImpl(new RAMDirectory());
        LuceneSessionFactory sf2 = new LuceneSessionFactoryImpl(new RAMDirectory());
        LuceneSessionFactory sf3 = new LuceneSessionFactoryImpl(new RAMDirectory());
        
        AspectJProxyFactory factory = new AspectJProxyFactory(new TestDaoImpl(sf1,sf2,sf3));
        factory.addAspect(handler);
        testDao = factory.getProxy();
        
        testDao.multiFactories();
        
        LuceneQuery q = sf1.openReadOnlySession().createQuery();
        assertEquals(1, q.where(path.title.eq("sf1")).count());
        
        q = sf2.openReadOnlySession().createQuery();
        assertEquals(1, q.where(path.title.eq("sf2")).count());
        
        q = sf3.openReadOnlySession().createQuery();
        assertEquals(1, q.where(path.title.eq("sf3")).count());
    }
    
    @Test
    public void NestedSession() {
        AspectJProxyFactory factory = new AspectJProxyFactory(new NestedDaoImpl(sessionFactory));
        factory.addAspect(handler);
        NestedDao nestedDao = factory.getProxy();
        
        testDao.setNested(nestedDao);
        
        testDao.nested();
        
        LuceneSession session = sessionFactory.openReadOnlySession();
        assertEquals(1, session.createQuery().where(path.title.eq("nested")).count());
        session.close();
        
    }

    @Test
    public void RuntimeExceptionCausesRollback() {
        try {
            testDao.throwsException();
        } catch (RuntimeException e) {
            // Nothing
        }

        LuceneQuery q = sessionFactory.openReadOnlySession().createQuery();
        assertEquals(0, q.where(path.title.eq("rollback")).count());
    }
    
    @Test
    public void CheckedExceptionDoesNotCauseRollback() {
        try {
            testDao.throwsCheckedException();
        } catch (Exception e) {
            // Nothing
        }

        LuceneQuery q = sessionFactory.openReadOnlySession().createQuery();
        assertEquals(1, q.where(path.title.eq("rollback-checked")).count());
    }
    

    private static interface TestDao {
        void empty();

        int count();

        void noAnnotation();

        void annotation();

        void readOnly();

        void writing();
        
        void multiFactories();
        
        void nested();
        
        void setNested(NestedDao nested);
        
        void throwsException();
        
        void throwsCheckedException() throws Exception;
    }
    
    private static class TestDaoImpl implements TestDao {

        private int count = 0;

        private final LuceneSessionFactory[] factories;
        
        private NestedDao nested;
        
        TestDaoImpl(LuceneSessionFactory ... factories) {
            this.factories = factories;
        }

        @Override
        @LuceneTransactional
        public void empty() {
            count++;
        }

        @Override
        public int count() {
            return count;
        }

        @Override
        public void noAnnotation() {
            factories[0].getCurrentSession();
        }

        @Override
        @LuceneTransactional
        public void annotation() {
            count++;
            factories[0].getCurrentSession();
        }

        @Override
        @LuceneTransactional(readOnly=true)
        public void readOnly() {
           LuceneSession session =  factories[0].getCurrentSession();
           session.beginAppend().addDocument(getDocument());
        }

        @Override
        @LuceneTransactional
        public void writing() {
            LuceneSession session =  factories[0].getCurrentSession();
            createDocuments(session);
        }

        @Override
        @LuceneTransactional
        public void multiFactories() {
           LuceneSession s1 = factories[0].getCurrentSession();
           LuceneSession s2 = factories[1].getCurrentSession();
           LuceneSession s3 = factories[2].getCurrentSession();
           
           s1.beginReset().addDocument(createDocument("1", "sf1","","",0,0));
           s2.beginReset().addDocument(createDocument("2", "sf2","","",0,0));
           s3.beginReset().addDocument(createDocument("3", "sf3","","",0,0));
        }

        @Override
        public void setNested(NestedDao nested) {
            this.nested = nested;
        }
        
        @Override
        @LuceneTransactional
        public void nested() {
            LuceneSession session = factories[0].getCurrentSession();
            session.beginReset().addDocument(createDocument("1", "nested","","",0,0));
            nested.nested();
        }
        
        @Override
        @LuceneTransactional
        public void throwsException() {
            LuceneSession session = factories[0].getCurrentSession();
            session.beginAppend().addDocument(createDocument("1", "rollback","","",0,0));
            throw new RuntimeException();
        }
        
        @Override
        @LuceneTransactional
        public void throwsCheckedException() throws Exception {
            LuceneSession session = factories[0].getCurrentSession();
            session.beginAppend().addDocument(createDocument("1", "rollback-checked","","",0,0));
            throw new Exception();
        }
        
    }
    
    private static interface NestedDao {
        void nested();
        void nestedException();
    }
    
    private class NestedDaoImpl implements NestedDao {

        private final LuceneSessionFactory sessionFactory;
        
        public NestedDaoImpl(LuceneSessionFactory sessionFactory) {
            this.sessionFactory = sessionFactory;
        }
        
        @Override
        @LuceneTransactional
        public void nested() {
            LuceneSession session = sessionFactory.getCurrentSession(); 
            LuceneQuery query = session.createQuery();
            
            assertEquals(0, query.where(path.title.eq("nested")).count());

            // This verifies that the we are using the same session opened in
            // the caller scope
            session.commit();

            query = session.createQuery();
            assertEquals(1, query.where(path.title.eq("nested")).count());
        }
        
        @Override
        @LuceneTransactional
        public void nestedException() {
            LuceneSession session = sessionFactory.getCurrentSession();
            session.beginAppend().addDocument(createDocument("1", "nested rollback","","",0,0));
            throw new RuntimeException();
        }
    }
}
