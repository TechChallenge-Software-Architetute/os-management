# os-management
Tech challenge - Phase 1


# Build Project Image
```
docker build -t os-management .
```
# Running the container and mapping ports

```
docker run -p 8080:8080 -td os-management
```
# Building and running the project at once
```
docker-compose up -d
```
This will build and create both postgres and project image. Mapping the os-management port to 8080 and postgres to 5432.

Connection URL: jdbc:postgresql://localhost:5432/workshop

### SonarQube

The Docker Compose file also starts SonarQube at http://localhost:9000.

Default login:

```
admin
admin
```

After the first login, create a user token in SonarQube and run the Maven analysis with coverage:

```
./mvnw clean verify sonar:sonar -Dsonar.token=<your-token>
```

JaCoCo generates the coverage XML at `target/site/jacoco/jacoco.xml`, and the Sonar Maven scanner sends it to SonarQube.

### Swagger

This project uses OpenAPI as documentation, there you can find all endpoints of the app.
To access Swagger UI and see the endpoints click here -> [Swagger](http://localhost:8080/swagger-ui/index.html#/).
