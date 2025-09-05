public class Bitboards {

    private final long[] Pieces = new long[12];
    private long enPassant = 0L;

    public Bitboards() {

        Pieces[0] = 0x000000000000FF00L;
        Pieces[1] = 0x0000000000000042L;
        Pieces[2] = 0x0000000000000024L;
        Pieces[3] = 0x0000000000000081L;
        Pieces[4] = 0x0000000000000010L;
        Pieces[5] = 0x0000000000000008L;
        Pieces[6] = 0x00FF000000000000L;
        Pieces[7] = 0x4200000000000000L;
        Pieces[8] = 0x2400000000000000L;
        Pieces[9] = 0x8100000000000000L;
        Pieces[10] = 0x1000000000000000L;
        Pieces[11] = 0x0800000000000000L;
    }

    ///returns the Ranks 1-8 in bitboard representation
    public long getRank(int index) {

        if (index < 0 || index > 7) throw new IndexOutOfBoundsException("index "  + index + " out of range [0, 8)");

        long firstRank = 0xFFL;
        return firstRank << (index * 8);
    }

    ///returns the Files A-H in bitboard representation
    public long getFile(int index) {

        if (index < 0 || index > 7) throw new IndexOutOfBoundsException("index "  + index + " out of range [0, 8)");

        long aFile = 0x8080808080808080L;
        return aFile >>> index;
    }

    ///returns the current bitboard for each PieceType (0-5) White and (6 - 11) Black
    ///0/6 Pawns  |  1/7 Knights  |  2/8 Bishops  |  3/9 Rooks  |  4/10 Queens  |  5/11 Kings
    public long getPieces(int index) {

        if (index < 0 || index > 12) throw new IndexOutOfBoundsException("index "  + index + " out of range [0, 12)");

        return Pieces[index];
    }

    ///returns the current bitboard for each PieceType (0-5) White and (6 - 11) Black
    ///0/6 Pawns  |  1/7 Knights  |  2/8 Bishops  |  3/9 Rooks  |  4/10 Queens  |  5/11 Kings
    public long getPieces(Color color, PieceType pieceType) {

        int indAdj = (color == Color.Black)? 0 : 6;
        int piece;

        if (pieceType == PieceType.Pawn) piece = 0;
        else if (pieceType == PieceType.Knight) piece = 1;
        else if (pieceType == PieceType.Bishop) piece = 2;
        else if (pieceType == PieceType.Rook) piece = 3;
        else if (pieceType == PieceType.Queen) piece = 4;
        else piece = 5;

        return Pieces[piece + indAdj];
    }

    public long getColorOcc(Color color) {

        return getPieces(color, PieceType.Pawn)
                | getPieces(color, PieceType.Knight)
                | getPieces(color, PieceType.Bishop)
                | getPieces(color, PieceType.Rook)
                | getPieces(color, PieceType.Queen)
                | getPieces(color, PieceType.King);
    }

    public long getOcc() {

        return getColorOcc(Color.White) | getColorOcc(Color.Black);
    }

    public long getEnPassant() {
        return enPassant;
    }

    public void setEnPassant(long newEnPassant) {

        if (Long.bitCount(newEnPassant) > 1) {
            throw new IllegalArgumentException("Can't have more than 1 active enPassant target");
        }

        this.enPassant = newEnPassant;
    }


}
