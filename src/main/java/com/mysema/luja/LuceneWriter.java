package com.mysema.luja;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;

/**
 * The essential functionality to enable writing lucene index.
 * 
 * @author laimw
 */
public interface LuceneWriter {

    LuceneWriter addDocument(Document doc);
    
    LuceneWriter updateDocument(Term term, Document doc);
        
    LuceneWriter deleteDocuments(Term term);
    
}
