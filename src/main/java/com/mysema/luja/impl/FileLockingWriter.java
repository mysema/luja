package com.mysema.luja.impl;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysema.luja.LuceneWriter;
import com.mysema.luja.WriteLockObtainFailedException;
import com.mysema.query.QueryException;

public class FileLockingWriter implements LuceneWriter, Leasable {

    protected static final Logger logger = LoggerFactory.getLogger(LuceneWriter.class);

    protected IndexWriter writer;

    protected final LuceneSessionFactoryImpl sessionFactory;

    public FileLockingWriter(Directory directory, boolean createNew, long defaultLockTimeout,
                             LuceneSessionFactoryImpl sessionFactory) {
        IndexWriter.setDefaultWriteLockTimeout(defaultLockTimeout);
        boolean create = createNew;
        try {
            if (!create) {
                try {
                    writer =
                        new IndexWriter(directory, new StandardAnalyzer(Version.LUCENE_30), false,
                                        MaxFieldLength.LIMITED);

                } catch (FileNotFoundException e) {
                    // Convience to create a new index if it's not already
                    // existing
                    create = true;
                }
            }
            if (create) {
                writer =
                    new IndexWriter(directory, new StandardAnalyzer(Version.LUCENE_30), true,
                                    MaxFieldLength.LIMITED);
            }

        } catch (LockObtainFailedException e) {
            logger.error("Got lock timeout ");
            throw new WriteLockObtainFailedException(e);
        } catch (IOException e) {
            throw new QueryException(e);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Created writer " + writer);
        }
        this.sessionFactory = sessionFactory;
    }

    @Override
    public LuceneWriter addDocument(Document doc) {
        try {
            writer.addDocument(doc);
            return this;
        } catch (IOException e) {
            throw new QueryException(e);
        }
    }

    @Override
    public LuceneWriter addObject(Object object) {
        throw new UnsupportedOperationException("This feature is not yet supported");
        //return addDocument(sessionFactory.transformToDocument(object));
    }

    @Override
    public LuceneWriter deleteDocuments(Term term) {
        try {
            writer.deleteDocuments(term);
            return this;
        } catch (IOException e) {
            throw new QueryException(e);
        }
    }

    public void commit() {
        try {
            writer.commit();
        } catch (IOException e) {
            throw new QueryException(e);
        }
    }

    private void close() {
        Directory directory = writer.getDirectory();
        try {
            // TODO What would be best way to control this?
            writer.optimize();
            writer.close();
            if (logger.isDebugEnabled()) {
                logger.debug("Closed writer " + writer);
            }
        } catch (IOException e) {
            logger.error("Writer close failed", e);
            try {
                if (IndexWriter.isLocked(directory)) {
                    IndexWriter.unlock(directory);
                }
            } catch (IOException e1) {
                logger.error("Lock release failed", e1);
                throw new QueryException(e1);
            }
            throw new QueryException(e);
        }
    }

    public IndexWriter getIndexWriter() {
        return writer;
    }

    @Override
    public void lease() {
        // This is no-op for writer as we always create new
    }

    @Override
    public void release() {
        close();
    }

}