package adrian.ispas.core.retrive;

import adrian.ispas.helper.Constants;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
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

    private static final Logger LOG = Logger.getLogger(SearchDocsImpl.class);

    @Override
    public HashMap<String, Object> search(String query) {
        HashMap<String, Object> queryResults = new HashMap<>();
        List<HashMap> results = new ArrayList<>();
        Boolean passed;

        Searcher searcher = null;
        try {
            searcher = new Searcher(Constants.INDEX_DIR);
        } catch (IOException e) {
            LOG.error("Can't instantiate a Searcher object because: " + e);
        }

        TopDocs hits = null;
        long startTime = System.currentTimeMillis();
        try {
            if (searcher != null) {
                hits = searcher.search(query);
                passed = true;
            } else {
                LOG.error("Searcher object is null");
                passed = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            passed = false;
        }
        long endTime = System.currentTimeMillis();

        if (hits != null && passed) {
            for (ScoreDoc scoreDoc : hits.scoreDocs) {
                Document doc;
                HashMap<String, String> oneResult = new HashMap<>();
                try {
                    doc = searcher.getDocument(scoreDoc);

                    oneResult.put("fileName", doc.get(Constants.FILE_NAME));
                    oneResult.put("relativePath", doc.get(Constants.FILE_PATH));
                    oneResult.put("contentExtracted", doc.get(Constants.CONTENTS));

                    results.add(oneResult);
                } catch (IOException e) {
                    LOG.error("Can't load documents because: " + e);
                    passed = false;
                }
            }

            if (passed) {
                queryResults.put("results", results);
                queryResults.put("timeOfExecution", endTime - startTime);
                queryResults.put("totalResults", hits.totalHits);
            }
        }

        return queryResults;
    }
}
