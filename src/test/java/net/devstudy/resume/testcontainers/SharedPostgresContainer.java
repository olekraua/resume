package net.devstudy.resume.testcontainers;

import org.testcontainers.containers.PostgreSQLContainer;

final class SharedPostgresContainer extends PostgreSQLContainer<SharedPostgresContainer> {

    private static final String IMAGE = "postgres:16-alpine";
    private static final SharedPostgresContainer INSTANCE = new SharedPostgresContainer();

    static {
        INSTANCE.start();
    }

    private SharedPostgresContainer() {
        super(IMAGE);
        withDatabaseName("resume");
        withUsername("resume");
        withPassword("resume");
    }

    static SharedPostgresContainer getInstance() {
        return INSTANCE;
    }

    @Override
    public void stop() {
        // Keep container running for the JVM; Ryuk cleans up on exit.
    }
}
