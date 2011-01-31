package com.mysema.luja.mapping;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.lang.reflect.Field;

import org.apache.commons.collections15.BeanMap;
import org.junit.Test;

import com.mysema.luja.mapping.domain.FullyAnnotated;

public class MappedClassFactoryTest {

    private MappedClassFactory mappedClassFactory = new MappedClassFactory();
    
    @Test
    public void Superclass_fields_are_visible(){
        MappedClass mappedClass = mappedClassFactory.getMappedClass(SpecialCustomer.class);
        assertNotNull(mappedClass.getProperty("name"));
        assertNotNull(mappedClass.getProperty("specialProperty"));
    }
    
    @Test
    public void Subclass_fields_are_not_visible(){
        // just to make sure that this class has been inspected
        MappedClass mappedClass = mappedClassFactory.getMappedClass(SpecialCustomer.class);
        
        mappedClass = mappedClassFactory.getMappedClass(Customer.class);
        assertNull(mappedClass.getProperty("specialProperty"));
    }
    
    @Test
    public void Population_via_Field() throws Exception{
        MappedClass mappedClass = mappedClassFactory.getMappedClass(FullyAnnotated.class);
        FullyAnnotated annotated = new FullyAnnotated();
        MappedProperty<?> property = mappedClass.getProperty("name");
        property.setValue(new BeanMap(annotated), "Hello");
        
        Field field = FullyAnnotated.class.getDeclaredField("name");
        field.setAccessible(true);
        assertEquals("Hello", field.get(annotated));        
    }
    
    @Test
    public void Population_via_Method() throws Exception{
        MappedClass mappedClass = mappedClassFactory.getMappedClass(Customer.class);
        Customer annotated = new Customer();
        MappedProperty<?> property = mappedClass.getProperty("name");
        property.setValue(new BeanMap(annotated), "Hello");
        
        Field field = Customer.class.getDeclaredField("name");
        field.setAccessible(true);
        assertEquals("Hello", field.get(annotated));
    }
    
    @Test
    public void FullyAnnotated_has_mapped_properties(){
        MappedClass mappedClass = mappedClassFactory.getMappedClass(FullyAnnotated.class);
        assertNotNull(mappedClass.getProperty("name"));
        assertNotNull(mappedClass.getProperty("dog"));
        assertNotNull(mappedClass.getProperty("notIndexedButStored"));
        assertNotNull(mappedClass.getProperty("notStoredButIndexed"));
        assertNull(mappedClass.getProperty("notIndexedAndStored"));
    }
}
