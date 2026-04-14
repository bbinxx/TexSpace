# TexSpace — Professional LaTeX IDE

TexSpace is a professional, cross-platform LaTeX IDE and PDF preview application built with **Kotlin Multiplatform** and **Compose Multiplatform**. It brings a desktop-grade LaTeX editing experience to Android, iOS, JVM, and Web, with a focus on local file management and rapid compilation.

![v1.2.0](https://img.shields.io/badge/version-1.2.0-green.svg)
![KMP](https://img.shields.io/badge/Kotlin-Multiplatform-blue.svg)
![Compose](https://img.shields.io/badge/Compose-Multiplatform-orange.svg)

## 🚀 Key Features

- **Cross-Platform**: Unified experience across Android, Desktop (JVM), Web, and iOS.
- **Local-First Project Management**: Projects are stored as standard folders on your device, making your documents portable and compatible with other tools.
- **Professional IDE Interface**:
    - **Sidebar Explorer**: Integrated file tree for managing `.tex` and asset files.
    - **Syntax Highlighting**: Real-time syntax highlighting for LaTeX commands, environments, and math mode.
    - **Live Preview**: Side-by-side (Desktop) or tabbed (Mobile) PDF preview with instant refresh.
- **Native Platform Integration**:
    - **Android**: Supports Scoped Storage and "All Files Access" (MANAGE_EXTERNAL_STORAGE) for power users.
    - **Desktop**: Native AWT file and folder pickers for a seamless OS experience.
- **Powered by TexCompiler**: High-performance LaTeX compilation with multi-file support and asset management.

## 📂 Project Structure

* `composeApp`: The main application module containing the UI logic and platform-specific panels.
* `shared`: Business logic, network client (`TexClient`), and the core `ProjectRepository` for file I/O.
* `iosApp`: Entry point for the iOS application.
* `run.sh`: Interactive shell utility for building and running the project on different targets.

## 🛠 Prerequisites

- **JDK 17** or higher.
- **Android Studio** or **IntelliJ IDEA**.
- **A Compile Server**: Defaults to the public `https://texcompiler.onrender.com`. You can change this in the app settings to use your own backend.

## 🏃 Running the Project

The easiest way to run TexSpace is using the included **TexSpace Runner Utility**:

```bash
chmod +x run.sh
./run.sh
```

Alternatively, use the standard Gradle commands:

- **Android**: `./gradlew :composeApp:installDebug`
- **Desktop**: `./gradlew :composeApp:run`
- **Web**: `./gradlew :composeApp:jsBrowserDevelopmentRun`

## ⚙️ Configuration

1. **Project Root**: On the first launch, go to **Settings** and select a folder where your LaTeX projects will be stored.
2. **Download Location**: Set a specific folder for exported PDFs.
3. **Compile Server**: Configure your preferred LaTeX compilation backend.

## 🤝 Contributing

TexSpace is open-source and welcomes contributions! Whether it's adding platform-specific features, improving syntax highlighting, or fixing bugs, feel free to open a PR.

---
*Crafted with ❤️ by Bibin*