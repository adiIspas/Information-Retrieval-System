package adrian.ispas.core.retrieve;

import adrian.ispas.helper.Constants;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.highlight.*;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Processes Results is used to put the results of search in a specified format.
 *
 * Created by Adrian Ispas on Apr, 2018
 */
public class ProcessesResults {

    private static final Logger LOG = Logger.getLogger(ProcessesResults.class);

    private ProcessesResults(){}

    /**
     * Make path like FOLDER > SUBFOLDER
     * @param path Path of file
     * @param pathDelimiter Delimiter for the system configuration
     * @param pathStarter Name of the main folder for raw documents
     * @return New path like FOLDER > SUBFOLDER
     */
    public static String processesPath(String path, String pathDelimiter, String pathStarter) {

        boolean isActive = false;
        StringBuilder processedPath = new StringBuilder();
        for(String currentLocation : path.split(pathDelimiter)) {
            if (currentLocation.equals(pathStarter)) {
                isActive = true;
            }

            if (isActive) {
                processedPath.append(" > ").append(currentLocation.toUpperCase());
            }
        }

        processedPath.deleteCharAt(processedPath.indexOf("> "));
        processedPath.delete(processedPath.lastIndexOf(" > "), processedPath.length());

        return processedPath.toString();
    }

    /**
     * Get raw content from a document
     * @param document Document
     * @return Raw content for document passed
     */
    public static String getSimpleContent(Document document) {
        return document.get(Constants.CONTENTS);
    }

    /**
     * Get highlighter content for a document with a specified Highlighter
     * @param document Document for is want to highlight content
     * @param highlighter Highlighter configured for a query that has returned specified document
     * @return Highlight content for document
     */
    public static String getHighlighterContent(Document document, Highlighter highlighter) {
        String content = document.get(Constants.CONTENTS);
        TokenStream tokenStream = null;
        String fragments = null;

        try {
            tokenStream = TokenSources.getTokenStream(Constants.CONTENTS, null, content, Constants.Analyzer.getAnalyzer(), Constants.Highlighter.MAX_START_OFFSET);
        } catch (IOException e) {
            LOG.error("Token stream couldn't be created because: " + e);
        }

        try {
            assert tokenStream != null;
            fragments = highlighter.getBestFragments(tokenStream, content, Constants.Highlighter.MAX_NUM_FRAGMENTS,  Constants.Highlighter.SEPARATOR);
        } catch (IOException | InvalidTokenOffsetsException e) {
            LOG.error("Best fragments couldn't be retrieved because: " + e);
        }

        return fragments;
    }

    public static String getHighlighterContentV2(Document document, String searchQuery) throws IOException {
        Query query = initQuery(searchQuery);
        List<String> clauses = extractClausesFrom(query);

        if (clauses.size() > 0) {
            String content = document.get(Constants.CONTENTS);
            StringBuilder finalContent = new StringBuilder(content);

            TokenStream tokenStream = Constants.Analyzer.getAnalyzer().tokenStream(null, new StringReader(content));
            OffsetAttribute offsetAttribute = tokenStream.addAttribute(OffsetAttribute.class);
            CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);

            tokenStream.reset();
            int totalInserted = 0;
            while (tokenStream.incrementToken()) {
                int startOffset = offsetAttribute.startOffset() + totalInserted;
                int endOffset = offsetAttribute.endOffset() + totalInserted;
                String term = charTermAttribute.toString();

                if(clauses.contains(term)) {
                    finalContent.insert(startOffset, "<b>");
                    endOffset += 3;
                    finalContent.insert(endOffset, "</b>");
                    totalInserted += 7;
                }
            }

            tokenStream.end();
            tokenStream.close();

            return finalContent.toString();
        }

        return "";
    }

    private static Query initQuery(String searchQuery) {
        QueryParser queryParser = new QueryParser(Constants.CONTENTS, Constants.Analyzer.getAnalyzer());

        try {
            return  queryParser.parse(searchQuery);
        } catch (ParseException e) {
            LOG.error("Query couldn't be parsed because: " + e);
        }

        return null;
    }

    private static List<String> extractClausesFrom(Query query) {
        if(query != null) {
            return Arrays.asList(query.toString().replace(Constants.CONTENTS.toLowerCase() + ":", "").split(" "));
        }

        return new ArrayList<>();
    }
}
