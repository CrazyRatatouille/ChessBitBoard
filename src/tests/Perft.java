package tests;

import board.BoardState;
import board.Move;
import board.MoveGen;

public class Perft {

    private static final int SIMPLE_PERFT_1 = 20;
    private static final int SIMPLE_PERFT_2 = 400;
    private static final int SIMPLE_PERFT_3 = 8_902;
    private static final int SIMPLE_PERFT_4 = 197_281;
    private static final int SIMPLE_PERFT_5 = 4_865_609;

    private static final int KIWIPETE_PERFT_1 = 48;
    private static final int KIWIPETE_PERFT_2 = 2_039;
    private static final int KIWIPETE_PERFT_3 = 97_862;
    private static final int KIWIPETE_PERFT_4 = 4_085_603;
    private static final int KIWIPETE_PERFT_5 = 193_690_690;
    private static final String KIWIPETE_FEN = "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1";

    public static void main(String[] args) {

        BoardState boardState = new BoardState();

//        runPerftTest(1, boardState, SIMPLE_PERFT_1);
//        runPerftTest(2, boardState, SIMPLE_PERFT_2);
//        runPerftTest(3, boardState, SIMPLE_PERFT_3);
//        runPerftTest(4, boardState, SIMPLE_PERFT_4);
//        runPerftTest(5, boardState, SIMPLE_PERFT_5);
//
//        System.out.println("\nKIWIPETE PERFT:\n");
//
        boardState.setPos(KIWIPETE_FEN);
//        runPerftTest(1, boardState, KIWIPETE_PERFT_1);
//        runPerftTest(2, boardState, KIWIPETE_PERFT_2);
//        runPerftTest(3, boardState, KIWIPETE_PERFT_3);
//        runPerftTest(4, boardState, KIWIPETE_PERFT_4);
//        runPerftTest(5, boardState, KIWIPETE_PERFT_5);

//        System.out.println("\nPERFT PASSED!");

        //====================================================
        //                   Personal Nodes
        //====================================================

        boardState.setPos("r3k2r/p1pNqpb1/bn2pnp1/3P4/1p2P3/2N2Q1p/PPPBBPPP/R3K2R b KQkq - 0 1");
//        runPerftTest(1, boardState, 45);
    }

    private static void runPerftTest(int depth, BoardState boardState, int expected) {

        long startTime = System.nanoTime();

        int result = perft(depth, depth, boardState);

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

    private static int perft(int initialDepth, int depth, BoardState boardState) {

        if (depth == 0) {
            return 1;
        }

        int count = 0;

        short[] moves = MoveGen.moves(boardState);
        int index = 0;

        while (moves[index] != -1) {

            int curSide = boardState.getSide();

            boardState.makeMove(moves[index]);

            String debugger = Move.toString(moves[index]) + " - ";
            index++;

            if (boardState.isInCheck(curSide)) {
                boardState.unmakeMove();
                continue;
            }

            int perft = perft(depth, depth - 1, boardState);

//            //uncomment to find diverging path
//            if(depth == initialDepth) System.out.println(debugger + perft);

            count += perft;
            boardState.unmakeMove();
        }

        return count;
    }
}
