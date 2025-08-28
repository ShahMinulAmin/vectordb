## Prerequisite
- JDK version 21
- Docker environment to run using docker compose command

## Run as a local java project
- Install PostgreSQL server and pgvector extension locally. The instruction for pgvector can be found [here](https://github.com/pgvector/pgvector?tab=readme-ov-file#linux-and-mac).
- Build the project
  ```
  mvn clean package -DskipTests
  ```
- A jar file `vectordb-0.0.1-SNAPSHOT.jar` will be generated in target directory
- Set the required environment variables DB_URL, DB_USER, DB_PASSWORD, OPENAI_API_KEY
- Set environment variable value of EMBEDDING_GENERATION_SKIP as true or false as needed
- Run with java command
  ```
  java -jar /....../spring-ai/vectordb/target/vectordb-0.0.1-SNAPSHOT.jar
  ```

## Run using docker compose command
- Set the required environment variable OPENAI_API_KEY in .env file
- Set environment variable value of EMBEDDING_GENERATION_SKIP as true or false as needed
- Docker compose command will build the docker image and start running as a stand-alone server
  ```
  docker-compose up
  ```  
