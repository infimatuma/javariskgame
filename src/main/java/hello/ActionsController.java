package hello;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.HtmlUtils;

@Controller
public class ActionsController {
    
    @MessageMapping("/do")
    @SendTo("/topic/actions")
    public Action action(ActionMessage message) throws Exception {
        return new Action( message.getPlayerId(), message.getAction(), message.getArea(), message.getTargetArea(), message.getUnits());
    }

}