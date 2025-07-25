# Developer Guide

This document provides instructions for developers on how to build, run, and manage the Jtrac application and its services.

## Table of Contents

1.  [Prerequisites](#prerequisites)
2.  [Running with Docker Compose (Recommended)](#running-with-docker-compose-recommended)
    *   [Starting All Services](#starting-all-services)
    *   [Stopping All Services](#stopping-all-services)
    *   [Restarting a Service](#restarting-a-service)
    *   [Running the Seeder Script](#running-the-seeder-script)
    *   [Viewing Logs](#viewing-logs)
3.  [Running Services Individually](#running-services-individually)
    *   [Running with Docker](#running-with-docker)
    *   [Running Locally (Without Docker)](#running-locally-without-docker)
4.  [Building the Project](#building-the-project)

---

### Prerequisites

-   Java 17+
-   Docker and Docker Compose
-   Python 3.x (for running the seeder script locally)

---

### Running with Docker Compose (Recommended)

Using Docker Compose is the easiest and most reliable way to run the entire application stack.

#### Starting All Services

This command will build the images, start the PostgreSQL database, the Jtrac backend, and then run the database seeder script.

```bash
docker-compose up --build -d
```

-   `--build`: Forces a rebuild of the images to include any code changes.
-   `-d`: Runs the containers in detached (background) mode.

Your application will be available at `http://localhost:8082`.

#### Stopping All Services

To stop all running containers:

```bash
docker-compose stop
```

#### Restarting a Service

If you've made changes to the backend code, you need to rebuild and restart the service.

```bash
docker-compose up --build -d jtrac-backend
```

#### Running the Seeder Script

The seeder script runs automatically on the first start. If you need to run it again (e.g., after clearing the database), you can use the `run` command. This command runs the seeder as a one-off task and automatically removes the container when it's done.

```bash
docker-compose run --rm seeder
```

#### Viewing Logs

To view the logs for all running services:

```bash
docker-compose logs -f
```

To view the logs for a specific service (e.g., the backend):

```bash
docker-compose logs -f jtrac-backend
```

---

### Running Services Individually

#### Running with Docker

**1. Start the Database:**

```bash
docker-compose up -d db
```

**2. Build and Run the Jtrac Backend:**

```bash
# Build the image
docker build -t jtrac-backend .

# Run the container, connecting to the Docker network
docker run --rm -d --name jtrac-app --network=jtrac-src-github_default -p 8082:8082 -e SPRING_PROFILES_ACTIVE=docker jtrac-backend
```
*Note: Replace `jtrac-src-github_default` with the actual network name created by Docker Compose if it differs (`docker network ls`).*

**3. Run the Seeder Script:**

```bash
# Build the seeder image
docker build -t jtrac-seeder -f seeder.Dockerfile .

# Run the seeder, connecting to the Docker network
docker run --rm --network=jtrac-src-github_default -e BACKEND_URL=http://jtrac-app:8082 jtrac-seeder
```

#### Running Locally (Without Docker)

**1. Start the Database (using Docker):**

It's often easiest to still run the database in Docker.

```bash
docker-compose up -d db
```

**2. Run the Jtrac Backend:**

The application is configured to use the `dev` profile by default, which connects to an in-memory H2 database. To connect to the PostgreSQL database running in Docker, you need to activate the `docker` profile.

```bash
# Make sure you have built the project at least once
./gradlew build

# Run the application with the 'docker' profile
SPRING_PROFILES_ACTIVE=docker ./gradlew bootRun
```

**3. Run the Seeder Script:**

The script needs the `requests` library. It's best to use a virtual environment.

```bash
# Create virtual environment and install dependencies
python3 -m venv .venv
source .venv/bin/activate
pip install requests

# Run the script (it will connect to http://localhost:8082 by default)
python seed.py

# Deactivate the virtual environment
deactivate
```

---

### Building the Project

To build the project and create the executable JAR file without running it:

```bash
./gradlew build
```

The JAR file will be located in `build/libs/`.
