# java-genrewords-scrape
This is a scraper made with OkHttp, Jsoup and Neo4j. 
OkHttp is used to fetch the page. 
Jsoup for parsing the HTML return by OkHttp.
And Neo4j for storing the data scraped by the previous two.
What you get is a database filled with genres relation to words, 
and words relation to genres.

## Setup project
The program needs four environment variables to be 
set `SCRAPE_URL`, `DATABASE_URL`, `DATABASE_USERNAME`, 
and `DATABASE_PASSWORD`.
- `SCRAPE_URL=<website_to_scrape>`
- `DATABASE_URL=<url_to_neo4j>`
- `DATABASE_USERNAME=<username_to_neo4j>`
- `DATABASE_PASSWORD=<password_to_neo4j>`

I use http://everynoise.com/genrewords.html as website to scrape.

## Build
Use `gradle build`.

## Run
Before running set environment variables!

Use `gradle run`.

Alternatively use `java -jar <path_to_jar>`.

## Build on this
There is room for expansion in this program. 
One can for example add:
- Add artist relation to genre
- Add genre similarity to other genres
