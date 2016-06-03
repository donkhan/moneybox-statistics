package harmoney.statistics.datacollection;

import harmoney.statistics.datacollection.routines.CounterTransactionsRetrievalRoutine;
import harmoney.statistics.datacollection.routines.CustomerCollectionRoutine;
import harmoney.statistics.datacollection.routines.GetBranch;
import harmoney.statistics.datacollection.routines.MoneyBoxLogin;
import harmoney.statistics.datacollection.routines.MoneyBoxLogout;
import harmoney.statistics.model.CounterTransaction;
import harmoney.statistics.model.Credentials;
import harmoney.statistics.model.Customer;
import harmoney.statistics.repository.CounterTransactionRepository;
import harmoney.statistics.repository.CredentialsRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DataCollector {

	@Resource
	private CounterTransactionRepository cdsRepository;

	@Resource
	private CredentialsRepository credentialsRepository;
	 
    public void takeBackup(CredentialsRepository credentialsRepository, 
    		CounterTransactionRepository cdsRepository){
    	this.credentialsRepository = credentialsRepository;
    	this.cdsRepository = cdsRepository;
    	Calendar yesterday = new GregorianCalendar();
    	yesterday.set(Calendar.MILLISECOND, 0);
    	yesterday.set(Calendar.SECOND,0);
    	yesterday.set(Calendar.MINUTE,0);
    	yesterday.set(Calendar.HOUR_OF_DAY,0);
    	yesterday.add(Calendar.DATE,-1);
    
    	Calendar fourYearsBack = (GregorianCalendar)yesterday.clone();
    	fourYearsBack.add(Calendar.YEAR,-4);
    	
    	Credentials credentials = getCredentials();
    	if(credentials == null){
    		logger.error("No Credentials to speak to MoneyBox");
    		return;
    	}
    	
    	String sessionId = getSessionId(credentials);
    	runBackUp(fourYearsBack,yesterday,sessionId,credentials.getPort());
    	logout(credentials,sessionId);
    }
    
    private void runBackUp(Calendar fourYearsBack, Calendar yesterday,String sessionId,int port){
    	while(fourYearsBack.before(yesterday)){
    		collectCounterTransactions(fourYearsBack.getTimeInMillis(),fourYearsBack.getTimeInMillis() + 24*60*60*1000-1,sessionId,port);
    		fourYearsBack.add(Calendar.DATE, 1);
    	}
    }
    
    @Scheduled(cron = "0 0 4 * * ?")
    public void startDailyCollector(){
    	Credentials credentials = getCredentials();
    	if(credentials == null){
    		logger.error("No Credentials to speak to MoneyBox");
    		return;
    	}

    	Calendar calendar = new GregorianCalendar();
    	String sessionId = getSessionId(credentials);
    	calendar.set(Calendar.MILLISECOND, 0);
    	calendar.set(Calendar.SECOND,0);
    	calendar.set(Calendar.MINUTE,0);
    	calendar.set(Calendar.HOUR_OF_DAY,0);
    	calendar.add(Calendar.DATE,-1);
    	collectCounterTransactions(calendar.getTimeInMillis(),calendar.getTimeInMillis() + 24*60*60*1000-1,sessionId,credentials.getPort());
    	logout(credentials,sessionId);
    }

    final Logger logger = LoggerFactory.getLogger(DataCollector.class);
    
    public void collectCounterTransactions(long st,long en,String sessionId,int port){
    	if(sessionId.equals("")){
    		logger.error("Session Id is null. Check credentials ...");
    		return;
    	}
    	logger.info("Start {} End {} ", new Date(st), new Date(en));
    	Credentials credentials = getCredentials();
    	if(credentials == null){
    		logger.error("No Credentials to speak to MoneyBox");
    		return;
    	}
    	try {
			CounterTransactionsRetrievalRoutine routine = 
					new CounterTransactionsRetrievalRoutine(credentials,sessionId);
			routine.setEn(en);
			routine.setSt(st);
			HttpResponse response = routine.execute();
			StringBuffer buffer = routine.getContent(response);
			logger.error("Content {}",buffer.toString());
			JSONObject object = new JSONObject(buffer.toString());
			JSONObject data = (JSONObject)object.get("data");
			JSONArray content = (JSONArray)data.get("content");
			logger.info("Collection for {} Data Size {} ",new Date(st) , content.length());
			prepareStatistics(content,collectCustomers(collectAccountIds(content),sessionId,port),st);
			
		} catch (ClientProtocolException e) {
			logger.error("Error {}",e);
		} catch (IOException e) {
			logger.error("Error {}",e);
		}
    	
    }

    private void prepareStatistics(JSONArray content,Map<Integer,Customer> customerMap,long st){
    	logger.info("CDS Repo {} Credentials Repo {}",cdsRepository, credentialsRepository);
    	int length = content.length();
    	logger.info("No of Records {}",length);
    	List<CounterTransaction> list = new ArrayList<CounterTransaction>();
    	for(int i = 0;i<length;i++){
			JSONObject record = (JSONObject)content.get(i);
			int accountId = (Integer) record.get("accountId");
			Customer customer = customerMap.get(accountId);
			String nationality = customer.getCountry();
			CounterTransaction cds = new CounterTransaction();
			String type = (String) record.get("type");
			Double amount = (Double) record.get("amount");
			cds.setAmount(amount);
			cds.setCountry(nationality);
			cds.setType(type);
			cds.setTime((Long)record.get("date"));
			cds.setId((Integer)record.get("id"));
			cds.setBranchId((Integer)record.get("branchId"));
			cds.setCustomerName(customer.getName());
			list.add(cds);
    	}
    	cdsRepository.save(list);
    	logger.info("Saved Successfully");
    }

	private Set<Integer> collectAccountIds(JSONArray content) {
		int length = content.length();
		Set<Integer> accountIds = new HashSet<Integer>();
		for(int i = 0;i<length;i++){
			JSONObject record = (JSONObject)content.get(i);
			accountIds.add((Integer)record.get("accountId"));
		}
		return accountIds;
	}

	private Map<Integer,Customer> collectCustomers(Set<Integer> accountIds,String sessionId,int port) {
		Map<Integer,Customer> customers = new HashMap<Integer,Customer>();
		for(Integer accountId : accountIds){
			CustomerCollectionRoutine routine = new CustomerCollectionRoutine("sadmin",sessionId,accountId,port);
			try {
				HttpResponse response = routine.execute();
				StringBuffer buffer = routine.getContent(response);
				JSONObject object = new JSONObject(buffer.toString());
				JSONObject data = (JSONObject) object.get("data");
				Customer customer = new Customer();
				if(data != null){
					Object nationality = data.get("nationality");
					if(nationality != null){
						customer.setCountry(nationality.toString());
					}else{
						customer.setCountry("Unknown");
					}
					Object firstName = data.get("firstName");
					if(firstName != null){
						customer.setName(firstName.toString());
					}else{
						customer.setName("Unknown");
					}
					customers.put(accountId, customer);
				}else{
					customer.setName("Unknown");
					customer.setCountry("Unknown");
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return customers;
		
	}

	public void checkAndCollectForToday(long from, long to,
			CredentialsRepository credentialsRepository2, CounterTransactionRepository cdsRepository2) {
		this.credentialsRepository = credentialsRepository2;
		this.cdsRepository = cdsRepository2;
		GregorianCalendar today = new GregorianCalendar();
		today.set(Calendar.MILLISECOND, 0);
		today.set(Calendar.SECOND,0);
		today.set(Calendar.MINUTE,0);
		today.set(Calendar.HOUR_OF_DAY,0);
		
		long todayStart =  today.getTimeInMillis();
		long todayEnd = todayStart + (24*60*60*1000) - 1;
		
		if(from > todayStart && from < todayEnd){
			collectForToday(todayStart,todayEnd);
		}
		
		if(to > todayStart && to < todayEnd){
			collectForToday(todayStart,todayEnd);
		}
	}

	private void collectForToday(long from,long to) {
		logger.info("Going to collect for today...");
		Credentials credentials = getCredentials();
		if(credentials == null){
    		logger.error("No Credentials to speak to MoneyBox");
    		return;
    	}
		String sessionId = getSessionId(credentials);
		collectCounterTransactions(from,to,sessionId,credentials.getPort());
		logout(credentials,sessionId);
	}
	
	private Credentials getCredentials(){
		logger.info("Credentials Repo {} CDS repo {} ",credentialsRepository,cdsRepository);
		List<Credentials> list = credentialsRepository.findAll();
		if(list.size() == 0){
			logger.error("No Credentials are provided... we cannot do anything");
			return null;
		}
		Credentials credential = list.get(0);
		logger.info("Credential {}",credential);
		return credential;
	}
	
	private String getSessionId(Credentials credentials){
		MoneyBoxLogin mBox = new MoneyBoxLogin(credentials.getUserName(),credentials.getPassword(),
    			credentials.getPort());
		String sessionId = "";
		try {
			 sessionId = mBox.login();
		} catch (ClientProtocolException e) {
			logger.error("Error {}",e);
		} catch (IOException e) {
			logger.error("Error {}",e);
		}
		logger.info("Session ID from Money Box {}",sessionId);
		return sessionId;
	}
	
	private void logout(Credentials credentials, String sessionId){
		MoneyBoxLogout logout = new MoneyBoxLogout(credentials.getUserName(),sessionId,credentials.getPort());
		try {
			logout.execute();
		} catch (ClientProtocolException e) {
			logger.error("Error {}",e);
		} catch (IOException e) {
			logger.error("Error {}",e);
		}
		logger.info("Session with {} for user {} is logged out",sessionId,credentials.getUserName());
	}

	public String getBranch(CredentialsRepository credentialsRepository2, CounterTransactionRepository cdsRepository2, int branchId) {
		this.credentialsRepository = credentialsRepository2;
		this.cdsRepository = cdsRepository2;
		Credentials credentials = getCredentials();
		if(credentials == null){
			logger.error("Credentials is missing");
			return "";
		}
		String sessionId = getSessionId(getCredentials());
		if(sessionId.equals("")){
			logger.error("Credentials are wrong");
			return "";
		}
		GetBranch gb = new GetBranch(credentials.getUserName(),sessionId,credentials.getPort(),branchId);
		return gb.getBranchName();
	}
}