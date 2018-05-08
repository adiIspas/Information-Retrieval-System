package adrian.ispas.core.similarity;

import adrian.ispas.helper.Constants;
import org.apache.lucene.search.similarities.BM25Similarity;

/**
 * Created by Adrian Ispas on May, 2018
 */
public class DocumentAbstractBM25Similarity extends BM25Similarity {

    @Override
    protected float idf(long docFreq, long docCount) {
        return Constants.ABSTRACT_MULTIPLICATION_SCORE * super.idf(docFreq, docCount);
    }
}
