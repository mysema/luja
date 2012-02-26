package com.mysema.luja.impl;

import static com.mysema.luja.QueryTestHelper.createDocument;
import static com.mysema.luja.QueryTestHelper.randomWords;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

import com.mysema.luja.LuceneSessionFactory;
import com.mysema.luja.LuceneTransactional;

/**
 * Testing we can manage to release locks on the disk full situations
 * 
 * @author laimw
 */
public class DiskFullTest {

    LuceneSessionFactory sessionFactory;

    interface Dao {
        void addLotsOfData();

        void addData();
    }

    class DaoImpl implements Dao {

        long counter = 0;

        @LuceneTransactional
        public void addLotsOfData() {

            for (int i = 0; i < 1000; i++) {

                Document doc =
                    createDocument(
                            "" + counter++,
                            randomWords(100),
                            randomWords(100),
                            randomWords(1000),
                            1,
                            1);
                sessionFactory.getCurrentSession().beginAppend().addDocument(doc);

            }

        }

        @LuceneTransactional
        public void addData() {
            Document doc =
                createDocument(
                        "" + counter++,
                        randomWords(100),
                        randomWords(100),
                        randomWords(1000),
                        1,
                        1);
            sessionFactory.getCurrentSession().beginAppend().addDocument(doc);
        }

    }

    @Test
    @Ignore
    public void diskFullTest() throws IOException {

        String path = "dont_run_this_on_your_harddisk"; // "/Volumes/Cruzer/lucenetest";

        Directory dir = FSDirectory.open(new File(path));
        sessionFactory = new LuceneSessionFactoryImpl(dir);

        AspectJProxyFactory factory = new AspectJProxyFactory(new DaoImpl());
        factory.addAspect(new LuceneTransactionHandler());
        Dao dao = factory.getProxy();

        // Fill disk
        try {
            for (;;) {
                dao.addLotsOfData();
            }
        } catch (Exception e) {

            System.out.println("Got exception " + e.getCause().getMessage());

        }

        dao.addData();

    }

}
