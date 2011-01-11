package com.mysema.luja;

public interface LuceneSessionFactory {

    LuceneSession getCurrentSession();
    
    LuceneSession openSession(boolean readOnly);
    
}
