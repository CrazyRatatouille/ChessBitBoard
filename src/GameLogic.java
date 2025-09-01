import javax.swing.text.AsyncBoxView;

public class GameLogic {

    //test

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

        if ((selected & wBishops) != 0) return legalBishopMoves(selected, whitePieces, blackPieces);
        if ((selected & bBishops) != 0) return legalBishopMoves(selected, blackPieces, whitePieces);

        if ((selected & wRooks) != 0) return legalRookMoves(selected, whitePieces, blackPieces);
        if ((selected & bRooks) != 0) return legalRookMoves(selected, blackPieces, whitePieces);

        if ((selected & wQueens) != 0) return legalQueenMoves(selected, whitePieces, blackPieces);
        if ((selected & bQueens) != 0) return legalQueenMoves(selected, blackPieces, whitePieces);

        if ((selected & wKing) != 0) return legalKingMoves(wKing, whitePieces, blackPieces);
        if ((selected & bKing) != 0) return legalKingMoves(bKing, blackPieces, whitePieces);

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

    //TODO: if it makes you be in a check abort and improve perf with magic bitboard
    private long legalBishopMoves(long selected, long myPieces, long enemyPieces) {

        long emptyTiles = ~fullBoard();

        long NW = 0L;
        long NE = 0L;
        long SW = 0L;
        long SE = 0L;

        boolean bNW = true;
        boolean bNE = true;
        boolean bSW = true;
        boolean bSE = true;


        for (int inc = 1; inc < 8; inc++) {

            long shiftNW = selected << (inc * 9);
            long shiftNE = selected << (inc * 7);
            long shiftSW = selected >>> (inc * 7);
            long shiftSE = selected >>> (inc * 9);


            if (bNW & (shiftNW & ~aFile) != 0) {

                if ((shiftNW & myPieces) != 0) bNW = false;
                else if ((shiftNW & enemyPieces) != 0) {
                    NW |= shiftNW;
                    bNW = false;
                } else {
                    NW |= shiftNW;
                }
            }

            if (bNE & (shiftNE & ~hFile) != 0) {

                if ((shiftNE & myPieces) != 0) bNE = false;
                else if ((shiftNE & enemyPieces) != 0) {
                    NE |= shiftNE;
                    bNE = false;
                } else {
                    NE |= shiftNE;
                }
            }

            if (bSW & (shiftSW & ~aFile) != 0){

                if ((shiftSW & myPieces) != 0) bSW = false;
                else if ((shiftSW & enemyPieces) != 0) {
                    SW |= shiftSW;
                    bSW = false;
                } else {
                    SW |= shiftSW;
                }
            }

            if (bSE & (shiftSE & ~hFile) != 0) {

                if ((shiftSE & myPieces) != 0) bSE = false;
                else if ((shiftSE & enemyPieces) != 0) {
                    SE |= shiftSE;
                    bSE = false;
                } else {
                    SE |= shiftSE;
                }
            }
        }

        return NW | NE | SW | SE;


    }

    //TODO: if it makes you be in a check abort
    private long legalRookMoves(long selected, long myPieces, long enemyPieces) {

        long emptyTile = ~fullBoard();

        long North = 0L;
        long West = 0L;
        long East = 0L;
        long South = 0L;

        boolean N = true;
        boolean W = true;
        boolean E = true;
        boolean S = true;

        for (int inc = 1; inc < 8; inc++) {

            long shiftN = selected << (inc * 8);
            long shiftW = selected << inc;
            long shiftE = selected >>> inc;
            long shiftS = selected >>> (inc * 8);

            if (N) {

                if ((shiftN & enemyPieces) != 0) {
                    North |= shiftN;
                    N = false;
                } else if ((shiftN & myPieces) != 0) {
                    N = false;
                } else {
                    North |= shiftN;
                }
            }

            if (W) {

                if ((shiftW & enemyPieces) != 0) {
                    West |= shiftW;
                    W = false;
                } else if ((shiftW & myPieces) != 0) {
                    W = false;
                } else {
                    West |= shiftW;
                }
            }

            if (E) {

                if ((shiftE & enemyPieces) != 0) {
                    East |= shiftE;
                    E = false;
                } else if ((shiftE & myPieces) != 0) {
                    E = false;
                } else {
                    East |= shiftE;
                }
            }

            if (S) {

                if ((shiftS & enemyPieces) != 0) {
                    South |= shiftS;
                    S = false;
                } else if ((shiftS & myPieces) != 0) {
                    S = false;
                } else {
                    South |= shiftS;
                }
            }
        }

        return North | West | East | South;


    }

    //TODO: if it makes you be in a check abort
    private long legalQueenMoves(long selected, long myPieces, long enemyPieces) {
        return legalRookMoves(selected, myPieces, enemyPieces) | legalBishopMoves(selected, myPieces, enemyPieces);
    }

    //TODO: if it makes you be in a check abort
    private long legalKingMoves(long myKing, long myPieces, long enemyPieces) {

        long legalMoves = 0L;

        if ((myKing & ~aFile) != 0) {
            legalMoves |= myKing << 9;
            legalMoves |= myKing << 1;
            legalMoves |= myKing >>> 7;
        }
        if ((myKing & ~hFile) != 0) {
            legalMoves |= myKing << 7;
            legalMoves |= myKing >>> 1;
            legalMoves |= myKing >>> 9;
        }
        legalMoves |= myKing << 8;
        legalMoves |= myKing >>> 8;

        legalMoves = (legalMoves & enemyPieces) & (legalMoves & ~myPieces);

        return legalMoves;
    }


    private long wPawnAtkBoard(long atkPieces) {

        long atkBoard = 0L;
        atkBoard |= (atkPieces & ~aFile) << 9;
        atkBoard |= (atkPieces & ~hFile) << 7;

        return atkBoard;
    }

    private long bPawnAtkBoard(long atkPieces) {

        long atkBoard = 0L;
        atkBoard |= (atkPieces & ~aFile) >>> 7;
        atkBoard |= (atkPieces & ~hFile) >>> 9;

        return atkBoard;
    }

    private long knightAtkBoard (long atkPieces) {

        return (atkPieces & ~aFile) << 17
                | (atkPieces & ~hFile) << 15
                | (atkPieces & ~(aFile & bFile)) << 10
                | (atkPieces & ~(gFile & hFile)) << 6
                | (atkPieces & ~(aFile & bFile)) >>> 6
                | (atkPieces & ~(gFile & hFile)) >>> 10
                | (atkPieces & ~aFile) >>> 15
                | (atkPieces & ~hFile) >>> 17;
    }

    private long bishopAtkBoard (long atkPieces) {

        long atkBoard = 0L;

        long AB = aFile | bFile;
        long ABC = AB | cFile;
        long ABCD = ABC | dFile;
        long GH = gFile | hFile;
        long FGH = GH | fFile;


        atkBoard |= (atkPieces & ~aFile) << 9 | (atkPieces & ~aFile) >>> 7;
        atkBoard |= (atkPieces & ~AB) << 18 | (atkPieces & ~AB) >>> 14;
        atkBoard |= (atkPieces & ~ABC) << 27 | (atkPieces & ~ABC) >>> 21;
        atkBoard |= (atkPieces & ~ABCD) << 36 | (atkPieces & ~ABCD) >>> 28;
        atkBoard |= (atkPieces & ~ABCD) << 36 | (atkPieces & ~ABCD) >>> 28;

        return atkBoard;
    }
}
