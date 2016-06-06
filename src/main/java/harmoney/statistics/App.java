package harmoney.statistics;


import harmoney.server.RegistrationServer;
import harmoney.statistics.model.Credentials;
import harmoney.statistics.repository.CredentialsRepository;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    
    @Autowired
	private CredentialsRepository credentialsRepository;
    
    private Credentials getAppCredentials(){
    	List<Credentials> list = credentialsRepository.findAll();
    	if(list.size() == 0){
    		logger.info("Server Ports are not configured. Going to use 3332 for Registration. Please use Application Settings to change that in case you want.After that Restart me");
    		Credentials configuration = new Credentials();
    		configuration.setRegistrationServerPort(3332);
    		return configuration;
    	}
    	return list.get(0);
    }
    
    final Logger logger = LoggerFactory.getLogger(App.class);
    @Override
	public void run(String... args) throws Exception {
    	RegistrationServer registrationServer = new RegistrationServer();
    	registrationServer.setPort(getAppCredentials().getRegistrationServerPort());
    	registrationServer.start();
    }
    
    
}