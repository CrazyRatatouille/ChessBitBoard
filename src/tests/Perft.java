package tests;

import board.BoardState;
import board.MoveGen;

public class Perft {

    private static final int SIMPLE_PERFT_1 = 20;
    private static final int SIMPLE_PERFT_2 = 400;
    private static final int SIMPLE_PERFT_3 = 8_902;
    private static final int SIMPLE_PERFT_4 = 197_281;
    private static final int SIMPLE_PERFT_5 = 4_865_609;

    public static void main(String[] args) {

        BoardState boardState = new BoardState();

        runPerftTest(1, boardState, SIMPLE_PERFT_1);
        runPerftTest(2, boardState, SIMPLE_PERFT_2);
        runPerftTest(3, boardState, SIMPLE_PERFT_3);
        runPerftTest(4, boardState, SIMPLE_PERFT_4);
        runPerftTest(5, boardState, SIMPLE_PERFT_5);

        System.out.println("\nPERFT PASSED!");

    }

    private static void runPerftTest(int depth, BoardState boardState, int expected) {

        long startTime = System.nanoTime();

        int result = perft(depth, boardState);

        long endTime = System.nanoTime();
        long durationNano = endTime - startTime;

        double durationMs = durationNano / 1_000_000.0;

        long nps = 0;
        if (durationNano > 0) {
            nps = (result * 1_000_000_000L) / durationNano;
        }

        if (result == expected) {

            System.out.printf("Depth %d: PASS | Nodes: %-12s | Time: %12.2f ms | NPS: %,d%n",
                    depth,
                    String.format("%,d", result),
                    durationMs,
                    nps);
        } else {
            System.err.printf("Depth %d: FAIL! (Expected: %,d, Got: %,d)%n", depth, expected, result);
            System.exit(1);
        }
    }

    private static int perft(int depth, BoardState boardState) {

        if (depth == 0) {
            return 1;
        }

        int count = 0;

        short[] moves = MoveGen.moves(boardState);
        int index = 0;

        while (moves[index] != -1) {

            int curSide = boardState.getSide();

            boardState.makeMove(moves[index++]);

            if (boardState.isInCheck(curSide)) {
                boardState.unmakeMove();
                continue;
            }

            count += perft(depth - 1, boardState);
            boardState.unmakeMove();
        }

        return count;
    }
}
