package hello;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ActionsController {
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/do/{gameId}")
    public void action(@DestinationVariable String gameId, ActionMessage message) throws Exception {
        System.out.println("Received [do] message in game [" + gameId + "]");

        Game game = new Game();
        Action myAction = game.handleAction(message);

        if(game.getId() != null){
            System.out.println("Sending message to /topic/game/" + game.getId());
            messagingTemplate.convertAndSend("/topic/game/" + game.getId() + "/actions", myAction);
        }
    }
}