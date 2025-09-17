import java.util.Arrays;

public class Bitboards {

    private long[] Pieces;
    private long enPassant = 0L;
    private static long aFile = 0x8080808080808080L;
    private static long firstRank = 0xFFL;

    private boolean a1RookMoved;
    private boolean h1RookMoved;
    private boolean a8RookMoved;
    private boolean h8RookMoved;
    private boolean wKingMoved;
    private boolean bKingMoved;


    public Bitboards() {

        Pieces = new long[12];

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

        a1RookMoved = false;
        h1RookMoved = false;
        a8RookMoved = false;
        h8RookMoved = false;
        wKingMoved = false;
        bKingMoved = false;
    }

    public Bitboards(Bitboards bitboard) {
        this.Pieces = Arrays.copyOf(bitboard.Pieces,12);

        this.a1RookMoved = bitboard.a1RookMoved;
        this.a8RookMoved = bitboard.a8RookMoved;
        this.h1RookMoved = bitboard.h1RookMoved;
        this.h8RookMoved = bitboard.h8RookMoved;
        this.wKingMoved = bitboard.wKingMoved;
        this.bKingMoved = bitboard.bKingMoved;
    }

    ///returns the Ranks 1-8 in bitboard representation
    public long getRank(int index) {

        if (index < 0 || index > 7) throw new IndexOutOfBoundsException("index "  + index + " out of range [0, 8)");

        return firstRank << (index * 8);
    }

    ///returns the Files A-H in bitboard representation
    public long getFile(int index) {

        if (index < 0 || index > 7) throw new IndexOutOfBoundsException("index "  + index + " out of range [0, 8)");

        return aFile >>> index;
    }

    ///returns the current bitboard for each PieceType (0-5) White and (6 - 11) Black
    ///0/6 Pawns  |  1/7 Knights  |  2/8 Bishops  |  3/9 Rooks  |  4/10 Queens  |  5/11 Kings
    public long getPieces(SideColor sideColor, PieceType pieceType) {

        int indAdj = (sideColor == SideColor.Black)? 0 : 6;
        int piece;

        if (pieceType == PieceType.Pawn) piece = 0;
        else if (pieceType == PieceType.Knight) piece = 1;
        else if (pieceType == PieceType.Bishop) piece = 2;
        else if (pieceType == PieceType.Rook) piece = 3;
        else if (pieceType == PieceType.Queen) piece = 4;
        else piece = 5;

        return Pieces[piece + indAdj];
    }

    public long getColorOcc(SideColor sideColor) {

        return getPieces(sideColor, PieceType.Pawn)
                | getPieces(sideColor, PieceType.Knight)
                | getPieces(sideColor, PieceType.Bishop)
                | getPieces(sideColor, PieceType.Rook)
                | getPieces(sideColor, PieceType.Queen)
                | getPieces(sideColor, PieceType.King);
    }

    public long getOcc() {

        return getColorOcc(SideColor.White) | getColorOcc(SideColor.Black);
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

    public void setPieces(SideColor sideColor, PieceType pieceType, long from, long to) {

        int ind = (sideColor == SideColor.White)? 0 : 6;
        ind += pieceType.ordinal();

        Pieces[ind] = (Pieces[ind] & ~from) | to;
    }

    public void setPieces(SideColor sideColor, PieceType pieceType, long newPiece) {

        int ind = (sideColor == SideColor.White)? 0 : 6;
        ind += pieceType.ordinal();

        Pieces[ind] |= newPiece;
    }

    /// true -> king has moved already | king -> rook is yet to move
    public boolean kingMoved(SideColor sideColor) {

        if (sideColor == SideColor.White) return wKingMoved;
        return bKingMoved;
    }

    /// true -> rook has moved already | false -> rook is yet to move
    public boolean rookMoved(SideColor sideColor, long file) {

        if (file != aFile && file != (aFile) >>> 7) throw new IllegalArgumentException("file must be a/h-File!");

        if (sideColor == SideColor.White) {
            if (file == aFile) return a1RookMoved;
            return h1RookMoved;
        }
        if (file == aFile) return a8RookMoved;
        return h8RookMoved;
    }

    public void changeKingCastlingRights(SideColor sideColor) {

        if (sideColor == SideColor.White) {
            wKingMoved = !wKingMoved;
            return;
        }

        bKingMoved = !bKingMoved;
    }

    public void changeRookCastlingRights(SideColor sideColor, long file) {

        long RookOnA = file & aFile;
        long RookOnH = file & (aFile >>> 7);

        if ((RookOnA | RookOnH) == 0x0L) {
            throw new IllegalArgumentException("Starting position of the Rook can only be on the a/h File!");
        }

        if (RookOnA != 0x0L) {
            if (sideColor == SideColor.White) a1RookMoved = !a1RookMoved;
            else a8RookMoved = !a8RookMoved;
        }
        else if (sideColor == SideColor.White) h1RookMoved = !h1RookMoved;
        else h8RookMoved = !h8RookMoved;
    }

    public SideColor findPosInfo(long pos, PieceType[] pieceType) {

        for (int i = 0; i < Pieces.length; i++) {

            if ((pos & Pieces[i]) != 0x0L) {

                pieceType[0] = PieceType.values()[i % 6];
                return (i < 6)? SideColor.White : SideColor.Black;
            }
        }

        return null;
    }
}
