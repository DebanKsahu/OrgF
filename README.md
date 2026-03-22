# OrgF

**On-device AI file organizer for Android**

[![minSdk](https://img.shields.io/badge/minSdk-24-brightgreen)](https://developer.android.com/tools/releases/platforms)
[![targetSdk](https://img.shields.io/badge/targetSdk-36-blue)](https://developer.android.com/tools/releases/platforms)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.3.10-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Compose BOM](https://img.shields.io/badge/Compose%20BOM-2026.01.01-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/license-MIT-lightgrey)](LICENSE)

OrgF is an Android application that automatically organizes your files using **on-device AI**. All
ML inference — LLM generation, text embedding, and semantic clustering — runs entirely on the
device. No data ever leaves it.

---

## Table of Contents

- [Features](#features)
- [Architecture](#architecture)
   - [Data Flow](#data-flow)
   - [Key Components](#key-components)
   - [Hierarchical Clustering](#hierarchical-clustering)
- [Tech Stack](#tech-stack)
- [Prerequisites & Setup](#prerequisites--setup)
   - [1. Clone & open](#1-clone--open)
   - [2. Push the LLM model](#2-push-the-llm-model)
   - [3. Build & install](#3-build--install)
- [Build Commands](#build-commands)
- [Privacy](#privacy)

---

## Features

| Feature                       | Description                                                                                                                     |
|-------------------------------|---------------------------------------------------------------------------------------------------------------------------------|
| **Privacy-first AI**          | LLM inference and text embedding run fully on-device via MediaPipe. No telemetry, no cloud calls.                               |
| **Real-time file monitoring** | `FolderObserverService` uses Android's `FileObserver` API to react instantly to `MOVED_TO`, `CREATE`, and `CLOSE_WRITE` events. |
| **Semantic clustering**       | Files are embedded as high-dimensional vectors and grouped by cosine similarity into a 3-layer hierarchy.                       |
| **PDF text extraction**       | Digital PDFs are parsed with PDFBox; scanned/image PDFs fall back to ML Kit OCR automatically.                                  |
| **Concurrent processing**     | `AgentService` handles up to 4 file events in parallel using `flatMapMerge(concurrency = 4)`.                                   |
| **Persistent state**          | All categories, cluster centroids, and prompt metadata are stored in a Room database with BLOB vector columns.                  |

---

## Architecture

OrgF uses an **event-driven foreground-service** architecture. Two long-lived foreground services
communicate through a Koin-provided `SharedFlow` event bus (`ServiceState`).

### Data Flow

```
┌──────────────────────────┐        ┌─────────────────────┐        ┌──────────────────────────────┐
│  FolderObserverService   │        │    ServiceState      │        │       AgentService           │
│  (FileObserver API)      │──────► │  MutableSharedFlow   │──────► │  flatMapMerge(concurrency=4) │
│                          │        │  <NewFileEvent>       │        │  ├─ PdfTextExtractor         │
│  MOVED_TO / CREATE /     │        └─────────────────────┘        │  ├─ TextEmbedding            │
│  CLOSE_WRITE events      │                                        │  └─ PromptManager            │
└──────────────────────────┘                                        │       └─ AppDatabase (Room)  │
                                                                    └──────────────────────────────┘
```

### Key Components

| Component               | File                                        | Responsibility                                                                              |
|-------------------------|---------------------------------------------|---------------------------------------------------------------------------------------------|
| `FolderObserverService` | `core/filemanager/FolderObserverService.kt` | Foreground service; monitors a user-selected directory via `FileObserver`                   |
| `AgentService`          | `core/agent/AgentService.kt`                | Foreground service; consumes `NewFileEvent`s concurrently                                   |
| `ServiceState`          | `core/ServiceState.kt`                      | Koin singleton `MutableSharedFlow<NewFileEvent>` — the event bus between both services      |
| `PromptManager`         | `core/agent/prompt/PromptManager.kt`        | 3-layer hierarchical clustering with cosine-similarity threshold `0.5` on all layers        |
| `LlmInferences`         | `core/agent/LlmInferences.kt`               | Pre-built `LlmInferenceOptions` for Gemma 3 1B INT4 (small / medium / large token variants) |
| `TextEmbedding`         | `core/agent/tool/TextEmbedding.kt`          | MediaPipe text embedder; wraps Universal Sentence Encoder or Sentence Transformer           |
| `PdfTextExtractor`      | `core/agent/tool/PdfTextExtractor.kt`       | PDFBox for digital PDFs; ML Kit OCR fallback for scanned PDFs                               |
| `AppDatabase`           | `core/database/AppDatabase.kt`              | Room database with `PromptCategoryTable` and `PromptClusterTable`                           |

### Hierarchical Clustering

`PromptManager` organizes prompts and file metadata into a **3-layer tree** stored in
`PromptClusterTable`:

```
PromptCategory (e.g. DocumentType)
└── Layer 1 — top-level centroid (parentClusterId = null)
    └── Layer 2 — sub-cluster centroid (parentClusterId → Layer 1 id, no text field)
        └── Layer 3 — leaf node (parentClusterId → Layer 2 id, stores text + embedding)
```

- **Cosine-similarity threshold**: `0.5` at every layer.
- **Centroid updates**: Running-average via `calculateNewVectorEmbeddingCentroid()` (top-level
  function in `PromptManager.kt`).
- **Vector storage**: Little-endian `ByteBuffer` BLOB via `PromptClusterTableConverter`.

---

## Tech Stack

| Category             | Library / Tool                          | Version    |
|----------------------|-----------------------------------------|------------|
| Language             | Kotlin                                  | 2.3.10     |
| UI                   | Jetpack Compose BOM (Material 3)        | 2026.01.01 |
| Dependency Injection | Koin BOM                                | 4.1.1      |
| Database             | Room                                    | 2.8.4      |
| LLM Inference        | MediaPipe Tasks GenAI — Gemma 3 1B INT4 | 0.10.32    |
| Text Embedding       | MediaPipe Tasks Text                    | 0.10.32    |
| PDF Parsing          | PDFBox-Android                          | 2.0.27.0   |
| OCR                  | Google ML Kit Text Recognition          | 19.0.1     |
| Async                | kotlinx-coroutines-android              | 1.10.2     |
| Build tooling        | Android Gradle Plugin                   | 9.0.0      |

---

## Prerequisites & Setup

### 1. Clone & open

```bash
git clone https://github.com/orgf/OrgF.git
```

Open the project root in **Android Studio Meerkat (2024.3)** or later. All dependency versions are
managed via the version catalog at `gradle/libs.versions.toml` — no manual dependency resolution is
needed.

**Device requirements:**

| Requirement     | Minimum                                       |
|-----------------|-----------------------------------------------|
| Android API     | 24 (Android 7.0 Nougat)                       |
| Recommended API | 34+ (for `FOREGROUND_SERVICE_TYPE_DATA_SYNC`) |
| Free storage    | ~1.5 GB for the Gemma 3 1B INT4 model         |

### 2. Push the LLM model

The Gemma 3 1B INT4 `.task` file must be placed on the device before launching the app:

```bash
adb push gemma3-1b-it-int4.task /data/local/tmp/llm/gemma/gemma3-1b-it-int4.task
```

> Obtain the model from [Kaggle / Google AI](https://www.kaggle.com/models/google/gemma) and convert
> it to the MediaPipe `.task` format if necessary.

### 3. Build & install

```bash
# Build a debug APK
./gradlew assembleDebug

# Build and install directly to a connected device
./gradlew installDebug
```

---

## Build Commands

```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease

# Install debug APK to connected device
./gradlew installDebug

# Run unit tests
./gradlew test

# Run instrumented tests on connected device
./gradlew connectedAndroidTest

# Clean build artifacts
./gradlew clean
```


## Privacy

OrgF is built on the principle that **your file system is personal**.

- All AI inference (LLM + embeddings) runs **on-device**.
- No file content, metadata, or telemetry is ever transmitted to a remote server.
- The app does not require an internet permission for its core functionality.
