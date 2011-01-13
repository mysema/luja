package com.mysema.luja.mapping;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

import com.mysema.query.apt.DefaultConfiguration;
import com.mysema.query.apt.Processor;

@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class LujaAnnotationProcessor extends AbstractProcessor{
    
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Running " + getClass().getSimpleName());
        Class<? extends Annotation> entities = null;
        Class<? extends Annotation> entity = Indexed.class;
        Class<? extends Annotation> superType = null;
        Class<? extends Annotation> embeddable = IndexEmbeddable.class;
        Class<? extends Annotation> embedded = IndexEmbedded.class;
        Class<? extends Annotation> skip = Transient.class;
        
        DefaultConfiguration configuration = new DefaultConfiguration(
                roundEnv, 
                processingEnv.getOptions(), 
                Collections.<String>emptySet(), 
                entities, entity, superType, embeddable, embedded, skip);

        Processor processor = new Processor(processingEnv, roundEnv, configuration);
        processor.process();
        return true;
    }       
    
}