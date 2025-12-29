package board;

import static constants.BoardConstants.*;
import static constants.Zobrist.*;

//TODO: add comments
//TODO: add unmake
//TODO: remove copy constructor

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
     * Occupancy bitboards.
     * Index 0: White pieces, Index 1: Black pieces, Index 2: Combined (all) pieces.
     */
    private long[] occupancy = {0xFFFFL, 0xFFFF000000000000L};

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

    private byte[] historyCastlingRights = new byte[MAX_GAME_LENGTH];
    private long[] historyHash = new long[MAX_GAME_LENGTH];
    private byte[] historyCaptures = new byte[MAX_GAME_LENGTH];
    private byte[] historyEnPassant = new byte[MAX_GAME_LENGTH];
    private short[] historyMoves = new short[MAX_GAME_LENGTH];
    private byte[] historyHalfMoves = new byte[MAX_GAME_LENGTH];
    int curMove = 0;

    private long zobristHash = STARTING_HASH;

    private int side = WHITE;

    private int halfMoveCounter = 0;

    static final int[] HALF_MOVE_RESET_MASK = {0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};

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

        System.arraycopy(other.pieceBB, 0, this.pieceBB, 0, this.pieceBB.length);
        System.arraycopy(other.occupancy, 0, this.occupancy, 0, this.occupancy.length);
        System.arraycopy(other.pieceAt, 0, this.pieceAt, 0, this.pieceAt.length);

        this.enPassantTarget = other.enPassantTarget;
        this.castlingRights = other.castlingRights;
    }

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

        //remove side, the previous Castling Rights and the previous enPassantTarget from the Hash
        zobristHash ^= SIDE_KEY ^ CASTLING_KEYS[castlingRights] ^ EN_PASSANT_KEYS[Long.numberOfTrailingZeros(enPassantTarget)];
        enPassantTarget = 0x0L;

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
        side ^= 1;

//        assert((occupancy[0] & occupancy[1]) == 0);
//        assert(pieceBB[10] != 0 && pieceBB[11] != 0);
    }

    private void quietMove(int from, int to) {

        long moveMask = (1L << from) | (1L << to);

        int movingPiece = pieceAt[from];

        pieceBB[movingPiece] ^= moveMask;

        pieceAt[from] = EMPTY_SQUARE;
        pieceAt[to] = (byte) movingPiece;

        occupancy[side] ^= moveMask;

        zobristHash ^= PIECE_SQUARE_KEYS[movingPiece * BOARD_SIZE + from]
                ^ PIECE_SQUARE_KEYS[movingPiece * BOARD_SIZE + to];

        halfMoveCounter += halfMoveCounter * HALF_MOVE_RESET_MASK[movingPiece] + 1; //Resets on pawn Moves
    }

    private void capture(int from, int to) {

        int oppSide = 1 ^ side;

        long toMask = 1L << to;
        long moveMask = 1L << from | toMask;

        int movingPiece = pieceAt[from];
        int capturedPiece = pieceAt[to];

        pieceBB[movingPiece] ^= moveMask;
        pieceBB[capturedPiece] ^= toMask;

        pieceAt[from] = EMPTY_SQUARE;
        pieceAt[to] = (byte) movingPiece;

        occupancy[side] ^= moveMask;
        occupancy[oppSide] ^= toMask;

        zobristHash ^= PIECE_SQUARE_KEYS[movingPiece * BOARD_SIZE + from]
                ^ PIECE_SQUARE_KEYS[movingPiece * BOARD_SIZE + to]
                ^ PIECE_SQUARE_KEYS[capturedPiece * BOARD_SIZE + to];

        halfMoveCounter = 1;
    }

    private void promotion(int from, int to, int moveType) {

        long fromMask = 1L << from;
        long toMask = 1L << to;
        long moveMask = fromMask | toMask;

        int movingPiece = pieceAt[from];
        int promotionPiece = side + Move.getPromotedPieceBase(moveType);

        pieceBB[movingPiece] ^= fromMask;
        pieceBB[promotionPiece] ^= toMask;

        pieceAt[from] = EMPTY_SQUARE;
        pieceAt[to] = (byte) promotionPiece;

        occupancy[side] ^= moveMask;

        zobristHash ^= PIECE_SQUARE_KEYS[movingPiece * BOARD_SIZE + from]
                ^ PIECE_SQUARE_KEYS[promotionPiece * BOARD_SIZE + to];

        halfMoveCounter = 1;
    }

    private void promotionAndCapture(int from, int to, int moveType) {

        int oppSide = 1 ^ side;

        long fromMask = 1L << from;
        long toMask = 1L << to;
        long moveMask = fromMask | toMask;

        int movingPiece = pieceAt[from];
        int capturedPiece = pieceAt[to];
        int promotionPiece = side + Move.getPromotedPieceBase(moveType);

        pieceBB[movingPiece] ^= fromMask;
        pieceBB[capturedPiece] ^= toMask;
        pieceBB[promotionPiece] ^= toMask;

        pieceAt[from] = EMPTY_SQUARE;
        pieceAt[to] = (byte) promotionPiece;

        occupancy[side] ^= moveMask;
        occupancy[oppSide] ^= toMask;

        zobristHash ^= PIECE_SQUARE_KEYS[movingPiece * BOARD_SIZE + from]
                ^ PIECE_SQUARE_KEYS[capturedPiece * BOARD_SIZE + to]
                ^ PIECE_SQUARE_KEYS[promotionPiece * BOARD_SIZE + to];

        halfMoveCounter = 1;
    }

    private void kingSideCastle(int from, int to) {

        int rookFrom = to + 1;
        int rookTo = to - 1;

        long moveMaskK = 1L << from | 1L << to;
        long moveMaskR = 1L << rookFrom | 1L << rookTo;
        long moveMaskCombined = (moveMaskK | moveMaskR);

        int movingKing = W_KING + side;
        int movingRook = W_ROOK + side;

        pieceBB[movingKing] ^= moveMaskK;
        pieceBB[movingRook] ^= moveMaskR;

        pieceAt[from] = EMPTY_SQUARE;
        pieceAt[to] = (byte) movingKing;

        pieceAt[rookFrom] = EMPTY_SQUARE;
        pieceAt[rookTo] = (byte) movingRook;

        occupancy[side] ^= moveMaskCombined;

        zobristHash ^= PIECE_SQUARE_KEYS[movingKing * BOARD_SIZE + from]
                ^ PIECE_SQUARE_KEYS[movingKing * BOARD_SIZE + to]
                ^ PIECE_SQUARE_KEYS[movingRook * BOARD_SIZE + rookFrom]
                ^ PIECE_SQUARE_KEYS[movingRook * BOARD_SIZE + rookTo];

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

        pieceBB[movingKing] ^= moveMaskK;
        pieceBB[movingRook] ^= moveMaskR;

        pieceAt[from] = EMPTY_SQUARE;
        pieceAt[to] = (byte) movingKing;

        pieceAt[rookFrom] = EMPTY_SQUARE;
        pieceAt[rookTo] = (byte) movingRook;

        occupancy[side] ^= moveMaskCombined;

        zobristHash ^= PIECE_SQUARE_KEYS[movingKing * BOARD_SIZE + from]
                ^ PIECE_SQUARE_KEYS[movingKing * BOARD_SIZE + to]
                ^ PIECE_SQUARE_KEYS[movingRook * BOARD_SIZE + rookFrom]
                ^ PIECE_SQUARE_KEYS[movingRook * BOARD_SIZE + rookTo];

        halfMoveCounter++;
    }

    private void pawnDoubleMove(int from, int to) {

        quietMove(from, to);

        switch (side) {
            case 0 -> enPassantTarget = (1L << to) >>> 8;
            case 1 -> enPassantTarget = (1L << to) << 8;
        }

        zobristHash ^= EN_PASSANT_KEYS[Long.numberOfTrailingZeros(enPassantTarget)];

        halfMoveCounter = 1;
    }

    private void enPassantCapture(int from, int to) {

        int oppSide = 1 ^ side;

        int captured = to;

        long toMask = 1L << to;
        long moveMask = 1L << from | toMask;
        long captureMask = toMask;

        int movingPiece = pieceAt[from];
        int capturedPiece = oppSide; //pawn indices align with side

        switch (side) {
            case 0 -> {
                captured = to - 8;
                captureMask >>>= 8;
            }
            case 1 -> {
                captured = to + 8;
                captureMask <<= 8;
            }
        }

        pieceBB[movingPiece] ^= moveMask;
        pieceBB[capturedPiece] ^= captureMask;

        pieceAt[from] = EMPTY_SQUARE;
        pieceAt[to] = (byte) movingPiece;
        pieceAt[captured] = EMPTY_SQUARE;

        occupancy[side] ^= moveMask;
        occupancy[oppSide] ^= captureMask;

        zobristHash ^= PIECE_SQUARE_KEYS[movingPiece * BOARD_SIZE + from]
                ^ PIECE_SQUARE_KEYS[capturedPiece * BOARD_SIZE + captured]
                ^ PIECE_SQUARE_KEYS[movingPiece * BOARD_SIZE + to];

        halfMoveCounter = 1;
    }

    private void updateCastlingRights(int from, int to) {
        castlingRights &= (CASTLING_MASK_BY_SQUARE[from] & CASTLING_MASK_BY_SQUARE[to]);
        zobristHash ^= CASTLING_KEYS[castlingRights];

    }

    /* ==========================================================================================
                                             unmake move
     ========================================================================================== */

    //TODO: refactor to make it faster
    public void unmakeMove1() {

        short move = historyMoves[--curMove];
        side ^= 1;

        int from = Move.getFrom(move);
        int to = Move.getTo(move);
        int captured = to;

        int moveType = Move.getMoveType(move);

        int movedPiece = pieceAt[to];
        byte capturedPiece = historyCaptures[curMove];

        long fromMask = 1L << from;
        long toMask = 1L << to;

        pieceBB[movedPiece] ^= fromMask | toMask;

        pieceAt[from] = (byte)movedPiece;
        pieceAt[to] = EMPTY_SQUARE;

        occupancy[side] ^= fromMask | toMask;

        //promotion
        if ((moveType & 0x8) != 0) {

            int pawnIndex = W_PAWN + side;

            pieceBB[movedPiece] ^= fromMask;
            pieceBB[pawnIndex] ^= fromMask;

            pieceAt[from] = (byte)pawnIndex;
        }

        //capture
        if ((moveType & 0x4) != 0) {

            long capturedMask = toMask;

            //enPassant
            if ((moveType & 0xD) == 0x5) {
                captured += (side == WHITE)? -8 : 8;
                capturedMask = 1L << captured;
            }

            pieceBB[capturedPiece] ^= capturedMask;

            pieceAt[captured] = capturedPiece;

            occupancy[1 ^ side] ^= capturedMask;
        }

        //castle
        if (moveType == 0x3 || moveType == 0x2) {

            int sideOffset = 56 * side;

            int rookFrom = A1 + sideOffset;
            int rookTo = D1 + sideOffset;

            //kingSide
            if (moveType == 0x2) {
                rookFrom = H1 + sideOffset;
                rookTo = F1 + sideOffset;
            }

            long rookMask = 1L << rookFrom | 1L << rookTo;

            pieceBB[W_ROOK + side] ^= rookMask;

            pieceAt[rookFrom] = (byte)(W_ROOK + side);
            pieceAt[rookTo] = EMPTY_SQUARE;

            occupancy[side] ^= rookMask;
        }

        zobristHash = historyHash[curMove];
        enPassantTarget = historyEnPassant[curMove];
        castlingRights = historyCastlingRights[curMove];
        halfMoveCounter = historyHalfMoves[curMove];
    }

    public void unmakeMove() {

        short move = historyMoves[--curMove];
        side ^= 1;

        int from = Move.getFrom(move);
        int to = Move.getTo(move);
        int moveType = Move.getMoveType(move);

        switch (moveType) {
            case 0, 1 -> unmakeQuit(from, to);
            case 2 -> unmakeKingCastle(from, to);
            case 3 -> unmakeQueenCastle(from, to);
            case 4 -> {}
            case 5 -> {}

            case 8, 9, 10, 11 -> {}

            case 12, 13, 14, 15 -> {}
        }

        zobristHash = historyHash[curMove];
        enPassantTarget = historyEnPassant[curMove];
        castlingRights = historyCastlingRights[curMove];
        halfMoveCounter = historyHalfMoves[curMove];
    }

    private void unmakeQuit(int from, int to) {

        long moveMask = 1L << from | 1L << to;

        int movedPiece = pieceAt[to];

        pieceBB[movedPiece] ^= moveMask;

        pieceAt[from] = (byte)movedPiece;
        pieceAt[to] = EMPTY_SQUARE;

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

        pieceBB[movingKing] ^= moveMaskK;
        pieceBB[movingRook] ^= moveMaskR;

        pieceAt[from] = (byte)movingKing;
        pieceAt[to] = EMPTY_SQUARE;

        pieceAt[rookFrom] = (byte)movingRook;
        pieceAt[rookTo] = EMPTY_SQUARE;

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

        pieceBB[movingKing] ^= moveMaskK;
        pieceBB[movingRook] ^= moveMaskR;

        pieceAt[from] = (byte)movingKing;
        pieceAt[to] = EMPTY_SQUARE;

        pieceAt[rookFrom] = (byte)movingRook;
        pieceAt[rookTo] = EMPTY_SQUARE;

        occupancy[side] ^= moveMaskCombined;
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