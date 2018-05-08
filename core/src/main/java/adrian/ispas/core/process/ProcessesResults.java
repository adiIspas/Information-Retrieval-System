package adrian.ispas.core.process;

import adrian.ispas.core.context.CloseToken;
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
            content = document.get(Constants.DocumentParts.ABSTRACT) + " " + document.get(Constants.DocumentParts.CONTENT);
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
                    Context context = extractContextFrom(startOffset, endOffset, content, clauses);
                    context.addTerm(term);

                    contexts.add(context);
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

    private static Context extractContextFrom(int startOffset, int endOffset, String content, List<String> clauses) throws IOException {
        StringBuilder documentContent = new StringBuilder(content);

        List<String> tempWordsBeforeTerm = Arrays.asList(documentContent.substring(0, startOffset).trim().split(" "));
        List<String> tempWordsAfterTerm = Arrays.asList(documentContent.substring(endOffset).trim().split(" "));

        List<String> wordsBeforeTerm = tempWordsBeforeTerm.subList(Math.max(0, tempWordsBeforeTerm.size() - Constants.CONTEXT_WINDOW_LENGTH), tempWordsBeforeTerm.size());
        List<String> wordsAfterTerm = tempWordsAfterTerm.subList(0, Math.min(Constants.CONTEXT_WINDOW_LENGTH, tempWordsAfterTerm.size()));

        Context context = new Context();
        List<String> newList = new ArrayList<>(wordsBeforeTerm);
        newList.addAll(wordsAfterTerm);
        for(String word:newList) {
            TokenStream tokenStream = Constants.Analyzer.RomanianAnalyzer().tokenStream(null, new StringReader(word));
            CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
            tokenStream.reset();
            tokenStream.incrementToken();
            String term = charTermAttribute.toString();
            if(clauses.contains(term)) {
                context.addTerm(term);
            }
        }

        String beforeContext = String.join(" ", wordsBeforeTerm);
        String afterContext = String.join(" ", wordsAfterTerm);

        int start = Math.max(0, startOffset - beforeContext.length() - 1);
        int end = Math.min(content.length(), endOffset + afterContext.length() + 1);


        context.setText(beforeContext + " " + content.substring(startOffset, endOffset) + " " + afterContext);
        context.setStartOffset(start);
        context.setEndOffset(end);

        return context;
    }

    private static List<Context> merge(List<Context> contexts, String content) {
        List<Context> newContexts = new ArrayList<>();

        int startOffset = 0;
        int endOffset = 0;
        for (Context context:contexts) {
            if (context.getStartOffset() <= endOffset) {
                startOffset = Math.min(startOffset, context.getStartOffset());
                endOffset = Math.max(endOffset, context.getEndOffset());

                Context contextTemp = new Context();
                contextTemp.setStartOffset(startOffset);
                contextTemp.setEndOffset(endOffset);
                contextTemp.setText(content.substring(startOffset, endOffset));
                contextTemp.setContentLength(content.length());
                contextTemp.setTerms(context.getTerms());

                if(newContexts.size() > 0) {
                    context.addAllTerms(newContexts.get(newContexts.size() - 1).getTerms());
                    newContexts.remove(newContexts.size() - 1);
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

    private static List<Context> sortContextsByTermsAndPosition(List<Context> contexts) {
        contexts.sort((context1, context2) -> {
            Integer x1 = context1.getTerms().size();
            Integer x2 = context2.getTerms().size();
            int result = x2.compareTo(x1);

            if (result != 0) {
                return result;
            }

            x1 = context1.getStartOffset();
            x2 = context2.getStartOffset();
            return x1.compareTo(x2);
        });

        return contexts;
    }

    private static List<Context> sortContextsByPosition(List<Context> contexts) {
        contexts.sort((context1, context2) -> {
            Integer x1 = context1.getStartOffset();
            Integer x2 = context2.getStartOffset();
            return x1.compareTo(x2);
        });

        return contexts;
    }

    private static List<Context> preProcessBestContexts(List<Context> contexts) {
        List<Context> finalContexts = new ArrayList<>();

        List<Context> tempContexts = new ArrayList<>(contexts);
        Set<Context> deletedContexts = new HashSet<>();
        for(int index = 0; index < tempContexts.size(); index++) {
            Context tempContext = tempContexts.get(index);
            Set<String> tempContextTerms = new HashSet<>(tempContext.getTerms());
            Set<String> copyTempContextTerms = new HashSet<>(tempContext.getTerms());

            for(int secondIndex = 0; secondIndex < tempContexts.size(); secondIndex++) {
                if(secondIndex != index && !deletedContexts.contains(tempContexts.get(secondIndex))) {
                    Context secondTempContext = tempContexts.get(secondIndex);
                    for (String term : tempContextTerms) {
                        if (secondTempContext.getTerms().contains(term)) {
                            copyTempContextTerms.remove(term);
                        }
                    }
                }
            }

            if(copyTempContextTerms.size() > 0) {
                finalContexts.add(tempContext);
            } else {
                deletedContexts.add(tempContext);
            }
        }

        return sortContextsByTermsAndPosition(finalContexts);
    }

    private static String processesBestContexts(List<Context> contexts, List<String> clauses) throws IOException {
        contexts = preProcessBestContexts(contexts);

        String finalResult = "";
        Set<String> checkedTerms = new HashSet<>();
        boolean first = true;

        List<Context> finalContexts = new ArrayList<>();

        for(Context context: contexts) {
            Map<String, Integer> results = getHighlightCloseTokens(context, clauses);

            String tempContext = "";
            Boolean existTerm = false;
            List<String> firstWords = new ArrayList<>();
            boolean finishFirst = false;

            String beforeWords;
            int position = 0;
            int newStartOffset = 0;
            for (String word : context.getText().split(" ")) {
                TokenStream tokenStream = Constants.Analyzer.RomanianAnalyzer().tokenStream(null, new StringReader(word));
                CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);

                tokenStream.reset();
                tokenStream.incrementToken();
                String term = charTermAttribute.toString();
                if (clauses.contains(term) && !checkedTerms.contains(term) && results.get(term).equals(position)) {
                    checkedTerms.add(term);
                    existTerm = true;
                    context.removeTerms(term);

                    if(!finishFirst) {
                        finishFirst = true;
                        newStartOffset = String.join(" ", firstWords.subList(0,Math.max(firstWords.size() - Constants.CONTEXT_WINDOW_LENGTH, 0))).length();
                        firstWords = firstWords.subList(Math.max(firstWords.size() - Constants.CONTEXT_WINDOW_LENGTH, 0), firstWords.size());
                        beforeWords = String.join(" ", firstWords);
                        tempContext = beforeWords + " <b>" + word + "</b> ";
                    } else {
                        tempContext += " <b>" + word + "</b> ";
                    }
                } else {
                    if(!finishFirst) {
                        firstWords.add(word);
                    } else {
                        tempContext += word + " ";
                    }
                }

                tokenStream.end();
                tokenStream.close();
                position++;
            }

            if (existTerm) {
                context.setText(tempContext);
                if(newStartOffset > 0) {
                    context.setStartOffset(newStartOffset);
                }
            }
            finalContexts.add(context);
        }

        finalContexts = sortContextsByPosition(finalContexts);

        for (Context context:finalContexts) {
            if (first && context.getStartOffset() > 0) {
                finalResult += " ... ";
                first = false;
            }

            finalResult += context.getText();

            if (context.getContentLength() > context.getEndOffset()) {
                finalResult += " ... ";
                first = false;
            }
        }

        return finalResult;
    }

    private static Map<String, Integer> getHighlightCloseTokens(Context context, List<String> clauses) throws IOException {

        int position = 0;
        List<CloseToken> closeTokens = new ArrayList<>();
        Map<String, Integer> results = new HashMap<>();
        Map<String, Integer> positions = new HashMap<>();

        for (String word : context.getText().split(" ")) {
            TokenStream tokenStream = Constants.Analyzer.RomanianAnalyzer().tokenStream(null, new StringReader(word));
            OffsetAttribute offsetAttribute = tokenStream.addAttribute(OffsetAttribute.class);
            CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);

            tokenStream.reset();
            tokenStream.incrementToken();
            String term = charTermAttribute.toString();
            int startOffset = offsetAttribute.startOffset();
            int endOffset = offsetAttribute.endOffset();

            if(clauses.contains(term)) {
                closeTokens.add(new CloseToken(term, position, startOffset, endOffset));
            }

            position++;
        }

        for(CloseToken closeToken:closeTokens) {
            int distance = 0;
            for(CloseToken closeTokenSecond:closeTokens) {
                distance += Math.abs(closeTokenSecond.getPosition() - closeToken.getPosition());
            }

            results.putIfAbsent(closeToken.getToken(), distance);
            positions.putIfAbsent(closeToken.getToken(), closeToken.getPosition());

            if(results.get(closeToken.getToken()) > distance) {
                results.put(closeToken.getToken(), distance);
                positions.put(closeToken.getToken(), closeToken.getPosition());
            }
        }

        return positions;
    }
}
