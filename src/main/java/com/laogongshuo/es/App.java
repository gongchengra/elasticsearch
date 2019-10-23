package com.laogongshuo.es;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.http.HttpHost;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.ShardSearchFailure;
import org.elasticsearch.action.support.replication.ReplicationResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;

public class App {

  private static final String[] FETCH_FIELDS = {
    "account_number",
    "balance",
    "firstname",
    "lastname",
    "age",
    "gender",
    "address",
    "employer",
    "email",
    "city",
    "state"
  };
  private static RestHighLevelClient client =
      new RestHighLevelClient(RestClient.builder(new HttpHost("localhost", 9200, "http")));
  private static SearchRequest searchRequest = new SearchRequest("bank");

  public static void printResponse(SearchResponse searchResponse) {
    if (searchResponse.getHits().getTotalHits().value > 0) {
      System.out.println(searchResponse.getHits().getTotalHits());
      for (SearchHit hit : searchResponse.getHits()) {
        System.out.println("Match: ");
        for (String fetchField : FETCH_FIELDS) {
          System.out.println(" - " + fetchField + " " + hit.getSourceAsMap().get(fetchField));
        }
      }
    } else {
      System.out.println("No results matching the criteria.");
    }
  }

  public static void matchAll() throws IOException {
    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
    searchSourceBuilder.query(QueryBuilders.matchAllQuery());
    searchRequest.source(searchSourceBuilder);
    SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
    printResponse(searchResponse);
  }

  public static void search() throws IOException {
    SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
    sourceBuilder.query(QueryBuilders.termQuery("account_number", "49"));
    searchRequest.source(sourceBuilder);
    SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
    printResponse(searchResponse);
  }

  private static CountRequest countRequest = new CountRequest("bank");

  public static void count() throws IOException {
    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
    searchSourceBuilder.query(QueryBuilders.matchAllQuery());
    countRequest.source(searchSourceBuilder);
    CountResponse countResponse = client.count(countRequest, RequestOptions.DEFAULT);
    long count = countResponse.getCount();
    System.out.println(count);
    RestStatus status = countResponse.status();
    System.out.println(status);
    Boolean terminatedEarly = countResponse.isTerminatedEarly();
    System.out.println(terminatedEarly);
    int totalShards = countResponse.getTotalShards();
    System.out.println(totalShards);
    int skippedShards = countResponse.getSkippedShards();
    System.out.println(skippedShards);
    int successfulShards = countResponse.getSuccessfulShards();
    System.out.println(successfulShards);
    int failedShards = countResponse.getFailedShards();
    System.out.println(failedShards);
    if (failedShards > 0) {
      for (ShardSearchFailure failure : countResponse.getShardFailures()) {
        System.out.println(failure);
      }
    }
  }

  public static void index() throws IOException {
    Map<String, Object> jsonMap = new HashMap<>();
    jsonMap.put("account_number", "1000");
    jsonMap.put("balance", "59999");
    jsonMap.put("firstname", "Alan");
    jsonMap.put("lastname", "Hans");
    jsonMap.put("age", "32");
    jsonMap.put("gender", "M");
    jsonMap.put("address", "Shenzhen Fuyong Street");
    jsonMap.put("employer", "Pingan Group");
    jsonMap.put("email", "alan@gmail.com");
    jsonMap.put("city", "Shenzhen");
    jsonMap.put("state", "GD");
    IndexRequest indexRequest = new IndexRequest("bank").id("1000").source(jsonMap);
    IndexResponse indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);
    String index = indexResponse.getIndex();
    System.out.println(index);
    String id = indexResponse.getId();
    System.out.println(id);
    if (indexResponse.getResult() == DocWriteResponse.Result.CREATED) {
      System.out.println("created.");
    } else if (indexResponse.getResult() == DocWriteResponse.Result.UPDATED) {
      System.out.println("updated.");
    }
    ReplicationResponse.ShardInfo shardInfo = indexResponse.getShardInfo();
    if (shardInfo.getTotal() != shardInfo.getSuccessful()) {}

    if (shardInfo.getFailed() > 0) {
      for (ReplicationResponse.ShardInfo.Failure failure : shardInfo.getFailures()) {
        String reason = failure.reason();
        System.out.println(reason);
      }
    }
  }

  public static void main(String[] args) throws IOException {
    matchAll();
    search();
    count();
    index();
    client.close();
  }
}
