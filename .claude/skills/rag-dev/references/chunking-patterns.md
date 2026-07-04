# Chunking Patterns with Code Examples

## Word Definitions → Atomic Chunks

```java
// In IngestionService.java
public List<Chunk> chunkWord(Word word) {
    String text = String.format(
        "Word: %s\nPhonetic: %s\nDefinition: %s\nExample: %s\nPart of Speech: %s",
        word.getWord(),
        word.getPhonetic(),
        word.getDefinition(),
        word.getExampleSentence(),
        word.getPartOfSpeech()
    );
    return List.of(new Chunk(
        text,
        Map.of(
            "source_type", "word",
            "source_id", word.getId().toString(),
            "word", word.getWord(),
            "difficulty", word.getDifficulty(),
            "word_type", word.getWordType().getName()
        )
    ));
}
```

## Articles → Paragraph-Level Chunks

```java
public List<Chunk> chunkArticle(String articleText, String articleId, String title) {
    List<Chunk> chunks = new ArrayList<>();
    List<String> paragraphs = splitByParagraphs(articleText);

    StringBuilder buffer = new StringBuilder();
    int chunkIndex = 0;
    int currentLen = 0;

    for (String para : paragraphs) {
        if (currentLen + para.length() > 800 && currentLen > 0) {
            chunks.add(new Chunk(buffer.toString(), Map.of(
                "source_type", "article",
                "source_id", articleId,
                "title", title,
                "chunk_index", String.valueOf(chunkIndex++)
            )));
            buffer.setLength(0);
            currentLen = 0;
        }
        buffer.append(para).append("\n\n");
        currentLen += para.length();
    }
    // Last chunk
    if (buffer.length() > 0) {
        chunks.add(new Chunk(buffer.toString(), Map.of(
            "source_type", "article",
            "source_id", articleId,
            "title", title,
            "chunk_index", String.valueOf(chunkIndex)
        )));
    }
    return chunks;
}

private List<String> splitByParagraphs(String text) {
    return Arrays.stream(text.split("\n{2,}"))
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .collect(Collectors.toList());
}
```

## Exam Questions → Atomic Chunks with Rich Metadata

```java
public Chunk chunkQuestion(QuestionBank question) {
    String text = String.format(
        "Question: %s\nOptions:\n%s\nCorrect Answer: %s\nExplanation: %s",
        question.getQuestion(),
        question.getOptions().stream()
            .map(o -> "  " + o.getLabel() + ". " + o.getContent())
            .collect(Collectors.joining("\n")),
        question.getCorrectAnswer(),
        question.getExplanation()
    );
    return new Chunk(text, Map.of(
        "source_type", "question",
        "source_id", question.getId().toString(),
        "question_type", question.getType(),
        "difficulty", question.getDifficulty(),
        "tags", String.join(",", question.getTags())
    ));
}
```

## Chunk Interface

```java
// Common chunk representation
public record Chunk(
    String text,
    Map<String, String> metadata
) {}

// Vector DB entry
public record VectorEntry(
    String id,           // "source_type:source_id:chunk_index"
    float[] embedding,
    String text,
    Map<String, String> metadata
) {}
```

## Token-Aware Chunk Sizing

Target token counts, not character counts. Rule of thumb:

- 1 English word ≈ 1.3 tokens
- 1 Chinese character ≈ 1.5-2 tokens
- 800 chars mixed text ≈ 500-700 tokens

For a model with 8K context, budget:
- System prompt: ~200 tokens
- User query: ~100 tokens
- Retrieved context: ~3000 tokens (5-6 chunks at 500 tokens each)
- Response: ~1000 tokens
- Buffer: ~3700 tokens
