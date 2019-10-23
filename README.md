# elasticsearch
Java High Level REST Client 7.4 for elasticsearch examples

昨天折腾了一下Elasticsearch, 今天把过程记录下来免得忘记。

首先当然是安装，我用的mac, 所以安装很简单:
```
brew tap elastic/tap
brew install elastic/tap/elasticsearch-full
elasticsearch
```
搞定。

然后就是按照[官方的教程](https://www.elastic.co/guide/en/elasticsearch/reference/current/index.html),
用curl把这几个页面上([index](https://www.elastic.co/guide/en/elasticsearch/reference/current/getting-started-index.html), 
[search](https://www.elastic.co/guide/en/elasticsearch/reference/current/getting-started-search.html), [aggregations](https://www.elastic.co/guide/en/elasticsearch/reference/current/getting-started-aggregations.html))的命令跑了一遍。
为了免得以后重新打命令，我把这个命令都放到一个[reference.sh](https://github.com/gongchengra/elasticsearch/blob/master/reference.sh)里面去了，想要跑哪一行，就把那个命令前面的#去掉，然后bash reference.sh就可以了。

最后还折腾了一下官方的Java REST Client [7.4](https://www.elastic.co/guide/en/elasticsearch/client/java-rest/7.4/java-rest-high.html), 其中有一个坑就是[Maven Repository](https://www.elastic.co/guide/en/elasticsearch/client/java-rest/7.4/java-rest-high-getting-started-maven.html)页面上写的是<version>7.4.1</version>，但是我在pom.xml里面用7.4.1死活编译不了，我还以为是因为我用了阿里云的maven源，里面没有更新，换成官方的源还是不行，我去官方的源上一看，到今天(2019-10-23)为止, [官方源](https://search.maven.org/search?q=g:org.elasticsearch.client)上最新的版本是7.4.0，所以上面应该写<version>7.4.0</version>.

其它的就没什么了，先搞个project:
```
mvn -B archetype:generate -DarchetypeGroupId=org.apache.maven.archetypes -DgroupId=com.laogongshuo.es -DartifactId=elasticsearch
cd elasticsearch
```
然后把依赖关系加到pom.xml里面去:
```
<properties>
	<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
	<maven.compiler.encoding>UTF-8</maven.compiler.encoding>
	<java.version>1.8</java.version>
	<maven.compiler.source>1.8</maven.compiler.source>
	<maven.compiler.target>1.8</maven.compiler.target>
</properties>
<dependencies>
	<dependency>
		<groupId>org.elasticsearch.client</groupId>
		<artifactId>elasticsearch-rest-high-level-client</artifactId>
		<version>7.4.0</version>
	</dependency>
</dependencies>
```
在src/main/java/com/laogongshuo/es/App.java里面撸一些代码:
```
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
```
最后编译运行：
```
mvn clean package
mvn exec:java -Dexec.mainClass="com.laogongshuo.es.App"
```
因为没有在pom.xml里面写执行的mainClass，所以只能这么跑，不过能看到结果就行了。

后续：
在pom.xml中加上下面一段:
```
<build>
	<plugins>
		<plugin>
			<artifactId>maven-assembly-plugin</artifactId>
			<configuration>
				<archive>
					<manifest>
						<mainClass>com.laogongshuo.es.App</mainClass>
					</manifest>
				</archive>
				<descriptorRefs>
					<descriptorRef>jar-with-dependencies</descriptorRef>
				</descriptorRefs>
			</configuration>
		</plugin>
	</plugins>
</build>
```
然后就可以java -jar运行了。
```
mvn clean compile assembly:single
java -jar target/elasticsearch-1.0-SNAPSHOT-jar-with-dependencies.jar
```
文中全部代码都在这里，[github](https://github.com/gongchengra/elasticsearch), 欢迎star.
