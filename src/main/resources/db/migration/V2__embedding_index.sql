
CREATE INDEX IF NOT EXISTS quotes_embedding_idx
    ON quotes USING hnsw (embedding vector_cosine_ops);