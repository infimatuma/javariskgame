package lv.dium.riskserver;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
public class HelloController {
	
	@RequestMapping("/hll")
	public String index() {
		return "Welcome to Risk!";
	}
	
}
