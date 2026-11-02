# Architecture decisions

## ADR-001: Docker Hub for the container registry

**Context:** The existing project already publishes to Docker Hub, while GitHub Container Registry would couple image publishing to GitHub permissions and packages.

**Decision:** Keep Docker Hub for this iteration and use immutable commit-SHA tags for deployment. `latest` is published only as a convenience tag; deployments never use it.

**Consequences:** CI requires a Docker Hub username and access token. The EC2 host must be able to pull the repository; private repositories need Docker Hub authentication on the host. GHCR remains a possible later simplification.

## ADR-002: SSH deployment rather than AWS Systems Manager

**Context:** The current deployment model uses SSH and the project has no AWS OIDC role or SSM setup.

**Decision:** Keep SSH to limit scope, pass credentials only to the deploy action, and restrict EC2 SSH ingress to a trusted source CIDR.

**Consequences:** A GitHub-hosted runner has changing outbound IPs, so a fixed personal CIDR will not allow the hosted runner to deploy. The owner must use a controlled static-egress/self-hosted runner or redesign deployment around SSM before enabling the pipeline. Do not solve this by opening SSH to the internet. SSH remains a portfolio/demo trade-off, not a production recommendation.

## ADR-003: Stop-start deployment with rollback instead of blue/green

**Context:** The application is a single container on one EC2 instance with no load balancer.

**Decision:** Replace the running container, check database-backed readiness, and attempt to restore the previous image if the new one fails.

**Consequences:** The service has a downtime window during replacement and health-check retries. Rollback is automatic only when a previous container image is recorded and that image remains pullable. Blue/green needs additional infrastructure and is out of scope.

## ADR-004: Testcontainers with MySQL instead of H2

**Context:** The application depends on MySQL-specific schema details including foreign keys, `DECIMAL`, and a unique cart/product constraint.

**Decision:** Use unit tests with Mockito for fast logic checks and a MySQL 8.4 Testcontainers integration suite for repository/database behavior.

**Consequences:** The integration tests require a Docker-capable test environment. They are configured to skip when Docker is unavailable; a local green run without Docker does not verify persistence integration.

## ADR-005: Readiness includes the database

**Context:** An HTTP process that cannot connect to its required database cannot serve the shopping cart.

**Decision:** The readiness health group includes Spring's readiness state and the database health indicator; liveness remains independent of database availability.

**Consequences:** A bad DB URL or credentials prevent readiness and cause the deployment script to attempt rollback. Database outages also remove the app from ready state.

## ADR-006: Calibrated JaCoCo coverage gate

**Context:** The original project had only a disabled test. A coverage gate should be measured from meaningful tests, not selected as an unsupported CV metric.

**Decision:** Add service/controller unit tests and MySQL integration tests, exclude plain JPA entities and the application bootstrap class, measure 98.46% line coverage for the remaining code in the local run, and set a 95% minimum (the measured whole-number percentage minus three points).

**Consequences:** The unit-test run measured coverage after the documented exclusions and the configured gate passes. The percentage is a CI quality floor, not proof of completeness; integration tests and mutation checks provide complementary evidence. Recalibrate if the measured coverage changes materially.

## ADR-007: Constrain EC2 egress and document the HTTPS exception

**Context:** Docker Hub image pulls and Amazon Linux package updates require outbound HTTPS, while an all-protocol/all-port egress rule is unnecessarily broad. Docker Hub/CDN destination addresses are not a stable small allowlist for a beginner single-host deployment.

**Decision:** Allow outbound TCP/443 for image pulls and updates, restrict MySQL egress to a required configured IPv4 CIDR and port, and suppress Trivy's AWS-0104 finding for the remaining HTTPS-to-anywhere rule with a checked-in, documented exception.

**Consequences:** The host still has broad HTTPS reachability; this is a deliberate demo trade-off, not a fully locked-down egress design. Revisit it with VPC endpoints or controlled egress before production use.
