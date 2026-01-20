package board;

import java.util.Arrays;

import static constants.BoardConstants.*;
import static constants.Zobrist.*;

/**
 * Represents the state of a chess board using bitboard representation.
 * <p>
 * This class uses 64-bit {@code long} primitives to represent the positions of pieces,
 * occupancy masks, and special game states. It follows the Little-Endian Rank-File (LERF)
 * mapping where bit 0 is A1 and bit 63 is H8.
 * </p>
 */
public class BoardState {

    private int[] pieceAt = {

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
    private int fullMoveCounter = 1;
    private int curMove = 0;

    //TODO: future optimization moving complete history into one long[]
    //history stacks for all data needed to unmake a move
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
     * The encoding follows the standard 16-bit move encoding {@link Move}.
     * It packs the Source Square, Destination Square, and Move Type (promotion, capture, etc.)
     * into a single short/int for cache efficiency.
     * </p>
     *
     * @param move The 16-bit encoded move.
     */
    public void makeMove(short move) {

        int from = Move.getFrom(move);
        int to = Move.getTo(move);
        int moveType = Move.getMoveType(move);

        //current state pushed into history stack
        historyHash[curMove] = zobristHash;
        historyCastlingRights[curMove] = castlingRights;
        historyHalfMoves[curMove] = halfMoveCounter;
        historyMoves[curMove] = move;
        historyEnPassant[curMove] = enPassantTarget;

        //removes side, the previous Castling Rights and the previous enPassantTarget from the Hash
        zobristHash ^= SIDE_KEY ^ CASTLING_KEYS[castlingRights] ^ EN_PASSANT_KEYS[Long.numberOfTrailingZeros(enPassantTarget)];
        enPassantTarget = 0;

        switch (moveType) {

            case QUIET_MOVE -> quietMove(from, to);
            case DOUBLE_PAWN_PUSH -> pawnDoubleMove(from, to);
            case KING_SIDE_CASTLE -> kingSideCastle(from, to);
            case QUEEN_SIDE_CASTLE -> queenSideCastle(from, to);
            case CAPTURE -> capture(from, to);
            case EP_CAPTURE -> enPassantCapture(from, to);

            case PROMO_N, PROMO_B, PROMO_R, PROMO_Q -> promotion(from, to, moveType);

            case PROMO_CAP_N, PROMO_CAP_B, PROMO_CAP_R, PROMO_CAP_Q -> promotionAndCapture(from, to, moveType);
        }

        updateCastlingRights(from, to);
        
        fullMoveCounter += side;
        side = 1 ^ side;
        curMove++;
    }

    private void quietMove(int from, int to) {

        long fromMask = 1L << from;
        long toMask = 1L << to;
        long moveMask = fromMask | toMask;

        int movingPiece = pieceAt[from];

        pieceAt[from] = EMPTY_SQUARE;
        pieceAt[to] = movingPiece;

        pieceBB[movingPiece] ^= moveMask;

        occupancy[side] ^= moveMask;

        zobristHash ^= PIECE_SQUARE_KEYS[movingPiece * BOARD_SIZE + from]
                ^ PIECE_SQUARE_KEYS[movingPiece * BOARD_SIZE + to];

        historyCaptures[curMove] = EMPTY_SQUARE;

        //Reset half-move counter if pawn move, otherwise increment
        halfMoveCounter = (halfMoveCounter + 1) * HALF_MOVE_RESET_MASK[movingPiece];
}

    private void capture(int from, int to) {

        int oppSide = 1 ^ side;

        long fromMask = 1L << from;
        long toMask = 1L << to;
        long moveMask = fromMask | toMask;

        int movingPiece = pieceAt[from];
        int capturedPiece = pieceAt[to];

        pieceAt[from] = EMPTY_SQUARE;
        pieceAt[to] = movingPiece;

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
        pieceAt[to] = promotionPiece;

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
        pieceAt[to] = promotionPiece;

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
        pieceAt[to] = movingKing;

        pieceAt[rookFrom] = EMPTY_SQUARE;
        pieceAt[rookTo] = movingRook;

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
        pieceAt[to] = movingKing;

        pieceAt[rookFrom] = EMPTY_SQUARE;
        pieceAt[rookTo] = movingRook;

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
        pieceAt[to] = movingPawn;
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

    /**
     * Restores the board state to the position before the last move was made.
     * <p>
     * This method pops the previous state (hash, castling rights, en passant target, etc.)
     * from the history stacks and reverses the bitboard updates performed by {@code makeMove}.
     */
    public void unmakeMove() {

        short move = historyMoves[--curMove];
        side = 1 ^ side;

        int from = Move.getFrom(move);
        int to = Move.getTo(move);
        int moveType = Move.getMoveType(move);

        switch (moveType) {
            case QUIET_MOVE, DOUBLE_PAWN_PUSH -> unmakeQuiet(from, to);
            case KING_SIDE_CASTLE -> unmakeKingCastle(from, to);
            case QUEEN_SIDE_CASTLE -> unmakeQueenCastle(from, to);
            case CAPTURE -> unmakeCapture(from, to);
            case EP_CAPTURE -> unmakeEnPassantCapture(from, to);

            case PROMO_N, PROMO_B, PROMO_R, PROMO_Q -> unmakePromotion(from, to, moveType);

            case PROMO_CAP_N, PROMO_CAP_B, PROMO_CAP_R, PROMO_CAP_Q -> unmakePromotionAndCapture(from, to, moveType);
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

        pieceAt[from] = movingPiece;
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

        pieceAt[from] = movingKing;
        pieceAt[to] = EMPTY_SQUARE;

        pieceAt[rookFrom] = movingRook;
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

        pieceAt[from] = movingKing;
        pieceAt[to] = EMPTY_SQUARE;

        pieceBB[movingKing] ^= moveMaskK;
        pieceBB[movingRook] ^= moveMaskR;

        pieceAt[rookFrom] = movingRook;
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

        pieceAt[from] = movingPiece;
        pieceAt[to] = capturedPiece;

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

        pieceAt[from] = movingPawn;
        pieceAt[to] = EMPTY_SQUARE;
        pieceAt[captured] = 1 ^ movingPawn;

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

        pieceAt[from] = movingPawn;
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

        pieceAt[from] = movingPawn;
        pieceAt[to] = capturedPiece;

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
     * Retrieves the piece type currently residing on a specific square.
     *
     * @param square the square index (0-63)
     * @return the piece identifier (e.g., {@code W_PAWN}), or {@code EMPTY_SQUARE}
     */
    public int pieceAt(int square) {
        return pieceAt[square];
    }

    /**
     * Retrieves the bitboard for a specific piece type.
     *
     * @param piece the piece index (e.g., {@code W_PAWN}, {@code B_KING})
     * @return the bitboard for that piece
     */
    public long getPieceBB(int piece) {
        return pieceBB[piece];
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
     * Retrieves the occupancy mask for the entire board.
     *
     * @return A bitboard of all pieces.
     */
    public long getOccupancy() {
        return occupancy[WHITE] | occupancy[BLACK];
    }

    /**
     * Gets the side currently to move.
     *
     * @return {@code WHITE} (0) or {@code BLACK} (1)
     */
    public int getSide() {
        return side;
    }

    /**
     * Retrieves the square currently available for an en passant capture.
     *
     * @return The target Square, or null if en passant is not available.
     */
    public long getEnPassantTarget() {
        return enPassantTarget;
    }

    /**
     * Retrieves the current castling rights bitmask.
     *
     * @return a 4-bit integer representing {BlackQueen, BlackKing, WhiteQueen, WhiteKing} rights
     */
    public int castlingRights() {
        return castlingRights;
    }

    /* ==========================================================================================
                                            state setters
     ========================================================================================== */

    /**
     * Resets the board to a completely empty state.
     * <p>
     * Clears all bitboards, occupancy masks, history stacks, and counters. <br>
     * <b>Note:</b> This does not set up the starting chess position; it creates a void board.
     */
    public void clear() {
        Arrays.fill(pieceAt, EMPTY_SQUARE);
        Arrays.fill(pieceBB, 0L);
        Arrays.fill(occupancy, 0L);

        side = WHITE;
        enPassantTarget = 0L;
        castlingRights = 0;
        zobristHash = 0L;

        // Reset Move Counters
        halfMoveCounter = 0;
        fullMoveCounter = 1;
        curMove = 0;
    }

    /**
     * Places a piece on the board via the mailbox array.
     * <p>
     * <b>Note:</b> This only updates the {@code pieceAt} array. To fully update the board state,
     * you must also update the corresponding bitboards and occupancy.
     *
     * @param square the square index (0-63)
     * @param piece the piece identifier
     */
    public void setPieceAt(int square, int piece) {
        pieceAt[square] = piece;
    }

    /**
     * Toggles specific bits in a piece's bitboard.
     * <p>
     * Uses XOR ({@code ^}) to flip the bits in the {@code bbMask}.
     *
     * @param piece the piece index
     * @param bbMask the mask of bits to toggle
     */
    public void updatePieceBB(int piece, long bbMask) {
        pieceBB[piece] ^= bbMask;
    }

    /**
     * Sets the entire bitboard for a specific piece.
     *
     * @param piece the piece index
     * @param newPieceBB the new bitboard configuration
     */
    public void setPieceBB(int piece, long newPieceBB){
        pieceBB[piece] = newPieceBB;
    }

    /**
     * Toggles specific bits in the occupancy mask for a side.
     * <p>
     * Uses XOR ({@code ^}) to flip the bits in the {@code mask}.
     *
     * @param side the side index ({@code WHITE} or {@code BLACK})
     * @param mask the mask of bits to toggle
     */
    public void updateOccupancy(int side, long mask) {
        occupancy[side] ^= mask;
    }

    /**
     * Sets the entire occupancy bitboard for a specific side.
     *
     * @param side the side index ({@code WHITE} or {@code BLACK})
     * @param newOccupancy the new occupancy bitboard configuration
     */
    public void setOccupancy(int side, long newOccupancy) {
        occupancy[side] = newOccupancy;
    }

    /**
     * Sets the side currently to move.
     *
     * @param newSide the new side ({@code WHITE} or {@code BLACK})
     */
    public void setSide(int newSide) {
        side = newSide;
    }

    /**
     * Sets the en passant target square.
     *
     * @param newEnPassantTarget a bitboard with a single bit set at the target square,
     * or 0 if no en passant is available
     */
    public void setEnPassantTarget(long newEnPassantTarget) {
        enPassantTarget = newEnPassantTarget;
    }

    /**
     * Sets the castling rights bitmask.
     *
     * @param newCastlingRights the new rights configuration (typically 0-15)
     */
    public void setCastlingRights(byte newCastlingRights) {
        castlingRights = newCastlingRights;
    }

    /**
     * Updates the Zobrist hash by XORing it with the provided mask.
     *
     * @param zobristMask the hash key to apply
     */
    public void updateZobristHash(long zobristMask) {
        zobristHash ^= zobristMask;
    }

    /**
     * Sets the Zobrist hash directly.
     *
     * @param newZobristHash the new 64-bit hash key
     */
    public void setZobristHash(long newZobristHash) {
        zobristHash = newZobristHash;
    }

    /**
     * Sets the half-move counter used for the 50-move rule.
     *
     * @param newHalfMoveCounter the number of half-moves since the last capture or pawn push
     */
    public void setHalfMoveCounter(int newHalfMoveCounter) {
        halfMoveCounter = newHalfMoveCounter;
    }

    /**
     * Sets the full-move counter.
     *
     * @param newFullMoveCounter the new full-move number
     */
    public void setFullMoveCounter(int newFullMoveCounter) {
        fullMoveCounter = newFullMoveCounter;
    }
}