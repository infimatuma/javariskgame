package lv.dium.riskserver;

/* old greeting controller */

/*
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
}*/