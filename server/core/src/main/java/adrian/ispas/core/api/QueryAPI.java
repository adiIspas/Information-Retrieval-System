package adrian.ispas.core.api;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Adrian Ispas on Mar, 2018
 */
@RestController
public class QueryAPI {

    @RequestMapping("/")
    public String hello() {
        return "Hello World from Spring Boot App!";
    }
}
