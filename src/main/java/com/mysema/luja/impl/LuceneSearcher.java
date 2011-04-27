package com.mysema.luja.impl;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;

import com.mysema.query.QueryException;

/**
 * Simple wrapper to encapsulate searcher specific actions.
 * 
 * @author laim
 * 
 */
public class LuceneSearcher implements Leasable {

	private final IndexSearcher searcher;

	private final AtomicInteger refCount = new AtomicInteger();
	
	private final AtomicBoolean closed = new AtomicBoolean(false);

	public LuceneSearcher(Directory directory) {
		try {
			this.searcher = new IndexSearcher(directory);
			//System.out.println(searcher + " searcher created for " + this);
		} catch (IOException e) {
			throw new QueryException(e);
		}
	}

	public boolean isCurrent() {
		try {
			return searcher.getIndexReader().isCurrent();
		} catch (IOException e) {
			throw new QueryException(e);
		}
	}

	@Override
	public void release() {
		
		if (closed.get()) {
			throw new QueryException(
					"Cannot release, LuceneSearcher is already closed");
		}
		
		//System.out.println("release for " + this);
		if (refCount.getAndDecrement() == 1) {
			closed.set(true);
			try {
				searcher.close();
				//System.out.println(searcher + " searcher closed for " + this);
			} catch (IOException e) {
				throw new QueryException(e);
			}
		}
	}

	@Override
	public boolean lease() {
		if (closed.get()) {
			System.out.println("Lease failed as searcher already closed");
			return false;
		}
		//System.out.println("lease for " + this);
		refCount.getAndIncrement();
		return true;
	}

	public IndexSearcher getIndexSearcer() {
		return searcher;
	}

}
