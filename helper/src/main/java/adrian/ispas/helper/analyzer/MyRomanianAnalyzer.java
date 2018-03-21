package adrian.ispas.helper.analyzer;

import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter;
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

    private CharArraySet stopWords;


    public MyRomanianAnalyzer() {
        this.stopWords = RomanianAnalyzer.getDefaultStopSet();
    }

    protected TokenStreamComponents createComponents(String fieldName) {
        Tokenizer tokenizer = new StandardTokenizer();
        TokenStream stream = new StandardFilter(tokenizer);

        stream = new LowerCaseFilter(stream);
        stream = new SnowballFilter(stream, new RomanianStemmer());
        stream = new ASCIIFoldingFilter(stream);
        stream = new StopFilter(stream, stopWords);

        return new TokenStreamComponents(tokenizer, stream);
    }
}
