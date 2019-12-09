package com.github.ghostbear.javagenrewordscrape;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.neo4j.driver.v1.*;

import static org.neo4j.driver.v1.Values.parameters;

public class Application {

  public static void main(String[] args) {
    new Application();
  }

  public Application() {
    init();
  }

  public void init() {
    // BUG: All genres doesn't get loaded. Could probably be fixed with OkHttp
    String _url = System.getenv("SCRAPE_URL");
    if (_url == null) {
      System.out.println("Couldn't find SCRAPE_URL in env");
      System.exit(-1);
    }

    Elements scrape = scrape(_url);
    if (scrape == null) {
      System.out.println("Couldn't scrape website");
      System.exit(-2);
    }
    System.out.println(scrape.html());

    String url = System.getenv("DATABASE_URL");
    String username = System.getenv("DATABASE_USERNAME");
    String password = System.getenv("DATABASE_PASSWORD");
    if (url == null || username == null || password == null) {
      System.out.println("Couldn't find DATABASE_URL, DATABASE_USERNAME, or DATABASE_PASSWORD in env");
      System.exit(-3);
    }

    connectDatabase(url, username, password);
    fillDatabase(scrape);
  }

  public Elements scrape(String url) {
    try {
      Connection connect = Jsoup.connect(url);
      Connection.Response execute = connect.execute();
      Document parse = execute.parse();
      Elements tableRows = parse.select("table tr");
      return tableRows;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  private Driver driver;

  public void connectDatabase(String url, String username, String password) {
    driver = GraphDatabase.driver(url, AuthTokens.basic(username, password));
  }

  public void fillDatabase(Elements rows) {
    try (Session session = driver.session()) {
      for (Element row : rows) {
        Element genre = row.selectFirst("td:first-child a");
        String textGenre = genre.text();
        Elements words = row.select("td:last-child");
        String[] textWords = words.text().split(" ");
        for (String textWord : textWords) {
          session.writeTransaction(new TransactionWork<String>() {
            @Override
            public String execute(Transaction tx) {

              StatementResult statementResult = tx.run("MERGE (a:Genre {genre:$genre}) " +
                              "MERGE (b:Word {word:$word}) " +
                              "CREATE (b)-[:IN_GENRE]->(a)-[:IN_WORD]->(b) RETURN a.genre",
                      parameters("genre", textGenre, "word", textWord));
              System.out.println(statementResult.single().get(0).asString());

              return "null";
            }
          });
        }
      }
    }
  }
}
