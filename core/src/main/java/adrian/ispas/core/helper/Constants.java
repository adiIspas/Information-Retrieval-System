package adrian.ispas.core.helper;

import adrian.ispas.core.helper.analyzer.MyRomanianAnalyzer;

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
        private static volatile MyRomanianAnalyzer instance;
        private static final Object mutex = new Object();

        private Analyzer() {}

        public static MyRomanianAnalyzer  getAnalyzer() {
            MyRomanianAnalyzer  result = instance;

            if (result == null) {
                synchronized (mutex) {
                    result = instance;

                    if (result == null) {
                        instance = result = new MyRomanianAnalyzer();
                    }
                }
            }
            return result;
        }
    }
}