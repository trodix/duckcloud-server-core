# Duckcloud Server

## Search API

### Elasticsearch Queries

Search for text content (full text search)

```
GET node/_search
{
  "query": {
    "multi_match": {
      "query": "KEEP COOL",
      "fields": ["cm:textContent"]
    }
  }
}
```

Search by properties

```
GET node/_search
{
  "query": {
    "bool": {
      "must": [
        {
          "match": {
            "properties.cm:name": "Install-Linux-tar.txt"
          }
        }
      ]
    }
  }
}
```
