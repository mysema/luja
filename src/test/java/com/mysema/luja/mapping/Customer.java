package com.mysema.luja.mapping;

import com.mysema.luja.annotations.Field;
import com.mysema.luja.annotations.Indexed;

@Indexed
public class Customer {

    private String name;
    
    @Field
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
}
