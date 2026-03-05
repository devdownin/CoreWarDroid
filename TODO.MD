# CoreWar KMP - Finalization Roadmap

This document outlines the remaining tasks and planned enhancements to transform this version of CoreWar into a professional-grade production game.

## 1. Engine & Simulation (MARS)
- [ ] **Redcode '94 Full Compliance**: Implement P-Space (private storage for warriors) and the `DAT` instruction behavior for all addressing modes.
- [ ] **Advanced Parameters**: Add configuration for read/write limits per cycle to prevent infinite "scanning" loops from dominating.
- [ ] **Performance Profiling**: Optimize the memory visualizer update frequency for very high-speed simulations (10,000+ cycles/sec).
- [ ] **Deterministic Seed**: Allow setting a fixed seed for Chaos Mode events to enable "Replay" functionality.

## 2. Redcode Editor & Parser
- [ ] **Advanced Error Reporting**: Highlight the exact line and column in the editor when a parsing error occurs.
- [ ] **Auto-completion**: Suggest opcodes and addressing modes as the player types.
- [ ] **Macros & EQU**: Support for constants (EQU) and simple macros to make writing complex warriors easier.
- [ ] **Warrior Gallery**: Allow users to share their warriors via a "Copy to Clipboard" as a Base64/JSON string.

## 3. UI/UX & Gamification
- [ ] **Audio System**: Implement a retro "Terminal" sound system (keyboard clicks, bleeps for MOV, explosions for DAT).
- [ ] **Animations**:
    - Smooth transitions between screens.
    - Particle effects for "Memory Corruption" glitches.
    - Level-up celebration screen.
- [ ] **Responsive Design**: Optimize the Memory Visualizer for large tablet screens and desktop browsers.
- [ ] **Onboarding Tutorial**: An interactive "Step-by-Step" battle that teaches the player how the Imp and Dwarf work.

## 4. Battle Modes & Social
- [ ] **Tournament Mode**: A "Round Robin" system where N warriors fight each other in pairs to determine an overall champion.
- [ ] **Daily Challenges**: Fixed arena layouts (with specific anomaly placements) that refresh daily.
- [ ] **Local Multiplayer**: Pass-and-play warrior selection for two players on one device.

## 5. Persistence & Technical
- [ ] **Wasm Persistence**: Implement Origin Private File System (OPFS) or IndexedDB for reliable DataStore persistence in browsers.
- [ ] **Cloud Sync**: Optional Ktor-based backend to sync warriors and XP across Android and Web.
- [ ] **Unit Testing**:
    - Full test suite for every Opcode/AddressMode combination in `commonTest`.
    - Integration tests for classic warrior interactions (Imp vs. Stone).

## 6. Visual Themes
- [ ] **Vaporwave Theme**: Pink/Blue neon aesthetic with CRT scanline effects.
- [ ] **High Contrast Theme**: Accessible theme for visually impaired users.
- [ ] **Avatar Customization**: Allow players to spend XP to customize the procedural generation parameters of their warriors' avatars.
