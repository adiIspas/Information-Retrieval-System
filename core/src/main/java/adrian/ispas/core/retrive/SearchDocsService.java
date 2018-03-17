package adrian.ispas.core.retrive;

import java.util.HashMap;

/**
 * SearchDocsService give a way to entry in system and search in indexed documents
 *
 * Created by Adrian Ispas on Mar, 2018
 */
public interface SearchDocsService {

    /**
     * Search indexed documents by received query and return best matched documents
     * @param query Query for search
     * @return Results, total time of execution and total results that match with query
     */
    HashMap<String, Object> search(String query);
}
