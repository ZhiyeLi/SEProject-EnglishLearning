# End-to-End: ChromaDB + Spring Boot for English Learning RAG

This example shows how to add RAG to the existing `AiChatService` by integrating ChromaDB for retrieval over the project's word bank and question bank.

## Step 1: Add Dependencies

```xml
<!-- pom.xml additions -->
<dependency>
    <groupId>io.github.amikos-tech</groupId>
    <artifactId>chromadb-java-client</artifactId>
    <version>0.1.6</version>
</dependency>
```

## Step 2: Configuration

```yaml
# application.yml additions
chromadb:
  url: http://localhost:8000
  collection-prefix: english_learning_
  embedding:
    provider: openai  # or 'local_bge'
    api-key: ${OPENAI_API_KEY}
    model: text-embedding-3-small
  retrieval:
    default-top-k: 5
    max-tokens-context: 3000
```

## Step 3: ChromaDB Client Config

```java
// config/ChromaDBConfig.java
package com.example.english_learning_platform.config;

import io.github.amikos.tech.chromadb.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChromaDBConfig {

    @Value("${chromadb.url}")
    private String chromaUrl;

    @Bean
    public ChromaClient chromaClient() {
        return ChromaClient.getInstance(
            new ChromaConfiguration(chromaUrl)
        );
    }
}
```

## Step 4: Embedding Service

```java
// service/EmbeddingService.java
package com.example.english_learning_platform.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.*;

@Service
public class EmbeddingService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${chromadb.embedding.api-key}")
    private String apiKey;

    @Value("${chromadb.embedding.model}")
    private String model;

    public float[] embed(String text) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String body = String.format(
            "{\"model\":\"%s\",\"input\":\"%s\"}",
            model, text.replace("\"", "\\\"")
        );

        ResponseEntity<Map> resp = restTemplate.postForEntity(
            "https://api.openai.com/v1/embeddings",
            new HttpEntity<>(body, headers),
            Map.class
        );

        List<Double> vec = (List<Double>)
            ((Map)((List)resp.getBody().get("data")).get(0)).get("embedding");

        float[] result = new float[vec.size()];
        for (int i = 0; i < vec.size(); i++) result[i] = vec.get(i).floatValue();
        return result;
    }

    public List<float[]> embedBatch(List<String> texts) {
        return texts.stream().map(this::embed).toList();
    }
}
```

## Step 5: Ingestion Service (Index Content)

```java
// service/IngestionService.java
package com.example.english_learning_platform.service;

import com.example.english_learning_platform.entity.*;
import com.example.english_learning_platform.repository.*;
import io.github.amikos.tech.chromadb.*;
import io.github.amikos.tech.chromadb.model.*;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class IngestionService {

    private final ChromaClient chromaClient;
    private final EmbeddingService embeddingService;
    private final WordRepository wordRepository;
    private final QuestionBankRepository questionBankRepository;

    public IngestionService(ChromaClient chromaClient,
                            EmbeddingService embeddingService,
                            WordRepository wordRepository,
                            QuestionBankRepository questionBankRepository) {
        this.chromaClient = chromaClient;
        this.embeddingService = embeddingService;
        this.wordRepository = wordRepository;
        this.questionBankRepository = questionBankRepository;
    }

    /** Index all words into the vector DB */
    public void indexAllWords() {
        // Create or get collection (1536 = OpenAI embedding dims, cosine distance)
        ChromaCollection collection = chromaClient.createCollection(
            new CreateCollectionParams("english_learning_words")
                .putMetadata("hnsw:space", "cosine")
        );

        List<Word> words = wordRepository.findAll();
        List<String> ids = new ArrayList<>();
        List<float[]> embeddings = new ArrayList<>();
        List<Map<String, String>> metadatas = new ArrayList<>();
        List<String> documents = new ArrayList<>();

        for (Word word : words) {
            String text = buildWordText(word);
            ids.add("word:" + word.getId());
            embeddings.add(embeddingService.embed(text));
            metadatas.add(Map.of(
                "source_type", "word",
                "word", word.getWord(),
                "word_type", word.getWordType() != null ? word.getWordType().getName() : ""
            ));
            documents.add(text);
        }

        // Batch upsert
        collection.upsert(
            new UpsertParams()
                .setIds(ids)
                .setEmbeddings(embeddings)
                .setMetadatas(metadatas)
                .setDocuments(documents)
        );
    }

    /** Index all questions into the vector DB */
    public void indexAllQuestions() {
        ChromaCollection collection = chromaClient.createCollection(
            new CreateCollectionParams("english_learning_questions")
                .putMetadata("hnsw:space", "cosine")
        );

        List<QuestionBank> questions = questionBankRepository.findAll();
        // ... similar batch upsert with question text
    }

    private String buildWordText(Word word) {
        return String.format(
            "Word: %s\nDefinition: %s\nExample: %s",
            word.getWord(),
            word.getDefinitionEn() != null ? word.getDefinitionEn() : word.getDefinitionZh(),
            word.getExampleSentence() != null ? word.getExampleSentence() : ""
        );
    }
}
```

## Step 6: Retrieval Service

```java
// service/RetrievalService.java
package com.example.english_learning_platform.service;

import io.github.amikos.tech.chromadb.*;
import io.github.amikos.tech.chromadb.model.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class RetrievalService {

    private final ChromaClient chromaClient;
    private final EmbeddingService embeddingService;

    @Value("${chromadb.retrieval.default-top-k}")
    private int defaultTopK;

    public RetrievalService(ChromaClient chromaClient, EmbeddingService embeddingService) {
        this.chromaClient = chromaClient;
        this.embeddingService = embeddingService;
    }

    public List<RetrievedChunk> search(String query, String collectionName, int topK) {
        ChromaCollection collection = chromaClient.getCollection(collectionName);
        float[] queryEmbedding = embeddingService.embed(query);

        QueryResponse results = collection.query(
            new QueryParams()
                .setQueryEmbeddings(List.of(queryEmbedding))
                .setNResults(topK)
                .setInclude(List.of("documents", "metadatas", "distances"))
        );

        List<RetrievedChunk> chunks = new ArrayList<>();
        for (int i = 0; i < results.getIds().get(0).size(); i++) {
            chunks.add(new RetrievedChunk(
                results.getIds().get(0).get(i),
                results.getDocuments().get(0).get(i),
                results.getMetadatas().get(0).get(i),
                1.0 - results.getDistances().get(0).get(i) // cosine distance → similarity
            ));
        }
        return chunks;
    }

    public List<RetrievedChunk> searchAllCollections(String query, int topK) {
        List<RetrievedChunk> allResults = new ArrayList<>();
        // Search across all relevant collections
        allResults.addAll(search(query, "english_learning_words", topK));
        allResults.addAll(search(query, "english_learning_questions", topK));
        // Sort by similarity, deduplicate, return topK
        allResults.sort((a, b) -> Double.compare(b.similarity(), a.similarity()));
        return allResults.stream().limit(topK).toList();
    }

    public record RetrievedChunk(
        String id,
        String text,
        Map<String, String> metadata,
        double similarity
    ) {}
}
```

## Step 7: Modify AiChatService to Use RAG

```java
// Modified AiChatService.java — inject retrieval into chat
@Service
public class AiChatService {

    private final RetrievalService retrievalService;
    // ... existing fields

    public String chatWithRag(Long userId, String userMessage) {
        // 1. Retrieve relevant context
        List<RetrievalService.RetrievedChunk> context =
            retrievalService.searchAllCollections(userMessage, 5);

        // 2. Build augmented prompt
        String contextText = context.stream()
            .map(c -> "[" + c.metadata().getOrDefault("source_type", "unknown")
                + "] " + c.text())
            .collect(Collectors.joining("\n\n"));

        String augmentedPrompt = buildPrompt(contextText, userMessage);

        // 3. Send to LLM
        return callLLM(augmentedPrompt);
    }

    private String buildPrompt(String context, String query) {
        return """
            You are an English learning assistant. Use the retrieved context below
            to answer the user's question accurately.

            If the context contains relevant information, use it. If not, say
            "I don't have enough information to answer that" and suggest what
            the user might search for.

            Retrieved context:
            %s

            User question: %s
            """.formatted(context, query);
    }

    // ... existing callLLM() method
}
```

## Step 8: Admin Endpoint to Trigger Re-indexing

```java
// controller/AdminController.java (or add to existing controller)
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final IngestionService ingestionService;

    @PostMapping("/reindex/words")
    public ResponseEntity<String> reindexWords() {
        ingestionService.indexAllWords();
        return ResponseEntity.ok("Words re-indexed successfully");
    }

    @PostMapping("/reindex/questions")
    public ResponseEntity<String> reindexQuestions() {
        ingestionService.indexAllQuestions();
        return ResponseEntity.ok("Questions re-indexed successfully");
    }

    @PostMapping("/reindex/all")
    public ResponseEntity<String> reindexAll() {
        ingestionService.indexAllWords();
        ingestionService.indexAllQuestions();
        return ResponseEntity.ok("All content re-indexed");
    }
}
```

## Testing the RAG Pipeline

```bash
# 1. Start ChromaDB
docker run -d -p 8000:8000 chromadb/chroma:latest

# 2. Start Spring Boot app
cd backend/english_learning_platform
mvn spring-boot:run

# 3. Index content
curl -X POST http://localhost:8080/api/admin/reindex/all

# 4. Test via AI Chat
curl -X POST http://localhost:8080/api/ai-chat/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "What does ubiquitous mean? Give me an example sentence."}'
```
