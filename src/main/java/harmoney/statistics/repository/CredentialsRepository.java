package harmoney.statistics.repository;

import harmoney.statistics.model.Credentials;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface CredentialsRepository extends MongoRepository<Credentials,String>{

}
