import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class WebCrawler {
    private Set<String> crawledURLs;
    private Set<String> indexedURLs;

    public WebCrawler() {
        crawledURLs = new HashSet<>();
        indexedURLs = new HashSet<>();
    }

    public void crawl(String path) {
        // Step 4: Check if the path has already been crawled and indexed
        if (crawledURLs.contains(path) || indexedURLs.contains(path)) {
            return;
        }

        // Step 4 (i): Add the path to the index
        indexedURLs.add(path);
        System.out.println(path);

        try {
            // Step 2: Read the file or fetch the HTML code
            Document document;
            if (isURL(path)) {
                document = Jsoup.connect(path).get();
            } else {
                BufferedReader reader = new BufferedReader(new FileReader(path));
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] urls = line.split(" ");
                    for (String url : urls) {
                        crawl(url);
                    }
                }
                reader.close();
                return; // Skip parsing and crawling for local files
            }

            // Step 3: Parse the HTML to extract links to other URLs
            Elements linksOnPage = document.select("a[href]");

            // Step 5: For each extracted URL, go back to Step 4
            for (Element link : linksOnPage) {
                String extractedURL = link.absUrl("href");
                crawl(extractedURL);
            }
        } catch (IOException e) {
            System.err.println("Failed to crawl: " + path);
            e.printStackTrace();
        }

        // Step 4: Add the crawled path to the set
        crawledURLs.add(path);
    }

    private boolean isURL(String path) {
        try {
            new URL(path);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
