package adrian.ispas.core.helper;

import org.apache.lucene.analysis.ro.RomanianAnalyzer;

/**
 * Store all constants used in system
 *
 * Created by Adrian Ispas on Mar, 2018
 */
public class Constants {
    /** Constants about components of indexed documents */
    public static final String CONTENTS = "contents";
    public static final String FILE_NAME = "filename";
    public static final String FILE_PATH = "filepath";
    public static final int MAX_SEARCH = 10;

    /** Constants about store place for raw and indexed documents */
    public static final String DATA_DIR = "core/src/main/resources/documents";
    public static final String INDEX_DIR = "core/src/main/resources/documents_indexes";

    /** Analyzer used in whole project. Use default stop words list */
    public static class Analyzer {
        private static volatile RomanianAnalyzer instance;
        private static final Object mutex = new Object();

        private Analyzer() {}

        public static RomanianAnalyzer getAnalyzer() {
            RomanianAnalyzer result = instance;

            if (result == null) {
                synchronized (mutex) {
                    result = instance;

                    if (result == null) {
                        instance = result = new RomanianAnalyzer();
                    }
                }
            }
            return result;
        }
    }
}