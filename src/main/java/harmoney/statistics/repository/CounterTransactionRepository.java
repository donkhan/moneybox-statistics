package harmoney.statistics.repository;

import harmoney.statistics.model.CounterTransaction;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface CounterTransactionRepository extends MongoRepository<CounterTransaction,String>{
	
}
