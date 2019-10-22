package com.laogongshuo.es;

import java.io.IOException;
import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
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

  public static void matchAll() throws IOException {
    RestHighLevelClient client =
        new RestHighLevelClient(RestClient.builder(new HttpHost("localhost", 9200, "http")));
    SearchRequest searchRequest = new SearchRequest("bank");
    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
    searchSourceBuilder.query(QueryBuilders.matchAllQuery());
    searchRequest.source(searchSourceBuilder);

    SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
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
    client.close();
  }

  public static void search() throws IOException {
    RestHighLevelClient client =
        new RestHighLevelClient(RestClient.builder(new HttpHost("localhost", 9200, "http")));
    SearchRequest searchRequest = new SearchRequest("bank");
    SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
    sourceBuilder.query(QueryBuilders.termQuery("account_number", "49"));
    searchRequest.source(sourceBuilder);

    SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
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
    client.close();
  }

  public static void main(String[] args) throws IOException {
    matchAll();
    search();
  }
}
