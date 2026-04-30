# QEats

QEats is a **restaurant discovery backend** built with Spring Boot and MongoDB. Given a user's latitude and longitude, it returns nearby restaurants that are currently open.

## Core Business Rules

- **Serving radius is time-dependent:**
  - **Peak hours** (8-10 AM, 1-2 PM, 7-9 PM) -> **3 KM** radius
  - **Off-peak** -> **5 KM** radius
- Only **currently open** restaurants are returned
- Distance is computed using the **Haversine formula**

## Execution Flow

```
GET /qeats/v1/restaurants?latitude=X&longitude=Y
|
+-- RestaurantController
|   +-- Validates lat (-90..90) and lng (-180..180)
|   +-- Calls RestaurantService.findAllRestaurantsCloseBy(request, currentTime)
|
+-- RestaurantServiceImpl
|   +-- Checks isPeakHour(currentTime) -> picks 3 KM or 5 KM radius
|   +-- Calls RestaurantRepositoryService.findAllRestaurantsCloseBy(lat, lng, currentTime, radius)
|
+-- RestaurantRepositoryServiceImpl
|   +-- Fetches ALL restaurants from MongoDB via RestaurantRepository.findAll()
|   +-- Filters by distance (GeoUtils.findDistanceInKm <= radius)
|   +-- Filters by open status (opensAt <= now < closesAt)
|   +-- Maps RestaurantEntity -> Restaurant DTO (ModelMapper)
|   +-- Returns List<Restaurant>
|
+-- RestaurantRepository (MongoRepository<RestaurantEntity, String>)
|   +-- Hits MongoDB "restaurants" collection
|
+-- Response: GetRestaurantsResponse { List<Restaurant> }
```

## Key Entities


| Layer        | Class                             | Role                                                                |
| ------------ | --------------------------------- | ------------------------------------------------------------------- |
| Controller   | `RestaurantController`            | REST endpoint, input validation                                     |
| Service      | `RestaurantServiceImpl`           | Peak-hour logic, radius selection                                   |
| Repo Service | `RestaurantRepositoryServiceImpl` | Distance/time filtering, entity-to-DTO mapping                      |
| Repository   | `RestaurantRepository`            | Spring Data MongoDB CRUD                                            |
| Model        | `RestaurantEntity`                | MongoDB document (id, name, lat/lng, opensAt, closesAt, attributes) |
| DTO          | `Restaurant`                      | API response object                                                 |
| Exchange     | `GetRestaurantsRequest/Response`  | Request params / response wrapper                                   |


## Technologies & Modules Used

### Core Framework

- **Spring Boot 2.1.4** — auto-configuration, dependency injection, application startup
- **Spring MVC (REST)** — `@RestController`, `@GetMapping`, request/response handling
- **Spring Data MongoDB** — `MongoRepository`, entity mapping with `@Document`
- **Spring Boot Actuator** — application health and monitoring endpoints
- **Spring Boot DevTools** — live reload during development

### Database

- **MongoDB** — NoSQL document database for storing restaurant data

### Architecture & Design Patterns

- **Layered Architecture** — Controller -> Service -> RepositoryService -> Repository
- **DTO Pattern** — `Restaurant` (DTO) vs `RestaurantEntity` (DB model) to decouple API from DB schema
- **Request/Response Objects (Exchanges)** — `GetRestaurantsRequest`, `GetRestaurantsResponse`
- **Dependency Injection** — `@Autowired`, loose coupling between layers
- **Separation of Concerns** — each layer has a single responsibility

### Libraries

- **ModelMapper 2.3.2** — automatic entity-to-DTO conversion
- **Lombok 1.18.4** — `@Data`, `@NoArgsConstructor`, reduces boilerplate code
- **Jackson 2.9.8** — JSON serialization/deserialization
- **Swagger (Springfox 2.9.2)** — API documentation and try-it-out UI
- **Log4j2 2.16.0** — structured logging to file
- **GeoHash 1.3.0** — geographic hashing for location-based operations

### Key Algorithms

- **Haversine Formula** — calculating distance between two lat/lng points on Earth
- **Peak Hour Logic** — dynamic business rules that change serving radius based on time of day

### Testing

- **JUnit 5 (5.3.1)** — unit testing framework
- **Mockito 2.22.0** — mocking dependencies in unit tests

### Code Quality

- **Checkstyle 7.8.1** — code style enforcement
- **PMD 6.10.0** — static code analysis

### Build Tool

- **Gradle** — build automation and dependency management

