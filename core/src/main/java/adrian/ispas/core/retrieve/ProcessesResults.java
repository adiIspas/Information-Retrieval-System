package adrian.ispas.core.retrieve;

import adrian.ispas.helper.Constants;
import org.apache.commons.lang.StringUtils;
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
import java.util.*;

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
//            fragments = highlighter.getBestFragments(tokenStream, content, Constants.Highlighter.MAX_NUM_FRAGMENTS,  Constants.Highlighter.SEPARATOR);
            fragments = highlighter.getBestFragment(tokenStream, content);
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
            String finalResult = "";
            int totalTerms = 0;
            while (tokenStream.incrementToken()) {
                int startOffset = offsetAttribute.startOffset();
                int endOffset = offsetAttribute.endOffset();
                String term = charTermAttribute.toString();

                if(clauses.contains(term)) {
                    List<String> beginWords = Arrays.asList(finalContent.substring(0, startOffset).trim().split(" "));
                    List<String> endWords = Arrays.asList(finalContent.substring(endOffset).trim().split(" "));

                    List<String> firstWords = beginWords.subList(Math.max(0, beginWords.size() - 5), beginWords.size());
                    List<String> lastWords = endWords.subList(0, Math.min(5, endWords.size()));



                    Set<String> terms = new HashSet<>();
                    terms.add(term);
                    Map<String, Object> resultBegin = analyseWords(firstWords, clauses, terms);
                    terms = (Set<String>)resultBegin.get("score");
                    Map<String, Object> resultEnd = analyseWords(lastWords, clauses, terms);
                    terms = (Set<String>)resultEnd.get("score");

                    if(terms.size() > totalTerms) {
                        finalResult = "";
                        finalResult += " ... " +  resultBegin.get("content") + " <b>" + content.substring(startOffset, endOffset)  + "</b> " + resultEnd.get("content") + " ... " + "<br>";
                        totalTerms = terms.size();
                    }

//                    String begin = analyseWords(firstWords, clauses, terms);
//                    String end = analyseWords(lastWords, clauses, terms);

//                    String begin = StringUtils.join(firstWords, " ");
//                    String end = StringUtils.join(lastWords, " ");
                }
            }

            tokenStream.end();
            tokenStream.close();

//            return finalContent.toString();
            return finalResult;
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

    private static Map<String, Object> analyseWords(List<String> words, List<String> clauses, Set<String> terms) throws IOException {
        Map<String, Object> results = new HashMap<>();

        String finalResult = "";

        for(String word:words) {
            TokenStream tokenStream = Constants.Analyzer.anotherAnalyzer().tokenStream(null, new StringReader(word));
            CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);

            tokenStream.reset();
            tokenStream.incrementToken();
            String term = charTermAttribute.toString();
            if(clauses.contains(term) && !terms.contains(term)) {
                finalResult += "<b>" + word + "</b> ";
            }
            else {
                finalResult += word + " ";
            }
            terms.add(term);

            tokenStream.end();
            tokenStream.close();
        }


        results.put("score", terms);
        results.put("content", finalResult);
        return results;
    }
}
