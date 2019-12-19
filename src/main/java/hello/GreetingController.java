package hello;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class GreetingController {
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @RequestMapping("/my_name_is")
    public Greeting greeting(HelloMessage message) throws Exception {
        Game game = new Game();

        Greeting myGreeting = game.handleGreeting(message);

        if(game.getId() != null){
            GreetingSimple simpleGreeting = new GreetingSimple(message.getName(), game.getPlayers().size(), game.getMaxPlayers());
            messagingTemplate.convertAndSend("/topic/game/" + game.getId() + "/greetings", simpleGreeting);
        }

        return myGreeting;
    }
}