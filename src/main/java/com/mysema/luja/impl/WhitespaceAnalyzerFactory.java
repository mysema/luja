package com.mysema.luja.impl;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.WhitespaceAnalyzer;

import com.mysema.luja.AnalyzerFactory;

/**
 * Whitespace analyzer factory
 */
public class WhitespaceAnalyzerFactory implements AnalyzerFactory {

    @Override
    public Analyzer newAnalyzer() {
        return new WhitespaceAnalyzer();
    }

}
