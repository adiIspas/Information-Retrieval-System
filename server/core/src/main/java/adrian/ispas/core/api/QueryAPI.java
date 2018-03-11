package adrian.ispas.core.api;

import adrian.ispas.core.helper.Constants;
import adrian.ispas.core.index.IndexDocs;
import adrian.ispas.core.retrive.SearchDocs;
import org.apache.lucene.queryparser.classic.ParseException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * Created by Adrian Ispas on Mar, 2018
 */
@RestController
public class QueryAPI {

    private Boolean indexed = false;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String hello(@RequestParam(value = "query") String query) throws IOException, ParseException {
        index();

        return "Results for your query [\"" + query + "\"] is\n" + SearchDocs.search(query);
    }

    private Boolean index() throws IOException {
        if (!indexed) {
            IndexDocs.indexDocs(Constants.INDEX_DIR);
            indexed = true;
        }

        return indexed;
    }
}

