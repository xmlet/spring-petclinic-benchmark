# Spring PetClinic Benchmark

Compares the performance of three web architectural patterns using the [Spring PetClinic](https://github.com/spring-projects/spring-petclinic) sample application. Each implementation runs the same **Create Pet** operation, measured end-to-end with JMeter and Selenium WebDriver.

| Implementation | Port | Pattern |
|---|---|---|
| Petclinic-Thymeleaf | 8080 | MPA — full page reloads |
| Petclinic-HtmlFlow-DataStar | 8081 | Hypermedia SSE — partial DOM updates |
| Petclinic-React | 8082 (backend) / 4444 (frontend) | SPA — REST API + client-side rendering |

## Quick Start (Docker)

```bash
cd benchmark
./run-benchmark.sh --docker
```

Or step by step:

```bash
cd benchmark
docker compose build --no-cache
docker compose up -d
docker compose logs -f jmeter

# Results are extracted to benchmark/results/report_<timestamp>/
```

## Quick Start (Local)

```bash
# Build all implementations
./benchmark/build-all.sh

# Run benchmark (starts services, runs JMeter, stops services)
./benchmark/run-benchmark.sh --local
# or simply
./benchmark/run-benchmark.sh
```

Services are started automatically, benchmark runs, then everything shuts down.

## Prerequisites

### Local
- Java 21+
- JMeter 5.6.3+ with [WebDriver plugin](https://jmeter-plugins.org/plugins/install/) (`jpgc-webdriver`)
- Chrome/Chromium + ChromeDriver
- Maven 3.8+ (React backend)
- npm (React frontend)

### Docker
- Docker Compose v2+

## Benchmark

The benchmark runs three test types per implementation:

1. **Initial Load** — Selenium opens a real browser and waits for page to load. Measures first loading performance.
2. **Create Pet** — Selenium opens a real browser, fills the pet creation form, submits, and waits for the DOM to update. Measures end-to-end user-perceived performance.
3. **Find Owners** — Selenium opens a real browser, fills the search form, submits, and waits for the results table. Measures end-to-end search performance.

### Configuration

Edit the variables at the top of `benchmark/jmeter/run-jmeter.sh`:

```bash
TEST_THREADS=1                # concurrent browsers
TEST_LOOPS=250                # test loops per thread
WARMUP_LOOPS=10               # warm-up iterations (discarded from results)
```

### Benchmark Phases

1. **Warm-up** — Runs `WARMUP_LOOPS` iterations to warm JVM caches, JIT compilation, and connection pools. Results are discarded. Output is captured to `warmup.log`.
2. **Main Benchmark** — Runs `TEST_LOOPS` iterations with results saved to JTL and stdout/stderr captured to `jmeter.log`.

### Network Constraints (`--tc`)

Optionally simulate a realistic wide-area network with:

```bash
./run-benchmark.sh --docker --tc 
./run-benchmark.sh --local --tc 
```
These require sudo.

Uses `iptables` to mark HTTP traffic to/from ports 8080-8082 and 4444 with a firewall mark, then `tc` (Traffic Control) `netem` on the loopback interface routes marked packets through a delay/loss qdisc. Chromedriver traffic (random ephemeral ports) is unmarked and bypasses the constraint.

Default parameters: **600ms delay ±100ms jitter, 5% packet loss** — configured in `benchmark/tc/setup-tc.sh` (arguments: `DELAY`, `JITTER`, `LOSS`).

Teardown with `benchmark/tc/teardown-tc.sh` (also runs automatically on Ctrl+C).

### Results

Results are saved to `benchmark/results/report_YYYYMMDD_HHMMSS/`:

| File | Description |
|---|---|
| `results.jtl` | Raw JMeter results |
| `jmeter.log` | JMeter main benchmark log |
| `warmup.log` | Warm-up phase log |
| `report/index.html` | HTML report |
| `server_logs/*.log` | Per-service server logs |

## Docker Architecture

Uses `network_mode: "host"` so all containers share the host network. This avoids cross-container networking issues: the browser (inside the JMeter container) accesses all servers via `localhost`, and the React frontend's API calls to `localhost:8082` reach the backend without any proxy configuration.

| Container | Base Image | Build Tool | Ports |
|---|---|---|---|
| `petclinic-thymeleaf` | `eclipse-temurin:17` | Gradle `bootJar` | 8080 |
| `petclinic-htmlflow-datastar` | `eclipse-temurin:21` | Gradle `bootJar` | 8081 |
| `petclinic-react-backend` | `eclipse-temurin:17` | Maven `package` | 8082 |
| `petclinic-react-frontend` | `node:20` | `npm install --legacy-peer-deps` | 4444 |
| `jmeter` | `debian:bookworm-slim` | — (JMeter binary + Plugins Manager) | — |

### Services

**petclinic-thymeleaf** — Multi-stage build: JDK 17 builds the Gradle project into a JAR, then runs with JRE 17 on port 8080.

**petclinic-htmlflow-datastar** — Same pattern as thymeleaf but with JDK/JRE 21, runs on port 8081.

**petclinic-react-backend** — Multi-stage build: JDK 17 builds the Maven project into a JAR, then runs with JRE 17 on port 8082.

**petclinic-react-frontend** — Node.js image installs npm dependencies, runs webpack dev server on port 4444.

**jmeter** — Single-stage:
1. Installs Chromium + ChromeDriver for WebDriver browser tests
2. Downloads Apache JMeter 5.6.3
3. Installs WebDriver plugin and Custom Samplers via Plugins Manager
4. Entrypoint waits for all four services to be reachable, then runs JMeter (always headless)

## Project Structure

```
├── petclinic-thymeleaf/                   # Thymeleaf MPA (Kotlin, Gradle)
├── petclinic-htmlflow-datastar/           # HtmlFlow-DataStar SSE (Kotlin, Gradle)
├── petclinic-react/                       # React SPA (Java + TypeScript, Maven + npm)
├── .dockerignore                          # Docker build context exclusions
├── .gitattributes                         # LF line endings for cross-platform Docker builds
├── benchmark/
│   ├── docker/
│   │   ├── Dockerfiles/
│   │   │   ├── Dockerfile.thymeleaf
│   │   │   ├── Dockerfile.htmlflow-datastar
│   │   │   ├── Dockerfile.react-backend
│   │   │   ├── Dockerfile.react-frontend
│   │   │   └── Dockerfile.jmeter
│   │   └── entrypoint-jmeter.sh
│   ├── docker-compose.yml
│   ├── jmeter/
│   │   ├── petclinic_create_pet_webdriver.jmx
│   │   └── run-jmeter.sh
│   ├── webdriver-samples/
│   │   ├── initial_load/
│   │   └── create_pet/
│   ├── lib/
│   │   └── common.sh                      # Shared functions (wait_for, colors, print helpers)
│   ├── tc/
│   │   ├── setup-tc.sh                    # iptables + tc network constraint setup
│   │   └── teardown-tc.sh                 # network constraint teardown
│   ├── build-all.sh
│   └── run-benchmark.sh                   # Entry point (--docker / --local / --tc)
```
