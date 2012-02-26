package com.mysema.luja;

import org.apache.lucene.analysis.Analyzer;

/**
 * Provides configurable way to get analyzer for session factory
 */
public interface AnalyzerFactory {

    /**
     * @return a new analyzer instance
     */
    Analyzer newAnalyzer();
}
