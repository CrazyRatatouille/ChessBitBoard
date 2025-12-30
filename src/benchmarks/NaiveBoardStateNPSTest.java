package benchmarks;

import board.BoardState;
import java.util.Random;

/**
 * AI WRITTEN, specifically Gemini <br> <br>
 *
 * This benchmark was created to test how different approaches lead to different speed in NPS. The purpose of this
 * Class isn't to market this as a self-written proper test to compare to other engines, but rather for data gathering
 */
public class NaiveBoardStateNPSTest {

    private static final int ITERATIONS = 1_000_000_000;
    private static final int WARMUP_ITERATIONS = 10_000_000;
    private static final int MOVES_CACHE_SIZE = 4096;

    public static void main(String[] args) {
        System.out.println("Initializing Benchmark...");

        BoardState board = new BoardState();
        short[] randomMoves = generateSafeRandomMoves();

        // ---------------------------------------------------------
        // 1. Warmup Phase (Compiles methods to Native Code via JIT)
        // ---------------------------------------------------------
        System.out.println("Warming up JIT...");
        long checkSum = 0; // Prevent Dead Code Elimination
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            short move = randomMoves[i & (MOVES_CACHE_SIZE - 1)];

            board.makeMove(move);
            checkSum += board.getOccupancy(); // Read something
            board.unmakeMove();
        }

        // ---------------------------------------------------------
        // 2. Testing Phase
        // ---------------------------------------------------------
        System.out.println("Running " + ITERATIONS + " iterations...");
        System.gc(); // Clean up before starting
        long start = System.nanoTime();

        for (int i = 0; i < ITERATIONS; i++) {
            // Fast modulo using bitwise AND (works because size is power of 2)
            short move = randomMoves[i & (MOVES_CACHE_SIZE - 1)];

            board.makeMove(move);
            board.unmakeMove();
        }

        long end = System.nanoTime();

        // ---------------------------------------------------------
        // 3. Results
        // ---------------------------------------------------------
        double durationSeconds = (end - start) / 1_000_000_000.0;
        long nps = (long) (ITERATIONS / durationSeconds);

        System.out.println("------------------------------------------");
        System.out.printf("Total Time: %.3f s%n", durationSeconds);
        System.out.printf("NPS (Nodes/Sec): %,d%n", nps);
        System.out.println("------------------------------------------");

        // Print checksum to ensure JIT didn't optimize away the warmup
        if (checkSum == 1) System.out.print("");
    }

    /**
     * Generates moves that are guaranteed to be "Index Safe" for the STARTING POSITION.
     * Since make/unmake resets the board every time, we only need to respect the
     * starting board layout:
     * - Pieces exist at indices 0-15 (White) and 48-63 (Black).
     * - Empty squares are at indices 16-47.
     */
    private static short[] generateSafeRandomMoves() {
        short[] moves = new short[MOVES_CACHE_SIZE];
        Random rng = new Random(12345);

        for (int i = 0; i < MOVES_CACHE_SIZE; i++) {
            boolean isCapture = rng.nextBoolean(); // 50% mix of Captures and Quiet

            int from, to, type;

            if (isCapture) {
                // TEST CAPTURE (Type 4)
                // To avoid crashing, we must capture a square that HAS a piece.
                // We simulate White capturing Black pieces directly (teleporting).
                // Logic validity doesn't matter, only Array Access safety.

                from = rng.nextInt(16);       // From White area (0-15)
                to = 48 + rng.nextInt(16);    // To Black area (48-63)
                type = 4;                     // Capture Move Type

            } else {
                // TEST QUIET MOVE (Type 0)
                // Move from a Piece square to an Empty square.

                from = rng.nextInt(16);       // From White area (0-15)
                to = 16 + rng.nextInt(32);    // To Empty area (16-47)
                type = 0;                     // Quiet Move Type
            }

            // Encode (To | From<<6 | Type<<12)
            int moveInt = (to & 0x3F) | ((from & 0x3F) << 6) | ((type & 0xF) << 12);
            moves[i] = (short) moveInt;
        }
        return moves;
    }
}