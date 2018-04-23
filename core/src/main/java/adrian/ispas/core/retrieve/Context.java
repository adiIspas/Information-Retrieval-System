package adrian.ispas.core.retrieve;

/**
 * Created by Adrian Ispas on Apr, 2018
 */
public class Context {

    private String word;
    private String phraseBefore;
    private String phraseAfter;
    private int score;

    public Context() {}

    public Context(String word, String phraseBefore, String phraseAfter, int score) {
        this.phraseBefore = phraseBefore;
        this.phraseAfter = phraseAfter;
        this.score = score;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getPhraseBefore() {
        return phraseBefore;
    }

    public void setPhraseBefore(String phraseBefore) {
        this.phraseBefore = phraseBefore;
    }

    public String getPhraseAfter() {
        return phraseAfter;
    }

    public void setPhraseAfter(String phraseAfter) {
        this.phraseAfter = phraseAfter;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
