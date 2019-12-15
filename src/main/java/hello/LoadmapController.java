package hello;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

@RestController
public class LoadmapController {

    @RequestMapping("/loadmap")
    public Scenario action(@RequestParam("name") String scenarioName) throws Exception {
        Scenario basicScenario = new Scenario(scenarioName);
        basicScenario.load();
        basicScenario.prepareForClient();

        /* perfect option is to create method which will generate GameFullState out of Scenario so we can unify it all */

        return basicScenario;
    }

}