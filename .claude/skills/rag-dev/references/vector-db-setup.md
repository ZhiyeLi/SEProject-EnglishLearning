# Vector Database Quickstart

## ChromaDB (Recommended for Prototyping)

### Docker Setup

```bash
docker run -d --name chromadb \
  -p 8000:8000 \
  -v chroma_data:/chroma/chroma \
  -e IS_PERSISTENT=TRUE \
  -e ANONYMIZED_TELEMETRY=FALSE \
  chromadb/chroma:latest
```

### Spring Boot Client

```xml
<!-- pom.xml -->
<dependency>
    <groupId>io.github.amikos-tech</groupId>
    <artifactId>chromadb-java-client</artifactId>
    <version>0.1.6</version>
</dependency>
```

### Node.js Client (for Express server)

```bash
npm install chromadb chromadb-default-embed
```

---

## pgvector (If Adding PostgreSQL)

### Docker Setup

```bash
docker run -d --name pgvector \
  -p 5432:5432 \
  -e POSTGRES_PASSWORD=postgres \
  -e POSTGRES_DB=vectors \
  pgvector/pgvector:pg16
```

### Spring Boot Dependencies

```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
</dependency>
<!-- Hibernate 6.3+ has pgvector support via hibernate-spatial -->
<dependency>
    <groupId>org.hibernate.orm</groupId>
    <artifactId>hibernate-spatial</artifactId>
</dependency>
```

### Entity Example

```java
@Entity
@Table(name = "document_vectors")
public class DocumentVector {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "vector(1536)")  // OpenAI embedding dim
    private float[] embedding;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String sourceType;
    private Long sourceId;
}
```

---

## Milvus (Production Scale)

### Docker Setup

```bash
curl -sfL https://raw.githubusercontent.com/milvus-io/milvus/master/scripts/standalone_embed.sh -o standalone_embed.sh
bash standalone_embed.sh start
```

### Spring Boot Client

```xml
<dependency>
    <groupId>io.milvus</groupId>
    <artifactId>milvus-sdk-java</artifactId>
    <version>2.4.1</version>
</dependency>
```
