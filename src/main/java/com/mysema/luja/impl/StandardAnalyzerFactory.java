package com.mysema.luja.impl;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.util.Version;

import com.mysema.luja.AnalyzerFactory;

/**
 * StandardAnalyzer factory
 */
public class StandardAnalyzerFactory implements AnalyzerFactory {

    @Override
    public Analyzer newAnalyzer() {
        return new StandardAnalyzer(Version.LUCENE_30);
    }

}
