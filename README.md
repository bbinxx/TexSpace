# TexSpace

TexSpace is a fully-functional, cross-platform LaTeX IDE and PDF preview application built with [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html) and [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/). 
Designed to provide an Overleaf-like experience cleanly and natively on your devices, TexSpace supports multi-file projects, local LaTeX compilation, real-time PDF rendering, and flexible file management.

## Features

- **Cross-Platform**: Available on Android, iOS, Desktop (JVM), Web, and supported by a Kotlin Server backend.
- **Local LaTeX Compilation**: Compile your LaTeX documents right on your device for lightning-fast feedback without relying on a constant internet connection.
- **Integrated PDF Preview**: Instantly view the compiled PDF alongside your LaTeX source code.
- **Multi-File Support**: Handle complex documents effortlessly. Manage multiple `.tex` files, references, and images in a unified project structure.
- **Robust File Management**: Organize, create, delete, and structure your LaTeX workspace with a built-in file manager.

## Project Structure

* `composeApp`: Shared UI layout and components powered by Compose Multiplatform. Contains targets for Android, Desktop, Web, and iOS shared UI.
* `iosApp`: The iOS application entry point, containing Xcode project files and SwiftUI wrappers.
* `server`: The Ktor-based backend server application.
* `shared`: The core shared codebase containing business logic, state management, and the `TexSpaceRepository` database (powered by SQLDelight).

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

**Server**
```shell
./gradlew :server:run
```

**Web (Wasm / JS)**
```shell
./gradlew :composeApp:wasmJsBrowserDevelopmentRun
# OR for JS target
./gradlew :composeApp:jsBrowserDevelopmentRun
```

**iOS**
Open the `iosApp` folder in Xcode and launch it from there, or use the respective IDE run configuration if available.