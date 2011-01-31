/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.luja.mapping;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.List;

import net.jcip.annotations.Immutable;

import com.mysema.commons.lang.Assert;

/**
 * @author tiwe
 * @author sasa
 *
 */
@Immutable
public final class MappedConstructor {

    private final Constructor<?> constructor;
    
    private final List<MappedProperty<?>> mappedArguments;
    
    public MappedConstructor(Constructor<?> constructor) {
        this(constructor, Collections.<MappedProperty<?>>emptyList());
    }

    public MappedConstructor(Constructor<?> constructor, List<MappedProperty<?>> mappedArguments) {
        this.constructor = Assert.notNull(constructor,"constructor");
        this.mappedArguments = Assert.notNull(mappedArguments,"mappedArguments");
        this.constructor.setAccessible(true);
    }

    public List<MappedProperty<?>> getMappedArguments() {
        return mappedArguments;
    }

    public Constructor<?> getConstructor() {
        return constructor;
    }

    public int getArgumentCount() {
        return mappedArguments.size();
    }
    
    public String toString() {
        return constructor.toString();
    }

    public Class<?> getDeclaringClass() {
        return constructor.getDeclaringClass();
    }
}
