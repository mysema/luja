package com.mysema.luja.impl;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysema.luja.LuceneTransactional;
import com.mysema.luja.SessionClosedException;
import com.mysema.luja.SessionNotBoundException;
import com.mysema.luja.SessionReadOnlyException;

@Aspect
public class LuceneTransactionHandler {

    private static final Logger logger = LoggerFactory.getLogger(LuceneTransactionHandler.class);

    @Around("@annotation(annotation)")
    public Object transactionalMethod(ProceedingJoinPoint joinPoint, LuceneTransactional annotation)
            throws Throwable {
    	
    	//If this is nested method, let the most outer scope deal with
    	//leasing and releasing
    	if (LuceneSessionHolder.isTransactionalScope()) {
    		if (logger.isDebugEnabled()) {
    			logger.debug("LuceneSessionHolder proceed in inner scope");
    		}
    		return joinPoint.proceed();
    	}

        if (logger.isDebugEnabled()) {
            logger.debug("LuceneSessionHolder.lease");
        }

        LuceneSessionHolder.lease(annotation.readOnly());
        boolean rollback = false;
        try {
            return joinPoint.proceed();
        } catch (Exception e) {

            rollback = isRollback(annotation, e);

            throw e;
        } finally {
            if (!rollback) {
                
                if (logger.isDebugEnabled()) {
                    logger.debug("LuceneSessionHolder.release");
                }
                
                LuceneSessionHolder.release();
            }
            else {
                
                if (logger.isDebugEnabled()) {
                    logger.debug("LuceneSessionHolder.rollbackAndRelease");
                }
                
                LuceneSessionHolder.rollbackAndRelease();
            }
        }
    }

    private boolean isRollback(LuceneTransactional annotation, Exception exception) {
        // TODO Add annotation support for exceptions ala Hibernate

        // Filter runtime exceptions which are not valid for rollback
        if (exception instanceof SessionReadOnlyException
            || exception instanceof SessionNotBoundException
            || exception instanceof SessionClosedException) {
            return false;
        }
        
        logger.error("Exception from transactional method, rollbacking", exception);
        return true;
    }

}
