# TexSpace

TexSpace is a comprehensive **Kotlin Multiplatform** application that serves as a **full LaTeX editor and PDF preview application**, similar to Overleaf. It supports Desktop (JVM), Web (WASM/JS), Android, and iOS.

## Project Overview

With TexSpace, users can:
- Write LaTeX code with a dedicated editor.
- Compile the document utilizing a backend Ktor server with Dockerized TeXLive sandbox.
- Preview the generated PDF synchronously.
- See compilation logs and parsed errors directly in the IDE-style UI.
- Organize projects via the integrated file tree.

## Architecture

The project follows a standard Kotlin Multiplatform (KMP) architecture:
- **`composeApp/`**: Contains the Compose Multiplatform UI code. Features a 4-pane layout (File Tree, Editor, PDF Preview, Logs). Shared Kotlin logic defines the UI models and workflows.
- **`shared/`**: Houses the Ktor API client for communicating with the backend, and shared data models `CompileRequest` and `CompileResponse` serialized via `kotlinx.serialization`.
- **`server/`**: A Ktor backend REST API. Evaluates incoming LaTeX using docker running a `texlive` image, returning standard logs and rendered PDF inside `CompileResponse`.
- **`docker/`**: Contains the `Dockerfile` specifying the `texlive-full` environment.

## Setup Instructions

### Backend Compilation Sandbox
The application requires Docker installed locally to execute LaTeX without installing it entirely onto your local host.
To set up the image:
```bash
cd docker
docker build -t texlive/texlive:latest .
```
(Alternatively, if you already have a `texlive` or pdflatex installation, you could tweak the server logic to invoke it directly).

### Running the Ktor Backend
The backend runs on port 8080 by default. From the root directory:
```bash
./gradlew server:run
```

### Running the Frontend
You can launch the front end depending on your target system:

#### Desktop (JVM)
```bash
./gradlew composeApp:run
```

#### Web (Browser via JS/WASM)
Depending on the exact target defined via gradle (JS or WASM), you can typically run:
```bash
./gradlew composeApp:wasmJsBrowserDevelopmentRun
```
Or for standard JS:
```bash
./gradlew composeApp:jsBrowserDevelopmentRun
```

#### Android
Open the project in Android Studio, select the `composeApp` run configuration, and deploy to an emulator or physical device.

#### iOS
Open `iosApp/iosApp.xcworkspace` in Xcode and hit the Run button to build into the iOS Simulator.

## Features Currently Implemented
- Shared Kotlin Multiplatform dependencies injected (Serialization, Coroutines, Ktor Client integration).
- Real-time `LatexClient` integration triggering Debounced automatic re-compiles.
- Complete Ktor backend `POST /compile` utilizing isolated Docker TeX compilations with line-error parsing.
- Multiplatform-compatible Compose split panels with fallback text editors and PDF preview components. Keyboard shortcuts (`Ctrl+S`, `Ctrl+Enter`) for core events. 

*Further requested enhancements such as Monaco Webviews, WebSocket collaboration, and persistent SQLite project trees can be easily integrated onto these robust foundation blocks.*