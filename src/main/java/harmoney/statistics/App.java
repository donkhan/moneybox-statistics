package harmoney.statistics;


import harmoney.statistics.server.RegistrationServer;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class App implements CommandLineRunner{
	
	
    public static void main(String[] args) {
    	SpringApplication.run(App.class, args);
    	
    }
    
    @Override
	public void run(String... args) throws Exception {
    	RegistrationServer registrationServer = new RegistrationServer();
    	registrationServer.start(3333);
    }
    
    
}