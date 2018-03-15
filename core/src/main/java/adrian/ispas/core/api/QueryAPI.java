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
        HashMap<String, List> response = new HashMap<>();
        try {
            response.put("results", searchDocsService.search(query));
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            LOG.error("Error: Your query can't be executed because: " + e);
        }

        response.put("results", new ArrayList());
        return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
    }
}

