package com.laogongshuo.es;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.http.HttpHost;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.ShardSearchFailure;
import org.elasticsearch.action.support.replication.ReplicationResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.client.core.MainResponse;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.get.GetResult;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;

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

  public static void get() throws IOException {
    GetRequest getRequest = new GetRequest("bank", "1000");
    GetResponse getResponse = client.get(getRequest, RequestOptions.DEFAULT);
    String index = getResponse.getIndex();
    System.out.println(index);
    String id = getResponse.getId();
    System.out.println(id);
    if (getResponse.isExists()) {
      long version = getResponse.getVersion();
      System.out.println(version);
      String sourceAsString = getResponse.getSourceAsString();
      System.out.println(sourceAsString);
      Map<String, Object> sourceAsMap = getResponse.getSourceAsMap();
      System.out.println(sourceAsMap);
      byte[] sourceAsBytes = getResponse.getSourceAsBytes();
      String result = new String(sourceAsBytes);
      System.out.println(result);
    } else {
      System.out.println("not exist");
    }
  }

  public static void exist() throws IOException {
    GetRequest getRequest = new GetRequest("bank", "2000");
    getRequest.fetchSourceContext(new FetchSourceContext(false));
    getRequest.storedFields("_none_");
    boolean exists = client.exists(getRequest, RequestOptions.DEFAULT);
    System.out.println(exists);
  }

  public static void delete() throws IOException {
    DeleteRequest request = new DeleteRequest("bank", "1000");
    DeleteResponse deleteResponse = client.delete(request, RequestOptions.DEFAULT);
    String index = deleteResponse.getIndex();
    System.out.println(index);
    String id = deleteResponse.getId();
    System.out.println(id);
    long version = deleteResponse.getVersion();
    System.out.println(version);
    ReplicationResponse.ShardInfo shardInfo = deleteResponse.getShardInfo();
    if (shardInfo.getTotal() != shardInfo.getSuccessful()) {
      System.out.println("some thing wrong");
    }
    if (shardInfo.getFailed() > 0) {
      for (ReplicationResponse.ShardInfo.Failure failure : shardInfo.getFailures()) {
        String reason = failure.reason();
        System.out.println(reason);
      }
    }
  }

  public static void update() throws IOException {

    Map<String, Object> jsonMap = new HashMap<>();
    jsonMap.put("balance", "89999");
    jsonMap.put("city", "Shanghai");
    UpdateRequest request = new UpdateRequest("bank", "1000").doc(jsonMap);
    UpdateResponse updateResponse = client.update(request, RequestOptions.DEFAULT);
    String index = updateResponse.getIndex();
    System.out.println(index);
    String id = updateResponse.getId();
    System.out.println(id);
    long version = updateResponse.getVersion();
    System.out.println(version);
    if (updateResponse.getResult() == DocWriteResponse.Result.CREATED) {
      System.out.println("created");
    } else if (updateResponse.getResult() == DocWriteResponse.Result.UPDATED) {
      System.out.println("updated");
    } else if (updateResponse.getResult() == DocWriteResponse.Result.DELETED) {
      System.out.println("deleted");
    } else if (updateResponse.getResult() == DocWriteResponse.Result.NOOP) {
      System.out.println("noop");
    }
    GetResult result = updateResponse.getGetResult();
    if (result != null && result.isExists()) {
      String sourceAsString = result.sourceAsString();
      System.out.println(sourceAsString);
      Map<String, Object> sourceAsMap = result.sourceAsMap();
      System.out.println(sourceAsMap);
      byte[] sourceAsBytes = result.source();
      String source = new String(sourceAsBytes);
      System.out.println(source);
    } else {
      System.out.println("not exist");
    }
  }

  public static void bulk() throws IOException {
    BulkRequest request = new BulkRequest();
    request.add(new IndexRequest("posts").id("1").source(XContentType.JSON, "field", "foo"));
    request.add(new IndexRequest("posts").id("2").source(XContentType.JSON, "field", "bar"));
    request.add(new IndexRequest("posts").id("3").source(XContentType.JSON, "field", "baz"));
    request.add(new DeleteRequest("posts", "3"));
    request.add(new UpdateRequest("posts", "2").doc(XContentType.JSON, "other", "test"));
    request.add(new IndexRequest("posts").id("4").source(XContentType.JSON, "field", "baz"));
    request.add(new DeleteRequest("posts", "4"));
    request.add(new DeleteRequest("posts", "2"));
    request.add(new DeleteRequest("posts", "1"));
    BulkResponse bulkResponse = client.bulk(request, RequestOptions.DEFAULT);
    for (BulkItemResponse bulkItemResponse : bulkResponse) {
      DocWriteResponse itemResponse = bulkItemResponse.getResponse();
      switch (bulkItemResponse.getOpType()) {
        case INDEX:
        case CREATE:
          IndexResponse indexResponse = (IndexResponse) itemResponse;
          String index = indexResponse.getIndex();
          System.out.println(index);
          break;
        case UPDATE:
          UpdateResponse updateResponse = (UpdateResponse) itemResponse;
          String id = updateResponse.getId();
          System.out.println(id);
          break;
        case DELETE:
          DeleteResponse deleteResponse = (DeleteResponse) itemResponse;
          String id1 = deleteResponse.getId();
          System.out.println(id1);
      }
    }
  }

  public static void info() throws IOException {
    MainResponse response = client.info(RequestOptions.DEFAULT);
    String clusterName = response.getClusterName();
    System.out.println(clusterName);
    String clusterUuid = response.getClusterUuid();
    System.out.println(clusterUuid);
    String nodeName = response.getNodeName();
    System.out.println(nodeName);
    MainResponse.Version version = response.getVersion();
    String buildDate = version.getBuildDate();
    System.out.println(buildDate);
    String buildFlavor = version.getBuildFlavor();
    System.out.println(buildFlavor);
    String buildHash = version.getBuildHash();
    System.out.println(buildHash);
    String buildType = version.getBuildType();
    System.out.println(buildType);
    String luceneVersion = version.getLuceneVersion();
    System.out.println(luceneVersion);
    String minimumIndexCompatibilityVersion = version.getMinimumIndexCompatibilityVersion();
    System.out.println(minimumIndexCompatibilityVersion);
    String minimumWireCompatibilityVersion = version.getMinimumWireCompatibilityVersion();
    System.out.println(minimumWireCompatibilityVersion);
    String number = version.getNumber();
    System.out.println(number);
  }

  public static void main(String[] args) throws IOException {
    matchAll();
    search();
    count();
    index();
    get();
    exist();
    delete();
    update();
    bulk();
    info();
    client.close();
  }
}
