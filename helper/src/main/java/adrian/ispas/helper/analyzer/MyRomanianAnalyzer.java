package adrian.ispas.helper.analyzer;

import java.io.IOException;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.StopwordAnalyzerBase;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter;
import org.apache.lucene.analysis.miscellaneous.SetKeywordMarkerFilter;
import org.apache.lucene.analysis.ro.RomanianAnalyzer;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.tartarus.snowball.ext.RomanianStemmer;

/**
 * A custom analyzer based on RomanianAnalyzer. Method createComponents was modified and added in it an extra step
 * with ASCIIFoldingFilter for result variable.
 *
 * Created by Adrian Ispas on Mar, 2018
 */
public final class MyRomanianAnalyzer extends StopwordAnalyzerBase {
    private final CharArraySet stemExclusionSet;
    private static final String DEFAULT_STOPWORD_FILE = "stopwords.txt";
    private static final String STOPWORDS_COMMENT = "#";

    public static CharArraySet getDefaultStopSet() {
        return DefaultSetHolder.DEFAULT_STOP_SET;
    }

    public MyRomanianAnalyzer() {
        this(DefaultSetHolder.DEFAULT_STOP_SET);
    }

    public MyRomanianAnalyzer(CharArraySet stopwords) {
        this(stopwords, CharArraySet.EMPTY_SET);
    }

    public MyRomanianAnalyzer(CharArraySet stopwords, CharArraySet stemExclusionSet) {
        super(stopwords);
        this.stemExclusionSet = CharArraySet.unmodifiableSet(CharArraySet.copy(stemExclusionSet));
    }

    protected TokenStreamComponents createComponents(String fieldName) {
        Tokenizer source = new StandardTokenizer();
        TokenStream result = new StandardFilter(source);
        result = new LowerCaseFilter(result);
        result = new StopFilter(result, this.stopwords);
        if (!this.stemExclusionSet.isEmpty()) {
            result = new SetKeywordMarkerFilter((TokenStream)result, this.stemExclusionSet);
        }

        result = new SnowballFilter((TokenStream)result, new RomanianStemmer());
        result = new ASCIIFoldingFilter(result);
        return new TokenStreamComponents(source, result);
    }

    protected TokenStream normalize(String fieldName, TokenStream in) {
        TokenStream result = new StandardFilter(in);
        result = new LowerCaseFilter(result);
        return result;
    }

    private static class DefaultSetHolder {
        static final CharArraySet DEFAULT_STOP_SET;

        private DefaultSetHolder() {}

        static {
            try {
                DEFAULT_STOP_SET = MyRomanianAnalyzer.loadStopwordSet(false, RomanianAnalyzer.class, DEFAULT_STOPWORD_FILE, STOPWORDS_COMMENT);
            } catch (IOException var1) {
                throw new RuntimeException("Unable to load default stopword set");
            }
        }
    }
}
