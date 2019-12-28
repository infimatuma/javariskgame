package lv.dium.riskserver;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class SavemapController {
    
    @MessageMapping("/savemap")
    @SendTo("/topic/actions")
    public Action action(ScenarioMessage message) throws Exception {
        Scenario newScenario = new Scenario( message.getName(), message.getAreas());
        newScenario.save();
        return new Action( "", "scenario-saved", "", "", 0);
    }

}