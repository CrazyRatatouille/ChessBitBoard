public class BitBoards {

    private long[] bitboards = {0xFF00L, 0x00FF000000000000L,   //wPawn     bPawn
                                0x0042L, 0x4200000000000000L,   //wKnight   bKnight
                                0x0024L, 0x2400000000000000L,   //wBishop   bBishop
                                0x0081L, 0x8100000000000000L,   //wRook     bRook
                                0x0008L, 0x0800000000000000L,   //wQueen    bQueen
                                0x0010L, 0x1000000000000000L};  //wKing     bKing

    private boolean[] rooksMovedFlags = {false, false, false, false}; // wARook, wHRook, bARook, bHRook
    private boolean[] kingsMovedFlags = {false, false}; //wKing, bKing
//
//    boolean wARookMoved = false;
//    boolean wHRookMoved = false;
//    boolean bARookMoved = false;
//    boolean bHRookMoved = false;
//
//    boolean wKingMoved = false;
//    boolean bKingMoved = false;

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
     * @param other the chessBoard to be copied
     */
    public BitBoards(BitBoards other) {

        for (int i = 0; i < other.bitboards.length; i++) {
            this.bitboards[i] = other.bitboards[i];
        }

        for (int i = 0; i < other.rooksMovedFlags.length; i++) {
            this.rooksMovedFlags[i] = other.rooksMovedFlags[i];
        }

        for (int i = 0; i < other.kingsMovedFlags.length; i++) {
            this.kingsMovedFlags = other.kingsMovedFlags;
        }
    }

    public void makeMove(Move move) {
        //TODO: get this working
    }
}