package adrian.ispas.core.retrive;

import adrian.ispas.core.helper.Constants;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Adrian Ispas on Mar, 2018
 */
@Service(value = "searchDocsService")
public class SearchDocsImpl implements SearchDocsService {

    @Override
    public List search(String query) {
        List results = new ArrayList<>();

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
            HashMap<String, String> oneResult = new HashMap<>();
            try {
                doc = searcher.getDocument(scoreDoc);
                oneResult.put("fileName",doc.get(Constants.FILE_NAME));
                oneResult.put("relativePath","calea/relativa/in/lucru");
                oneResult.put("contentExtracted",doc.get(Constants.CONTENTS));
            } catch (IOException e) {
                e.printStackTrace();
            }
            results.add(oneResult);
        }

        return results;
    }
}
