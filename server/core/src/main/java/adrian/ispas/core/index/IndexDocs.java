package adrian.ispas.core.index;

import adrian.ispas.core.helper.Constants;
import adrian.ispas.core.helper.TextFileFilter;

import java.io.IOException;

/**
 * Created by Adrian Ispas on Mar, 2018
 */
public class IndexDocs {

    public static void indexDocs(String indexDirectory) throws IOException {
        Indexer indexer = new Indexer(indexDirectory);
        Integer numIndexed = 0;

        Long startTime = System.currentTimeMillis();
        numIndexed = indexer.createIndex(Constants.DATA_DIR, new TextFileFilter());
        Long endTime = System.currentTimeMillis();
        indexer.close();

        System.out.println(numIndexed+" File indexed, time taken: "
                +(endTime-startTime)+" ms");
    }
}
