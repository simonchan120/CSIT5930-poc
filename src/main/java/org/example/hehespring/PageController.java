package org.example.hehespring;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {
    @GetMapping("/home")
    public String homePage() {
        return "home";
    }

    @GetMapping("/crawler")
    public String crawlerPage() {
        return "crawler";
    }

    @GetMapping("/indexer")
    public String indexerPage() {
        return "indexer";
    }

    @GetMapping("/retriever")
    public String retrieverPage() {
        return "retriever";
    }
}