package adrian.ispas.core.retrieve;

import adrian.ispas.core.process.ProcessesResults;
import adrian.ispas.helper.Constants;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
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
        boolean passed = false;

        /** Create searcher */
        Searcher searcher = null;
        try {
            searcher = new Searcher(Constants.INDEX_DIR);
        } catch (IOException e) {
            LOG.error("Can't instantiate a Searcher object because: " + e);
        }

        /** Search in indexed documents */
        assert searcher != null;

        long startTime = System.currentTimeMillis();

        TopDocs hits = getHits(searcher, query);
        if (hits.totalHits > 0) {
            passed = true;
        }

        long endTime = System.currentTimeMillis();

        /** Create list of results ordered by score */
        if (passed) {
            for (ScoreDoc scoreDoc : hits.scoreDocs) {
                Document doc;
                HashMap<String, String> oneResult = new HashMap<>();
                try {
                    doc = searcher.getDocument(scoreDoc);

                    oneResult.put("fileName", doc.get(Constants.FILE_NAME));
                    oneResult.put("relativePath", ProcessesResults.processesPath(doc.get(Constants.FILE_PATH), Constants.PATH_DELIMITER, Constants.PATH_STARTER));
                    oneResult.put("contentExtracted", ProcessesResults.getSimpleContent(doc));

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

    @Override
    public HashMap<String, Object> highlighterSearch(String query) {
        HashMap<String, Object> queryResults = new HashMap<>();
        List<HashMap> results = new ArrayList<>();
        boolean passed = false;
        Highlighter highlighter = initHighlighterFor(query);

        /** Create searcher */
        Searcher searcher = null;
        try {
            searcher = new Searcher(Constants.INDEX_DIR);
        } catch (IOException e) {
            LOG.error("Can't instantiate a Searcher object because: " + e);
        }

        /** Search in indexed documents */
        assert searcher != null;

        long startTime = System.currentTimeMillis();

        TopDocs hits = getHits(searcher, query);
        if (hits.totalHits > 0) {
            passed = true;
        }

        long endTime = System.currentTimeMillis();

        /** Create list of results ordered by score */
        if (passed) {
            for (ScoreDoc scoreDoc : hits.scoreDocs) {
                Document doc;
                HashMap<String, String> oneResult = new HashMap<>();
                try {
                    doc = searcher.getDocument(scoreDoc);

                    oneResult.put("fileName", doc.get(Constants.FILE_NAME));
                    oneResult.put("relativePath", ProcessesResults.processesPath(doc.get(Constants.FILE_PATH), Constants.PATH_DELIMITER, Constants.PATH_STARTER));
//                    oneResult.put("contentExtracted", ProcessesResults.getHighlighterContent(doc, highlighter));
                    oneResult.put("contentExtracted", ProcessesResults.getHighlighterContentV2(doc, query));

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

    private TopDocs getHits(Searcher searcher, String query) {
        TopDocs hits = null;

        try {
            if (searcher != null) {
                hits = searcher.search(query);
            } else {
                LOG.error("Searcher object is null");
            }
        } catch (Exception e) {
            LOG.error("Hits can't be loaded because: " + e);
        }

        return hits;
    }

    private Highlighter initHighlighterFor(String searchQuery) {
        QueryParser queryParser = new QueryParser(Constants.CONTENTS, Constants.Analyzer.getAnalyzer());
        Query query = null;

        try {
            query = queryParser.parse(searchQuery);
        } catch (ParseException e) {
            LOG.error("Query couldn't be parsed because: " + e);
        }

        QueryScorer queryScorer = new QueryScorer(query, Constants.CONTENTS);
        Fragmenter fragmenter = new SimpleSpanFragmenter(queryScorer);

        Formatter formatter = new SimpleHTMLFormatter("<b>", "</b>");
        Highlighter highlighter = new Highlighter(formatter, queryScorer);
        highlighter.setTextFragmenter(fragmenter);

        return highlighter;
    }
}
