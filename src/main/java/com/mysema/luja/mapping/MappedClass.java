/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.luja.mapping;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.mysema.commons.lang.Assert;

/**
 * @author tiwe
 * @author sasa
 * 
 */
public final class MappedClass {
    
    private final Class<?> clazz;

    @Nullable
    private MappedConstructor constructor;
    
    @Nullable
    private MappedProperty<?> idProperty;
    
    private Map<String, MappedProperty<?>> properties = new LinkedHashMap<String, MappedProperty<?>>();
    
    private final List<MappedClass> mappedSuperClasses;
    
    MappedClass(Class<?> clazz, List<MappedClass> mappedSuperClasses) {
        this.clazz = Assert.notNull(clazz,"clazz");
        this.mappedSuperClasses = mappedSuperClasses;
        for (MappedClass superClass : mappedSuperClasses){
            for (MappedProperty<?> property : superClass.getProperties()){
                addMappedProperty(property);
            }
        }
    }
    
        
    public static boolean isPolymorphic(Class<?> clazz) {
        // TODO use configuration to check if there's any mapped subclasses 
        return !Modifier.isFinal(clazz.getModifiers());
    }
    
    void addMappedProperty(MappedProperty<?> property){
        if (properties.containsKey(property.getName())){
            // merge properties
            properties.get(property.getName()).addAnnotations(property);
        }else{
            properties.put(property.getName(), property);    
        }
        
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof MappedClass) {
            return clazz.equals(((MappedClass) obj).clazz);
        } else {
            return false;
        }
    }
    
    @Nullable
    public <T extends Annotation> T getAnnotation(Class<T> atype) {
        return clazz.getAnnotation(atype);
    }
    
    @Nullable
    public MappedConstructor getConstructor() {
        return constructor;
    }

    @Nullable
    public MappedProperty<?> getIdProperty() {
        return idProperty;
    }
    
    public Class<?> getJavaClass() {
        return clazz;
    }
    
    public MappedProperty<?> getProperty(String name) {
        return properties.get(name);
    }
    
    public List<MappedClass> getMappedSuperClasses() {
        return mappedSuperClasses;
    }
    
    public Collection<MappedProperty<?>> getProperties() {
        return properties.values();
    }
        
    @Override
    public int hashCode() {
        return clazz.hashCode();
    }

    public boolean isEnum() {
        return clazz.isEnum();
    }

    public boolean isPolymorphic() {
        return isPolymorphic(clazz);        
    }

    Type resolveTypeVariable(String typeVariableName, MappedClass declaringClass) {
        int i = 0;
        for (TypeVariable<?> typeParameter : declaringClass.clazz.getTypeParameters()) {
            if (typeParameter.getName().equals(typeVariableName)) {
                break;
            } else {
                i++;
            }
        }
        int j = 0;
        boolean found = false;
        for (MappedClass superClass : getMappedSuperClasses()) {
            if (declaringClass.equals(superClass)) {
                found = true;
                break;
            } else {
                j++;
            }
        }
        if (!found) {
            throw new IllegalStateException("Super class declaration for " + declaringClass + " not found from " + this);
        }
        
        Type type = (j == 0 ? clazz.getGenericSuperclass() : clazz.getGenericInterfaces()[j-1]);
        if (type instanceof ParameterizedType) {
            return ((ParameterizedType) type).getActualTypeArguments()[i];
        } else {
            throw new IllegalStateException("Generic parameters not supplied from " + this + " to " + declaringClass);
        }
    }

    void setMappedConstructor(@Nullable MappedConstructor constructor) {
        if (constructor == null && !clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers())) {
            throw new IllegalArgumentException("Default or mapped constructor required for " + clazz);
        } else {
            // TODO Is this needed?
//            for (MappedPath path : constructor.getMappedArguments()) {
//                if (path.getFirstPredicate() != null) {
//                    mappedUIDs.add(path.getFirstPredicate());
//                }
//            }
            
            this.constructor = constructor;
        }
    }

    @Override
    public String toString() {
        return clazz.toString();
    }

}
