package org.example.hehespring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class DatabaseManagerConfig {

    @Bean
    public DatabaseManager invertedIndexTitleManager() throws IOException {
        return new DatabaseManager(DatabaseFiles.INVERTED_INDEX_TITLE);
    }

    @Bean
    public DatabaseManager invertedIndexContentManager() throws IOException {
        return new DatabaseManager(DatabaseFiles.INVERTED_INDEX_CONTENT);
    }

    @Bean
    public DatabaseManager webpageDbManager() throws IOException {
        return new DatabaseManager(DatabaseFiles.WEBPAGE_DB);
    }
}