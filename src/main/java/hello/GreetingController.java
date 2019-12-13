package hello;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.HtmlUtils;

import java.io.File;

@Controller
public class GreetingController {
    
    @MessageMapping("/hello")
    @SendTo("/topic/greetings")
    public Greeting greeting(HelloMessage message) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();

        File resource = new ClassPathResource(
                "static/basic-java-map.json").getFile();

        //read json file and convert to customer object
        GameFullState gameData = objectMapper.readValue(resource, GameFullState.class);

        //print customer details
        System.out.println(gameData);

        return new Greeting("Hello, " + HtmlUtils.htmlEscape(message.getName()) + "!", gameData);
    }

}