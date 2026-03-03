package shah.dev.vectordb.quote;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.IntStream;

@Component
public class QuoteGenerator implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(QuoteGenerator.class);

    private final JdbcClient jdbcClient;

    private final EmbeddingModel embeddingModel;

    @Value("${embedding.generation.skip}")
    Boolean skipEmbeddingGeneration;

    public QuoteGenerator(JdbcClient jdbcClient, EmbeddingModel embeddingModel) {
        this.jdbcClient = jdbcClient;
        this.embeddingModel = embeddingModel;
    }

    public void generate() {
        SqlRowSet rowSet = jdbcClient.sql("select * from quotes ").query().rowSet();

        while (rowSet.next()) {
            long id = rowSet.getLong("id");
            String quote = rowSet.getString("quote");
            String person = rowSet.getString("person");

            // Prepare text to generate embedding
            String text = "\"" + person + "\"" + " told \"" + quote + "\"";
            log.info(text);

            // Generate embedding using embedding model 'text-embedding-ada-002' of 'gpt-4'
            EmbeddingRequest request = new EmbeddingRequest(List.of(text), null);
            EmbeddingResponse embeddingResponse = embeddingModel.call(request);
            log.info("embeddingResponse: " + embeddingResponse);

            float[] outputArray = embeddingResponse.getResult().getOutput();
            List<Double> textEmbeddings = IntStream.range(0, outputArray.length)
                    .mapToDouble(i -> outputArray[i]).boxed().toList();
            log.info("textEmbeddings: " + textEmbeddings);

            jdbcClient.sql("update quotes set embedding = :embedding::vector where id = :id")
                    .param("id", id)
                    .param("embedding", textEmbeddings.toString())
                    .update();
            log.info("updated embedding for " + id);
        }
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Embedding generation is skipped: " + skipEmbeddingGeneration);
        if (!skipEmbeddingGeneration) {
            generate();
        }
    }

}
