# Dogs API Application

A Spring Boot REST API application for managing dog records with full CRUD operations, built with Java 17 and H2 database using JDBC connectivity.

## Features

- **Dog Management**: Create, read, update, and delete dog records
- **Filter Results**: Filter dogs by name, breed, or supplier
- **Soft Delete**: Dogs are marked as deleted rather than permanently removed
- **H2 Database**: File-based H2 database with direct JDBC connectivity
- **Status Validation**: Enforced status values for data integrity
- **MapStruct Integration**: Automatic mapping between DTOs and entities
- **Testing**: Unit tests using TestNG and Mockito

## Dog Data Model

Each dog record contains the following information:
- **Basic Info**: ID, name, breed, supplier, badge ID, gender
- **Dates**: Birth date, date acquired, leaving date
- **Predefined Values**: 'status' and 'leaving reason' have predefined acceptable values (listed below)
- **Care**: Kenneling characteristics
- **System**: Date deleted (for soft deletes)

### Field Validation

The status field is required and must be one of the following values:
- `"in training"` - Dog is currently in training
- `"in service"` - Dog is actively working/serving
- `"retired"` - Dog has been retired from service (leaving reason can be provided)
- `"left"` - Dog has left the service

The leaving reason field is optional but, if provided, must be one of:
- `"transferred"` - Dog has been transferred to another service or organization
- `"retired (put down)"` - Dog was retired due to euthanasia for health or welfare reasons
- `"kia"` - Dog was killed in action during service
- `"retired (re-homed)"` - Dog was retired and adopted into a new home
- `"died"` - Dog passed away due to natural causes or illness

## Getting Started

### Prerequisites

- **Java 17** or higher
- **Maven 3.6+**
- **Git** (for cloning the repository)

### Installation & Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/mickeyatwork/dogs.git
   cd dogs
   ```

2. **Build the application**
   ```bash
   mvn clean compile
   ```

3. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

   Or alternatively:
   ```bash
   mvn clean package
   java -jar target/dogs-0.0.1-SNAPSHOT.jar
   ```
   
## API Endpoints

All endpoints are prefixed with `/api/dogs`

### Core Dog Operations

| Method | Endpoint          | Description | Example |
|--------|-------------------|-------------|---------|
| `GET` | `/`               | Get all active dogs | `GET /api/dogs` |
| `GET` | `/?filter={term}` | Search dogs by name, breed, or supplier | `GET /api/dogs?filter=labrador` |
| `GET` | `/{id}`           | Get dog by ID | `GET /api/dogs/1` |
| `POST` | `/`               | Create new dog | `POST /api/dogs` |
| `PUT` | `/{id}`           | Update existing dog | `PUT /api/dogs/1` |
| `DELETE` | `/{id}`           | Soft delete dog | `DELETE /api/dogs/1` |

### Additional Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/dogs` | Get all dogs (including deleted) |

### Example API Calls

**Create a new dog:**
```bash
curl -X POST http://localhost:8080/api/dogs \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Buddy",
    "breed": "Golden Retriever",
    "supplier": "Local Breeder",
    "gender": "Male",
    "badgeID": 12345,
    "status": "in training"
  }'
```

**Get all dogs with filtering:**
```bash
curl http://localhost:8080/api/dogs?filter=retriever
```

**Update a dog:**
```bash
curl -X PUT http://localhost:8080/api/dogs/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Updated Buddy",
    "breed": "Golden Retriever",
    "status": "in service"
  }'
```

**Delete a dog:**
```bash
curl -X DELETE http://localhost:8080/api/dogs/1
```

## Architecture

### Project Structure
```
src/main/java/com/test/dogs/
├── DogsApplication.java          # Main Spring Boot application
└── api/
    └── rest/
        ├── endpoint/
        │   └── DogEndpoint.java      # REST controller
        ├── exception/
        │   ├── DogNotFoundException.java
        │   ├── DogServiceException.java
        │   ├── DogValidationException.java
        │   └── GlobalExceptionHandler.java
        ├── mapper/
        │   └── DogMapper.java        # MapStruct mapper interface
        ├── model/
        │   ├── DogDTO.java           # Data Transfer Object
        │   ├── DogEntity.java        # JPA Entity
        │   └── ErrorResponse.java    # Error response model
        └── service/
            └── DogService.java       # Business logic layer
```

### Key Technologies
- **Spring Boot 3.5.6** - Application framework
- **Spring Web** - REST API support
- **H2 Database** - Embedded database
- **MapStruct 1.6.0.Beta1** - DTO/Entity mapping
- **Maven** - Build and dependency management

Key service methods:
- `getAllDogs(String filter)` - Retrieves active dogs with optional filtering
- `getAllDogsIncludingDeleted()` - Retrieves all dogs including soft-deleted ones
- `getDogById(int id)` - Retrieves a specific dog by ID
- `saveNewDog(DogDTO dogDto)` - Creates a new dog with validation
- `updateDog(int id, DogDTO dogDto)` - Updates existing dog record
- `markDogAsDeleted(int id)` - Soft deletes a dog record

### Validation Rules

The application enforces the following validation rules:
- **Name**: Required, cannot be null or empty
- **Breed**: Required, cannot be null or empty  
- **Status**: Required, must be one of: "in training", "in service", "retired", "left"
- **Leaving Reason**: Required, must be one of: "transferred", "retired (put down)", "kia", "retired (re-homed)","died"

## Development Notes

### Soft Delete Implementation
Dogs are not permanently deleted from the database. Instead, the `dateDeleted` field is set, and the default queries filter out deleted records. 

### MapStruct Code Generation
The project uses MapStruct for automatic DTO-Entity mapping. Generated mapper implementations are created at compile time in the `target/generated-sources/annotations/` directory.

## Troubleshooting

### Common Issues

1. **Port 8080 already in use**
   - Add `server.port=8081` to application.properties
   - Or kill the process using port 8080

2. **Maven build issues**
   - Ensure Java 17 is installed and properly configured
   - Clear Maven cache: `./mvnw clean`

## License

This project is a test application for managing Dog records through a RESTful API.
