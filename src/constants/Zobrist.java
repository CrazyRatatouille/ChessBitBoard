package constants;

import java.util.Random;
import static constants.BoardConstants.*;

/**
 * Generates and stores the Zobrist hash values used for board state hashing.
 * <p>
 * Zobrist Hashing provides a highly efficient and collision-resistant method for uniquely identifying
 * a chess board position with a single {@code long} value. It is essential for implementing a
 * Transposition Table to detect repeated positions (3-fold repetition).
 * <p>
 * <b>Mechanism:</b>
 * <br>
 * Each unique component of the board (Piece on Square, Castling Right, En Passant file, Side to Move)
 * is assigned a random 64-bit integer. The hash for the board is the XOR sum of all these components.
 * Since XOR is their own inverse, updating the hash during a move is O(1).
 */
public class Zobrist {

    private Zobrist() {}

    /**
     * Keys for every piece on every square.
     * <p>
     * <b>Indexing:</b> {@code [PieceType * 64 + SquareIndex]}
     * <br>where PieceType is your integer constant (e.g., W_PAWN).
     */
    public static final long[] PIECE_SQUARE_KEYS = new long[DISTINCT_PIECES_COUNT * BOARD_SIZE];

    /**
     * Keys for the currently valid En Passant target square.
     * <p>
     * <b>Indexing:</b> {@code [SquareIndex]}
     * <br>Index 64 is reserved for "No En Passant" and is initialized to 0.
     */
    public static final long[] EN_PASSANT_KEYS = new long[BOARD_SIZE + 1];

    /**
     * Keys for the castling rights state.
     * <p>
     * <b>Indexing:</b> {@code [CastlingRightsMask]} (0-15)
     * <br>Index 0 (no rights) is initialized to 0.
     */
    public static final long[] CASTLING_KEYS = new long[16];

    /**
     * Key XORed into the hash when it is Black's turn to move.
     */
    public static final long SIDE_KEY;

    /**
     * The pre-calculated hash of the standard chess starting position.
     */
    public static final long STARTING_HASH;

    static{
        long defaultPosition1 = 0;
        Random random = new Random(1);

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
