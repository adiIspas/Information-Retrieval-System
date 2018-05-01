package adrian.ispas.core.process;

import adrian.ispas.core.context.Context;
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
            fragments = highlighter.getBestFragments(tokenStream, content, Constants.Highlighter.MAX_NUM_FRAGMENTS,  Constants.Highlighter.SEPARATOR);
        } catch (IOException | InvalidTokenOffsetsException e) {
            LOG.error("Best fragments couldn't be retrieved because: " + e);
        }

        return fragments;
    }

    public static String getHighlighterContentV2(Document document, String searchQuery) throws IOException {
        Query query = initQuery(searchQuery);
        List<String> clauses = extractClausesFrom(query);
        List<Context> contexts = new ArrayList<>();

        String finalContentResult = "";

        String content = "";
        if (clauses.size() > 0) {
            content = document.get(Constants.CONTENTS);
            content = content.replace("\n", "");

            TokenStream documentContentStream = Constants.Analyzer.getAnalyzer().tokenStream(null, new StringReader(content));
            OffsetAttribute offsetAttribute = documentContentStream.addAttribute(OffsetAttribute.class);
            CharTermAttribute charTermAttribute = documentContentStream.addAttribute(CharTermAttribute.class);

            documentContentStream.reset();
            while (documentContentStream.incrementToken()) {
                int startOffset = offsetAttribute.startOffset();
                int endOffset = offsetAttribute.endOffset();

                String term = charTermAttribute.toString();
                if(clauses.contains(term)) {
                    contexts.add(extractContextFrom(startOffset, endOffset, content));
                }
            }

            documentContentStream.end();
            documentContentStream.close();
        }

        List<Context> bestContexts = new ArrayList<>();
        if(contexts.size() > 0) {
            bestContexts = merge(contexts, content);
        }

        finalContentResult = processesBestContexts(bestContexts, clauses);

        return finalContentResult;
    }

    private static Context extractContextFrom(int startOffset, int endOffset, String content) {
        StringBuilder documentContent = new StringBuilder(content);

        List<String> tempWordsBeforeTerm = Arrays.asList(documentContent.substring(0, startOffset).trim().split(" "));
        List<String> tempWordsAfterTerm = Arrays.asList(documentContent.substring(endOffset).trim().split(" "));

        List<String> wordsBeforeTerm = tempWordsBeforeTerm.subList(Math.max(0, tempWordsBeforeTerm.size() - Constants.CONTEXT_WINDOW_LENGTH), tempWordsBeforeTerm.size());
        List<String> wordsAfterTerm = tempWordsAfterTerm.subList(0, Math.min(Constants.CONTEXT_WINDOW_LENGTH, tempWordsAfterTerm.size()));

        String beforeContext = String.join(" ", wordsBeforeTerm);
        String afterContext = String.join(" ", wordsAfterTerm);

        int start = Math.max(0, startOffset - beforeContext.length() - 1);
        int end = Math.min(content.length(), endOffset + afterContext.length() + 1);

        Context context = new Context();
        context.setText(beforeContext + " " + content.substring(startOffset, endOffset) + " " + afterContext);
        context.setStartOffset(start);
        context.setEndOffset(end);

        return context;
    }

    public static List<Context> merge(List<Context> contexts, String content) {
        List<Context> newContexts = new ArrayList<>();

        int startOffset = 0;
        int endOffset = 0;
        int index = 0;
        for (Context context:contexts) {
            if (context.getStartOffset() <= endOffset) {
                startOffset = Math.min(startOffset, context.getStartOffset());
                endOffset = Math.max(endOffset, context.getEndOffset());

                Context contextTemp = new Context();
                contextTemp.setStartOffset(startOffset);
                contextTemp.setEndOffset(endOffset);
                contextTemp.setText(content.substring(startOffset, endOffset));
                contextTemp.setContentLength(content.length());

                if(newContexts.size() > index) {
                    newContexts.remove(index);
                    index++;
                }

                newContexts.add(contextTemp);
            }
            else {
                startOffset = context.getStartOffset();
                endOffset = context.getEndOffset();
                context.setContentLength(content.length());

                newContexts.add(context);
            }
        }

        return newContexts;
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

    private static String processesBestContexts(List<Context> contexts, List<String> clauses) throws IOException {
        String finalResult = "";
        Set<String> checkedTerms = new HashSet<>();

        for(Context context: contexts) {
            String tempContext = "";
            Boolean existTerm = false;
            for (String word : context.getText().split(" ")) {
                TokenStream tokenStream = Constants.Analyzer.RomanianAnalyzer().tokenStream(null, new StringReader(word));
                CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);

                tokenStream.reset();
                tokenStream.incrementToken();
                String term = charTermAttribute.toString();
                if (clauses.contains(term) && !checkedTerms.contains(term)) {
                    tempContext += "<b>" + word + "</b> ";
                    checkedTerms.add(term);
                    existTerm = true;
                } else {
                    tempContext += word + " ";
                }

                tokenStream.end();
                tokenStream.close();
            }

            if (existTerm) {
                finalResult += tempContext;

                if(context.getContentLength() > context.getEndOffset()) {
                    finalResult += " ... ";
                }
            }
        }

        return finalResult;
    }
}
