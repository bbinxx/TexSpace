# TexSpace

<<<<<<< HEAD
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
=======
TexSpace is a fully-functional, cross-platform LaTeX IDE and PDF preview application built with [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html) and [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/). 
Designed to provide an Overleaf-like experience cleanly and natively on your devices, TexSpace supports multi-file projects, local LaTeX compilation, real-time PDF rendering, and flexible file management.

## Features

- **Cross-Platform**: Available on Android, iOS, Desktop (JVM), Web, and supported by a Kotlin Server backend.
- **Local LaTeX Compilation**: Compile your LaTeX documents right on your device for lightning-fast feedback without relying on a constant internet connection.
- **Integrated PDF Preview**: Instantly view the compiled PDF alongside your LaTeX source code.
- **Multi-File Support**: Handle complex documents effortlessly. Manage multiple `.tex` files, references, and images in a unified project structure.
- **Robust File Management**: Organize, create, delete, and structure your LaTeX workspace with a built-in file manager.

## Project Structure

* `composeApp`: Shared UI layout and components powered by Compose Multiplatform.
* `iosApp`: The iOS application entry point.
* `shared`: The core shared codebase containing business logic, file-based project repository, and settings.

## Getting Started

### Prerequisites

- JDK 17 or higher
- Android Studio or IntelliJ IDEA (with Kotlin Multiplatform plugins enabled)
- Xcode (for iOS development, macOS only)
- A local LaTeX distribution (like TeX Live, MacTeX, or MiKTeX) installed and available in your system path for local rendering.

### Building & Running

You can run the application for various targets using the provided Gradle wrapper from your terminal or via your IDE's run configurations.

**Android**
```shell
./gradlew :composeApp:assembleDebug
```

**Desktop (JVM)**
```shell
./gradlew :composeApp:run
```

**Web (JS)**
```shell
./gradlew :composeApp:jsBrowserDevelopmentRun
```

**iOS**
Open the `iosApp` folder in Xcode and launch it from there.

## Powered by TexCompiler

TexSpace is proud to be powered by [TexCompiler](https://github.com/bbinxx/TexCompiler), a high-performance LaTeX compilation API. By offloading the heavy lifting of TeX processing to a dedicated backend, TexSpace remains lightweight and incredibly fast on all platforms.

### Key Backend Features:
- **Instant Compilation**: Optimized for rapid feedback loops.
- **Multi-File Support**: Seamlessly handles complex project structures.
- **Render-Hosted**: Reliable default service at `https://texcompiler.onrender.com`.

## Open Source & Contributions

TexSpace is fully open-source! We welcome contributions, bug reports, and feature requests. Our goal is to create the best mobile and desktop LaTeX experience for everyone.
>>>>>>> dev
