package com.mysema.luja.mapping.domain;

import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;

import com.mysema.luja.annotations.Field;
import com.mysema.luja.annotations.Indexed;
import com.mysema.luja.annotations.Transient;

@Indexed
@SuppressWarnings("unused")
public class FullyAnnotated {

    @Field
    private String name = "Batman";

    @Field(name = "pet")
    private String dog = "Labrador";

    @Field(index = Index.NO)
    private String notIndexedButStored = "a";

    @Field(store = Store.NO)
    private String notStoredButIndexed = "a";
    
    @Transient
    private String notIndexedAndStored = "a";

    @Field
    private String nullValue;

    @Field
    private byte byteNum = 1;

    @Field
    private Byte byteInst = 1;

    @Field
    private Byte nullByteInst;

    @Field
    private short shortNum = 1;

    @Field
    private Short shortInst = 1;

    @Field
    private Short nullShortInst;

    @Field
    private int intNum = 1;

    @Field
    private Integer intInst = 1;

    @Field
    private Integer nullIntInst;

    @Field
    private long longNum = 1;

    @Field
    private Long longInst = 1L;

    @Field
    private Long nullLongInst;

    @Field
    private float floatNum = 1.0f;

    @Field
    private Float floatInst = 1.0f;

    @Field
    private Float nullFloatInst;

    @Field
    private double doubleNum = 1.0;

    @Field
    private Double doubleInst = 1.0;

    @Field
    private Double nullDoubleInst;

    @Field
    private boolean bool = true;

    @Field
    private Boolean boolInst = true;

    @Field
    private Boolean nullBoolInst;

    @Field
    private char character = 'a';

    @Field
    private Character characterInst = 'a';

    @Field
    private Character nullCharacterInst;

}
