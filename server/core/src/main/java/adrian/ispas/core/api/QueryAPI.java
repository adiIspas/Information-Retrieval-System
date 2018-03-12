package adrian.ispas.core.api;

import adrian.ispas.core.index.Indexer;
import adrian.ispas.core.retrive.SearchDocs;
import org.apache.log4j.Logger;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


/**
 * Created by Adrian Ispas on Mar, 2018
 */
@RestController
public class QueryAPI {

    private static final Logger LOG = Logger.getLogger(Indexer.class);

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String query(@RequestParam(value = "query") String query) {
        try {
            return "Results for your query [\"" + query + "\"] is\n" + SearchDocs.search(query);
        } catch (Exception e) {
            LOG.error("Error: Your query can't be executed because: " + e);
        }

        return "Something is wrong ... try again later.";
    }
}

