# BPMNFlow Spring Boot Demo

> A runnable Spring Boot application that demonstrates the [bpmnflow-spring-boot-starter](https://github.com/jefersonferr/bpmnflow-spring-boot-starter) — parse any BPMN model at startup, navigate it at runtime, and hot-swap it without restarting.

![Java](https://img.shields.io/badge/Java-17-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.x-brightgreen)
![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)
![CI](https://github.com/jefersonferr/bpmnflow-spring-boot-demo/actions/workflows/ci.yml/badge.svg)

---

## Table of Contents

- [What this demo shows](#what-this-demo-shows)
- [Prerequisites](#prerequisites)
- [Running the application](#running-the-application)
- [API endpoints](#api-endpoints)
- [Project structure](#project-structure)
- [How it works](#how-it-works)

---

## What this demo shows

This demo illustrates the core value proposition of BPMNFlow: **a Spring Boot application that navigates workflow transitions without any hardcoded business logic**. All routing decisions come from the active BPMN model at runtime.

Specifically it demonstrates:

- Zero-config startup — the BPMN model and config are loaded automatically via the starter
- `ProcessController` — generic navigation endpoints that work with any active BPMN model
- Hot-swap — upload a new `.bpmn` file at runtime via `POST /bpmnflow/model` and all endpoints immediately reflect the new model
- Swagger UI with full OpenAPI documentation

---

## Prerequisites

- Java 17+
- Maven 3.8+
- Access to GitHub Packages (the starter and core are published there)

### GitHub Packages authentication

Add the following to `~/.m2/settings.xml`, replacing `YOUR_TOKEN` with a GitHub Personal Access Token that has `read:packages` scope:

```xml
<settings>
  <servers>
    <server>
      <id>github-starter</id>
      <username>YOUR_GITHUB_USERNAME</username>
      <password>YOUR_TOKEN</password>
    </server>
    <server>
      <id>github-core</id>
      <username>YOUR_GITHUB_USERNAME</username>
      <password>YOUR_TOKEN</password>
    </server>
  </servers>
</settings>
```

---

## Running the application

```bash
git clone https://github.com/jefersonferr/bpmnflow-spring-boot-demo.git
cd bpmnflow-spring-boot-demo
mvn spring-boot:run
```

The application starts on port `8080`. Swagger UI is available at:

```
http://localhost:8080/swagger-ui.html
```

---

## API endpoints

### Process (generic navigation)

| Method | Path | Description |
|---|---|---|
| `POST` | `/process/open?caseId={id}` | Opens a new process instance — entry activity and status resolved from the active BPMN model |
| `GET` | `/process/transition?from={abbreviation}` | Returns all possible next steps from a given activity |
| `GET` | `/process/guide` | Returns a full process guide — every activity and its outgoing transitions |

### Workflow inspection (from the starter)

| Method | Path | Description |
|---|---|---|
| `POST` | `/bpmnflow/model` | Upload a new `.bpmn` file to replace the active model at runtime |
| `GET` | `/bpmnflow/info` | Workflow metadata: name, version, type, health summary |
| `GET` | `/bpmnflow/validate` | Validation result and list of inconsistencies |
| `GET` | `/bpmnflow/activities` | All activities in the workflow |
| `GET` | `/bpmnflow/activities/{abbreviation}` | Single activity by abbreviation |
| `GET` | `/bpmnflow/activities/{abbreviation}/next` | Outgoing transitions from a given activity |
| `GET` | `/bpmnflow/stages` | All stages declared in the workflow lanes |
| `GET` | `/bpmnflow/rules` | All workflow rules (transitions) |
| `GET` | `/bpmnflow/rules/by-status?status={status}` | Rules triggered by a given process status |

---

## Project structure

```
src/main/
├── java/org/bpmnflow/demo/
│   ├── DemoApplication.java       — Spring Boot entry point
│   ├── SwaggerConfig.java         — OpenAPI / Swagger UI configuration
│   └── ProcessController.java     — Generic process navigation endpoints
└── resources/
    ├── application.yaml           — Server config and bpmnflow properties
    ├── bpmn-config.yaml           — Validation/extraction rules for the BPMN parser
    └── process.bpmn               — Default BPMN model loaded at startup
```

---

## How it works

The starter auto-configures a `WorkflowEngine` bean by parsing `process.bpmn` against `bpmn-config.yaml` at startup. The `ProcessController` injects `AtomicReference<WorkflowEngine>` — the same shared reference managed by the starter — so every request always resolves to the currently active engine.

**Open a process instance** — finds all `START_TO_TASK` rules in the active model and resolves the entry activity and initial status directly from the `StartEvent`, without requiring any input from the caller.

**Resolve transitions** — calls `nextSteps(abbreviation)` to get every outgoing path from the current activity, including the conclusion code, resulting process status, and target activity.

**Generate a process guide** — iterates all activities and maps each one to its available exits — useful for populating UI flows or feeding a decision engine without any hardcoded logic.

**Hot-swap the model** — upload any `.bpmn` file via `POST /bpmnflow/model` and all three `ProcessController` endpoints immediately reflect the new model without restarting the application.

In a production system these endpoints would be backed by a database of case instances. Here they are stateless to keep the demo self-contained and focused on the BPMNFlow integration.

---

## License

MIT — see [LICENSE](LICENSE).