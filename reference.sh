#!/bin/bash
index_accounts(){
    curl -H "Content-Type: application/json" -XPOST "localhost:9200/bank/_bulk?pretty&refresh" --data-binary "@accounts.json"
}
#index_accounts
cat_indices(){
    curl "localhost:9200/_cat/indices?v"
}
#cat_indices
bank_search(){
    curl -X GET "localhost:9200/bank/_search?pretty" -H 'Content-Type: application/json' -d'
    {
        "query": { "match_all": {} },
        "sort": [
        { "account_number": "asc" }
        ]
    }
'
}
#bank_search
bank_search_from(){
	curl -X GET "localhost:9200/bank/_search?pretty" -H 'Content-Type: application/json' -d'
	{
		"query": { "match_all": {} },
		"sort": [
		{ "account_number": "asc" }
		],
		"from": 10,
		"size": 10
	}
'
}
#bank_search_from
bank_search_term(){
    curl -X GET "localhost:9200/bank/_search?pretty" -H 'Content-Type: application/json' -d'
    {
        "query": { "match": { "address": "mill lane" } }
    }
'
}
#bank_search_term
bank_search_match_phrase(){
    curl -X GET "localhost:9200/bank/_search?pretty" -H 'Content-Type: application/json' -d'
    {
        "query": { "match_phrase": { "address": "mill lane" } }
    }
'
}
#bank_search_match_phrase
bank_search_complex(){
    curl -X GET "localhost:9200/bank/_search?pretty" -H 'Content-Type: application/json' -d'
    {
        "query": {
            "bool": {
                "must": [
                    {
                        "match": {
                            "age": "40"
                        }
                    }
                ],
                "must_not": [
                    {
                        "match": {
                            "state": "ID"
                        }
                    }
                ]
            }
        }
    }
'
}
#bank_search_complex
bank_search_range(){
    curl -X GET "localhost:9200/bank/_search?pretty" -H 'Content-Type: application/json' -d'
	{
	  "query": {
		"bool": {
		  "must": { "match_all": {} },
		  "filter": {
			"range": {
			  "balance": {
				"gte": 20000,
				"lte": 30000
			  }
			}
		  }
		}
	  }
	}
'
}
#bank_search_range
bank_search_aggs(){
	curl -X GET "localhost:9200/bank/_search?pretty" -H 'Content-Type: application/json' -d'
	{
	  "size": 0,
	  "aggs": {
		"group_by_state": {
		  "terms": {
			"field": "state.keyword"
		  }
		}
	  }
	}
	'
}
#bank_search_aggs
bank_search_aggs_avg(){
	curl -X GET "localhost:9200/bank/_search?pretty" -H 'Content-Type: application/json' -d'
	{
	  "size": 0,
	  "aggs": {
		"group_by_state": {
		  "terms": {
			"field": "state.keyword"
		  },
		  "aggs": {
			"average_balance": {
			  "avg": {
				"field": "balance"
			  }
			}
		  }
		}
	  }
	}
	'
}
#bank_search_aggs_avg
bank_search_aggs_sort(){
curl -X GET "localhost:9200/bank/_search?pretty" -H 'Content-Type: application/json' -d'
	{
	  "size": 0,
	  "aggs": {
		"group_by_state": {
		  "terms": {
			"field": "state.keyword",
			"order": {
			  "average_balance": "desc"
			}
		  },
		  "aggs": {
			"average_balance": {
			  "avg": {
				"field": "balance"
			  }
			}
		  }
		}
	  }
	}
	'
}
#bank_search_aggs_sort
