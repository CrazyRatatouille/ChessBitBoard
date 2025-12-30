package board;

import static constants.BoardConstants.*;
import static constants.Zobrist.*;

//TODO: add comments

/**
 * Represents the state of a chess board using bitboard representation.
 * <p>
 * This class uses 64-bit {@code long} primitives to represent the positions of pieces,
 * occupancy masks, and special game states. It follows the Little-Endian Rank-File (LERF)
 * mapping where bit 0 is A1 and bit 63 is H8.
 * </p>
 */
public class BoardState {

    private byte[] pieceAt = {

            //RANK 1
            W_ROOK, W_KNIGHT, W_BISHOP, W_QUEEN, W_KING, W_BISHOP, W_KNIGHT, W_ROOK,

            //Rank 2
            W_PAWN, W_PAWN, W_PAWN, W_PAWN, W_PAWN, W_PAWN, W_PAWN, W_PAWN,

            //Ranks 3 - 6
            EMPTY_SQUARE, EMPTY_SQUARE, EMPTY_SQUARE, EMPTY_SQUARE, EMPTY_SQUARE, EMPTY_SQUARE, EMPTY_SQUARE, EMPTY_SQUARE,
            EMPTY_SQUARE, EMPTY_SQUARE, EMPTY_SQUARE, EMPTY_SQUARE, EMPTY_SQUARE, EMPTY_SQUARE, EMPTY_SQUARE, EMPTY_SQUARE,
            EMPTY_SQUARE, EMPTY_SQUARE, EMPTY_SQUARE, EMPTY_SQUARE, EMPTY_SQUARE, EMPTY_SQUARE, EMPTY_SQUARE, EMPTY_SQUARE,
            EMPTY_SQUARE, EMPTY_SQUARE, EMPTY_SQUARE, EMPTY_SQUARE, EMPTY_SQUARE, EMPTY_SQUARE, EMPTY_SQUARE, EMPTY_SQUARE,

            //Rank 7
            B_PAWN, B_PAWN, B_PAWN, B_PAWN, B_PAWN, B_PAWN, B_PAWN, B_PAWN,

            //Rank 8
            B_ROOK, B_KNIGHT, B_BISHOP, B_QUEEN, B_KING, B_BISHOP, B_KNIGHT, B_ROOK
    };

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

    /**
     * Occupancy bitboards.
     * Index 0: White pieces, Index 1: Black pieces
     */
    private long[] occupancy = {0xFFFFL, 0xFFFF000000000000L};

    private int side = WHITE;
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
    private long zobristHash = STARTING_HASH;

    private int halfMoveCounter = 0;
    int curMove = 0;

    //TODO: future optimization moving complete history into one long[]
    private final byte[] historyCastlingRights = new byte[MAX_GAME_LENGTH];
    private final long[] historyHash = new long[MAX_GAME_LENGTH];
    private final int[] historyCaptures = new int[MAX_GAME_LENGTH];
    private final long[] historyEnPassant = new long[MAX_GAME_LENGTH];
    private final short[] historyMoves = new short[MAX_GAME_LENGTH];
    private final int[] historyHalfMoves = new int[MAX_GAME_LENGTH];

    /* ==========================================================================================
                                              make move
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

        int from = Move.getFrom(move);
        int to = Move.getTo(move);
        int moveType = Move.getMoveType(move);

        historyHash[curMove] = zobristHash;
        historyCastlingRights[curMove] = castlingRights;
        historyHalfMoves[curMove] = halfMoveCounter;
        historyMoves[curMove] = move;
        historyEnPassant[curMove] = enPassantTarget;

        //remove side, the previous Castling Rights and the previous enPassantTarget from the Hash
        zobristHash ^= SIDE_KEY ^ CASTLING_KEYS[castlingRights] ^ EN_PASSANT_KEYS[Long.numberOfTrailingZeros(enPassantTarget)];
        enPassantTarget = 0;

        switch (moveType) {

            case 0 -> quietMove(from, to);
            case 1 -> pawnDoubleMove(from, to);
            case 2 -> kingSideCastle(from, to);
            case 3 -> queenSideCastle(from, to);
            case 4 -> capture(from, to);
            case 5 -> enPassantCapture(from, to);

            case 8, 9, 10, 11 -> promotion(from, to, moveType);

            case 12, 13, 14, 15 -> promotionAndCapture(from, to, moveType);
        }

        updateCastlingRights(from, to);
        
        side = 1 ^ side;
        curMove++;
        
//        assert((occupancy[0] & occupancy[1]) == 0);
//        assert(pieceBB[10] != 0 && pieceBB[11] != 0);
    }

    private void quietMove(int from, int to) {

        long fromMask = 1L << from;
        long toMask = 1L << to;
        long moveMask = fromMask | toMask;

        int movingPiece = pieceAt[from];

        pieceAt[from] = EMPTY_SQUARE;
        pieceAt[to] = (byte) movingPiece;

        pieceBB[movingPiece] ^= moveMask;

        occupancy[side] ^= moveMask;

        zobristHash ^= PIECE_SQUARE_KEYS[movingPiece * BOARD_SIZE + from]
                ^ PIECE_SQUARE_KEYS[movingPiece * BOARD_SIZE + to];

        historyCaptures[curMove] = EMPTY_SQUARE;

        halfMoveCounter *= HALF_MOVE_RESET_MASK[movingPiece];
}

    private void capture(int from, int to) {

        int oppSide = 1 ^ side;

        long fromMask = 1L << from;
        long toMask = 1L << to;
        long moveMask = fromMask | toMask;

        int movingPiece = pieceAt[from];
        int capturedPiece = pieceAt[to];

        pieceAt[from] = EMPTY_SQUARE;
        pieceAt[to] = (byte) movingPiece;

        pieceBB[movingPiece] ^= moveMask;
        pieceBB[capturedPiece] ^= toMask;

        occupancy[side] ^= moveMask;
        occupancy[oppSide] ^= toMask;

        zobristHash ^= PIECE_SQUARE_KEYS[movingPiece * BOARD_SIZE + from]
                ^ PIECE_SQUARE_KEYS[movingPiece * BOARD_SIZE + to]
                ^ PIECE_SQUARE_KEYS[capturedPiece * BOARD_SIZE + to];

        historyCaptures[curMove] = capturedPiece;

        halfMoveCounter = 0;
    }

    private void promotion(int from, int to, int moveType) {

        long fromMask = 1L << from;
        long toMask = 1L << to;
        long moveMask = fromMask | toMask;

        int movingPawn = side;
        int promotionPiece = side + Move.getPromotedPieceBase(moveType);

        pieceAt[from] = EMPTY_SQUARE;
        pieceAt[to] = (byte) promotionPiece;

        pieceBB[movingPawn] ^= fromMask;
        pieceBB[promotionPiece] ^= toMask;

        occupancy[side] ^= moveMask;

        zobristHash ^= PIECE_SQUARE_KEYS[movingPawn * BOARD_SIZE + from]
                ^ PIECE_SQUARE_KEYS[promotionPiece * BOARD_SIZE + to];

        historyCaptures[curMove] = EMPTY_SQUARE;

        halfMoveCounter = 0;
    }

    private void promotionAndCapture(int from, int to, int moveType) {

        int oppSide = 1 ^ side;

        long fromMask = 1L << from;
        long toMask = 1L << to;
        long moveMask = fromMask | toMask;

        int movingPawn = side;
        int capturedPiece = pieceAt[to];
        int promotionPiece = side + Move.getPromotedPieceBase(moveType);

        pieceAt[from] = EMPTY_SQUARE;
        pieceAt[to] = (byte) promotionPiece;

        pieceBB[movingPawn] ^= fromMask;
        pieceBB[capturedPiece] ^= toMask;
        pieceBB[promotionPiece] ^= toMask;

        occupancy[side] ^= moveMask;
        occupancy[oppSide] ^= toMask;

        zobristHash ^= PIECE_SQUARE_KEYS[movingPawn * BOARD_SIZE + from]
                ^ PIECE_SQUARE_KEYS[capturedPiece * BOARD_SIZE + to]
                ^ PIECE_SQUARE_KEYS[promotionPiece * BOARD_SIZE + to];

        historyCaptures[curMove] = capturedPiece;

        halfMoveCounter = 0;
    }

    private void kingSideCastle(int from, int to) {

        int rookFrom = to + 1;
        int rookTo = to - 1;

        long moveMaskK = 1L << from | 1L << to;
        long moveMaskR = 1L << rookFrom | 1L << rookTo;
        long moveMaskCombined = (moveMaskK | moveMaskR);

        int movingKing = W_KING + side;
        int movingRook = W_ROOK + side;

        pieceAt[from] = EMPTY_SQUARE;
        pieceAt[to] = (byte) movingKing;

        pieceAt[rookFrom] = EMPTY_SQUARE;
        pieceAt[rookTo] = (byte) movingRook;

        pieceBB[movingKing] ^= moveMaskK;
        pieceBB[movingRook] ^= moveMaskR;

        occupancy[side] ^= moveMaskCombined;

        zobristHash ^= PIECE_SQUARE_KEYS[movingKing * BOARD_SIZE + from]
                ^ PIECE_SQUARE_KEYS[movingKing * BOARD_SIZE + to]
                ^ PIECE_SQUARE_KEYS[movingRook * BOARD_SIZE + rookFrom]
                ^ PIECE_SQUARE_KEYS[movingRook * BOARD_SIZE + rookTo];

        historyCaptures[curMove] = EMPTY_SQUARE;

        halfMoveCounter++;
    }

    private void queenSideCastle(int from, int to) {

        int rookFrom = to - 2;
        int rookTo = to + 1;

        long moveMaskK = 1L << from | 1L << to;
        long moveMaskR = 1L << rookFrom | 1L << rookTo;
        long moveMaskCombined = (moveMaskK | moveMaskR);

        int movingKing = W_KING + side;
        int movingRook = W_ROOK + side;


        pieceAt[from] = EMPTY_SQUARE;
        pieceAt[to] = (byte) movingKing;

        pieceAt[rookFrom] = EMPTY_SQUARE;
        pieceAt[rookTo] = (byte) movingRook;

        pieceBB[movingKing] ^= moveMaskK;
        pieceBB[movingRook] ^= moveMaskR;

        occupancy[side] ^= moveMaskCombined;

        zobristHash ^= PIECE_SQUARE_KEYS[movingKing * BOARD_SIZE + from]
                ^ PIECE_SQUARE_KEYS[movingKing * BOARD_SIZE + to]
                ^ PIECE_SQUARE_KEYS[movingRook * BOARD_SIZE + rookFrom]
                ^ PIECE_SQUARE_KEYS[movingRook * BOARD_SIZE + rookTo];

        historyCaptures[curMove] = EMPTY_SQUARE;

        halfMoveCounter++;
    }

    private void pawnDoubleMove(int from, int to) {

        quietMove(from, to);

        switch (side) {
            case 0 -> enPassantTarget = (1L << to) >>> 8;
            case 1 -> enPassantTarget = (1L << to) << 8;
        }

        zobristHash ^= EN_PASSANT_KEYS[Long.numberOfTrailingZeros(enPassantTarget)];
    }

    private void enPassantCapture(int from, int to) {

        int oppSide = 1 ^ side;

        int captured = to;

        long fromMask = 1L << from;
        long toMask = 1L << to;
        long moveMask = fromMask | toMask;
        long capturedMask = toMask;

        int movingPawn = side;
        int capturedPawn = oppSide;

        switch (side) {
            case WHITE -> {
                captured = to - 8;
                capturedMask >>>= 8;
            }
            case BLACK -> {
                captured = to + 8;
                capturedMask <<= 8;
            }
        }

        pieceAt[from] = EMPTY_SQUARE;
        pieceAt[to] = (byte) movingPawn;
        pieceAt[captured] = EMPTY_SQUARE;

        pieceBB[movingPawn] ^= moveMask;
        pieceBB[capturedPawn] ^= capturedMask;

        occupancy[side] ^= moveMask;
        occupancy[oppSide] ^= capturedMask;

        zobristHash ^= PIECE_SQUARE_KEYS[movingPawn * BOARD_SIZE + from]
                ^ PIECE_SQUARE_KEYS[capturedPawn * BOARD_SIZE + captured]
                ^ PIECE_SQUARE_KEYS[movingPawn * BOARD_SIZE + to];

        historyCaptures[curMove] = capturedPawn;

        halfMoveCounter = 0;
    }

    private void updateCastlingRights(int from, int to) {
        castlingRights &= (CASTLING_MASK_BY_SQUARE[from] & CASTLING_MASK_BY_SQUARE[to]);
        zobristHash ^= CASTLING_KEYS[castlingRights];
    }

    /* ==========================================================================================
                                             unmake move
     ========================================================================================== */

    public void unmakeMove() {

        short move = historyMoves[--curMove];
        side = 1 ^ side;

        int from = Move.getFrom(move);
        int to = Move.getTo(move);
        int moveType = Move.getMoveType(move);

        switch (moveType) {
            case 0, 1 -> unmakeQuiet(from, to);
            case 2 -> unmakeKingCastle(from, to);
            case 3 -> unmakeQueenCastle(from, to);
            case 4 -> unmakeCapture(from, to);
            case 5 -> unmakeEnPassantCapture(from, to);

            case 8, 9, 10, 11 -> unmakePromotion(from, to, moveType);

            case 12, 13, 14, 15 -> unmakePromotionAndCapture(from, to, moveType);
        }

        zobristHash = historyHash[curMove];
        enPassantTarget = historyEnPassant[curMove];
        castlingRights = historyCastlingRights[curMove];
        halfMoveCounter = historyHalfMoves[curMove];
    }

    private void unmakeQuiet(int from, int to) {

        long fromMask = 1L << from;
        long toMask = 1L << to;
        long moveMask = fromMask | toMask;

        int movingPiece = pieceAt[to];

        pieceAt[from] = (byte) movingPiece;
        pieceAt[to] = EMPTY_SQUARE;

        pieceBB[movingPiece] ^= moveMask;

        occupancy[side] ^= moveMask;
    }

    private void unmakeKingCastle(int from, int to) {

        int rookFrom = to + 1;
        int rookTo = to - 1;

        long moveMaskK = 1L << from | 1L << to;
        long moveMaskR = 1L << rookFrom | 1L << rookTo;
        long moveMaskCombined = (moveMaskK | moveMaskR);

        int movingKing = W_KING + side;
        int movingRook = W_ROOK + side;

        pieceAt[from] = (byte) movingKing;
        pieceAt[to] = EMPTY_SQUARE;

        pieceAt[rookFrom] = (byte) movingRook;
        pieceAt[rookTo] = EMPTY_SQUARE;

        pieceBB[movingKing] ^= moveMaskK;
        pieceBB[movingRook] ^= moveMaskR;

        occupancy[side] ^= moveMaskCombined;
    }

    private void unmakeQueenCastle(int from, int to) {

        int rookFrom = to - 2;
        int rookTo = to + 1;

        long moveMaskK = 1L << from | 1L << to;
        long moveMaskR = 1L << rookFrom | 1L << rookTo;
        long moveMaskCombined = (moveMaskK | moveMaskR);

        int movingKing = W_KING + side;
        int movingRook = W_ROOK + side;

        pieceAt[from] = (byte) movingKing;
        pieceAt[to] = EMPTY_SQUARE;

        pieceBB[movingKing] ^= moveMaskK;
        pieceBB[movingRook] ^= moveMaskR;

        pieceAt[rookFrom] = (byte) movingRook;
        pieceAt[rookTo] = EMPTY_SQUARE;

        occupancy[side] ^= moveMaskCombined;
    }

    private void unmakeCapture(int from, int to) {

        int oppSide = 1 ^ side;

        long fromMask = 1L << from;
        long toMask = 1L << to;
        long moveMask = fromMask | toMask;

        int movingPiece = pieceAt[to];
        int capturedPiece = historyCaptures[curMove];

        pieceAt[from] = (byte) movingPiece;
        pieceAt[to] = (byte) capturedPiece;

        pieceBB[movingPiece] ^= moveMask;
        pieceBB[capturedPiece] ^= toMask;

        occupancy[side] ^= moveMask;
        occupancy[oppSide] ^= toMask;
    }

    private void unmakeEnPassantCapture(int from, int to) {

        int oppSide = 1 ^ side;

        int captured = to;

        long fromMask = 1L << from;
        long toMask = 1L << to;
        long moveMask = fromMask | toMask;
        long capturedMask = toMask;

        int movingPawn = side;
        int capturedPawn = oppSide;

        switch (side) {
            case WHITE -> {
                captured -= 8;
                capturedMask >>>= 8;
            }
            case BLACK -> {
                captured += 8;
                capturedMask <<= 8;
            }
        }

        pieceAt[from] = (byte) movingPawn;
        pieceAt[to] = EMPTY_SQUARE;
        pieceAt[captured] = (byte) (movingPawn ^ 1);

        pieceBB[movingPawn] ^= moveMask;
        pieceBB[capturedPawn] ^= capturedMask;

        occupancy[side] ^= moveMask;
        occupancy[oppSide] ^= capturedMask;
    }

    private void unmakePromotion(int from, int to, int moveType) {

        long fromMask = 1L << from;
        long toMask = 1L << to;
        long moveMask = fromMask | toMask;

        int movingPawn = side;
        int promotedPiece = side + Move.getPromotedPieceBase(moveType);

        pieceAt[from] = (byte) movingPawn;
        pieceAt[to] = EMPTY_SQUARE;

        pieceBB[promotedPiece] ^= toMask;
        pieceBB[movingPawn] ^= fromMask;

        occupancy[side] ^= moveMask;
    }

    private void unmakePromotionAndCapture(int from, int to, int moveType) {

        int oppSide = 1 ^ side;

        long fromMask = 1L << from;
        long toMask = 1L << to;
        long moveMask = fromMask | toMask;

        int movingPawn = side;
        int promotedPiece = side + Move.getPromotedPieceBase(moveType);
        int capturedPiece = historyCaptures[curMove];

        pieceAt[from] = (byte) movingPawn;
        pieceAt[to] = (byte) capturedPiece;

        pieceBB[movingPawn] ^= fromMask;
        pieceBB[capturedPiece] ^= toMask;
        pieceBB[promotedPiece] ^= toMask;

        occupancy[side] ^= moveMask;
        occupancy[oppSide] ^= toMask;
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
        return occupancy[WHITE] | occupancy[BLACK];
    }

    /**
     * Retrieves the occupancy mask for a specific color.
     *
     * @param color The side to query (0) = WHITE | (1) = BLACK.
     * @return A bitboard of all pieces belonging to the specified color.
     */
    public long getColorOccupancy(int color) {
        return occupancy[color];
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