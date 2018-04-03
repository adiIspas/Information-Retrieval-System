package adrian.ispas.core.retrieve;

import adrian.ispas.helper.Constants;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * Searcher init a IndexSearcher and a QueryParser and expose methods for get results from a query and get documents
 * from a result
 *
 * Created by Adrian Ispas on Mar, 2018
 */
public class Searcher {

    private IndexSearcher indexSearcher;
    private QueryParser queryParser;

    /**
     * @param indexDirectoryPath Path to indexes directory
     * @throws IOException Path can't exist
     */
    Searcher(String indexDirectoryPath) throws IOException {
        Directory indexDirectory = FSDirectory.open(Paths.get(indexDirectoryPath));
        DirectoryReader directoryReader = DirectoryReader.open(indexDirectory);

        indexSearcher = new IndexSearcher(directoryReader);
        queryParser = new QueryParser(Constants.CONTENTS, Constants.Analyzer.getAnalyzer());
    }

    /**
     * Return indexed documents based on query
     * @param searchQuery Query used for search
     * @return TopDocs matched with query
     * @throws ParseException If query can't be parsed
     * @throws IOException If documents doesn't exit
     */
    TopDocs search(String searchQuery) throws ParseException, IOException {
        Query query = queryParser.parse(searchQuery);
        return indexSearcher.search(query, Constants.MAX_SEARCH);
    }

    /**
     * Get a document from indexed document
     * @param scoreDoc Indexed document
     * @return Document from indexed document
     * @throws IOException If documents doesn't exit
     */
    Document getDocument(ScoreDoc scoreDoc) throws IOException {
        return indexSearcher.doc(scoreDoc.doc);
    }
}
