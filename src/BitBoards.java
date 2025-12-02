public class BitBoards {

    private long[] bitboards = {0xFF00L, 0x00FF000000000000L,   //wPawn     bPawn
                                0x0042L, 0x4200000000000000L,   //wKnight   bKnight
                                0x0024L, 0x2400000000000000L,   //wBishop   bBishop
                                0x0081L, 0x8100000000000000L,   //wRook     bRook
                                0x0008L, 0x0800000000000000L,   //wQueen    bQueen
                                0x0010L, 0x1000000000000000L};  //wKing     bKing

    private long enPassant = 0x0L;
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
     * @param other the {@code BitBoards} to be copied
     */
    public BitBoards(BitBoards other) {

        System.arraycopy(other.bitboards, 0, this.bitboards, 0, this.bitboards.length);
        System.arraycopy(other.rooksMovedFlags, 0, this.rooksMovedFlags, 0, this.rooksMovedFlags.length);
        System.arraycopy(other.kingsMovedFlags, 0, this.kingsMovedFlags, 0, this.kingsMovedFlags.length);

        this.enPassant = other.enPassant;
    }


    public void makeMove(Move move) {
        //TODO: get this working
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

            int index = ((color == SideColor.White)? 0 : 1) + (2 * i);

            occ |= bitboards[index];
        }

        return occ;
    }
}