package com.mysema.luja.mapping;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.document.NumericField;
import org.junit.Before;
import org.junit.Test;

import com.mysema.luja.mapping.converters.Constants;
import com.mysema.luja.mapping.domain.FullyAnnotated;
import com.mysema.luja.mapping.domain.NotAnnotated;


public class ObjectToDocumentTransformerTest {

    private ObjectToDocumentTransformer transformer;
    
    @Before
    public void before() {
        transformer = new ObjectToDocumentTransformer();
    }
    
    @Test
    public void AnyObjectCanBeTransformed() {
        assertNotNull(transformer.transform(new Object()));
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void InputCannotBeNull() {
        transformer.transform(null);
    }
    
    @Test
    public void BasicFieldAnnotated() {
        
        FullyAnnotated domain = new FullyAnnotated();
        Document doc = transformer.transform(domain);

        assertField(doc, "name", "Batman");
        assertField(doc, "pet", "Labrador");
        assertNullField(doc, "nullValue");
        assertField(doc, "notIndexedButStored", "a", true, false);
        assertField(doc, "notStoredButIndexed", "a", false, true);
        
        assertField(doc, "bool", "true");
        assertField(doc, "boolInst", "true");
        assertNullField(doc, "nullBoolInst");
        
        assertField(doc, "character", "a");
        assertField(doc, "characterInst", "a");
        assertNullField(doc, "nullCharacterInst");
        
        assertNull(doc.getField("notIndexedAndStored"));
      
    }
    

    @Test
    public void BasicFieldNotAnnotated() {
        
        NotAnnotated domain = new NotAnnotated();
        Document doc = transformer.transform(domain);

        assertField(doc, "name", "Batman");
        assertNullField(doc, "nullValue");
        
        assertField(doc, "bool", "true");
        assertField(doc, "boolInst", "true");
        assertNullField(doc, "nullBoolInst");
        
        assertField(doc, "character", "a");
        assertField(doc, "characterInst", "a");
        assertNullField(doc, "nullCharacterInst");

        assertNumericIntField(doc, "byteNum",1);
        assertNumericIntField(doc, "byteInst",1);
        
        assertNumericIntField(doc, "shortNum",1);
        assertNumericIntField(doc, "shortInst",1);
        
        assertNumericIntField(doc, "intNum",1);
        assertNumericIntField(doc, "intInst",1);
        
        assertNumericIntField(doc, "longNum",1);
        assertNumericIntField(doc, "longInst",1);
        
        assertNumericDoubleField(doc, "floatNum",1.0);
        assertNumericDoubleField(doc, "floatInst",1.0);
        
        assertNumericDoubleField(doc, "doubleNum",1.0);
        assertNumericDoubleField(doc, "doubleInst",1.0);
        
        assertNullField(doc, "nullByteInst");
        assertNullField(doc, "nullShortInst");
        assertNullField(doc, "nullIntInst");
        assertNullField(doc, "nullLongInst");
        assertNullField(doc, "nullFloatInst");
        assertNullField(doc, "nullDoubleInst");

        
    }
    
    @Test
    public void NumericFields() {
        FullyAnnotated domain = new FullyAnnotated();
        Document doc = transformer.transform(domain);
        
        assertNumericIntField(doc, "byteNum",1);
        assertNumericIntField(doc, "byteInst",1);
        
        assertNumericIntField(doc, "shortNum",1);
        assertNumericIntField(doc, "shortInst",1);
        
        assertNumericIntField(doc, "intNum",1);
        assertNumericIntField(doc, "intInst",1);
        
        assertNumericIntField(doc, "longNum",1);
        assertNumericIntField(doc, "longInst",1);
        
        assertNumericDoubleField(doc, "floatNum",1.0);
        assertNumericDoubleField(doc, "floatInst",1.0);
        
        assertNumericDoubleField(doc, "doubleNum",1.0);
        assertNumericDoubleField(doc, "doubleInst",1.0);
        
        assertNullField(doc, "nullByteInst");
        assertNullField(doc, "nullShortInst");
        assertNullField(doc, "nullIntInst");
        assertNullField(doc, "nullLongInst");
        assertNullField(doc, "nullFloatInst");
        assertNullField(doc, "nullDoubleInst");
        
    }
    
//    @Test
//    public void Arrays() {
//        IndexedArrays domain = new IndexedArrays();
//        Document doc = transformer.transform(domain);
//        
//        assertNumericIntField(doc, "stringArray.length", 2);
//        assertField(doc, "stringArray", "a b", false, true);
//        assertField(doc, "stringArray:serialized", "{\"a\", \"b\"}", true, false);
//        
//        
//    }
    
    private void assertNumericIntField(Document doc, String name, int value) {
        NumericField nf = (NumericField) doc.getFieldable(name);
        assertNotNull(nf);
        assertEquals(value, nf.getNumericValue().intValue());
    }
    
    private void assertNumericDoubleField(Document doc, String name, double value) {
        NumericField nf = (NumericField) doc.getFieldable(name);
        assertNotNull(nf);
        assertEquals(value, nf.getNumericValue().doubleValue(),0.0);
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
