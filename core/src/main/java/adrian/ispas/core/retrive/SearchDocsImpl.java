package adrian.ispas.core.retrive;

import adrian.ispas.core.helper.Constants;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Created by Adrian Ispas on Mar, 2018
 */
@Service(value = "searchDocsService")
public class SearchDocsImpl implements SearchDocsService {

    @Override
    public String search(String query) {
        Searcher searcher = null;
        try {
            searcher = new Searcher(Constants.INDEX_DIR);
        } catch (IOException e) {
            e.printStackTrace();
        }
        long startTime = System.currentTimeMillis();
        TopDocs hits = null;
        try {
            hits = searcher.search(query);
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        long endTime = System.currentTimeMillis();

        String result = "";

        result += hits.totalHits + " documents found. Time :" + (endTime - startTime);

        for(ScoreDoc scoreDoc : hits.scoreDocs) {
            Document doc = null;
            try {
                doc = searcher.getDocument(scoreDoc);
            } catch (IOException e) {
                e.printStackTrace();
            }
            result += "\n" + "File: " + doc.get(Constants.FILE_NAME);
        }

        return result;
    }
}
