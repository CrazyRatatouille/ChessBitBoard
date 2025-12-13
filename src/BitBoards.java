public class BitBoards {

    private long[] bitboards = {0xFF00L, 0x00FF000000000000L,   //wPawn     bPawn
                                0x0042L, 0x4200000000000000L,   //wKnight   bKnight
                                0x0024L, 0x2400000000000000L,   //wBishop   bBishop
                                0x0081L, 0x8100000000000000L,   //wRook     bRook
                                0x0008L, 0x0800000000000000L,   //wQueen    bQueen
                                0x0010L, 0x1000000000000000L};  //wKing     bKing

    /**
     * files[0] (aFile) - files[7] (hFile)
     */
    private static final long[] files = {
            0x0101010101010101L, 0x0202020202020202L, 0x0404040404040404L, 0x0808080808080808L,
            0x1010101010101010L, 0x2020202020202020L, 0x4040404040404040L, 0x8080808080808080L};

    /**
     * ranks[0] (1st rank) - ranks[7] (8th rank)
     */
    private static final long[] ranks = {
            0xFFL, 0xFF00L, 0xFF0000, 0xFF000000L,
            0xFF00000000L, 0xFF0000000000L, 0xFF000000000000L, 0xFF00000000000000L};

    private long enPassant = 0x0L;
    private boolean[] rooksMovedFlags = {false, false, false, false}; // wARook, wHRook, bARook, bHRook
    private boolean[] kingsMovedFlags = {false, false}; //wKing, bKing

    private static final long Castling_Mask = Square.A1.pos() | Square.A8.pos()
                                            | Square.H1.pos() | Square.H8.pos()
                                            | Square.E1.pos() | Square.E8.pos();

    /**
     * This constructor sets up a new chess board with all Pieces on their home square.
     * A1 is the LSB and H8 being the MSB
     */
    public BitBoards(){}

    /**
     * This constructor sets up a new chess board and copies the game state of {@code other}.
     * The purpose of this constructor is to allow engines to create branches, to analyze the best moves without
     * altering the main bitboards.
     *
     * @param other the {@code BitBoards} to be copied
     */
    public BitBoards(BitBoards other) {

        System.arraycopy(other.bitboards, 0, this.bitboards, 0, this.bitboards.length);
        System.arraycopy(other.rooksMovedFlags, 0, this.rooksMovedFlags, 0, this.rooksMovedFlags.length);
        System.arraycopy(other.kingsMovedFlags, 0, this.kingsMovedFlags, 0, this.kingsMovedFlags.length);

        this.enPassant = other.enPassant;
    }

    public void makeMove(Move move) {

        int colorIndexAdjustment = move.color().ordinal();
        int pieceIndexAdjustment = move.pieceType().ordinal() * 2;

        long fromBit = move.From().pos();
        long toBit = move.To().pos();
        Move.MoveType moveType = move.moveType();

        PieceType promotionTo = move.promotionTo();
        PieceType capturedPiece = move.capturedPieceType();

        enPassant = 0x0L;

        //this is always executed, no matter what moveType
        int movingPieceIndex = colorIndexAdjustment + pieceIndexAdjustment;
        bitboards[movingPieceIndex] = (bitboards[movingPieceIndex] & ~fromBit) | toBit;

        switch (moveType) {
            case CAPTURE -> capture(toBit, move.color().other().ordinal() + capturedPiece.ordinal() * 2);
            case PAWN_DOUBLE_MOVE -> pawnDoubleMove(fromBit, colorIndexAdjustment);
            case KING_SIDE_CASTLE -> kingSideCastle(colorIndexAdjustment);
            case QUEEN_SIDE_CASTLE -> queenSideCastle(colorIndexAdjustment);
            case PROMOTION -> promotion(toBit, movingPieceIndex, colorIndexAdjustment + promotionTo.ordinal() * 2);
            case ENPASSANT -> enPassant(toBit, move.color());
        }

        //TODO: castling rights update
        updateCastlingRights(fromBit, toBit);
    }

    private void capture(long toBit, int capturedPieceIndex) {
        bitboards[capturedPieceIndex] &= ~toBit;
    }

    private void promotion(long toBit, int movingPieceIndex, int promotionPieceIndex) {
        bitboards[movingPieceIndex] &= ~toBit;
        bitboards[promotionPieceIndex] |= toBit;
    }

    private void kingSideCastle(int colorSideAdjustment) {

        int rookIndex = 6 + colorSideAdjustment;
        long rookPos = (colorSideAdjustment == 0)? Square.H1.pos() : Square.H8.pos();
        bitboards[rookIndex] = ((bitboards[rookIndex] & ~rookPos) | (rookPos >>> 2));
    }

    private void queenSideCastle(int colorSideAdjustment) {

        int rookIndex = 6 + colorSideAdjustment;
        long rookPos = (colorSideAdjustment == 0)? Square.A1.pos() : Square.A8.pos();
        bitboards[rookIndex] = ((bitboards[rookIndex] & ~rookPos) | (rookPos << 3));
    }

    private void pawnDoubleMove(long fromBit, int colorSideAdjustment) {

        switch (colorSideAdjustment) {
            case 0 -> enPassant = fromBit << 8;
            case 1 -> enPassant = fromBit >>> 8;
        }
    }

    private void enPassant(long toBit, SideColor color) {

        switch (color) {
            case White -> bitboards[1] &= ~(toBit >>> 8);
            case Black -> bitboards[0] &= ~(toBit << 8);
        }
    }

    private void updateCastlingRights(long fromBit, long toBit) {

    }
    
    /**
     * This method returns an occupancyMap of the whole chessBoard with all its Pieces
     *
     * @return the occupancyMap of the whole chessBoard with all its Pieces
     */
    public long getOccupancy() {

        long occ = 0L;

        for (long num : bitboards) {
            occ |= num;
        }

        return occ;
    }

    /**
     * This method returns an occupancyMap of the whole chessBoard with all its Pieces
     *
     * @param color the color of Pieces, whose occMap is to be returned
     * @return the occupancyMap of the whole chessBoard with all Pieces of the color {@code color}
     */
    public long getColorOccupancy(SideColor color) {

        long occ = 0L;

        for (int i = 0; i < 6; i++) {

            int index = color.ordinal() + (2 * i);

            occ |= bitboards[index];
        }

        return occ;
    }

    /**
     * This method returns the square which can be attacked due to enPassant. EnPassant is a rule in chess, where a pawn
     * who moved up two square, can be taken by an opposite pawn for the next turn, if and only if the pawn could have
     * been taken if it was only moved 1 square up the board
     *
     * @return the Square which can be attacked due to the enPassant - rule, null if there is no such square
     */
    public Square getEnPassant() {
        return Square.getSquare(enPassant);
    }
}