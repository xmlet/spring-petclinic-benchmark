# Spring PetClinic Benchmark

Compares the performance of three web architectural patterns using the [Spring PetClinic](https://github.com/spring-projects/spring-petclinic) sample application. Each implementation runs the same **Create Pet** operation, measured end-to-end with JMeter and Selenium WebDriver.

| Implementation | Port | Pattern |
|---|---|---|
| Petclinic-Thymeleaf | 8080 | MPA — full page reloads |
| Petclinic-HtmlFlow-DataStar | 8081 | Hypermedia SSE — partial DOM updates |
| Petclinic-React | 8082 (backend) / 4444 (frontend) | SPA — REST API + client-side rendering |

## Quick Start

```bash
# Build all implementations
./benchmark/build-all.sh

# Start all services (Ctrl+C to stop)
./benchmark/start-all.sh

# Run benchmark
./benchmark/petclinic_benchmark_runner.sh --headless
```

## Prerequisites

- Java 21+
- JMeter 5.6.3+ with [WebDriver plugin](https://jmeter-plugins.org/plugins/install/) (`jpgc-webdriver`)
- Chrome/Chromium + ChromeDriver
- Maven 3.8+ (React backend)
- npm (React frontend)

## Benchmark

The benchmark runs two test types per implementation:

1. **Initial Load** — Selenium opens a real browser and waits for page to load. Measures first loading performance.
2. **WebDriver Full Roundtrip** — Selenium opens a real browser, fills the pet creation form, submits, and waits for the DOM to update. Measures end-to-end user-perceived performance.

### Configuration

Edit the variables at the top of `benchmark/petclinic_benchmark_runner.sh`:

```bash
TEST_THREADS=1                # concurrent browsers
TEST_LOOPS=250                # test loops per thread
WARMUP_LOOPS=10               # warm-up iterations (discarded from results)
```

### Benchmark Phases

1. **Warm-up** — Runs `WARMUP_LOOPS` iterations to warm JVM caches, JIT compilation, and connection pools. Results are discarded.
2. **Main Benchmark** — Runs `TEST_LOOPS` iterations with results saved to JTL.

### Results

Results are saved to `benchmark/results/report_YYYYMMDD_HHMMSS/`:

| File | Description |
|---|---|
| `benchmark/results/report_.../results.jtl` | Raw JMeter results |
| `benchmark/results/report_.../jmeter.log` | JMeter log |
| `benchmark/results/report_.../warmup.log` | Warm-up phase log |
| `benchmark/results/report_.../report/index.html` | HTML report (with `--headless`) |

## Project Structure

```
├── petclinic-thymeleaf/           # Thymeleaf MPA (Kotlin, Gradle)
├── petclinic-htmlflow-datastar/   # HtmlFlow-DataStar SSE (Kotlin, Gradle)
├── petclinic-react/               # React SPA (Java + TypeScript, Maven + npm)
└── benchmark/
    ├── jmeter/                    # JMeter test plan
    ├── scripts/                   # Selenium Groovy scripts
    ├── build-all.sh
    ├── start-all.sh
    └── petclinic_benchmark_runner.sh
```
