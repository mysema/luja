package com.mysema.luja;

public interface LuceneSessionFactory {

    /**
     * Gets current session bound to current thread.
     * 
     * Throws SessionNotBoundException if there is no transactional scope.
     * 
     * @return Current session
     */
    LuceneSession getCurrentSession();
    
    /**
     * Opens session.
     * 
     * Use openReadOnlySession() or openSession() instead.
     * 
     * @param readOnly if true, opens readonly session
     * @return New session
     */
    @Deprecated
    LuceneSession openSession(boolean readOnly);
    
    /**
     * Opens read only session.
     * 
     * @return New session
     */
    LuceneSession openReadOnlySession();
    
    /**
     * Opens read-write session.
     * 
     * @return New session
     */
    LuceneSession openSession();
    
}
