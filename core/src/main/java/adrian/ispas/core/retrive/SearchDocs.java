package adrian.ispas.core.retrive;

import adrian.ispas.core.helper.Constants;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

import java.io.IOException;

/**
 * Created by Adrian Ispas on Mar, 2018
 */
public class SearchDocs {

    public static String search(String query) throws IOException, ParseException {
        Searcher searcher = new Searcher(Constants.INDEX_DIR);
        long startTime = System.currentTimeMillis();
        TopDocs hits = searcher.search(query);
        long endTime = System.currentTimeMillis();

        String result = "";

        result += hits.totalHits + " documents found. Time :" + (endTime - startTime);

        for(ScoreDoc scoreDoc : hits.scoreDocs) {
            Document doc = searcher.getDocument(scoreDoc);
            result += "\n" + "File: " + doc.get(Constants.FILE_NAME);
        }

        return result;
    }
}
