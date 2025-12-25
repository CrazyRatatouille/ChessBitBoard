package board;

import static constants.BoardConstants.*;

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
    private long[] pieceBB = {
            0xFF00L, 0x00FF000000000000L,
            0x0042L, 0x4200000000000000L,
            0x0024L, 0x2400000000000000L,
            0x0081L, 0x8100000000000000L,
            0x0008L, 0x0800000000000000L,
            0x0010L, 0x1000000000000000L
    };

    private byte[] pieceAt = {
            6, 2, 4, 8, 10, 4, 2, 6,
            0, 0, 0, 0, 0, 0, 0, 0,
            -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1,
            1, 1, 1, 1, 1, 1, 1, 1,
            7, 3, 5, 9, 11, 5, 3, 7
    };

    /**
     * Occupancy bitboards.
     * Index 0: White pieces, Index 1: Black pieces, Index 2: Combined (all) pieces.
     */
    private long[] occupancy = {0xFFFFL, 0xFFFF000000000000L, 0xFFFF00000000FFFFL};

    /**
     * Bitmask representing the single square available for an en passant capture.
     */
    private long enPassantTarget = 0x0L;

    /**
     * bit 1 (LSB): wQueenSide <br>
     * bit 2 : wKingSide <br>
     * bit 3 : bQueenSide <br>
     * bit 4 (MSB) : bKingSide
     */
    private byte castlingRights = 0xF;

    /**
     * Default constructor. Initializes the board to the standard starting position.
     */
    public BoardState() {
    }

    //TODO: remove copy constructor
    /**
     * Deep-copy constructor for state branching during search.
     * * @param other The source board to copy from.
     */
    public BoardState(BoardState other) {

        System.arraycopy(other.pieceBB, 0, this.pieceBB, 0, this.pieceBB.length);
        System.arraycopy(other.occupancy, 0, this.occupancy, 0, this.occupancy.length);
        System.arraycopy(other.pieceAt, 0, this.pieceAt, 0, this.pieceAt.length);

        this.enPassantTarget = other.enPassantTarget;
        this.castlingRights = other.castlingRights;
    }

    /* ==========================================================================================
                                            state updates
     ========================================================================================== */

    /**
     * Executes a move on the board using a 16-bit encoded integer.
     * <p>
     * The encoding follows the standard "From-To" layout. Bits [0,5] to, [6,11] from, 14 Capture, 15 Promotion.
     * It packs the Source Square, Destination Square, and Move Type (promotion, capture, etc.)
     * into a single short/int for cache efficiency.
     * </p>
     *
     * @param move The 16-bit encoded move.
     * @see <a href="https://www.chessprogramming.org/Encoding_Moves">Chess Programming Wiki: Encoding Moves</a>
     */
    public void makeMove(short move) {

        //TODO: add utility Class Move and refactor

        int from = (move >>> 6) & 0x3F;
        int to = move & 0x3F;
        int moveType = (move & 0xF000) >>> 12;

        long fromMask = 1L << from;
        long toMask = 1L << to;
        long moveMask = (fromMask | toMask);

        int movingPiece = pieceAt[from];
        int capturedPiece = pieceAt[to];
        int promotionBase = 2 + ((moveType & 0x3) << 1);

        int side = movingPiece & 1;

        enPassantTarget = 0x0L;

        switch (moveType) {

            case 0 -> quietMove(to, movingPiece, moveMask, side);
            case 1 -> pawnDoubleMove(to, toMask, moveMask, movingPiece, side);
            case 2 -> kingSideCastle(to, moveMask, movingPiece, side);
            case 3 -> queenSideCastle(to, moveMask, movingPiece, side);
            case 4 -> capture(to, fromMask, toMask, moveMask, movingPiece, capturedPiece, side);
            case 5 -> enPassantCapture(to, toMask, moveMask, movingPiece, side);

            case 8, 9, 10, 11 -> promotion(to, fromMask, toMask, moveMask, movingPiece, promotionBase, side);

            case 12, 13, 14, 15 -> promotionAndCapture(to, fromMask, toMask, moveMask, movingPiece, capturedPiece, promotionBase, side);
        }

        pieceAt[from] = -1;

        updateCastlingRights(from, to);

//        assert((occs[0] | occs[1]) == occs[2]);
//        assert((occs[0] & occs[1]) == 0);
//        assert(bitboards[10] != 0 && bitboards[11] != 0);
    }

    private void quietMove(int to, int movingPiece, long moveMask, int side) {

        pieceBB[movingPiece] ^= moveMask;

        pieceAt[to] = (byte)movingPiece;

        occupancy[side] ^= moveMask;
        occupancy[BOTH] ^= moveMask;
    }

    private void capture(int to, long fromMask, long toMask, long moveMask, int movingPiece, int capturedPiece, int side) {

        pieceBB[movingPiece] ^= moveMask;
        pieceBB[capturedPiece] ^= toMask;

        pieceAt[to] = (byte)movingPiece;

        occupancy[side] ^= moveMask;
        occupancy[0x1 ^ side] ^= toMask;
        occupancy[BOTH] ^= fromMask;
    }

    private void promotion(int to, long fromMask, long toMask, long moveMask, int movingPiece, int promotionBase, int side) {

        byte promotionPiece = (byte)(side + promotionBase);

        pieceBB[movingPiece] ^= fromMask;
        pieceBB[promotionPiece] ^= toMask;

        pieceAt[to] = promotionPiece;

        occupancy[side] ^= moveMask;
        occupancy[BOTH] ^= moveMask;
    }

    private void promotionAndCapture(int to, long fromMask, long toMask, long moveMask, int movingPiece
            , int capturedPiece, int promotionBase, int side) {

        byte promotionPiece = (byte)(side + promotionBase);

        pieceBB[movingPiece] ^= fromMask;
        pieceBB[promotionPiece] ^= toMask;
        pieceBB[capturedPiece] ^= toMask;

        pieceAt[to] = promotionPiece;

        occupancy[side] ^= moveMask;
        occupancy[0x1 ^ side] ^= toMask;
        occupancy[BOTH] ^= fromMask;
    }

    private void kingSideCastle(int to, long moveMaskKing, int movingPiece, int side) {

        byte rookIndex = (byte)(W_ROOK + side);
        long rookPos = SQUARE_BB[A7] << (side * 56);

        long moveMaskR = (rookPos | rookPos >>> 2);
        long moveMaskCombined = (moveMaskKing | moveMaskR);

        pieceBB[movingPiece] ^= moveMaskKing;
        pieceBB[rookIndex] ^= moveMaskR;

        pieceAt[to] = (byte)(movingPiece);
        pieceAt[to - 1] = rookIndex;
        pieceAt[to + 1] = -1;

        occupancy[side] ^= moveMaskCombined;
        occupancy[BOTH] ^= moveMaskCombined;
    }

    private void queenSideCastle(int to, long moveMaskKing, int movingPiece, int side) {

        byte rookIndex = (byte)(W_ROOK + side);
        long rookPos = SQUARE_BB[A1] << (side * 56);

        long moveMaskR = (rookPos | rookPos << 3);
        long moveMaskCombined = (moveMaskKing | moveMaskR);

        pieceBB[movingPiece] ^= moveMaskKing;
        pieceBB[rookIndex] ^= moveMaskR;

        pieceAt[to] = (byte)(movingPiece);
        pieceAt[to + 1] = rookIndex;
        pieceAt[to - 2] = -1;

        occupancy[side] ^= moveMaskCombined;
        occupancy[BOTH] ^= moveMaskCombined;
    }

    private void pawnDoubleMove(int to, long toMask, long moveMask, int movingPiece,  int side) {

        quietMove(to, movingPiece, moveMask, side);

        switch (side) {
            case 0 -> enPassantTarget = toMask >>> 8;
            case 1 -> enPassantTarget = toMask << 8;
        }
    }

    private void enPassantCapture(int to, long toMask, long moveMask, int movingPiece, int side) {

        int oppSide = 0x1 ^ side;
        long capBit = toMask;

        switch (side) {
            case 0 -> {
                capBit >>>= 8;
                pieceAt[to - 8] = -1;
            }
            case 1 -> {
                capBit <<= 8;
                pieceAt[to + 8] = -1;
            }
        }

        pieceBB[movingPiece] ^= moveMask;
        pieceBB[oppSide] ^= capBit; //pawn indices align with side

        pieceAt[to] = (byte)movingPiece;

        occupancy[side] ^= moveMask;
        occupancy[oppSide] ^= capBit;
        occupancy[BOTH] ^= (moveMask | capBit);
    }

    private void updateCastlingRights(int from, int to) {
        castlingRights &= (CASTLING_MASK_BY_SQUARE[from] & CASTLING_MASK_BY_SQUARE[to]);

    }

    /* ==========================================================================================
                                            state getters
     ========================================================================================== */

    /**
     * Retrieves the occupancy mask for the entire board.
     *
     * @return A bitboard of all pieces.
     */
    public long getOccupancy() {
        return occupancy[BOTH];
    }

    /**
     * Retrieves the occupancy mask for a specific color.
     *
     * @param color The side to query.
     * @return A bitboard of all pieces belonging to the specified color.
     */
    public long getColorOccupancy(SideColor color) {
        return occupancy[color.ordinal()];
    }

    /**
     * Retrieves the square currently available for an en passant capture.
     *
     * @return The target Square, or null if en passant is not available.
     */
    public long getEnPassantTarget() {
        return enPassantTarget;
    }

    public long getPieces(int i) {
        return pieceBB[i];
    }
}