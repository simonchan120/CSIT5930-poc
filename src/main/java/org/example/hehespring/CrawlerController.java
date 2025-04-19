package org.example.hehespring;

import org.htmlparser.util.ParserException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

@RestController
@RequestMapping("/crawler")
public class CrawlerController {

    private final Crawler crawler;

    @Autowired
    public CrawlerController(Crawler crawler) {
        this.crawler = crawler;
    }

    @PostMapping("/start")
    public String startCrawling(@RequestParam("url") String urlString,
                                @RequestParam("totalPages") int totalPages,
                                RedirectAttributes redirectAttributes) {
        try {
            URL url = new URL(urlString);
            this.crawler.start(url, totalPages);
            return "Crawling process started successfully.";
        } catch (MalformedURLException e) {
            return "Invalid URL: " + e.getMessage();
        } catch (IOException | ParserException e) {
            return "Error during crawling: " + e.getMessage();
        }
    }
}