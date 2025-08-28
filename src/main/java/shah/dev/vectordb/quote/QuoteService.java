package shah.dev.vectordb.quote;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.core.simple.JdbcClient.StatementSpec;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

@Service
public class QuoteService {
    private static final float THRESHOLD = 0.7f;
    private static final int LIMIT = 2;

    private final JdbcClient jdbcClient;

    private final EmbeddingModel embeddingModel;

    @Autowired
    public QuoteService(JdbcClient jdbcClient, EmbeddingModel embeddingModel) {
        this.jdbcClient = jdbcClient;
        this.embeddingModel = embeddingModel;
    }

    public List<Quote> searchQuotes(String prompt) {
        // Generate embedding of the prompt
        EmbeddingRequest request = new EmbeddingRequest(Collections.singletonList(prompt), null);
        EmbeddingResponse embeddingResponse = embeddingModel.call(request);

        float[] outputArray = embeddingResponse.getResult().getOutput();
        List<Double> promptEmbeddings = IntStream.range(0, outputArray.length)
                .mapToDouble(i -> outputArray[i]).boxed().toList();
        System.out.println("promptEmbeddings: " + promptEmbeddings);

        // Find cosine similarity, using (1 - cosine distance)
        String queryStr =
                "SELECT person, quote as quoteText, 1 - (embedding <=> :prompt_embedding::vector) as similarity " +
                        "FROM quotes WHERE 1 - (embedding <=> :prompt_embedding::vector) > :threshold " +
                        "ORDER BY embedding <=> :prompt_embedding::vector LIMIT :limit";
        StatementSpec query = jdbcClient.sql(queryStr)
                .param("prompt_embedding", promptEmbeddings.toString())
                .param("threshold", THRESHOLD)
                .param("limit", LIMIT);

        return query.query(Quote.class).list();
    }
}
