# ProjectMind

ProjectMind is a comprehensive Personal Knowledge and Project Operations Platform built with Spring Boot. It provides a robust backend for managing workspaces, projects, tasks, notes, and AI-related assets like model registries and prompts.

## Features

- **Workspace Management**: Organize your work into distinct environments.
- **Project & Task Management**: Full-featured project tracking with task status and progress monitoring.
- **Knowledge Base**: Integrated note-taking system with tagging support.
- **AI Integration Support**:
    - **Model Registry**: Manage and track various AI models.
    - **Prompt Management**: Store and version prompts for AI interactions.
- **Security**: Robust JWT-based authentication and authorization.
- **Audit Logging**: Comprehensive tracking of system activities.
- **Real-time Performance**: Optimized with Redis caching.

## Tech Stack

- **Framework**: Spring Boot 3.4.1
- **Language**: Java 21
- **Database**: PostgreSQL
- **Migration**: Flyway
- **Caching**: Redis
- **Security**: Spring Security & JJWT
- **Mapping**: MapStruct
- **Documentation**: SpringDoc OpenAPI (Swagger)
- **Containerization**: Docker & Testcontainers

## Getting Started

### Prerequisites

- JDK 21
- Docker (for database and Redis)
- Maven

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/Justin21523/project-mind.git
   cd project-mind
   ```

2. Set up environment variables:
   Copy `.env.example` to `.env` and adjust the values as needed.

3. Build the project:
   ```bash
   ./mvnw clean install
   ```

4. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```

## API Documentation

Once the application is running, you can access the Swagger UI at:
`http://localhost:8080/swagger-ui.html`

## Testing

The project uses JUnit 5 and Testcontainers for integration testing.
Run tests using:
```bash
./mvnw test
```

## License

This project is for personal use and development.
