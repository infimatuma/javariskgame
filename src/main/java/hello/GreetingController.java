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

    private String jsonString = "{\"areas\":[\n" +
            "  {\"id\":0,\"x\":42,\"y\":62, \"str\":12, \"color\":\"red\"},\n" +
            "  {\"id\":1,\"x\":109,\"y\":62, \"str\":1, \"color\":\"green\"},\n" +
            "  {\"id\":2,\"x\":277,\"y\":67, \"str\":3, \"color\":\"green\"},\n" +
            "  {\"id\":3,\"x\":110,\"y\":145, \"str\":2, \"color\":\"grey\"},\n" +
            "  {\"id\":4,\"x\":167,\"y\":124, \"str\":6, \"color\":\"red\"},\n" +
            "  {\"id\":5,\"x\":229,\"y\":117, \"str\":6, \"color\":\"red\"},\n" +
            "  {\"id\":6,\"x\":113,\"y\":208, \"str\":4, \"color\":\"green\"},\n" +
            "  {\"id\":7,\"x\":185,\"y\":185, \"str\":4, \"color\":\"green\"},\n" +
            "  {\"id\":8,\"x\":154,\"y\":263, \"str\":5, \"color\":\"grey\"},\n" +
            "\n" +
            "  {\"id\":9,\"x\":187,\"y\":295, \"str\":5, \"color\":\"yellow\"},\n" +
            "  {\"id\":10,\"x\":210,\"y\":352, \"str\":5, \"color\":\"yellow\"},\n" +
            "  {\"id\":11,\"x\":165,\"y\":385, \"str\":5, \"color\":\"yellow\"},\n" +
            "  {\"id\":12,\"x\":197,\"y\":485, \"str\":5, \"color\":\"yellow\"},\n" +
            "\n" +
            "  {\"id\":13,\"x\":347,\"y\":90, \"str\":2, \"color\":\"red\"},\n" +
            "  {\"id\":14,\"x\":320,\"y\":145, \"str\":4, \"color\":\"red\"},\n" +
            "  {\"id\":15,\"x\":400,\"y\":85, \"str\":4, \"color\":\"red\"},\n" +
            "  {\"id\":16,\"x\":320,\"y\":250, \"str\":4, \"color\":\"red\"},\n" +
            "  {\"id\":17,\"x\":385,\"y\":200, \"str\":4, \"color\":\"red\"},\n" +
            "  {\"id\":18,\"x\":470,\"y\":170, \"str\":22, \"color\":\"green\"},\n" +
            "  {\"id\":19,\"x\":425,\"y\":257, \"str\":4, \"color\":\"green\"},\n" +
            "\n" +
            "  {\"id\":31,\"x\":570,\"y\":105, \"str\":11, \"color\":\"green\"},\n" +
            "  {\"id\":20,\"x\":620,\"y\":115, \"str\":1, \"color\":\"green\"},\n" +
            "  {\"id\":21,\"x\":665,\"y\":80, \"str\":1, \"color\":\"green\"},\n" +
            "  {\"id\":22,\"x\":710,\"y\":100, \"str\":99, \"color\":\"grey\"},\n" +
            "  {\"id\":23,\"x\":646,\"y\":148, \"str\":2, \"color\":\"blue\"},\n" +
            "  {\"id\":24,\"x\":700,\"y\":170, \"str\":2, \"color\":\"blue\"},\n" +
            "  {\"id\":25,\"x\":770,\"y\":165, \"str\":2, \"color\":\"blue\"},\n" +
            "  {\"id\":26,\"x\":555,\"y\":225, \"str\":3, \"color\":\"red\"},\n" +
            "  {\"id\":27,\"x\":625,\"y\":220, \"str\":3, \"color\":\"yellow\"},\n" +
            "  {\"id\":28,\"x\":510,\"y\":310, \"str\":8, \"color\":\"yellow\"},\n" +
            "  {\"id\":29,\"x\":610,\"y\":325, \"str\":8, \"color\":\"blue\"},\n" +
            "  {\"id\":30,\"x\":685,\"y\":340, \"str\":2, \"color\":\"blue\"},\n" +
            "\n" +
            "  {\"id\":32,\"x\":690,\"y\":430, \"str\":1, \"color\":\"blue\"},\n" +
            "  {\"id\":33,\"x\":755,\"y\":370, \"str\":2, \"color\":\"grey\"},\n" +
            "  {\"id\":34,\"x\":725,\"y\":470, \"str\":2, \"color\":\"blue\"},\n" +
            "  {\"id\":35,\"x\":760,\"y\":445, \"str\":2, \"color\":\"blue\"},\n" +
            "\n" +
            "  {\"id\":36,\"x\":375,\"y\":325, \"str\":2, \"color\":\"grey\"},\n" +
            "  {\"id\":37,\"x\":430,\"y\":310, \"str\":2, \"color\":\"grey\"},\n" +
            "  {\"id\":38,\"x\":470,\"y\":360, \"str\":2, \"color\":\"grey\"},\n" +
            "  {\"id\":39,\"x\":445,\"y\":408, \"str\":2, \"color\":\"grey\"},\n" +
            "  {\"id\":40,\"x\":445,\"y\":520, \"str\":2, \"color\":\"grey\"},\n" +
            "  {\"id\":41,\"x\":540,\"y\":510, \"str\":2, \"color\":\"grey\"},\n" +
            "\n" +
            "  {\"id\":41,\"x\":795,\"y\":505, \"str\":5, \"color\":\"blue\"}\n" +
            "]}";

    @MessageMapping("/hello")
    @SendTo("/topic/greetings")
    public Greeting greeting(HelloMessage message) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();

        //read json file and convert to customer object
        GameFullState gameData = objectMapper.readValue(jsonString, GameFullState.class);

        //print customer details
        System.out.println(gameData);

        return new Greeting("Hello, " + HtmlUtils.htmlEscape(message.getName()) + "!", gameData);
    }

}