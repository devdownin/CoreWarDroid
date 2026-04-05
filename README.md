# CoreWar KMP 🚀

**CoreWar KMP** is a modern, cross-platform implementation of the classic programming game [Core War](https://en.wikipedia.org/wiki/Core_War), built with **Kotlin Multiplatform (KMP)** and **Compose Multiplatform**.

Create, optimize, and deploy digital warriors written in **Redcode** to compete in a virtual arena (the Core).

---

## ✨ Key Features

- **Multiplatform**: Runs on Android and Web (WasmGC). iOS support coming soon!
- **Advanced Engine**: High-performance MARS engine with support for 15 standard opcodes and multiple addressing modes.
- **Battle Arena**: Real-time visualization of memory with zoom/pan, event logs, and detailed battle statistics.
- **Modern Editor**: Redcode editor with syntax highlighting, opcode autocomplete, and warrior snippets.
- **Progression System**: Unlock instructions and special powers (Shield, Turbo) through a non-linear Tech Tree.
- **Chaos Mode**: Dynamic battle environment with random memory glitches and process warps.
- **Customizable**: Choose from multiple themes (Matrix, Retro, Neon) and configure arena parameters like core size and cycle limits.

---

## 🛠 Tech Stack

- **UI Framework**: [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)
- **Language**: [Kotlin](https://kotlinlang.org/) (JVM & Wasm targets)
- **Dependency Injection**: [Koin](https://insert-koin.io/)
- **Database**: [SQLDelight](https://cashapp.github.io/sqldelight/)
- **Settings**: [Jetpack DataStore](https://developer.android.com/jetpack/androidx/releases/datastore)
- **Navigation**: [Compose Navigation](https://developer.android.com/jetpack/compose/navigation) (Multiplatform)
- **Build System**: Gradle 8.8+ with Version Catalogs

---

## 🚀 Getting Started

### Prerequisites
- JDK 17 or 21
- Android Studio Ladybug+ or IntelliJ IDEA

### Build and Run

#### Android
```bash
./gradlew :composeApp:installDebug
```

#### Web (Wasm)
```bash
./gradlew :composeApp:wasmJsBrowserRun
```

#### Running Tests
```bash
./gradlew test
```

---

## 📂 Project Structure

- `commonMain`: Shared logic, models, ViewModels, and UI components.
- `androidMain`: Android-specific implementations and resources.
- `wasmJsMain`: Web-specific entry points and SQLDelight worker configuration.
- `commonTest`: Unit tests for the game engine and parser.

---

## 🤝 Contributing

Contributions are welcome! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines on how to get involved.

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 🧠 Documentation

- [Gameplay Suggestions](SUGGESTIONS.md)
- [Technical Improvements](TECH_IMPROVEMENTS.md)
- [Project Roadmap](ROADMAP.md)
- [Future Ideas](IDEAS.md)
