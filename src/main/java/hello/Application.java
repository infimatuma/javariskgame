package hello;

import java.util.Arrays;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import org.reflections.Reflections;

@SpringBootApplication
public class Application {
	
	public static void main(String[] args) {
		ApplicationContext ctx = SpringApplication.run(Application.class, args);

		// Find all RiskActionProcessors and store them for faster access
		Reflections ref = new Reflections("hello");
		for (Class<?> cl : ref.getTypesAnnotatedWith(RiskActionProcessor.class)) {
			RiskActionProcessor findable = cl.getAnnotation(RiskActionProcessor.class);
			RiskActionProcessors.addAction(findable.value(), cl.getSimpleName());
		}

		System.out.println("Go-go risk!");
	}
}
