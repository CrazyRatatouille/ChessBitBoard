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
            "A1", "A2", "A3", "A4", "A5", "A6", "A7", "A8",
            "B1", "B2", "B3", "B4", "B5", "B6", "B7", "B8",
            "C1", "C2", "C3", "C4", "C5", "C6", "C7", "C8",
            "D1", "D2", "D3", "D4", "D5", "D6", "D7", "D8",
            "E1", "E2", "E3", "E4", "E5", "E6", "E7", "E8",
            "F1", "F2", "F3", "F4", "F5", "F6", "F7", "F8",
            "G1", "G2", "G3", "G4", "G5", "G6", "G7", "G8",
            "H1", "H2", "H3", "H4", "H5", "H6", "H7", "H8",
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
    public static final long A_RANK = 0x00000000000000FFL;
    public static final long B_RANK = 0x000000000000FF00L;
    public static final long C_RANK = 0x0000000000FF0000L;
    public static final long D_RANK = 0x00000000FF000000L;
    public static final long E_RANK = 0x000000FF00000000L;
    public static final long F_RANK = 0x0000FF0000000000L;
    public static final long G_RANK = 0x00FF000000000000L;
    public static final long H_RANK = 0xFF00000000000000L;

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
}
