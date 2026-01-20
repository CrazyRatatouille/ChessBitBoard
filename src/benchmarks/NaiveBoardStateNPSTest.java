package benchmarks;

import java.util.Random;
import board.BoardState;
import board.Move;

import static constants.BoardConstants.QUIET_MOVE;
import static constants.BoardConstants.CAPTURE;

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
        long checkSum = 0;
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
        System.gc();
        long start = System.nanoTime();

        for (int i = 0; i < ITERATIONS; i++) {

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

        if (checkSum == 1) System.out.print("");
    }

    /**
     * Generates an array of random moves that abide by {@code BoardState} standards.
     *
     * <p>The moves are generated using the following constraints:
     * <ul>
     * <li><b>From:</b> Always a square in the range 0-15 (White area).</li>
     * <li><b>To:</b> Depends on the move type:
     * <ul>
     * <li>{@code CAPTURE}: Targets the range 48-63 (Black area).</li>
     * <li>{@code QUIET_MOVE}: Targets the range 16-47 (Empty area).</li>
     * </ul>
     * </li>
     * </ul>
     *
     * @return an array of encoded move shorts (not necessarily legal in a game context)
     */
    private static short[] generateSafeRandomMoves() {
        short[] moves = new short[MOVES_CACHE_SIZE];
        Random rng = new Random(12345);

        for (int i = 0; i < MOVES_CACHE_SIZE; i++) {
            boolean isCapture = rng.nextBoolean();

            int from, to, type;

            if (isCapture) {

                from = rng.nextInt(16);       // From White area (0-15)
                to = 48 + rng.nextInt(16);    // To Black area (48-63)
                type = CAPTURE;

            } else {

                from = rng.nextInt(16);       // From White area (0-15)
                to = 16 + rng.nextInt(32);    // To Empty area (16-47)
                type = QUIET_MOVE;
            }

            // Encode (To | From<<6 | Type<<12)
            int moveInt = Move.encode(from, to, type);
            moves[i] = (short) moveInt;
        }
        return moves;
    }
}
