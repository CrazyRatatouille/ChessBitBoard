# ChessBitBoard

ChessBitBoard is a high-performance chess engine written entirely in Java from scratch. It utilizes a 64-bit Little-Endian Rank-File (LERF) bitboard board representation to achieve extremely fast move generation and state evaluation.

This project is built with a heavy focus on performance, cache efficiency, and low garbage-collection overhead, making it an excellent demonstration of low-level optimization within the JVM.

## 🚀 Current Features

The engine's foundational mechanics and board representation are fully implemented and heavily optimized:
* **Bitboard Board Representation:** Uses 64-bit integers (`long`) to represent piece occupancies, ensuring blazing-fast bitwise operations.
* **Magic Bitboards:** Implements perfect hashing (Magic Bitboards) for $O(1)$ lookup of sliding piece attacks (Rooks and Bishops).
* **High-Speed Move Generation:** Encodes moves into 16-bit `short` primitives for memory efficiency and uses a pre-allocated array cache to prevent runtime object allocations.
* **Make/Unmake Move Operations:** Highly optimized board state traversal for deep search trees.
* **Zobrist Hashing:** Fast, collision-resistant incremental hashing for position tracking and transposition tables.
* **FEN Parsing:** Utility to translate standard Forsyth–Edwards Notation into the internal board state.
* **Perft Testing Suite:** Built-in recursive performance testing suite to validate move generation against known complex positions (e.g., "Kiwipete").
* **Graphical Visualizer:** A GUI debugging tool built with CodeDraw to visually render the board state, piece placement, and active bitboard masks.

## 📈 Benchmarks

Because this engine aims for high performance, move generation speed is a primary metric.

* **Current Perft Speed:** **~35,000,000 Nodes Per Second (NPS)**
* **Hardware Context:** Benchmarked on an AMD Ryzen 5 6600H with Radeon Graphics (3.30 GHz).

## 🗺️ Planned Features (Roadmap)

The current architecture provides a highly optimized foundation. The following features are planned for the distant future to evolve this from a move-generator into a fully competitive playing engine:

* **Search Algorithm:** Minimax search with Alpha-Beta pruning.
* **Transposition Table:** Caching previously evaluated positions (using the existing Zobrist hashing) to drastically reduce the search space and avoid redundant calculations.
* **Quiescence Search:** To evaluate dynamic tactical exchanges at the end of the main search depth and prevent the horizon effect.
* **Move Ordering:** Implementing heuristics to improve Alpha-Beta cutoff rates.
* **Evaluation:** Piece-Square Tables (PSQTs) for positional evaluation.
* **UCI Protocol:** Implementing the Universal Chess Interface (UCI) so the engine can be hooked up to any standard chess GUI (e.g., ChessArena, Arena, Cute Chess).
* **Time Management:** A timer system to allocate search time efficiently based on time controls.

## 🛠️ Setup & Installation

### Prerequisites
* **Java Development Kit (JDK):** This project requires **JDK 23**.
* **CodeDraw:** The visualizer depends on the `CodeDraw.jar` library.

### IDE Setup (IntelliJ IDEA)
This project includes standard `.idea` configuration files for IntelliJ.
1. Clone the repository and open the folder in IntelliJ IDEA.
2. Ensure your Project SDK is set to `openjdk-23` in `File > Project Structure > Project`.
3. **Add CodeDraw:** * The project expects `CodeDraw.jar` to be located in a `libraries/Java/` folder two directories above the project root.
    * If you don't have this folder structure, download [CodeDraw](https://github.com/Krassnig/CodeDraw), go to `File > Project Structure > Libraries`, add the `CodeDraw.jar` file manually, and apply it to the module.

### Running the Project

* **Run Perft Suite:** Execute the `main` method in `src/tests/Perft.java` to run recursive node counts.
* **Run Visualizer:** Execute the `main` method in `src/tools/PosVisualiser.java` to see the GUI board representation.