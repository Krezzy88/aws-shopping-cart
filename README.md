# RE Shopping Cart

A containerized Spring Boot shopping-cart demo with a GitHub Actions pipeline for building and deploying to an AWS EC2 instance.

## What it does

- Displays products loaded from MySQL.
- Adds products to a cart, changes quantities, removes items, and clears the cart.
- Calculates the cart total.
- Uses Spring MVC, Thymeleaf, Spring Data JPA, and MySQL.

This is a demo application, not a production-ready commerce platform. The home page is wired to the sample cart with ID `1`; there is no authentication, user-specific cart selection, payment processing, or inventory management.

## Architecture

```text
Browser
  -> EC2 instance (Docker, port 8080)
       -> Spring Boot application (Java 21)
            -> External MySQL database

GitHub Actions (push to main)
  -> Gradle tests
  -> Docker image -> Docker Hub
  -> SSH -> EC2 pulls and starts the image
```

The database is external to EC2. The current workflow does not provision AWS resources, create a database, or configure networking; those must be set up separately.

## Technology

- Java 21 and Spring Boot
- Gradle
- MySQL and Spring Data JPA
- Thymeleaf
- Docker
- GitHub Actions, Docker Hub, and AWS EC2

## Run locally

Prerequisites: Java 21, Docker (for the container option), and a reachable MySQL database.

1. Create a MySQL database and load the demo schema and sample data from [`src/main/resources/db/re_shopping_cart_db.sql`](src/main/resources/db/re_shopping_cart_db.sql). Run this script once against an empty database.
2. Copy the environment template to the file for the profile you will use:

   ```sh
   cp .env.example .env.local
   ```

3. Set `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` in `.env.local`. `DB_URL` must be a JDBC URL for the database, for example `jdbc:mysql://<host>:3306/defaultdb`; use your database provider's required TLS parameters.
4. Start the app:

   ```sh
   SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
   ```

   Visit [http://localhost:8080](http://localhost:8080).

The application loads `.env.<profile>` (for example, `.env.local`) at startup. Environment variables and JVM system properties take precedence over values in that file. Do not commit populated environment files.

### Run the container locally

With `.env.local` populated and the database reachable from Docker:

```sh
docker build -t re-shopping-cart-web-app .
docker run --rm --name re-shopping-cart-web-app \
  --env-file .env.local \
  -p 8080:8080 \
  re-shopping-cart-web-app
```

The container listens on port `8080`. It runs as a non-root user and expects the database to be provided externally.

## AWS EC2 deployment

The workflow in [`.github/workflows/ci-cd.yaml`](.github/workflows/ci-cd.yaml) runs on pushes to `main`. It tests the project, builds and pushes a Docker image to Docker Hub, then connects to EC2 over SSH to replace the running container. The deployed image is tagged with the commit SHA (and `latest` for convenience).

### Prerequisites

- An EC2 Linux instance with Docker installed, enough memory for the JVM, and access to the MySQL server.
- A Docker Hub repository named `re-shopping-cart-web-app` under the configured Docker Hub username. The EC2 host must be able to pull it; for a private repository, configure Docker Hub authentication on EC2.
- A MySQL database with the schema and sample data loaded. The database firewall must allow connections from the EC2 instance.
- Security-group/network rules for the required traffic. Restrict SSH to trusted sources. Because GitHub-hosted runner IPs can change, consider a self-hosted runner or AWS Systems Manager rather than broadly exposing SSH. Expose port `8080` only as appropriate for your demo.
- GitHub Actions secrets configured for the repository:

| Secret | Purpose |
| --- | --- |
| `DOCKER_USER` | Docker Hub account/repository namespace |
| `DOCKER_TOKEN` | Docker Hub access token used to push images |
| `EC2_HOST` | EC2 public DNS name or IP |
| `EC2_USER` | SSH user on EC2 |
| `EC2_KEY` | SSH private key accepted by EC2 |
| `DB_URL` | MySQL JDBC connection URL |
| `DB_USERNAME` | MySQL username |
| `DB_PASSWORD` | MySQL password |

Keep credentials in GitHub Actions secrets, not in the repository or image. The workflow currently publishes to Docker Hub and deploys by SSH; it does not use CodeDeploy, ECR, RDS provisioning, Terraform, a load balancer, or a zero-downtime strategy.

## CI/CD and configuration notes

- Java 21 is used by Gradle and the Docker build.
- The app requires `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` at runtime.
- The default server port is `8080`; set `SERVER_PORT` to override it.
- Optional database pool and template settings are documented in [`.env.example`](.env.example).
- The existing Spring context test is disabled, so a green test step does not currently represent active application test coverage. Add tests that can run without a live database (or configure a disposable test database) before relying on CI as a quality gate.
- Deployment replaces the container on a single EC2 host, so a restart can cause downtime. There is no automated health check or rollback yet.

## Resume description

Use only claims that match what you have actually configured and successfully run. For example:

> **AWS Shopping Cart Deployment** — Containerized a Java 21 / Spring Boot web application with Docker and implemented a GitHub Actions pipeline to build, publish versioned images to Docker Hub, and deploy to AWS EC2 over SSH. Configured runtime database credentials through GitHub Actions secrets and connected the application to MySQL.

If you have not yet completed a successful deployment, describe the pipeline as implemented/configured rather than deployed. Do not describe this project as production-grade or claim AWS services that are not in use.
