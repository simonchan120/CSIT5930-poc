package org.example.hehespring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/indexer")
public class IndexerController {

    private final Indexer indexer;

    @Autowired
    public IndexerController(Indexer indexer) {
        this.indexer = indexer;
    }

    @PostMapping("/start")
    public String startIndexing() {
        try {
            indexer.start();
            return "Indexing process started successfully.";
        } catch (Exception e) {
            return "Error during indexing: " + e.getMessage();
        }
    }
}