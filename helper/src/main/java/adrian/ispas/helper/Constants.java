package adrian.ispas.helper;

import adrian.ispas.helper.analyzer.MyRomanianAnalyzer;
import adrian.ispas.helper.filters.*;

import java.io.FileFilter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Store all constants used in system
 *
 * Created by Adrian Ispas on Mar, 2018
 */
public class Constants {

    private Constants() {}

    /** Path to stop words */
    private static final String STOP_WORDS = "/helper/src/main/resources/dictionary/ro-stop-words.txt";

    /** Constants about components of indexed documents */
    public static final String CONTENTS = "contents";
    public static final String FILE_NAME = "filename";
    public static final String FILE_PATH = "filepath";
    public static final int MAX_SEARCH = 10;

    /** Constants about store place for raw and indexed documents */
    public static final String DATA_DIR = "helper/src/main/resources/documents";
    public static final String INDEX_DIR = "helper/src/main/resources/documents_indexes";

    /** Types of file supported for index */
    public static class FileFilters {
        public static List<FileFilter> filters = new ArrayList<>();

        static {
            filters.add(new TextFileFilter());
            filters.add(new PdfFileFilter());
            filters.add(new HtmlFileFilter());
            filters.add(new DocxFileFilter());
            filters.add(new PptxFileFormat());
            filters.add(new RtfFileFilter());
        }
    }

    /** Constants for highlighter configuration */
    public static class Highlighter {
        public static final int MAX_START_OFFSET = -1;
        public static final int MAX_NUM_FRAGMENTS = 3;
        public static final String SEPARATOR = " ... ";
    }

    /** Constants used for make a path with a specified format */
    public static final String PATH_DELIMITER = "/";
    public static final String PATH_STARTER = "documents";

    /** Context highlighter */
    public static final int CONTEXT_WINDOW_LENGTH = 5;

    /** Analyzer used in whole project */
    public static class Analyzer {
        private static volatile MyRomanianAnalyzer instance;
        private static final Object mutex = new Object();

        private Analyzer() {}

        public static MyRomanianAnalyzer getAnalyzer() {
            MyRomanianAnalyzer result = instance;

            if (result == null) {
                synchronized (mutex) {
                    result = instance;

                    if (result == null) {
                        instance = result = new MyRomanianAnalyzer(Paths.get(Constants.STOP_WORDS));
                    }
                }
            }

            return result;
        }
    }
}