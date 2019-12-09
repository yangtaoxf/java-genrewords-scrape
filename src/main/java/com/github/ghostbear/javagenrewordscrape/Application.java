package com.github.ghostbear.javagenrewordscrape;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Application {

  public static void main(String[] args) {
    new Application();
  }

  public Application() {
    init();
  }

  public void init() {
    // 1. Scrape
    //  - Genre: table tr td:first-child-child a
    //  - Words: table tr td:last-child a
    // 2. Add to database
    String url = System.getenv("SCRAPE_URL");
    if (url == null) {
      System.out.println("Couldn't find SCRAPE_URL in env");
      System.exit(-1);
    }
    Elements scrape = scrape(url);
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

  public void fillDatabase() {
    // TODO
  }
}
