package com.anjingwsno1.langchain4jknowledgebase.service;

import com.anjingwsno1.langchain4jknowledgebase.common.utils.CustomPgVectorEmbeddingStore;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiTokenizer;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.langchain4j.model.openai.OpenAiModelName.GPT_3_5_TURBO;

@Slf4j
public class RAGService {
    private static final int MAX_RESULTS = 3;
    private static final double MIN_SCORE = 0.6;

    private String dataBaseUrl;

    private String dataBaseUserName;

    private String dataBasePassword;

    private String tableName;

    private EmbeddingModel embeddingModel;

    private EmbeddingStore<TextSegment> embeddingStore;

    public RAGService(String tableName, String dataBaseUrl, String dataBaseUserName, String dataBasePassword) {
        this.tableName = tableName;
        this.dataBasePassword = dataBasePassword;
        this.dataBaseUserName = dataBaseUserName;
        this.dataBaseUrl = dataBaseUrl;
    }

    public void init() {
        log.info("initEmbeddingModel");
        embeddingModel = new AllMiniLmL6V2EmbeddingModel();
        embeddingStore = initEmbeddingStore();
    }

    private EmbeddingStore<TextSegment> initEmbeddingStore() {
        // 正则表达式匹配
        String regex = "jdbc:postgresql://([^:/]+):(\\d+)/(\\w+).+";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(dataBaseUrl);

        String host = "";
        String port = "";
        String databaseName = "";
        if (matcher.matches()) {
            host = matcher.group(1);
            port = matcher.group(2);
            databaseName = matcher.group(3);

            System.out.println("Host: " + host);
            System.out.println("Port: " + port);
            System.out.println("Database: " + databaseName);
        } else {
            throw new RuntimeException("parse url error");
        }
        CustomPgVectorEmbeddingStore embeddingStore = CustomPgVectorEmbeddingStore.builder()
                .host(host)
                .port(Integer.parseInt(port))
                .database(databaseName)
                .user(dataBaseUserName)
                .password(dataBasePassword)
                .dimension(384)
                .createTable(true)
                .dropTableFirst(false)
                .table(tableName)
                .build();
        return embeddingStore;
    }

    public void ingest(Document document) {
        DocumentSplitter documentSplitter = DocumentSplitters.recursive(1000, 0, new OpenAiTokenizer(GPT_3_5_TURBO));
        EmbeddingStoreIngestor embeddingStoreIngestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(documentSplitter)
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();
        embeddingStoreIngestor.ingest(document);
    }
}
