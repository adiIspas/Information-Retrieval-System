package adrian.ispas.core.context;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Adrian Ispas on Apr, 2018
 */
public class Context {

    private int startOffset;
    private int endOffset;
    private String text;
    private int contentLength;
    private Set<String> terms;

    public int getStartOffset() {
        return startOffset;
    }

    public void setStartOffset(int startOffset) {
        this.startOffset = startOffset;
    }

    public int getEndOffset() {
        return endOffset;
    }

    public void setEndOffset(int endOffset) {
        this.endOffset = endOffset;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getContentLength() {
        return contentLength;
    }

    public void setContentLength(int contentLength) {
        this.contentLength = contentLength;
    }

    public Set<String> getTerms() {
        return terms;
    }

    public void setTerms(Set<String> terms) {
        this.terms = terms;
    }

    public void addTerm(String term) {
        if(this.terms == null) {
            this.terms = new HashSet<>();
        }

        this.terms.add(term);
    }

    public void addAllTerms(Set<String> terms) {
        if(this.terms == null) {
            this.terms = new HashSet<>();
        }

        this.terms.addAll(terms);
    }

    public void removeTerms(String term) {
        if(this.terms != null) {
            this.terms.remove(term);
        }
    }
}
