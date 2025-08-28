CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE IF NOT EXISTS "quotes" (
	id bigserial PRIMARY KEY,
	"quote" text NULL,
	person text NULL,
	embedding vector(1536) NULL
);

