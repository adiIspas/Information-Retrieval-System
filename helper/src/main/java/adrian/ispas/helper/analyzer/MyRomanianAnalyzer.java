package adrian.ispas.helper.analyzer;

import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.tartarus.snowball.ext.RomanianStemmer;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.Objects;
import java.util.Scanner;

/**
 * A custom analyzer based on RomanianAnalyzer. Method createComponents was modified and added in it an extra step
 * with ASCIIFoldingFilter for result variable.
 *
 * Created by Adrian Ispas on Mar, 2018
 */
public class MyRomanianAnalyzer extends Analyzer {

    private CharArraySet stopWords;

    /**
     * @param stopWordPath Path to a file with stop words
     */
    public MyRomanianAnalyzer(Path stopWordPath) {
        this.stopWords = loadStopWords(Objects.requireNonNull(stopWordPath));
    }

    /**
     * Create index component for every field
     * @param fieldName Field for is wanted to create a index component
     * @return Index component
     */
    protected TokenStreamComponents createComponents(String fieldName) {
        Tokenizer tokenizer = new StandardTokenizer();
        TokenStream stream = new StandardFilter(tokenizer);

        stream = new LowerCaseFilter(stream);
        stream = new ASCIIFoldingFilter(stream);
        stream = new StopFilter(stream, stopWords);
        stream = new SnowballFilter(stream, new RomanianStemmer());

        return new TokenStreamComponents(tokenizer, stream);
    }

    /**
     * Load stop word from a file
     * @param path Path to stop words file
     * @return Normalized list of stop words
     */
    private CharArraySet loadStopWords(Path path) {
        CharArraySet stopWords = CharArraySet.EMPTY_SET;
        String extraStopWords = "";
        Path absolutePath = FileSystems.getDefault().getPath("").toAbsolutePath();

        try(Scanner scanner = new Scanner(Paths.get(absolutePath.toString() + path.toString()), StandardCharsets.UTF_8.name())) {
            extraStopWords = removeDiacritics(scanner.useDelimiter("\\A").next());
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            stopWords = WordlistLoader.getWordSet(new StringReader(extraStopWords));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return stopWords;
    }

    /**
     * Remove diacritics from a string
     * @param initialStopWords Stop words like a string
     * @return Stop words string without diacritics
     */
    private String removeDiacritics(String initialStopWords) {
        return Normalizer.normalize(initialStopWords.toLowerCase(), Normalizer.Form.NFKD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }
}
