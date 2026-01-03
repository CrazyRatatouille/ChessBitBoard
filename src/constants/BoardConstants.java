package constants;

public class BoardConstants {

    private BoardConstants() {}

    public static final int BOARD_SIZE = 64;
    public static final int DISTINCT_PIECES_COUNT = 12;

    public static final int MAX_MOVES = 256;
    public static final int MAX_GAME_LENGTH = 1024;

    // ================== COLORS ==================
    public static final int WHITE = 0;
    public static final int BLACK = 1;
    public static final int BOTH = 2;

    // ================== PIECES ==================
    public static final int EMPTY_SQUARE = -1;

    public static final int P_PAWN = 0;
    public static final int P_KNIGHT = 1;
    public static final int P_BISHOP = 2;
    public static final int P_ROOK = 3;
    public static final int P_QUEEN = 4;
    public static final int P_KING = 5;

    // ============== COLORED PIECES ==============
    public static final int W_PAWN = 0, B_PAWN = 1;
    public static final int W_KNIGHT = 2, B_KNIGHT = 3;
    public static final int W_BISHOP = 4, B_BISHOP = 5;
    public static final int W_ROOK = 6, B_ROOK = 7;
    public static final int W_QUEEN = 8, B_QUEEN = 9;
    public static final int W_KING = 10, B_KING = 11;

    // ================== SQUARES ==================
    public static final int
            A1 = 0, B1 = 1, C1 = 2, D1 = 3, E1 = 4, F1 = 5, G1 = 6, H1 = 7,
            A2 = 8, B2 = 9, C2 = 10, D2 = 11, E2 = 12, F2 = 13, G2 = 14, H2 = 15,
            A3 = 16, B3 = 17, C3 = 18, D3 = 19, E3 = 20, F3 = 21, G3 = 22, H3 = 23,
            A4 = 24, B4 = 25, C4 = 26, D4 = 27, E4 = 28, F4 = 29, G4 = 30, H4 = 31,
            A5 = 32, B5 = 33, C5 = 34, D5 = 35, E5 = 36, F5 = 37, G5 = 38, H5 = 39,
            A6 = 40, B6 = 41, C6 = 42, D6 = 43, E6 = 44, F6 = 45, G6 = 46, H6 = 47,
            A7 = 48, B7 = 49, C7 = 50, D7 = 51, E7 = 52, F7 = 53, G7 = 54, H7 = 55,
            A8 = 56, B8 = 57, C8 = 58, D8 = 59, E8 = 60, F8 = 61, G8 = 62, H8 = 63;

    public static final String[] SQUARE_NAMES = {
            "a1", "b1", "c1", "d1", "e1", "f1", "g1", "h1",
            "a2", "b2", "c2", "d2", "e2", "f2", "g2", "h2",
            "a3", "b3", "c3", "d3", "e3", "f3", "g3", "h3",
            "a4", "b4", "c4", "d4", "e4", "f4", "g4", "h4",
            "a5", "b5", "c5", "d5", "e5", "f5", "g5", "h5",
            "a6", "b6", "c6", "d6", "e6", "f6", "g6", "h6",
            "a7", "b7", "c7", "d7", "e7", "f7", "g7", "h7",
            "a8", "b8", "c8", "d8", "e8", "f8", "g8", "h8"
    };

    public static final int NO_SQUARE = 64;

    // =============== SQUARE MASKS ===============
    public static final long[] SQUARE_BB = new long[64];

    static {
        for (int i = 0; i < 64; i++) {
            SQUARE_BB[i] = 1L << i;
        }
    }

    // =================== FILES ===================
    public static final long A_FILE = 0x0101010101010101L;
    public static final long B_FILE = 0x0202020202020202L;
    public static final long C_FILE = 0x0404040404040404L;
    public static final long D_FILE = 0x0808080808080808L;
    public static final long E_FILE = 0x1010101010101010L;
    public static final long F_FILE = 0x2020202020202020L;
    public static final long G_FILE = 0x4040404040404040L;
    public static final long H_FILE = 0x8080808080808080L;

    // =================== RANKS ===================
    public static final long FIRST_RANK = 0x00000000000000FFL;
    public static final long SECOND_RANK = 0x000000000000FF00L;
    public static final long THIRD_RANK = 0x0000000000FF0000L;
    public static final long FOURTH_RANK = 0x00000000FF000000L;
    public static final long FIFTH_RANK = 0x000000FF00000000L;
    public static final long SIXTH_RANK = 0x0000FF0000000000L;
    public static final long SEVENTH_RANK = 0x00FF000000000000L;
    public static final long EIGHT_RANK = 0xFF00000000000000L;

    // =============== CASTLING MASKS ==============
    /**
     * A lookup table used to update castling rights efficiently.
     * When a piece moves from or to a square, the rights are bitwise AND-ed with these values.
     */
    public static final byte[] CASTLING_MASK_BY_SQUARE = {
            0xE, 0xF, 0xF, 0xF, 0xC, 0xF, 0xF, 0xD,
            0xF, 0xF, 0xF, 0xF, 0xF, 0xF, 0xF, 0xF,
            0xF, 0xF, 0xF, 0xF, 0xF, 0xF, 0xF, 0xF,
            0xF, 0xF, 0xF, 0xF, 0xF, 0xF, 0xF, 0xF,
            0xF, 0xF, 0xF, 0xF, 0xF, 0xF, 0xF, 0xF,
            0xF, 0xF, 0xF, 0xF, 0xF, 0xF, 0xF, 0xF,
            0xF, 0xF, 0xF, 0xF, 0xF, 0xF, 0xF, 0xF,
            0xB, 0xF, 0xF, 0xF, 0x3, 0xF, 0xF, 0x7,
    };

    // ============= MOVE RESET MASKS =============
    public static final int[] HALF_MOVE_RESET_MASK = {0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};

    public static final short QUIET_MOVE = 0x0;
    public static final short DOUBLE_PAWN_PUSH = 0x1;
    public static final short KING_SIDE_CASTLE = 0x2;
    public static final short QUEEN_SIDE_CASTLE = 0x3;
    public static final short CAPTURE = 0x4;
    public static final short EP_CAPTURE = 0x5;
    public static final short PROMOTION = 0x8;
}
