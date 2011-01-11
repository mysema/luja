package com.mysema.luja.mapping;

import com.mysema.query.QueryException;

public class TypeNotMappedException extends QueryException {

    private static final long serialVersionUID = -370353786389559629L;

    public TypeNotMappedException(String msg) {
        super(msg);
    }

    public TypeNotMappedException(String msg, Throwable t) {
        super(msg, t);
    }

    public TypeNotMappedException(Throwable t) {
        super(t);
    }

}
