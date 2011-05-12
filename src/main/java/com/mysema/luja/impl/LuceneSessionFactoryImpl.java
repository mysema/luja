package com.mysema.luja.impl;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nullable;

import org.apache.commons.lang.math.RandomUtils;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysema.luja.AnalyzerFactory;
import com.mysema.luja.LuceneSession;
import com.mysema.luja.LuceneSessionFactory;
import com.mysema.luja.SessionNotBoundException;
import com.mysema.query.QueryException;

public class LuceneSessionFactoryImpl implements LuceneSessionFactory {

	private static final Logger logger = LoggerFactory
			.getLogger(LuceneSessionFactoryImpl.class);

	private final Directory directory;

	@Nullable
	private volatile LuceneSearcher searcher;

	private final AtomicBoolean creatingNew = new AtomicBoolean(false);

	private final Object firstTimeLock = new Object();

	private long defaultLockTimeout = 2000;

	private long closedReaderTimeout = 1000;

	private Locale sortLocale = Locale.getDefault();

	private AnalyzerFactory analyzerFactory = new StandardAnalyzerFactory();

	public LuceneSessionFactoryImpl(String indexPath) throws IOException {
		File folder = new File(indexPath);
		if (!folder.exists() && !folder.mkdirs()) {
			throw new IOException("Could not create directory: "
					+ folder.getAbsolutePath());
		}

		try {
			directory = FSDirectory.open(folder);
		} catch (IOException e) {
			logger.error("Could not create lucene directory to "
					+ folder.getAbsolutePath());
			throw e;
		}
	}

	public LuceneSessionFactoryImpl(Directory directory) {
		this.directory = directory;
	}

	@Override
	public LuceneSession getCurrentSession() {
		if (!LuceneSessionHolder.isTransactionalScope()) {
			throw new SessionNotBoundException(
					"There is no transactional scope");
		}

		if (!LuceneSessionHolder.hasCurrentSession(this)) {

			if (logger.isTraceEnabled()) {
				logger.trace("Binding new session to thread");
			}

			LuceneSession session = openSession(LuceneSessionHolder
					.getReadOnly());
			LuceneSessionHolder.setCurrentSession(this, session);
		}

		return LuceneSessionHolder.getCurrentSession(this);
	}

	@Override
	public LuceneSession openSession(boolean readOnly) {
		return new LuceneSessionImpl(this, readOnly, sortLocale);
	}

	@Override
	public LuceneSession openReadOnlySession() {
		return new LuceneSessionImpl(this, true, sortLocale);
	}

	@Override
	public LuceneSession openSession() {
		return new LuceneSessionImpl(this, false, sortLocale);
	}

	public FileLockingWriter leaseWriter(boolean createNew) {
		FileLockingWriter writer = new FileLockingWriter(directory, createNew,
				defaultLockTimeout, this);
		lease(writer);
		return writer;
	}

	public LuceneSearcher leaseSearcher() {
		try {

			long start = 0;
			boolean leased = true;
			
			LuceneSearcher leasedSearcher = null;
			
			do {
				// Creating first searcher instance in synchronized way
				if (searcher == null) {
					synchronized (firstTimeLock) {
						if (searcher == null) {
							if (logger.isDebugEnabled()) {
								logger.debug("Creating first searcher");
							}
							searcher = createNewSearcher();
						}
						//
					}
				}
				
				//Start counting timeout if we are on the lease failed loop
				if (!leased && start == 0) {
					start = System.currentTimeMillis();
				}
				//Timeout from lease failed loop
				if (!leased && start + closedReaderTimeout < System.currentTimeMillis()) {
					throw new QueryException(
							"Timed out while waiting the searcher lease");
				}
				
				// If the creating new is in the progress, it's still
				// possible to lease old reader
                if (creatingNew.compareAndSet(false, true)) {
                    try {
                        if (!searcher.isCurrent()) {
                            try {
                                // This release pairs with createNewSearcher
                                // lease
                                release(searcher);
                            } catch (QueryException e) {
                                logger.error("Could not release searcher", e);
                            }
                            searcher = createNewSearcher();
                        }
                    } finally {
                        creatingNew.set(false);
                    }
                }

                //Using local copy, as we want to give out the same
                //searcher we lease, not the new one that might have been created in the
                //between we leased and are returning the leased searcher
                leasedSearcher = searcher;
                
				// Incrementing reference as we lease this out
				// This pairs with LuceneSessions close
				// Try this in loop as we might be in the corner case
				// where actually searcher is being released and we want valid
				// searcher from here
				leased = lease(leasedSearcher);
				
				
			} while (!leased);

			return leasedSearcher;

		} catch (IOException e) {
			throw new QueryException(e);
		}
	}

	private LuceneSearcher createNewSearcher() throws IOException {
		LuceneSearcher s = new LuceneSearcher(directory);
		if (logger.isDebugEnabled()) {
			logger.debug("Created searcher " + s);
		}
		// Increment the first time so that only the changed index
		// will actually close the searcher
		lease(s);
		return s;
	}

	/**
	 * Callback to make single entry to all leases
	 * 
	 * @param leasable
	 */
	protected boolean lease(Leasable leasable) {
		return leasable.lease();
	}

	/**
	 * Callback to make single entry to all releases
	 * 
	 * @param leasable
	 */
	protected void release(Leasable leasable) {
		leasable.release();
	}

	public void setDefaultLockTimeout(long defaultLockTimeout) {
		this.defaultLockTimeout = defaultLockTimeout;
	}

	/**
	 * Sets the sorting locale used by this session factory. The default sort
	 * locale is the jvm's default locale.
	 * 
	 * @param sortLocale
	 */
	public void setSortLocale(Locale sortLocale) {
		this.sortLocale = sortLocale;
	}

	// public <T> Transformer<Document, T>
	// getDocumentToObjectTransformer(Class<T> clazz) {
	// //Luodaan transformer laiskasti, säilytetään tallessa
	// //Tsek morphia
	//
	// //Convertterit rdfbeanistä, uusi moduuli?
	// return null;
	// }
	//
	// public Document transformToDocument(Object object) {
	// // TODO Auto-generated method stub
	// return null;
	// }

	public void setAnalyzerFactory(AnalyzerFactory analyzerFactory) {
		this.analyzerFactory = analyzerFactory;
	}

	public AnalyzerFactory getAnalyzerFactory() {
		return analyzerFactory;
	}

}
