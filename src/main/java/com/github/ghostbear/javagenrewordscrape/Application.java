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
    for (Element row : rows) {
      Element genre = row.selectFirst("td:first-child a");
      Elements words = row.select("td:last-child");
      insert(genre, words);
    }
    driver.close();
  }

  public void insert(Element genre, Elements words) {
    String _genre = genre.text();
    String[] _words = words.text().split(" ");
    insertGenre(_genre, _words);
  }

  public void insertGenre(String genre, String[] words) {
    try (Session session = driver.session()) {
      Integer id = session.writeTransaction(tx -> {
        StatementResult statementResult = tx.run("MERGE (a:Genre {genre:$genre}) " +
                        "RETURN id(a)",
                parameters("genre", genre));
        return statementResult.single().get(0).asInt();
      });
      System.out.println(id + ": " + genre);
      insertWords(id, words);
    }
  }

  public void insertWords(Integer id, String[] words) {
    for (String word : words) {
      insertWord(id, word);
    }
  }

  public void insertWord(Integer id, String word) {
    try (Session session = driver.session()) {
      Integer _id = session.writeTransaction(tx -> {
        StatementResult statementResult = tx.run("MATCH (a:Genre) " +
                        "WHERE id(a)=$id " +
                        "MERGE (b:WORD {word:$word}) " +
                        "CREATE (a)-[:IN_WORD]->(b)-[:IN_GENRE]->(a) " +
                        "RETURN id(b)",
                parameters("id", id, "word", word));
        return statementResult.single().get(0).asInt();
      });
    }
  }
}
