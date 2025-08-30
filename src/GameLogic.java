public class GameLogic {

    private final long aFile = 0x8080808080808080L;
    private final long bFile = 0x4040404040404040L;
    private final long cFile = 0x2020202020202020L;
    private final long dFile = 0x1010101010101010L;
    private final long eFile = 0x0808080808080808L;
    private final long fFile = 0x0404040404040404L;
    private final long gFile = 0x0202020202020202L;
    private final long hFile = 0x0101010101010101L;

    private final long eightRank = 0xFF00000000000000L;
    private final long seventhRank = 0x00FF000000000000L;
    private final long sixthRank = 0x0000FF0000000000L;
    private final long fifthRank = 0x000000FF00000000L;
    private final long fourthRank = 0x00000000FF000000L;
    private final long thirdRank = 0x0000000000FF0000L;
    private final long secondRank = 0x000000000000FF00L;
    private final long firstRank = 0x00000000000000FFL;

    private long wPawns = 0x000000000000FF00L; //a2 - h2
    private long wKnights = 0x0000000000000042L; //b1 & g1
    private long wBishops = 0x0000000000000024L; //c1 & f1
    private long wRooks = 0x0000000000000081L; //a1 & h1
    private long wQueens = 0x0000000000000010L; //d1
    private long wKing = 0x0000000000000008L; //e1

    private long bPawns = 0x00FF000000000000L; //a7 - g7
    private long bKnights = 0x4200000000000000L; //b8 & g8
    private long bBishops = 0x2400000000000000L; //c8 & f8
    private long bRooks = 0x8100000000000000L; //a8 & h8
    private long bQueens = 0x1000000000000000L; //d8
    private long bKing = 0x0800000000000000L; //e8

    private long enPassant = 0x0L;

    private GameLogic() {
    }

    public long legalMoves(long selected) {

        long whitePieces = whitePieces();
        long blackPieces = blackPieces();

        if ((selected & wPawns) != 0) return legalWPawnMoves(selected);
        if ((selected & bPawns) != 0) return legalBPawnMoves(selected);

        if ((selected & wKnights) != 0) return legalKnightMoves(selected, whitePieces);
        if ((selected & bKnights) != 0) return legalKnightMoves(selected, blackPieces);

        if ((selected & wBishops) != 0) return legalBishopMoves(selected, whitePieces);
        if ((selected & bBishops) != 0) return legalBishopMoves(selected, blackPieces);

        if ((selected & wRooks) != 0) return legalRookMoves(selected, whitePieces);
        if ((selected & bRooks) != 0) return legalRookMoves(selected, blackPieces);

        if ((selected & wQueens) != 0) return legalQueenMoves(selected, whitePieces);
        if ((selected & bQueens) != 0) return legalQueenMoves(selected, blackPieces);

        if ((selected & wKing) != 0) return legalWKingMoves(selected);
        if ((selected & bKing) != 0) return legalBKingMoves(selected);

        else return 0;
    }

    private long fullBoard() {

        return whitePieces() & blackPieces();
    }

    private long whitePieces() {
        return wPawns & wKnights & wBishops & wRooks & wQueens & wKing;
    }

    private long blackPieces() {
        return bPawns & bKnights & bBishops & bRooks & bQueens & bKing;
    }

    //TODO: if it makes you be in a check abort
    private long legalWPawnMoves(long selected) {

        long emptyTiles = ~fullBoard();
        long blackPieces = blackPieces();

        long legalMoves = 0L;

        long one = (selected << 8) & emptyTiles;
        long two = ((one << 8) & emptyTiles) & fourthRank;

        long takeNE = ((selected & ~hFile) << 7) & (enPassant | blackPieces);
        long takeNW = ((selected & ~aFile) << 9) & (enPassant | blackPieces);

        legalMoves |= one | two | takeNE | takeNW;

        return legalMoves;
    }
    private long legalBPawnMoves(long selected) {

        long emptyTiles = ~fullBoard();
        long whitePieces = whitePieces();

        long legalMoves = 0L;

        long one = (selected >>> 8) & emptyTiles;
        long two = ((one >>> 8) & emptyTiles) & fifthRank;

        long takeSE = ((selected & ~hFile) >>> 9) & (enPassant | whitePieces);
        long takeSW = ((selected & ~aFile) >>> 7) & (enPassant | whitePieces);

        legalMoves |= one | two | takeSE | takeSW;

        return legalMoves;
    }

    //TODO: if it makes you be in a check abort
    private long legalKnightMoves(long selected, long myPieces) {

        long notA = selected & ~aFile;
        long notAB = selected & ~(aFile | bFile);
        long notGH = selected & ~(gFile | hFile);
        long notH = selected & ~hFile;

        return (notA << 17 | notH << 15
                | notAB << 10 | notGH << 6
                | notAB >>> 6 | notGH >>> 10
                | notA >>> 15 | notH >>> 17)
                & ~myPieces;
    }

    //TODO: if it makes you be in a check abort
    private long legalBishopMoves(long selected, long myPieces) {
    }

    //TODO: if it makes you be in a check abort
    private long legalRookMoves(long selected, long myPieces) {
    }

    //TODO: if it makes you be in a check abort
    private long legalQueenMoves(long selected, long myPieces) {
    }

    //TODO: if it makes you be in a check abort
    private long legalWKingMoves(long selected) {
    }

    private long legalBKingMoves(long selected) {
    }


}
