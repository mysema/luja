package com.mysema.luja.mapping;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.mysema.luja.annotations.Indexed;
import com.mysema.luja.annotations.Transient;

public class MappedClassFactory {
    
    private final Map<Class<?>, MappedClass> mappedClasses = new LinkedHashMap<Class<?>, MappedClass>();
    
    public MappedClass getMappedClass(Class<?> clazz){
        if (mappedClasses.containsKey(clazz)){
            return mappedClasses.get(clazz);
        }else{
            List<MappedClass> superClasses = Collections.<MappedClass>emptyList();
            if (clazz.getSuperclass().isAnnotationPresent(Indexed.class)){
                superClasses = Collections.singletonList(getMappedClass(clazz.getSuperclass()));
            }
            MappedClass mappedClass = new MappedClass(clazz, superClasses);            
            if (!clazz.isEnum()){
                collectFieldPaths(clazz, mappedClass);
                collectMethodPaths(clazz, mappedClass);
            }
            mappedClasses.put(clazz, mappedClass);
            return mappedClass;
        }
    }

    private void collectFieldPaths(Class<?> clazz, MappedClass mappedClass) {
        if (!clazz.isInterface()) {
            for (Field field : clazz.getDeclaredFields()) {
                if (!field.isAnnotationPresent(Transient.class)){
                    mappedClass.addMappedProperty(new FieldProperty(field, mappedClass));
                }
            }
        }
    }

    private void collectMethodPaths(Class<?> clazz, MappedClass mappedClass) {
        for (Method method : clazz.getDeclaredMethods()) {
            // TODO : tune this logic
            if (method.isAnnotationPresent(com.mysema.luja.annotations.Field.class)){
                mappedClass.addMappedProperty(new MethodProperty(method, mappedClass));
            }
        }
    }
}
