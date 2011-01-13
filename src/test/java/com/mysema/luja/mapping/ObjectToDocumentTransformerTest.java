package com.mysema.luja.mapping;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.document.NumericField;
import org.junit.Before;
import org.junit.Test;

import com.mysema.luja.annotations.Field;
import com.mysema.luja.annotations.Indexed;


public class ObjectToDocumentTransformerTest {

    private ObjectToDocumentTransformer transformer;
    
    private IndexedDomain domain;
    
    @Before
    public void before() {
        transformer = new ObjectToDocumentTransformer();
        domain = new IndexedDomain();
    }
    
    @Test(expected=TypeNotMappedException.class)
    public void NotIndexed() {
        transformer.transform(new Object());
    }
    
    @Test
    public void BasicField() {
        
        Document doc = transformer.transform(domain);

        assertField(doc, "name", "Batman");
        assertField(doc, "pet", "Labrador");
        assertNullField(doc, "nullValue");
        assertField(doc, "notIndexedButStored", "a", true, false);
        assertField(doc, "notStoredButIndexed", "a", false, true);
        assertNull(doc.getField("notIndexedAndStored"));
       
        
        assertField(doc, "bool", "true");
        assertField(doc, "boolInst", "true");
        assertNullField(doc, "nullBoolInst");
        
        assertField(doc, "character", "a");
        assertField(doc, "characterInst", "a");
        assertNullField(doc, "nullCharacterInst");
        
       
      
    }
    

    @Test
    public void NumericFields() {
        Document doc = transformer.transform(domain);
        
        NumericField nfield = (NumericField) doc.getFieldable("byteNum");
        assertEquals(1, nfield.getNumericValue().byteValue());
        nfield = (NumericField) doc.getFieldable("byteInst");
        assertEquals(1, nfield.getNumericValue().byteValue());
        
        nfield = (NumericField) doc.getFieldable("shortNum");
        assertEquals(1, nfield.getNumericValue().shortValue());
        nfield = (NumericField) doc.getFieldable("shortInst");
        assertEquals(1, nfield.getNumericValue().shortValue());
        
        nfield = (NumericField) doc.getFieldable("intNum");
        assertEquals(1, nfield.getNumericValue().intValue());
        nfield = (NumericField) doc.getFieldable("intInst");
        assertEquals(1, nfield.getNumericValue().intValue());
        
        nfield = (NumericField) doc.getFieldable("longNum");
        assertEquals(1, nfield.getNumericValue().longValue());
        nfield = (NumericField) doc.getFieldable("longInst");
        assertEquals(1, nfield.getNumericValue().longValue());
        
        nfield = (NumericField) doc.getFieldable("floatNum");
        assertEquals(1.0f, nfield.getNumericValue().floatValue(), 0.0);
        nfield = (NumericField) doc.getFieldable("floatInst");
        assertEquals(1.0f, nfield.getNumericValue().floatValue(), 0.0);
        
        nfield = (NumericField) doc.getFieldable("doubleNum");
        assertEquals(1.0, nfield.getNumericValue().doubleValue(), 0.0);
        nfield = (NumericField) doc.getFieldable("doubleInst");
        assertEquals(1.0, nfield.getNumericValue().doubleValue(), 0.0);
        
        assertNullField(doc, "nullByteInst");
        assertNullField(doc, "nullShortInst");
        assertNullField(doc, "nullIntInst");
        assertNullField(doc, "nullLongInst");
        assertNullField(doc, "nullFloatInst");
        assertNullField(doc, "nullDoubleInst");
        
    }
    
    @Test
    public void Arrays() {
        
        Document doc = transformer.transform(domain);
        
        //assertNumericIntField(doc, "stringArray.length", 2);
        //assertField(doc, "stringArray", "a b", false, true);
        //assertField(doc, "stringArray.data", "{\"a\", \"b\"}")
        
        
    }
    
    private void assertNumericIntField(Document doc, String name, int value) {
        NumericField nf = (NumericField) doc.getFieldable("name");
        assertNotNull(nf);
        assertEquals(value, nf.getNumericValue().intValue());
    }

    @Indexed
    @SuppressWarnings("unused")
    private static class IndexedDomain {
        
        @Field
        private String name = "Batman";
        
        @Field(name="pet")
        private String dog = "Labrador";
        
        @Field(index=Index.NO)
        private String notIndexedButStored = "a";
        
        @Field(store=Store.NO)
        private String notStoredButIndexed = "a";
        
        private String notIndexedAndStored;
        
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
        
        @Field
        private String[] stringArray = new String[] { "a", "b" };

    }
    

    
    
    
    private Fieldable assertNullField(Document doc, String name) {
        return assertField(
                doc,
                name + Constants.NULL_FIELD_POSTFIX,
                Constants.NULL_FIELD_VALUE,
                false,
                true);
    }

    private Fieldable assertField(Document doc, String name, String value) {
        return assertField(doc, name, value, true, true);
    }
    
    private Fieldable assertField(
            Document doc,
            String name,
            String value,
            boolean stored,
            boolean indexed) {

        Fieldable f = doc.getFieldable(name);
        assertNotNull(f);
        if (value != null) {
            assertEquals(value, f.stringValue());
        }
        assertEquals(stored, f.isStored());
        assertEquals(indexed, f.isIndexed());
        return f;
    }
    
}
