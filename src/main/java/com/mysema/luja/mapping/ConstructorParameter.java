/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.luja.mapping;

import java.lang.reflect.Constructor;
import java.lang.reflect.Type;

import net.jcip.annotations.Immutable;

import org.apache.commons.collections15.BeanMap;

/**
 * @author tiwe
 * @author sasa
 * 
 */
@Immutable
public final class ConstructorParameter extends MappedProperty<Constructor<?>> {

    private final Constructor<?> constructor;

    private final int parameterIndex;

    public ConstructorParameter(Constructor<?> constructor, int parameterIndex, MappedClass declaringClass) {
        super(null, constructor.getParameterAnnotations()[parameterIndex], declaringClass);
        this.constructor = constructor;
        this.parameterIndex = parameterIndex;
    }

    @Override
    protected Constructor<?> getMember() {
        return constructor;
    }

    @Override
    protected Class<?> getTypeInternal() {
        return constructor.getParameterTypes()[parameterIndex];
    }

    @Override
    public Type getGenericType() {
        return constructor.getGenericParameterTypes()[parameterIndex];
    }

    @Override
    public Object getValue(BeanMap instance) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setValue(BeanMap beanWrapper, Object value) {
        throw new UnsupportedOperationException();
    }


}
