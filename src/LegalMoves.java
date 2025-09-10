import java.lang.invoke.WrongMethodTypeException;

public class LegalMoves {

    private final Bitboards bitboards;
    private AttackPatterns attackPatterns;
    private final PieceType[] pieceTypes;

    public LegalMoves(Bitboards bitboards) {
        this.bitboards = bitboards;
        this.pieceTypes = PieceType.values();
        //this.attackPatterns = new AttackPatterns(bitboards);
    }

    public long legalMoves(long pos, Color color) throws NoPieceAtSquareException {

        PieceType pieceType = null;
        long legalMoves;

        for (PieceType p : pieceTypes) {

            long myPieces = bitboards.getPieces(color, p);

            if ((myPieces & pos) != 0x0L) {
                pieceType = p;
                break;
            }
        }

        switch (pieceType) {
            case Pawn -> legalMoves = (color == Color.White)? wPawnLegalMoves(pos): bPawnLegalMoves(pos);
            case Knight -> legalMoves = knightLegalMoves(pos, color);
            case Bishop -> legalMoves = bishopLegalMoves(pos, color);
            case Rook -> legalMoves = rookLegalMoves(pos, color);
            case Queen -> legalMoves = queenLegalMoves(pos, color);
            case King -> legalMoves = kingLegalMoves(pos, color);
            case null -> {throw new NoPieceAtSquareException("Not your Piece selected!");}
        }

        return legalMoves;
    }

    private long wPawnLegalMoves(long pos) {

        long enemyOcc = bitboards.getColorOcc(Color.Black);
        long occ = bitboards.getOcc();
        long enPassant = bitboards.getEnPassant();

        long one = (pos << 8) & ~occ;
        long two = ((one << 8) & bitboards.getRank(3)) & ~occ;
        long NE = ((pos & ~bitboards.getFile(7)) << 7) & (enemyOcc | enPassant);
        long NW = ((pos & ~bitboards.getFile(0)) << 9) & (enemyOcc | enPassant);

        one = quietMoveLegality(Color.White, PieceType.Pawn, pos, one)? one : 0x0L;
        two = quietMoveLegality(Color.White, PieceType.Pawn, pos, two)? two : 0x0L;
        NE = quietMoveLegality(Color.White, PieceType.Pawn, pos, NE)? NE : 0x0L;
        NW = quietMoveLegality(Color.White, PieceType.Pawn, pos, NW)? NW : 0x0L;

        return one | two | NE | NW;
    }

    private long bPawnLegalMoves(long pos) {

        long enemyOcc = bitboards.getColorOcc(Color.White);
        long occ = enemyOcc | bitboards.getColorOcc(Color.Black);
        long enPassant = bitboards.getEnPassant();

        long one = (pos >>> 8) & ~occ;
        long two = ((one >>> 8) & bitboards.getRank(4)) & ~occ;
        long SE = ((pos & ~bitboards.getFile(7)) >>> 9) & (enemyOcc | enPassant);
        long SW = ((pos & ~bitboards.getFile(0)) >>> 7) & (enemyOcc | enPassant);

        return one | two | SE | SW;
    }

    private long knightLegalMoves(long pos, Color color) {


        long aFile = bitboards.getFile(0);
        long abFiles = aFile | aFile >>> 1;

        long knightNotA = pos & ~aFile;
        long knightNotAB = pos & ~abFiles;
        long knightNotGH = pos & ~(abFiles >>> 6);
        long knightNotH = pos & ~(aFile >>> 7);
        long myOcc = bitboards.getColorOcc(color);


        return (knightNotA & ~myOcc) << 17
                | (knightNotH & ~myOcc) << 15
                | (knightNotAB & ~myOcc) << 10
                | (knightNotGH & ~myOcc) << 6
                | (knightNotAB & ~myOcc) >>> 6
                | (knightNotGH & ~myOcc) >>> 10
                | (knightNotA & ~myOcc) >>> 15
                | (knightNotH & ~myOcc) >>> 17;

    }

    ///TODO: CHECK IF LOGIC ACTUALLY WORKS
    private long bishopLegalMoves(long pos, Color color) {

        long legalMoves = 0x0L;
        long aFile = bitboards.getFile(0);
        long hFile = bitboards.getFile(7);

        long myOcc = bitboards.getColorOcc(color);
        long enemyOcc = bitboards.getColorOcc(color.other());

        long NE = (pos & ~hFile) << 7;
        long SE = (pos & ~hFile) >>> 9;
        long SW = (pos & ~aFile) >>> 7;
        long NW = (pos & ~aFile) << 9;

        boolean legality;

        while ((NE | SE | SW | NW) != 0x0L) {

            if (NE == 0x0L);
            else if ((NE & myOcc) != 0x0L) NE = 0x0L;
            else if ((NE & enemyOcc) != 0x0L) {
                legality = captureLegality(color, PieceType.Bishop, pos, NE);
                legalMoves |= (legality)? NE : 0x0L;
                NE = 0x0L;
            }
            else {
                legality = quietMoveLegality(color, PieceType.Bishop, pos, NE);
                legalMoves |= (legality)? NE : 0x0L;
                NE = (NE & ~hFile) << 7;
            }

            if (SE == 0x0L);
            else if ((SE & myOcc) != 0x0L) SE = 0x0L;
            else if ((SE & enemyOcc) != 0x0L) {
                legality = captureLegality(color, PieceType.Bishop, pos, SE);
                legalMoves |= (legality)? SE : 0x0L;
                SE = 0x0L;
            }
            else {
                legality = quietMoveLegality(color, PieceType.Bishop, pos, SE);
                legalMoves |= (legality)? SE : 0x0L;
                SE = (SE & ~hFile) >>> 9;
            }

            if (SW == 0x0L);
            else if ((SW & myOcc) != 0x0L) SW = 0x0L;
            else if ((SW & enemyOcc) != 0x0L) {
                legality = captureLegality(color, PieceType.Bishop, pos, SW);
                legalMoves |= (legality)? SW : 0x0L;
                SW = 0x0L;
            } else {
                legality = quietMoveLegality(color, PieceType.Bishop, pos, SW);
                legalMoves |= (legality)? SW : 0x0L;
                SW = (SW & ~aFile) >>> 7;
            }

            if (NW == 0x0L);
            else if ((NW & myOcc) != 0x0L) NW = 0x0L;
            else if ((NW & enemyOcc) != 0x0L) {
                legality = captureLegality(color, PieceType.Bishop, pos, NW);
                legalMoves |= (legality)? NW : 0x0L;
                NW = 0x0L;
            }
            else {
                legality = quietMoveLegality(color, PieceType.Bishop, pos, NW);
                legalMoves |= (legality)? NW : 0x0L;
                NW = (NW & ~aFile) << 9;
            }
        }

        return legalMoves;
    }

    private long rookLegalMoves(long pos, Color color) {

        long legalMoves = 0x0L;
        long aFile = bitboards.getFile(0);
        long hFile = bitboards.getFile(7);

        long myOcc = bitboards.getColorOcc(color);
        long enemyOcc = bitboards.getColorOcc(color.other());

        long N = pos << 8;
        long E = (pos & ~hFile) >>> 1;
        long S = pos >>> 8;
        long W = (pos & ~aFile) << 1;

        while ((N | E | S | W) != 0x0L);
        return 0x0L;
    }

    private long queenLegalMoves(long pos, Color color) {

        long queensNotA = pos & ~bitboards.getFile(0);
        long queensNotH = pos & ~bitboards.getFile(7);
        long myOcc = bitboards.getColorOcc(color);
        long occ = bitboards.getOcc();

        return recLineGen(pos << 8, myOcc, occ, Direction.North)
                | recLineGen(queensNotH >>> 1, myOcc, occ, Direction.East)
                | recLineGen(pos >>> 8, myOcc, occ, Direction.South)
                | recLineGen(queensNotA << 1, myOcc, occ, Direction.West)

                |recLineGen(queensNotH << 7, myOcc, occ, Direction.NorthEast)
                | recLineGen(queensNotH >>> 9, myOcc, occ, Direction.SouthEast)
                | recLineGen(queensNotA >>> 7, myOcc, occ, Direction.SouthWest)
                | recLineGen(queensNotA << 9, myOcc, occ, Direction.NorthWest);
    }

    private long kingLegalMoves(long pos, Color color) {

        long myOcc = bitboards.getColorOcc(color);
        long kingNotA = pos & ~bitboards.getFile(0);
        long kingNotH = pos & ~bitboards.getFile(7);

        return pos << 8
                | kingNotH << 7
                | kingNotH >>> 1
                | kingNotH >>> 9
                | pos >>> 8
                | kingNotA >>> 7
                | kingNotA << 1
                | kingNotA << 9;
    }

    private long recLineGen(long movedPieces, long myOcc, long occ, Direction direction) {

        if (movedPieces  == 0x0L) return 0x0L;

        long goFurther = movedPieces & ~occ;
        long notAtkMyPieces = movedPieces & ~myOcc;

        long piecesNotA = goFurther & ~bitboards.getFile(0);
        long piecesNotH = goFurther & ~bitboards.getFile(7);

        if (direction == Direction.North) return notAtkMyPieces | recLineGen(goFurther << 8, myOcc, occ, direction);
        if (direction == Direction.East) return notAtkMyPieces | recLineGen(piecesNotH >>> 1, myOcc, occ, direction);
        if (direction == Direction.South) return notAtkMyPieces | recLineGen(goFurther >>> 8, myOcc, occ, direction);
        if (direction == Direction.West) return notAtkMyPieces | recLineGen(piecesNotA << 1, myOcc, occ, direction);

        if (direction == Direction.NorthEast) return notAtkMyPieces | recLineGen(piecesNotH << 7, myOcc, occ, direction);
        if (direction == Direction.SouthEast) return notAtkMyPieces | recLineGen(piecesNotH >>> 9, myOcc, occ, direction);
        if (direction == Direction.SouthWest) return notAtkMyPieces | recLineGen(piecesNotA >>> 7, myOcc, occ, direction);
        else return notAtkMyPieces | recLineGen(piecesNotA << 9, myOcc, occ, direction);
    }

    ///true -> legal move | false -> illegal move
    /// TODO: enPassant undo, conusume undo, castling undo, castling check???
    private boolean quietMoveLegality (Color color, PieceType pieceType, long from, long to) {

        Color enemyColor = color.other();

        bitboards.setPieces(color, pieceType, from, to);
        boolean legality = !(checkForCheck(color, enemyColor));
        bitboards.setPieces(color, pieceType, to, from);
        return legality;
    }

    private boolean captureLegality (Color color, PieceType pieceType, long from, long to) {

        Color enemyColor = color.other();
        PieceType enemyPieceType = null;

        for (PieceType p : PieceType.values()) {

            long curPiece = bitboards.getPieces(enemyColor, p);
            if ((to & curPiece) != 0) {
                enemyPieceType = p;
                break;
            }
        }

        if (enemyPieceType == null) throw new WrongMethodTypeException("No Piece is Taken, Wrong Method called!!!");

        //make Move
        bitboards.setPieces(color, pieceType, from, to);
        bitboards.setPieces(enemyColor, enemyPieceType, to, 0x0L);

        boolean legality = !(checkForCheck(color, enemyColor));

        //Undo Move
        bitboards.setPieces(color, pieceType, to, from);
        bitboards.setPieces(enemyColor,enemyPieceType, to);

        return legality;
    }

    /// this method only checks if King is in check after Castling
    /// Precodition: king/rook present / unmoved / valid castling rights, path empty and not attacked
    /// Postcodition: After Castling king is not in check -> true, king is in check -> false
    private boolean castlingLegality (Color color, long from, long to, long rookFile) {

        Color enemyColor = color.other();

        long aFile = bitboards.getFile(0);
        long hFile = bitboards.getFile(7);
        long homeRank = (color == Color.White)? bitboards.getRank(0) : bitboards.getRank(7);

        if (rookFile != aFile && rookFile != hFile) throw new IllegalArgumentException("file should be a/h-File!");

        if (bitboards.kingMoved(color) || bitboards.rookMoved(color, rookFile)) return false;

        boolean kingSide = rookFile == hFile;

        long rookFrom = bitboards.getPieces(color, PieceType.Rook) & rookFile & homeRank;
        long rookTo = (kingSide)? (to << 1) : (to >>> 1);

        bitboards.setPieces(color, PieceType.King, from, to);
        bitboards.setPieces(color, PieceType.Rook, rookFrom, rookTo);

        boolean legality = !checkForCheck(color, enemyColor);

        bitboards.setPieces(color, PieceType.King, to, from);
        bitboards.setPieces(color, PieceType.Rook, rookTo, rookFrom);

        return legality;
    }

    private boolean enPassantCaptureLegality (Color color, PieceType pieceType, long from, long to) {

        if (pieceType != PieceType.Pawn) throw new WrongMethodTypeException("EP only applies to pawns!");

        long enPassant = bitboards.getEnPassant();

        if ((enPassant & to) == 0) throw new IllegalStateException("targetSquare is not the current EP target square!!!");

        Color enemyColor = color.other();
        long pawnCaptured = (enemyColor == Color.White)? to << 8: to >>> 8;

        bitboards.setPieces(color, PieceType.Pawn, from, to);
        bitboards.setEnPassant(0x0L);
        bitboards.setPieces(enemyColor, PieceType.Pawn, pawnCaptured, 0x0L);

        boolean legality = !(checkForCheck(color, enemyColor));

        bitboards.setPieces(color, PieceType.Pawn, to, from);
        bitboards.setEnPassant(enPassant);
        bitboards.setPieces(enemyColor, PieceType.Pawn, pawnCaptured);

        return legality;
    }

    ///true -> in check | false -> not in check
    private boolean checkForCheck (Color myColor, Color enemyColor) {

        long enemyAtkBoard = attackPatterns.getFullAtkBoard(enemyColor);
        long myKing = bitboards.getPieces(myColor, PieceType.King);

        return (enemyAtkBoard & myKing) != 0;
    }


}
