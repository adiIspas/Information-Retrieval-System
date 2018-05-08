package adrian.ispas.core.context;

/**
 * Created by Adrian Ispas on May, 2018
 */
public class CloseToken {

    private String token;
    private int startOffset;
    private int endOffset;
    private int position;

    public CloseToken() {}

    public CloseToken(String token, int position, int startOffset, int endOffset) {
        this.token = token;
        this.position = position;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

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
}
