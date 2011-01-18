package com.mysema.luja.mapping.converters;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LuceneConverters {

    private final Map<Class<?>, LuceneConverter<?>> converters =
        new ConcurrentHashMap<Class<?>, LuceneConverter<?>>();
    
    private final ObjectConverter objectConverter;

    public LuceneConverters() {
        FieldableFactory factory = new FieldableFactory();
        objectConverter = new ObjectConverter(factory);
        register(new StringConverter(factory));
        register(new BooleanConverter(factory));
        register(Boolean.TYPE, new BooleanConverter(factory));
        register(new CharacterConverter(factory));
        register(Character.TYPE, new CharacterConverter(factory));

        //Numeric types
        register(new ByteConverter(factory));
        register(Byte.TYPE, new ByteConverter(factory));
        register(new ShortConverter(factory));
        register(Short.TYPE, new ShortConverter(factory));
        register(new IntegerConverter(factory));
        register(Integer.TYPE, new IntegerConverter(factory));
        register(new LongConverter(factory));
        register(Long.TYPE, new LongConverter(factory));
        register(new FloatConverter(factory));
        register(Float.TYPE, new FloatConverter(factory));
        register(new DoubleConverter(factory));
        register(Double.TYPE, new DoubleConverter(factory));
    }

    public void register(LuceneConverter<?> converter) {
        converters.put(converter.getJavaType(), converter);
    }

    public void register(Class<?> clazz, LuceneConverter<?> converter) {
        converters.put(clazz, converter);
    }

    @SuppressWarnings("unchecked")
    public <T> LuceneConverter<T> getConverter(Class<T> clazz) {
        LuceneConverter<T> converter = (LuceneConverter<T>) converters.get(clazz);
        if (converter == null) {
            return (LuceneConverter<T>) objectConverter;
        }

        return converter;
    }

}
