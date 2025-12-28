package constants;

import java.util.Random;
import static constants.BoardConstants.*;

/**
 * Generates and stores the Zobrist hash values used for board state hashing.
 *
 * <p>Zobrist Hashing provides a highly efficient and collision-resistant method for uniquely identifying
 * a chess board position with a single {@code long} value. It is essential for implementing a
 * Transposition Table to detect repeated positions (3-fold repetition).</p>
 *
 * <p>Each unique combination of (legacyToDelete.PieceType, SideColor, Square) is assigned a single, cryptographically random {@code long}.
 * The hash for any given board is computed by XORing the random values for all pieces on the board.</p>
 */
public class Zobrist {

    private Zobrist() {}

    public static final long[] PIECE_SQUARE_KEYS = new long[DISTINCT_PIECES_COUNT * BOARD_SIZE];
    public static final long[] EN_PASSANT_KEYS = new long[BOARD_SIZE + 1];
    public static final long[] CASTLING_KEYS = new long[16];
    public static final long SIDE_KEY;

    public static final long STARTING_HASH;

    static{
        long defaultPosition1 = 0;
        Random random = new Random(0x5F8C9A72D3B1E4C7L);

        for (int i = 0; i < PIECE_SQUARE_KEYS.length; i++) {
            PIECE_SQUARE_KEYS[i] = random.nextLong();
        }

        for (int i = 0; i < BOARD_SIZE; i++) {
            EN_PASSANT_KEYS[i] = random.nextLong();
        }

        for (int i = 1; i < CASTLING_KEYS.length; i++) {
            CASTLING_KEYS[i] = random.nextLong();
        }


        SIDE_KEY= random.nextLong();


        for (int i = 8; i < 16; i++) {
            defaultPosition1 ^= PIECE_SQUARE_KEYS[W_PAWN * BOARD_SIZE + i];
        }

        defaultPosition1 ^= PIECE_SQUARE_KEYS[W_KNIGHT * BOARD_SIZE + 1];
        defaultPosition1 ^= PIECE_SQUARE_KEYS[W_KNIGHT * BOARD_SIZE + 6];

        defaultPosition1 ^= PIECE_SQUARE_KEYS[W_BISHOP * BOARD_SIZE + 2];
        defaultPosition1 ^= PIECE_SQUARE_KEYS[W_BISHOP * BOARD_SIZE + 5];

        defaultPosition1 ^= PIECE_SQUARE_KEYS[W_ROOK * BOARD_SIZE + 0];
        defaultPosition1 ^= PIECE_SQUARE_KEYS[W_ROOK * BOARD_SIZE + 7];

        defaultPosition1 ^= PIECE_SQUARE_KEYS[W_QUEEN * BOARD_SIZE + 3];
        defaultPosition1 ^= PIECE_SQUARE_KEYS[W_KING * BOARD_SIZE + 4];

        for (int i = 48; i < 56; i++) {
            defaultPosition1 ^= PIECE_SQUARE_KEYS[B_PAWN * BOARD_SIZE + i];
        }

        defaultPosition1 ^= PIECE_SQUARE_KEYS[B_KNIGHT * BOARD_SIZE + 57];
        defaultPosition1 ^= PIECE_SQUARE_KEYS[B_KNIGHT * BOARD_SIZE + 62];

        defaultPosition1 ^= PIECE_SQUARE_KEYS[B_BISHOP * BOARD_SIZE + 58];
        defaultPosition1 ^= PIECE_SQUARE_KEYS[B_BISHOP * BOARD_SIZE + 61];

        defaultPosition1 ^= PIECE_SQUARE_KEYS[B_ROOK * BOARD_SIZE + 56];
        defaultPosition1 ^= PIECE_SQUARE_KEYS[B_ROOK * BOARD_SIZE + 63];

        defaultPosition1 ^= PIECE_SQUARE_KEYS[B_QUEEN * BOARD_SIZE + 59];
        defaultPosition1 ^= PIECE_SQUARE_KEYS[B_KING * BOARD_SIZE + 60];

        defaultPosition1 ^= EN_PASSANT_KEYS[64];
        defaultPosition1 ^= CASTLING_KEYS[15];
        STARTING_HASH = defaultPosition1;
    }
}
