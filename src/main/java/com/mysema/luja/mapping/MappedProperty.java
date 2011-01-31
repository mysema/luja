/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.luja.mapping;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.rmi.server.UID;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.annotation.Nullable;

import org.apache.commons.collections15.BeanMap;
import org.apache.commons.lang.ObjectUtils;

import com.mysema.commons.lang.Assert;

/**
 * @author tiwe
 * @author sasa
 *
 */
public abstract class MappedProperty<M extends Member & AnnotatedElement> implements Cloneable {
    
//    @SuppressWarnings("unchecked")
//    public static final List<Class<? extends Annotation>> MAPPING_ANNOTATIONS = 
//        Collections.unmodifiableList(Arrays.<Class<? extends Annotation>>asList(
//            ComponentType.class,
//            Container.class,
//            Default.class, 
//            Defaults.class,
//            Id.class,
//            InjectService.class, 
//            Localized.class,
//            MapElements.class,
//            Mixin.class,
//            Required.class
//        ));


    @Nullable
    private String name;
    
    @Nullable
    private Class<?> type;
    
    @Nullable
    private Class<?> componentType;
    
    private Class<?> keyType;
    
    private boolean collection;
    
    private MappedClass declaringClass;
    
    private TypeVariable<?>[] typeVariables = new TypeVariable<?>[4];
    
    private Map<Class<? extends Annotation>, Annotation> annotations =
        new HashMap<Class<? extends Annotation>, Annotation>();

    private boolean includeMapped;

    @SuppressWarnings("unchecked")
    MappedProperty(@Nullable String name, Annotation[] annotations, MappedClass declaringClass) {
        this.name = name;
        this.declaringClass = declaringClass;
        for (Annotation annotation : Assert.notNull(annotations,"annotations")) {
            Class<? extends Annotation> aclass = (Class<? extends Annotation>) annotation.getClass().getInterfaces()[0];
            this.annotations.put(aclass, annotation);
        }
    }
    
    public MappedClass getDeclaringClass() {
        return declaringClass;
    }
    
    static Class<?> getUpper(@Nullable Class<?> clazz, Class<?> other) {
        if (clazz == null) {
            return other;
        } else if (other != null && !clazz.equals(other)) {
            if (clazz.isAssignableFrom(other)) {
                return other;
            }
        }
        return clazz;
    }
    
    @SuppressWarnings("unchecked")    
    void resolve(@Nullable MappedClass owner) {
        if (this.type == null) {
            this.type = getTypeInternal();
        }
        Type genericType = getGenericType();
        if (genericType instanceof TypeVariable) {
            this.type = getUpper(this.type, getGenericClass(genericType, 0, owner, 0));
        }

        this.collection = Collection.class.isAssignableFrom(type);
        
        if (collection || isClassReference()) {
            this.componentType = getUpper(this.componentType, getGenericClass(genericType, 0, owner, 1));
        } else if (isMap()) {
            keyType = getUpper(keyType, getGenericClass(genericType, 0, owner, 2));
            this.componentType = getUpper(componentType, getGenericClass(genericType, 1, owner, 3));
        } else {
            this.componentType = null;
        }
    }
    
    @SuppressWarnings("unchecked")
    @Nullable
    public Class<? extends Collection> getCollectionType() {
        if (isCollection()) {
            return getConcreteCollectionType(getType());
        } else {
            return null;
        }
    } 

    @Nullable
    @SuppressWarnings("unchecked")
    private Class<? extends Collection> getConcreteCollectionType(Class<?> collectionType) {
        
        if (collectionType.isInterface()) {
            if (List.class.isAssignableFrom(collectionType)) {
                return ArrayList.class;
            } else if (SortedSet.class.isAssignableFrom(collectionType)) {
                return TreeSet.class;
            } else if (Set.class.isAssignableFrom(collectionType)) {
                return LinkedHashSet.class;
            } else if (Collection.class.equals(collectionType)) {
                return HashSet.class;
            } else {
                throw new IllegalArgumentException("Unsupported Collection interface type: "+collectionType);
            }
        }
        else if (Collection.class.isAssignableFrom(collectionType)) {
            return (Class<? extends Collection>) collectionType;
        }
        
        return null;
    }
    
    @Nullable
    public Class<?> getComponentType() {
        return componentType;
    }
    
    public Class<?> getTargetType() {
        Class<?> clazz = getComponentType();
        if (clazz == null) {
            clazz = getType();
        }
        return clazz;
    }
    
    public boolean isAnnotationPresent(Class<? extends Annotation> atype) {
        return annotations.containsKey(atype);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    Class getGenericClass(@Nullable final Type t, int index, MappedClass owner, int typeVariableIndex) {
        Type gtype = t;
        if (gtype != null) {
            if (gtype instanceof Class) {
                return (Class) gtype;
            } else if (gtype instanceof ParameterizedType && index >= 0) {
                gtype = ((ParameterizedType) gtype).getActualTypeArguments()[index];
            }
            if (gtype instanceof Class) {
                return (Class) gtype;
            } else if (gtype instanceof WildcardType) { 
                return getGenericClass((WildcardType) gtype);
            } else if (gtype instanceof TypeVariable) {
                return getGenericClass(owner, typeVariableIndex, (TypeVariable) gtype);
            } else if (gtype instanceof ParameterizedType) {
                return (Class) ((ParameterizedType) gtype).getRawType();
            } else {
                throw new IllegalArgumentException("Unable to get generic type [" + index + "] of " + t + " from " + owner);
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private Class<?> getGenericClass(MappedClass owner, int typeVariableIndex, TypeVariable<?> type) {
        Type upperBound = null;
        if (owner == null || declaringClass.equals(owner)) {
            typeVariables[typeVariableIndex] = type;
            upperBound = typeVariables[typeVariableIndex].getBounds()[0]; 
        } else if (typeVariables[typeVariableIndex] != null) {
            Type genericType = owner.resolveTypeVariable(typeVariables[typeVariableIndex].getName(), declaringClass);
            if (genericType instanceof TypeVariable) {
                // Nested TypeVariable in a sub class
                typeVariables[typeVariableIndex] = (TypeVariable<?>) genericType;
                upperBound = typeVariables[typeVariableIndex].getBounds()[0];
            } else {
                typeVariables[typeVariableIndex] = null;
                upperBound = genericType;
            } 
            declaringClass = owner;
        }
        return getGenericClass(upperBound, -1, owner, -1);
    }

    @SuppressWarnings("unchecked")
    private Class<?> getGenericClass(WildcardType wildcardType) {
        if (wildcardType.getUpperBounds()[0] instanceof ParameterizedType) {
            return (Class) ((ParameterizedType) wildcardType.getUpperBounds()[0]).getRawType();
        } else if (wildcardType.getUpperBounds()[0] instanceof Class) {
            return (Class) wildcardType.getUpperBounds()[0];
        } else {
            //System.err.println("Unable to find out actual type of " + gtype);
            return Object.class;
        }
    }
    
    @SuppressWarnings("unchecked")
    @Nullable
    public <T extends Annotation> T getAnnotation(Class<T> atype) {
        return (T) annotations.get(atype);
    }
    
    public Class<?> getKeyType() {
        return keyType;
    }
    
    protected abstract  M getMember();
    
    public String getName() {
        return name;
    }
    
    public Class<?> getType() {
        return type;
    }
    
    protected abstract Class<?> getTypeInternal();
    
    protected abstract Type getGenericType();
    
    public abstract Object getValue(BeanMap instance);
    
//    public boolean isAnnotatedProperty() {
//        if (!annotations.isEmpty()) {
//            for (Class<? extends Annotation> anno : MAPPING_ANNOTATIONS) {
//                if (annotations.containsKey(anno)) {
//                    return true;
//                }
//            }
//        }
//        return false;
//    }
    
    public boolean isCollection() {
        return collection;
    }
    
//    public boolean isIdReference() {
//        return isAnnotationPresent(Id.class);
//    }
    
    public boolean isList() {
        return List.class.isAssignableFrom(getType());
    }
    
    public boolean isMap() {
        return Map.class.isAssignableFrom(getType());
    }
    
    public boolean isPolymorphic() {
        return MappedClass.isPolymorphic(getTargetType());
    }
    
    public boolean isConstructorParameter() {
        return getMember() instanceof Constructor<?>;
    }
    
    public boolean isSet() {
        return Set.class.isAssignableFrom(getType());
    }
    
    public boolean isSortedSet() {
        return SortedSet.class.isAssignableFrom(getType());
    }
    
    public abstract void setValue(BeanMap beanWrapper, @Nullable Object value);
    
    public String toString() {
        return getMember().toString();
    }

    public boolean isAssignableFrom(MappedProperty<?> other) {
        // Only methods may override...
        if (MethodProperty.class.isInstance(other) && ObjectUtils.equals(name, other.name)) {
            Class<?> domain = getMember().getDeclaringClass();
            Class<?> otherDomain = other.getMember().getDeclaringClass();
            return domain.isAssignableFrom(otherDomain);
        } else {
            return false;
        }
    }

    public void addAnnotations(MappedProperty<?> other) {
        this.annotations.putAll(other.annotations);
    }

    public boolean isAnyResource() {
        return UID.class == getType();
    }

    public boolean isURI() {
        return UID.class.isAssignableFrom(getTargetType());
    }

    public boolean isIndexed() {
        return isList();
    }

    public Object clone() {
        try {
            MappedProperty<?> clone = (MappedProperty<?>) super.clone();
            clone.annotations = new HashMap<Class<? extends Annotation>, Annotation>(annotations);
            clone.typeVariables = new TypeVariable<?>[4];
            System.arraycopy(typeVariables, 0, clone.typeVariables, 0, 4);
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException(e);
        }
    }

    public boolean isClassReference() {
        return Class.class.isAssignableFrom(type);
    }

    public boolean isIncludeMapped() {
        return includeMapped;
    }
    
    public boolean isDynamicCollection() {
        return Collection.class.isAssignableFrom(componentType);
    }
    
    @Nullable
    @SuppressWarnings("unchecked")
    public Class<? extends Collection> getDynamicCollectionType() {
        if (isDynamicCollection()) {
            return getConcreteCollectionType(componentType);
        }
        else {
            return null;
        }
    }
    
    @Nullable
    public Class<?> getDynamicCollectionComponentType() {
        Type genericType = getGenericType();
        
        if (genericType instanceof ParameterizedType) {
            ParameterizedType parameterizedComponentType = (ParameterizedType) ((ParameterizedType) genericType).getActualTypeArguments()[1];
            return (Class<?>) parameterizedComponentType.getActualTypeArguments()[0];
        }
        else {
            return null;
        }
    }
}