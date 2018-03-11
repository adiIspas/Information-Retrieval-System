package adrian.ispas.core.retrive;

import adrian.ispas.core.helper.Constants;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
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
import org.apache.lucene.store.RAMDirectory;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * Created by Adrian Ispas on Mar, 2018
 */
public class Searcher {

    private IndexSearcher indexSearcher;
    private QueryParser queryParser;
    private Query query;

    public Searcher(String indexDirectoryPath) throws IOException {
        Directory indexDirectory = FSDirectory.open(Paths.get(indexDirectoryPath));
//        Directory indexDirectory = new RAMDirectory();
        DirectoryReader directoryReader = DirectoryReader.open(indexDirectory);

        indexSearcher = new IndexSearcher(directoryReader);
        queryParser = new QueryParser(Constants.CONTENTS, new StandardAnalyzer());
    }

    public TopDocs search(String searchQuery) throws ParseException, IOException {
        query = queryParser.parse(searchQuery);
        return indexSearcher.search(query, Constants.MAX_SEARCH);
    }

    public Document getDocument(ScoreDoc scoreDoc) throws IOException {
        return indexSearcher.doc(scoreDoc.doc);
    }
}
