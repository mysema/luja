package com.mysema.luja.mapping;

import com.mysema.luja.annotations.Field;
import com.mysema.luja.annotations.Indexed;

@Indexed
public class SpecialCustomer extends Customer{

    @Field
    private String specialProperty;

    public String getSpecialProperty() {
        return specialProperty;
    }

    public void setSpecialProperty(String specialProperty) {
        this.specialProperty = specialProperty;
    }
    
}
