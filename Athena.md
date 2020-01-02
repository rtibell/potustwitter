## Athena sql commands

`SELECT tweetid,created,text FROM "glue-potustweets"."twitter" order by created desc;`

`SELECT tweetid,created,text FROM "glue-potustweets"."twitter" order by created desc;`

`SELECT count(*) FROM "glue-potustweets"."twitter" ;`

`SELECT min(created),max(created) FROM "glue-potustweets"."twitter" ;`

`SELECT date(created),tweetid,text FROM "glue-potustweets"."twitter" order by tweetid desc;`

