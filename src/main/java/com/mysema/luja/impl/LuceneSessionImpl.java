package com.mysema.luja.impl;

import java.io.IOException;
import java.util.Locale;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysema.luja.LuceneSession;
import com.mysema.luja.LuceneWriter;
import com.mysema.luja.SessionClosedException;
import com.mysema.luja.SessionReadOnlyException;
import com.mysema.luja.serializer.AnnotationSerializer;
import com.mysema.query.QueryException;
import com.mysema.query.lucene.LuceneQuery;
import com.mysema.query.lucene.LuceneSerializer;

public class LuceneSessionImpl implements LuceneSession {
    
    private static final Logger logger = LoggerFactory.getLogger(LuceneSessionHolder.class);

    private final boolean readOnly;

    private boolean closed = false;

    private final LuceneSessionFactoryImpl sessionFactory;

    @Nullable
    private LuceneSearcher searcher;

    @Nullable
    private FileLockingWriter writer;

    private final LuceneSerializer serializer;

    public LuceneSessionImpl(LuceneSessionFactoryImpl sessionFactory, boolean readOnly, Locale sortLocale) {
        this.sessionFactory = sessionFactory;
        this.readOnly = readOnly;
        this.serializer = new AnnotationSerializer(sortLocale);
    }

    @Override
    public LuceneQuery createQuery() {
        checkClosed();
        return new LuceneQuery(serializer, getSearcher().getIndexSearcer());
    }

    @Override
    public LuceneWriter beginAppend() {
        checkClosed();
        return getWriter(false);
    }

    @Override
    public LuceneWriter beginReset() {
        checkClosed();
        return getWriter(true);
    }

    @Override
    public boolean isClosed() {
        return closed;
    }
    
    @Override
    public void close() {
        if (closed) {
            return;
        }

        QueryException searcherException = null;

        if (searcher != null) {
            try {
                sessionFactory.release(searcher);
            } catch (QueryException e) {
                logger.error("Searcher release failed", e);
                searcherException = e;
            }
        }

        if (writer != null) {
            sessionFactory.release(writer);
        }

        if (searcherException != null) {
            throw searcherException;
        }

        closed = true;
    }

    @Override
    public void rollback() {
        checkClosed();

        //TODO How to get all the exceptions out, not only the first one
        try {
            if (writer != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Rollbacking session");
                }
                writer.getIndexWriter().rollback();
            }
        } catch (IOException e) {
            logger.error("Session rollback failed " + e);
            throw new QueryException(e);
        } finally {
            close();
        }
    }
    
    @Override
    public void commit() {
        checkClosed();

        if (writer != null) {
        	writer.commit();
        }

        if (searcher != null) {
            sessionFactory.release(searcher);
        }
        searcher = null;

    }
    
    @Override
    public void flush() {
        commit();
    }

    private LuceneSearcher getSearcher() {
        if (searcher == null) {
            searcher = sessionFactory.leaseSearcher();
        }
        return searcher;
    }

    private LuceneWriter getWriter(boolean createNew) {
        if (readOnly) {
            throw new SessionReadOnlyException("Read only session, cannot create writer");
        }

        if (writer == null) {
            writer = sessionFactory.leaseWriter(createNew);
        }

        return writer;
    }

    private void checkClosed() {
        if (closed) {
            throw new SessionClosedException("Session is closed");
        }
    }

}
