package hello;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ClientData {
	
	@RequestMapping("/client-data")
	public String index() {
		return "Thank you for yout data!";
	}
	
}
