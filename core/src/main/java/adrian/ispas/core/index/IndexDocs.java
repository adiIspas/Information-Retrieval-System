package adrian.ispas.core.index;

import adrian.ispas.helper.Constants;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * IndexDocs is a job profile used when is wanted to reindex new documents
 *
 * Created by Adrian Ispas on Mar, 2018
 */
@Component
@Profile({"index-docs"})
public class IndexDocs implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger LOG = Logger.getLogger(IndexDocs.class);

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        Indexer indexer = null;

        try {
            Indexer.deleteIndexes(Constants.INDEX_DIR);
            indexer = new Indexer(Constants.INDEX_DIR);
        } catch (IOException e) {
            LOG.error("Error: Create indexer failed because: " + e);
        }

        Integer totalIndexes = 0;
        Long startTime = System.currentTimeMillis();
        try {
            if (indexer != null) {
                totalIndexes = indexer.createIndex(Constants.DATA_DIR, Constants.FileFilters.filters);
            }
        } catch (IOException e) {
            LOG.error("Error: Create indexes failed because: " + e);
        }
        Long endTime = System.currentTimeMillis();

        if (indexer != null) {
            indexer.close();
        }

        System.out.println(totalIndexes + " File indexed, time taken: " + (endTime - startTime) + " ms");
        System.exit(0);
    }
}
