package lv.dium.riskserver;

import org.reflections.Reflections;

public class Application {
	public static void main(String[] args) {
		Thread t1 = new Thread(() -> {
			try {
				MpServer.start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		t1.start();

		// Find all RiskActionProcessors and store them for faster access
		Reflections ref = new Reflections("hello");
		for (Class<?> cl : ref.getTypesAnnotatedWith(RiskActionProcessor.class)) {
			RiskActionProcessor findable = cl.getAnnotation(RiskActionProcessor.class);
			RiskActionProcessors.addAction(findable.value(), cl.getSimpleName());
		}

		System.out.println("Go-go risk!");
	}
}
