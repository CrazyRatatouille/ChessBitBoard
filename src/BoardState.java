/**
 * Represents the state of a chess board using bitboard representation.
 * <p>
 * This class uses 64-bit {@code long} primitives to represent the positions of pieces,
 * occupancy masks, and special game states. It follows the Little-Endian Rank-File (LERF)
 * mapping where bit 0 is A1 and bit 63 is H8.
 * </p>
 */
public class BoardState {

    /**
     * Array of 12 bitboards representing each piece type and color.
     * Indexing: [0,1] Pawns, [2,3] Knights, [4,5] Bishops, [6,7] Rooks, [8,9] Queens, [10,11] Kings.
     * Even indices are White, odd indices are Black.
     */
    private long[] bitboards = {
            0xFF00L, 0x00FF000000000000L,
            0x0042L, 0x4200000000000000L,
            0x0024L, 0x2400000000000000L,
            0x0081L, 0x8100000000000000L,
            0x0008L, 0x0800000000000000L,
            0x0010L, 0x1000000000000000L
    };

    private byte[] mailbox = {
             6,  2,  4,  8, 10,  4,  2,  6,
             0,  0,  0,  0,  0,  0,  0,  0,
            -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1,
             1,  1,  1,  1,  1,  1,  1,  1,
             7,  3,  5,  9, 11,  5,  3,  7
    };

    /**
     * Bitmask constants for each file (A through H).
     */
    private static final long[] fileMasks = {
            0x0101010101010101L, 0x0202020202020202L, 0x0404040404040404L, 0x0808080808080808L,
            0x1010101010101010L, 0x2020202020202020L, 0x4040404040404040L, 0x8080808080808080L
    };

    /**
     * Bitmask constants for each rank (1 through 8).
     */
    private static final long[] rankMasks = {
            0xFFL, 0xFF00L, 0xFF0000, 0xFF000000L,
            0xFF00000000L, 0xFF0000000000L, 0xFF000000000000L, 0xFF00000000000000L
    };

    /**
     * Bitmask representing the single square available for an en passant capture.
     */
    private long enPassant = 0x0L;

    /**
     * bit 1 (LSB): wQueenSide <br>
     * bit 2 : wKingSide <br>
     * bit 3 : bQueenSide <br>
     * bit 4 (MSB) : bKingSide
     */
    private int castlingRights = 0xF;

    /**
     * A lookup table used to update castling rights efficiently.
     * When a piece moves from or to a square, the rights are bitwise AND-ed with these values.
     */
    private static final byte[] castlingMasks = {
            0xE, 0xF, 0xF, 0xF, 0xC, 0xF, 0xF, 0xD,
            0xF, 0xF, 0xF, 0xF, 0xF, 0xF, 0xF, 0xF,
            0xF, 0xF, 0xF, 0xF, 0xF, 0xF, 0xF, 0xF,
            0xF, 0xF, 0xF, 0xF, 0xF, 0xF, 0xF, 0xF,
            0xF, 0xF, 0xF, 0xF, 0xF, 0xF, 0xF, 0xF,
            0xF, 0xF, 0xF, 0xF, 0xF, 0xF, 0xF, 0xF,
            0xF, 0xF, 0xF, 0xF, 0xF, 0xF, 0xF, 0xF,
            0xB, 0xF, 0xF, 0xF, 0x3, 0xF, 0xF, 0x7,
    };

    /**
     * Occupancy bitboards.
     * Index 0: White pieces, Index 1: Black pieces, Index 2: Combined (all) pieces.
     */
    private long[] occs = {0xFFFFL, 0xFFFF000000000000L, 0xFFFF0000000000FFL};

    /**
     * Default constructor. Initializes the board to the standard starting position.
     */
    public BoardState() {
    }

    /**
     * Deep-copy constructor for state branching during search.
     * * @param other The source board to copy from.
     */
    public BoardState(BoardState other) {

        System.arraycopy(other.bitboards, 0, this.bitboards, 0, this.bitboards.length);
        System.arraycopy(other.occs, 0, this.occs, 0, this.occs.length);

        this.enPassant = other.enPassant;
        this.castlingRights = other.castlingRights;
    }

    /**
     * Executes a move on the board using a 16-bit encoded integer.
     * <p>
     * The encoding follows the standard "From-To" layout. Bits [0,5] to, [6,11] from, 14 Capture, 15 Promotion.
     * It packs the Source Square, Destination Square, and Move Type (promotion, capture, etc.)
     * into a single short/int for cache efficiency.
     * </p>
     * @param move The 16-bit encoded move.
     * @see <a href="https://www.chessprogramming.org/Encoding_Moves">Chess Programming Wiki: Encoding Moves</a>
     */
    public void makeMove(short move) {

        //TODO: remove codeDuplication as much as possible and lift general purpose computation
        //TODO: update mailbox

        int fromSq = (move >>> 6) & 0x3F;
        int toSq = move & 0x3F;

        long fromBit = 1L << fromSq;
        long toBit = 1L << toSq;
        int color = mailbox[fromSq] % 2;

        int moveType = (move & 0xF000) >>> 12;
        int promoTo = 2 + ((moveType & 0x3) << 1);
        int captured = mailbox[toSq];

        enPassant = 0x0L;

        int pIdx = mailbox[fromSq];
        long moveMask = (fromBit | toBit);

        switch (moveType) {
            case 0 -> quietMove(fromBit, toBit, color, pIdx);
            case 1 -> pawnDoubleMove(fromBit, toBit, color, pIdx);
            case 2 -> kingSideCastle(fromBit, toBit, color, pIdx);
            case 3 -> queenSideCastle(fromBit, toBit, color, pIdx);
            case 4 -> capture(fromBit, toBit, color, captured, pIdx);
            case 5 -> enPassant(fromBit, toBit, color, pIdx);

            case 8, 9, 10, 11-> promotion(fromBit, toBit, color, promoTo, pIdx);

            case 12, 13, 14, 15 -> promotionAndCapture(fromBit, toBit, color, captured, promoTo, pIdx);
        }

        updateCastlingRights(fromSq, toSq);
    }

    /**
     * Handles non-capturing, non-special moves using XOR toggling.
     *
     * @param fromBit Bitmask of the source square.
     * @param toBit   Bitmask of the destination square.
     * @param color   Ordinal of the moving side.
     * @param pIdx    Index of the specific piece bitboard.
     */
    private void quietMove(long fromBit, long toBit, int color, int pIdx) {

        long moveMask = (fromBit | toBit);

        bitboards[pIdx] ^= moveMask;
        occs[color] ^= moveMask;
        occs[2] ^= moveMask;
    }

    /**
     * Executes a capture move by clearing the victim's bit.
     *
     * @param fromBit  Bitmask of the source square.
     * @param toBit    Bitmask of the destination square.
     * @param color    Ordinal of the moving side.
     * @param captured Ordinal of the captured piece type.
     * @param pIdx     Index of the moving piece bitboard.
     */
    private void capture(long fromBit, long toBit, int color, int captured, int pIdx) {

        long moveMask = (fromBit | toBit);

        bitboards[pIdx] ^= (fromBit | toBit);
        bitboards[captured] ^= toBit;

        occs[color] ^= moveMask;
        occs[0x1 ^ color] ^= toBit;
        occs[2] ^= fromBit;
    }

    /**
     * Handles pawn promotion by swapping the pawn bit for a new piece bit.
     *
     * @param fromBit Bitmask of the source square.
     * @param toBit   Bitmask of the destination square.
     * @param color   Ordinal of the moving side.
     * @param promoTo Ordinal of the piece type being promoted to.
     * @param pIdx    Index of the pawn bitboard.
     */
    private void promotion(long fromBit, long toBit, int color, int promoTo, int pIdx) {

        long moveMask = (fromBit | toBit);

        bitboards[pIdx] ^= fromBit;
        bitboards[color + promoTo] ^= toBit;

        occs[color] ^= moveMask;
        occs[2] ^= moveMask;
    }

    /**
     * Executes a move that both captures a piece and promotes a pawn.
     *
     * @param fromBit  Bitmask of the source square.
     * @param toBit    Bitmask of the destination square.
     * @param color    Ordinal of the moving side.
     * @param captured Ordinal of the captured piece type.
     * @param promoTo  Ordinal of the piece type being promoted to.
     * @param pIdx     Index of the pawn bitboard.
     */
    private void promotionAndCapture(long fromBit, long toBit, int color, int captured, int promoTo, int pIdx) {

        bitboards[pIdx] ^= fromBit;
        bitboards[color + promoTo] ^= toBit;
        bitboards[captured] ^= toBit;

        occs[color] ^= (fromBit | toBit);
        occs[0x1 ^ color] ^= toBit;
        occs[2] ^= fromBit;
    }

    /**
     * Updates bitboards for a King-side castle, including the Rook shift.
     *
     * @param fromBit Bitmask of the King's start square.
     * @param toBit   Bitmask of the King's end square.
     * @param color   Ordinal of the moving side.
     * @param pIdx    Index of the King bitboard.
     */
    private void kingSideCastle(long fromBit, long toBit, int color, int pIdx) {

        int rookIndex = 6 + color;
        long rookPos = Square.H1.pos() << (color * 56);

        long moveMaskK = (fromBit | toBit);
        long moveMaskR = (rookPos | rookPos >>> 2);
        long moveMaskCombined = (moveMaskK | moveMaskR);

        bitboards[pIdx] ^= moveMaskK;
        bitboards[rookIndex] ^= moveMaskR;

        occs[color] ^= moveMaskCombined;
        occs[2] ^= moveMaskCombined;
    }

    /**
     * Updates bitboards for a Queen-side castle, including the Rook shift.
     *
     * @param fromBit Bitmask of the King's start square.
     * @param toBit   Bitmask of the King's end square.
     * @param color   Ordinal of the moving side.
     * @param pIdx    Index of the King bitboard.
     */
    private void queenSideCastle(long fromBit, long toBit, int color, int pIdx) {

        int rookIndex = 6 + color;
        long rookPos = Square.A1.pos() << (color * 56);

        long moveMaskK = (fromBit | toBit);
        long moveMaskR = (rookPos | rookPos << 3);
        long moveMaskCombined = (moveMaskK | moveMaskR);

        bitboards[pIdx] ^= moveMaskK;
        bitboards[rookIndex] ^= moveMaskR;

        occs[color] ^= moveMaskCombined;
        occs[2] ^= moveMaskCombined;
    }

    /**
     * Moves a pawn two squares forward and sets the en passant target square.
     *
     * @param fromBit Bitmask of the pawn's start square.
     * @param toBit   Bitmask of the pawn's end square.
     * @param color   Ordinal of the moving side.
     * @param pIdx    Index of the pawn bitboard.
     */
    private void pawnDoubleMove(long fromBit, long toBit, int color, int pIdx) {

        quietMove(fromBit, toBit, color, pIdx);

        switch (color) {
            case 0 -> enPassant = toBit >>> 8;
            case 1 -> enPassant = toBit << 8;
        }
    }

    /**
     * Executes an en passant capture, removing the opponent's pawn from the rank behind.
     *
     * @param fromBit Bitmask of the pawn's start square.
     * @param toBit   Bitmask of the pawn's end square.
     * @param color   Ordinal of the moving side.
     * @param pIdx    Index of the moving pawn bitboard.
     */
    private void enPassant(long fromBit, long toBit, int color, int pIdx) {

        int capColor = 0x1 ^ color;
        long moveMask = (fromBit | toBit);
        long capBit = toBit;

        switch (color) {
            case 0 -> capBit >>>= 8;
            case 1 -> capBit <<= 8;
        }

        bitboards[pIdx] ^= moveMask;
        bitboards[capColor] ^= capBit;

        occs[color] ^= moveMask;
        occs[capColor] ^= capBit;
        occs[2] ^= (moveMask | capBit);
    }

    /**
     * Updates the global castling rights by checking both source and destination squares.
     *
     * @param from The source square of the move.
     * @param to   The destination square of the move.
     */
    private void updateCastlingRights(int from, int to) {
        castlingRights &= (castlingMasks[from] & castlingMasks[to]);
    }

    /**
     * Retrieves the occupancy mask for the entire board.
     * @return A bitboard of all pieces.
     */
    public long getOccupancy() {
        return occs[2];
    }

    /**
     * Retrieves the occupancy mask for a specific color.
     *
     * @param color The side to query.
     * @return A bitboard of all pieces belonging to the specified color.
     */
    public long getColorOccupancy(SideColor color) {
        return occs[color.ordinal()];
    }

    /**
     * Retrieves the square currently available for an en passant capture.
     *
     * @return The target Square, or null if en passant is not available.
     */
    public Square getEnPassant() {
        return Square.getSquare(enPassant);
    }

    public long getPieces(int i) {
        return bitboards[i];
    }
}