package com.laogongshuo.es;

import java.io.IOException;
import org.apache.http.HttpHost;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.ShardSearchFailure;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
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

  public static void main(String[] args) throws IOException {
    matchAll();
    search();
    count();
    client.close();
  }
}
