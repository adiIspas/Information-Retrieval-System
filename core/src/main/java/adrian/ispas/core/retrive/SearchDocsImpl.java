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
 * Implementation for SearchService
 *
 * Created by Adrian Ispas on Mar, 2018
 */
@Service(value = "searchDocsService")
public class SearchDocsImpl implements SearchDocsService {

    private static final Logger LOG = Logger.getLogger(SearchDocsImpl.class);

    /**
     * Return result for a query
     * @param query Query for search
     * @return A list of results with extra information. List of results is a HashMap defined by document field name and
     * value for the field. Final result contain list of results, total time of execution and total number of results
     */
    @Override
    public HashMap<String, Object> search(String query) {
        HashMap<String, Object> queryResults = new HashMap<>();
        List<HashMap> results = new ArrayList<>();
        Boolean passed;

        /** Create searcher */
        Searcher searcher = null;
        try {
            searcher = new Searcher(Constants.INDEX_DIR);
        } catch (IOException e) {
            LOG.error("Can't instantiate a Searcher object because: " + e);
        }

        /** Search in indexed documents */
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

        /** Create list of results ordered by score */
        if (hits != null && passed) {
            for (ScoreDoc scoreDoc : hits.scoreDocs) {
                Document doc;
                HashMap<String, String> oneResult = new HashMap<>();
                try {
                    doc = searcher.getDocument(scoreDoc);

                    oneResult.put("fileName", doc.get(Constants.FILE_NAME));
                    oneResult.put("relativePath", ProcessesResults.processesPath(doc.get(Constants.FILE_PATH), Constants.PATH_DELIMITER, Constants.PATH_STARTER));
                    oneResult.put("contentExtracted", doc.get(Constants.CONTENTS));

                    results.add(oneResult);
                } catch (IOException e) {
                    LOG.error("Can't load documents because: " + e);
                    passed = false;
                }
            }

            /** Create final result for return */
            if (passed) {
                queryResults.put("results", results);
                queryResults.put("timeOfExecution", endTime - startTime);
                queryResults.put("totalResults", hits.totalHits);
            }
        }

        return queryResults;
    }
}
