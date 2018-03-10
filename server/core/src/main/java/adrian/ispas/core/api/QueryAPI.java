package adrian.ispas.core.api;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Adrian Ispas on Mar, 2018
 */
@RestController
public class QueryAPI {

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String hello(@RequestParam(value = "query") String query) {
        return "Hello World from Spring Boot App! Your query is " + query;
    }
}
