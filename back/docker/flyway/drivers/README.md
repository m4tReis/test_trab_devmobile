Place the Oracle JDBC driver used by the Flyway Docker container in this directory.

Expected file:

```text
ojdbc11.jar
```

The Spring Boot application keeps its own Oracle dependency through Gradle. This
directory is only for the Flyway container classpath.
