package adrian.ispas.core.api;

import adrian.ispas.core.index.Indexer;
import adrian.ispas.core.retrive.SearchDocsService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * Created by Adrian Ispas on Mar, 2018
 */
@RestController
@Service
public class QueryAPI {

    @Autowired
    private SearchDocsService searchDocsService;

    private static final Logger LOG = Logger.getLogger(Indexer.class);

    @CrossOrigin(origins = "*")
    @RequestMapping(value = "/", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity query(@RequestParam(value = "query") String query) {
        HashMap<String, Object> resultResponse = new HashMap<>();

        try {
            HashMap<String, Object> queryResults = searchDocsService.search(query);
            resultResponse.put("results", queryResults.get("results"));
            resultResponse.put("timeOfExecution", queryResults.get("timeOfExecution"));
            resultResponse.put("totalResults", queryResults.get("totalResults"));
            return new ResponseEntity<>(resultResponse, HttpStatus.OK);
        } catch (Exception e) {
            LOG.error("Error: Your query can't be executed because: " + e);
        }

        resultResponse.put("results", new ArrayList());
        resultResponse.put("timeOfExecution", 0);
        resultResponse.put("totalResults", 0);
        return new ResponseEntity<>(resultResponse, HttpStatus.NO_CONTENT);
    }
}

