package harmoney.statistics.web;


import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import harmoney.statistics.model.CounterTransaction;
import harmoney.statistics.model.CountryStatistics;
import harmoney.statistics.model.CountryStatisticsCollection;
import harmoney.statistics.model.SessionMap;
import harmoney.statistics.repository.CounterTransactionRepository;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import org.bson.BSONObject;
import org.bson.types.BasicBSONList;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.DBObject;

@RestController
public class StatisticsController {

	final Logger logger = LoggerFactory.getLogger(StatisticsController.class);
    @RequestMapping("/")
    @CrossOrigin
    public Response index() {
    	String result = "This Micro Service will provide statistics data of Money Box";
        return Response.ok().entity(result).header("Access-Control-Allow-Origin", "*")
    			.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT").build();
    }
  
    @Autowired
	private CounterTransactionRepository countryDayStatisticsLogRepository;
    
    @Autowired
    private MongoTemplate mongoTemplate;
    
    
    @RequestMapping("/tran-statistics/counter")
    @CrossOrigin
    public Response test(HttpServletRequest request) {
    	
    	String  token = request.getParameter("token");
    	SessionMap sessionMap = SessionMap.getSessionMap();
    	if(!sessionMap.containsKey(token)){
    		logger.error("Unable to serve as token {} is not present in Statistics Server ",token);
    		return Response.serverError().build();
    	}
    	JSONObject user = sessionMap.get(token);
    	
    	long from = Long.parseLong(request.getParameter("start-time"));
    	long to = Long.parseLong(request.getParameter("end-time"));
    	logger.info("From {} " , new Date(from));
    	logger.info("To {}" , new Date(to));
    	
    	Criteria criteria = Criteria.where("time").gte(from).lte(to);
    	if(request.getParameter("branchId") != null){
    		long branchId = Long.parseLong(request.getParameter("branchId"));
    		if(branchId != -1){
    			Criteria branchCriteria = Criteria.where("branchId").is(branchId);
    			criteria.andOperator(branchCriteria);
    		}
    	}
    	
    	TypedAggregation<CounterTransaction> aggregation = newAggregation(CounterTransaction.class,
    			match(criteria),
    		    group("country","type").
    		    sum("amount").as("totalAmount").
    		    count().as("noOfTransactions")
		);

    	logger.info("{}",aggregation.toString());
    	AggregationResults<CountryStatistics> aresult = mongoTemplate.aggregate(aggregation, CountryStatistics.class);
    	DBObject db = aresult.getRawResults();
    	BSONObject bo = (BSONObject)db.get("result");
   
    	CountryStatisticsCollection csc = new CountryStatisticsCollection();
    	convert(csc,bo);
    	logger.info("No of Items {}",csc.getItems().size());
        return Response.ok().entity(csc).header("Access-Control-Allow-Origin", "*")
    			.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT").build();
    }


	private void convert(CountryStatisticsCollection csc, BSONObject bo) {
		Map<String,CountryStatistics> map = new HashMap<String,CountryStatistics>();
		
		if(bo instanceof BasicBSONList){
			BasicBSONList  bbl = (BasicBSONList) bo;
			int size = bbl.size();
			for(int i = 0;i<size;i++){
				BSONObject b = (BSONObject)bbl.get(i);
				BSONObject id = (BSONObject) b.get("_id");
				String country = (String)id.get("country");
				CountryStatistics cs;
				if(!map.containsKey(country)){
					cs = new CountryStatistics();
					cs.setCountry(country);
					map.put(country, cs);
				}else{
					cs = map.get(country);
				}
				String type = (String)id.get("type");
				if(type.equals("B")){
					cs.setTotalBuyAmount((Double)b.get("totalAmount"));
					cs.setTotalBuys((Integer)b.get("noOfTransactions"));
				}else{
					cs.setTotalSellAmount((Double)b.get("totalAmount"));
					cs.setTotalSells((Integer)b.get("noOfTransactions"));
				}
			}
		}
		
		Iterator<CountryStatistics> iterator = map.values().iterator();
		int tb = 0, ts = 0;
		double tbv = 0L,tsv = 0L;
		while(iterator.hasNext()){
			CountryStatistics cs = iterator.next(); 
			csc.addItem(cs);
			tb += cs.getTotalBuys(); ts = cs.getTotalSells();
			tbv += cs.getTotalBuyAmount(); tsv += cs.getTotalSellAmount();
		}
		List<CountryStatistics> items = csc.getItems();
		for(CountryStatistics cs : items ){
			if(tb != 0)	cs.setBuyPercentage((double)cs.getTotalBuys()*100/tb);
			if(ts != 0) cs.setSellPercentage((double)cs.getTotalSells()*100/ts);
			if(tbv != 0) cs.setBuyValuePercentage((double)cs.getTotalBuyAmount()*100/tbv);
			if(tsv != 0) cs.setSellValuePercentage((double)cs.getTotalSellAmount()*100/tsv);
		}
		
	}
    
    
}