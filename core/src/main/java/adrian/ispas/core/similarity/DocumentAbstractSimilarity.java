package adrian.ispas.core.similarity;

import adrian.ispas.helper.Constants;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.PerFieldSimilarityWrapper;
import org.apache.lucene.search.similarities.Similarity;

/**
 * Created by Adrian Ispas on May, 2018
 */
public class DocumentAbstractSimilarity extends PerFieldSimilarityWrapper {

    @Override
    public Similarity get(String name) {
        return name.equals(Constants.DocumentParts.ABSTRACT) ? new DocumentAbstractBM25Similarity() : new BM25Similarity();
    }
}
