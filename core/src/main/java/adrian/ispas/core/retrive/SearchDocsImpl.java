package adrian.ispas.core.retrive;

import adrian.ispas.helper.Constants;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.*;
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

    public void searchAndHighLightKeywords(String searchQuery) throws Exception {
        QueryParser queryParser = new QueryParser(Constants.CONTENTS, Constants.Analyzer.getAnalyzer());
        Query query = queryParser.parse(searchQuery);
        QueryScorer queryScorer = new QueryScorer(query, Constants.CONTENTS);
        Fragmenter fragmenter = new SimpleSpanFragmenter(queryScorer);

        Highlighter highlighter = new Highlighter(queryScorer); // Set the best scorer fragments
        highlighter.setTextFragmenter(fragmenter); // Set fragment to highlight

        Searcher searcher = new Searcher(Constants.INDEX_DIR);

        ScoreDoc scoreDocs[] = searcher.search(searchQuery).scoreDocs;
        for (ScoreDoc scoreDoc : scoreDocs) {
            Document document = searcher.getDocument(scoreDoc);
            String content = document.get(Constants.CONTENTS);
//            TokenStream tokenStream = TokenSources.getAnyTokenStream(searcher.getIndexSearcher().getIndexReader(), scoreDoc.doc, Constants.CONTENTS, document, Constants.Analyzer.getAnalyzer());
            TokenStream tokenStream = TokenSources.getTokenStream(Constants.CONTENTS, null, content, Constants.Analyzer.getAnalyzer(), -1);
            String[] fragments = highlighter.getBestFragments(tokenStream, content, 3);
            for (String fragment:fragments) {
                System.out.println(fragment);
                System.out.println(".....");
            }
            System.out.println("||||||||");
        }
    }
}
