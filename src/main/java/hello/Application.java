package hello;

import java.awt.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import org.reflections.Reflections;

@SpringBootApplication
public class Application {
	
	public static void main(String[] args) {
		/*
		ApplicationContext ctx = SpringApplication.run(Application.class, args);

		// Find all RiskActionProcessors and store them for faster access
		Reflections ref = new Reflections("hello");
		for (Class<?> cl : ref.getTypesAnnotatedWith(RiskActionProcessor.class)) {
			RiskActionProcessor findable = cl.getAnnotation(RiskActionProcessor.class);
			RiskActionProcessors.addAction(findable.value(), cl.getSimpleName());
		}

		Thread t1 = new Thread(() -> startnMultiplayerServer());
		t1.start();

		 */

		Thread t1 = new Thread(() -> {
			try {
				MpServer.start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		t1.start();

		System.out.println("Go-go risk!");
	}
/*
	public static void startnMultiplayerServer(){
		ExecutorService executor = Executors.newCachedThreadPool();
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try {
				executor.shutdown();
				executor.awaitTermination(5, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}));

		int portNumber = 9965;

		try (ServerSocket serverSocket = new ServerSocket(portNumber)) {

			System.out.println("Server is listening on port " + portNumber);

			while (true) {
				Socket socket = serverSocket.accept();
				System.out.println("New client connected");

				executor.submit(new MultiplayerAcceptConnection(socket));
			}
		} catch (IOException ex) {
			System.out.println("Server exception: " + ex.getMessage());
			ex.printStackTrace();
		}

	}*/
}
