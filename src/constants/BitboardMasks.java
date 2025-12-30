package constants;

import static constants.BoardConstants.*;

public class BitboardMasks {

    private BitboardMasks(){}

    public static final long[] PAWN_MASK = new long[2 * BOARD_SIZE];
    public static final long[] KNIGHT_MASK = new long[BOARD_SIZE];
    public static final long[] KING_MASK = new long[BOARD_SIZE];

    static {
        for (int i = 0; i < BOARD_SIZE; i++) {

            long positionMask = 1L << i;

            PAWN_MASK[i] = ((positionMask & ~FIRST_RANK) & ~A_FILE) << 7 |  ((positionMask & ~FIRST_RANK) & ~H_FILE) << 9;
            PAWN_MASK[BOARD_SIZE + i] = ((positionMask & ~EIGHT_RANK) & ~A_FILE) >>> 9 |  ((positionMask & ~EIGHT_RANK) & ~H_FILE) >>> 7;

            KNIGHT_MASK[i] =
                    (positionMask & ~A_FILE) >>> 17 | (positionMask & ~A_FILE) << 15
                    | (positionMask & ~H_FILE) >>> 15 | (positionMask & ~H_FILE) << 17
                    | (positionMask & ~(A_FILE | B_FILE)) >>> 10 | (positionMask & ~(A_FILE | B_FILE)) << 6
                    | (positionMask & ~(G_FILE | H_FILE)) >>> 6 | (positionMask & ~(G_FILE | H_FILE)) << 10;


            KING_MASK[i] =
                    positionMask >>> 8 | positionMask << 8
                    | (positionMask & ~A_FILE) >>> 9 | (positionMask & ~A_FILE) >>> 1 | (positionMask & ~A_FILE) << 7
                    | (positionMask & ~H_FILE) >>> 7 | (positionMask & ~H_FILE) << 1 | (positionMask & ~H_FILE) << 9;
        }
    }
}
