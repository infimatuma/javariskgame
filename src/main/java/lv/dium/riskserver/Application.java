package lv.dium.riskserver;

import lv.dium.riskgame.GameEffect;
import org.reflections.Reflections;

public class Application {
	public static void main(String[] args) {

		// Find all RiskActionProcessors and store them for faster access
		Reflections ref = new Reflections("lv.dium.riskserver");
		for (Class<?> cl : ref.getTypesAnnotatedWith(RiskActionProcessor.class)) {

			RiskActionProcessor findable = cl.getAnnotation(RiskActionProcessor.class);
			RiskActionProcessors.addAction(findable.value(), cl.getSimpleName());
			System.out.println("Wired: " + findable.value());
		}

		GameEffect.initCosmetics();

		Thread t1 = new Thread(() -> {
			try {
				MpServer.start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		t1.start();

		/**
		 * Copy scenario from server to local Redis instance
		 */
		/*
		JedisConnection.connectRemote();
		Scenario basic = new Scenario("basic");
		basic.load();
		JedisConnection.connect();
		basic.save();
		 */
/*
		JedisConnection.connectRemote();
		Scenario basic = new Scenario("small");
		basic.load();
		basic.setName("oldsmall");
		basic.save();

		Scenario main = new Scenario("basic");
		main.load();
		main.setName("small");
		main.save();
*/
		System.out.println("Go-go risk!");
	}
}
