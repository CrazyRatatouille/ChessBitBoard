import java.lang.invoke.WrongMethodTypeException;

public class LegalMoves {

    private final Bitboards bitboards;
    private final PieceType[] pieceTypes;
    private final AttackPatterns attackPatterns;

    public LegalMoves(Bitboards bitboards) {
        this.bitboards = bitboards;
        this.pieceTypes = PieceType.values();
        this.attackPatterns = new AttackPatterns(this.bitboards);
    }

    public long legalMoves(long pos, SideColor sideColor) throws NoPieceAtSquareException {

        PieceType pieceType = null;
        long legalMoves;

        for (PieceType p : pieceTypes) {

            long myPieces = bitboards.getPieces(sideColor, p);

            if ((myPieces & pos) != 0x0L) {
                pieceType = p;
                break;
            }
        }

        switch (pieceType) {
            case Pawn -> legalMoves = (sideColor == SideColor.White)? wPawnLegalMoves(pos): bPawnLegalMoves(pos);
            case Knight -> legalMoves = knightLegalMoves(pos, sideColor);
            case Bishop -> legalMoves = bishopLegalMoves(pos, sideColor);
            case Rook -> legalMoves = rookLegalMoves(pos, sideColor);
            case Queen -> legalMoves = queenLegalMoves(pos, sideColor);
            case King -> legalMoves = kingLegalMoves(pos, sideColor);
            case null -> {throw new NoPieceAtSquareException("Not your Piece selected!");}
        }

        return legalMoves;
    }

    private long wPawnLegalMoves(long pos) {

        long enemyOcc = bitboards.getColorOcc(SideColor.Black);
        long occ = bitboards.getOcc();
        long enPassant = bitboards.getEnPassant();

        long one = (pos << 8) & ~occ;
        long two = ((one << 8) & bitboards.getRank(3)) & ~occ;
        long NE = ((pos & ~bitboards.getFile(7)) << 7) & (enemyOcc | enPassant);
        long NW = ((pos & ~bitboards.getFile(0)) << 9) & (enemyOcc | enPassant);

        one = quietMoveLegality(SideColor.White, PieceType.Pawn, pos, one)? one : 0x0L;
        two = quietMoveLegality(SideColor.White, PieceType.Pawn, pos, two)? two : 0x0L;
        NE = enPassantCaptureLegality(SideColor.White, pos, NE)? NE : 0x0L;
        NW = enPassantCaptureLegality(SideColor.White, pos, NW)? NW : 0x0L;

        return one | two | NE | NW;
    }

    private long bPawnLegalMoves(long pos) {

        long enemyOcc = bitboards.getColorOcc(SideColor.White);
        long occ = enemyOcc | bitboards.getColorOcc(SideColor.Black);
        long enPassant = bitboards.getEnPassant();

        long one = (pos >>> 8) & ~occ;
        long two = ((one >>> 8) & bitboards.getRank(4)) & ~occ;
        long SE = ((pos & ~bitboards.getFile(7)) >>> 9) & (enemyOcc | enPassant);
        long SW = ((pos & ~bitboards.getFile(0)) >>> 7) & (enemyOcc | enPassant);

        one = quietMoveLegality(SideColor.Black, PieceType.Pawn, pos, one)? one : 0x0L;
        two = quietMoveLegality(SideColor.Black, PieceType.Pawn, pos, two)? two : 0x0L;
        SE = enPassantCaptureLegality(SideColor.Black, pos, SE)? SE : 0x0L;
        SW = enPassantCaptureLegality(SideColor.Black, pos, SW)? SW : 0x0L;

        return one | two | SE | SW;
    }

    private long knightLegalMoves(long pos, SideColor sideColor) {


        long aFile = bitboards.getFile(0);
        long abFiles = aFile | aFile >>> 1;

        long knightNotA = pos & ~aFile;
        long knightNotAB = pos & ~abFiles;
        long knightNotGH = pos & ~(abFiles >>> 6);
        long knightNotH = pos & ~(aFile >>> 7);
        long myOcc = bitboards.getColorOcc(sideColor);


        return (knightNotA & ~myOcc) << 17
                | (knightNotH & ~myOcc) << 15
                | (knightNotAB & ~myOcc) << 10
                | (knightNotGH & ~myOcc) << 6
                | (knightNotAB & ~myOcc) >>> 6
                | (knightNotGH & ~myOcc) >>> 10
                | (knightNotA & ~myOcc) >>> 15
                | (knightNotH & ~myOcc) >>> 17;

    }

    private long bishopLegalMoves(long pos, SideColor sideColor) {

        long legalMoves = 0x0L;
        long aFile = bitboards.getFile(0);
        long hFile = bitboards.getFile(7);

        long myOcc = bitboards.getColorOcc(sideColor);
        long enemyOcc = bitboards.getColorOcc(sideColor.other());

        legalMoves |= legalLineGen(pos, sideColor, PieceType.Bishop, myOcc, enemyOcc, aFile, hFile, Direction.NorthEast);
        legalMoves |= legalLineGen(pos, sideColor, PieceType.Bishop, myOcc, enemyOcc, aFile, hFile, Direction.SouthEast);
        legalMoves |= legalLineGen(pos, sideColor, PieceType.Bishop, myOcc, enemyOcc, aFile, hFile, Direction.SouthWest);
        legalMoves |= legalLineGen(pos, sideColor, PieceType.Bishop, myOcc, enemyOcc, aFile, hFile, Direction.NorthWest);

        return legalMoves;
    }

    private long rookLegalMoves(long pos, SideColor sideColor) {

        long legalMoves = 0x0L;
        long aFile = bitboards.getFile(0);
        long hFile = bitboards.getFile(7);

        long myOcc = bitboards.getColorOcc(sideColor);
        long enemyOcc = bitboards.getColorOcc(sideColor.other());

        legalMoves |= legalLineGen(pos, sideColor, PieceType.Rook, myOcc, enemyOcc, aFile, hFile, Direction.North);
        legalMoves |= legalLineGen(pos, sideColor, PieceType.Rook, myOcc, enemyOcc, aFile, hFile, Direction.East);
        legalMoves |= legalLineGen(pos, sideColor, PieceType.Rook, myOcc, enemyOcc, aFile, hFile, Direction.South);
        legalMoves |= legalLineGen(pos, sideColor, PieceType.Rook, myOcc, enemyOcc, aFile, hFile, Direction.West);

        return legalMoves;
    }

    private long queenLegalMoves(long pos, SideColor sideColor) {

        long legalMoves = 0x0L;
        long aFile = bitboards.getFile(0);
        long hFile = bitboards.getFile(7);

        long myOcc = bitboards.getColorOcc(sideColor);
        long enemyOcc = bitboards.getColorOcc(sideColor.other());

        legalMoves |= legalLineGen(pos, sideColor, PieceType.Queen, myOcc, enemyOcc, aFile, hFile, Direction.North);
        legalMoves |= legalLineGen(pos, sideColor, PieceType.Queen, myOcc, enemyOcc, aFile, hFile, Direction.East);
        legalMoves |= legalLineGen(pos, sideColor, PieceType.Queen, myOcc, enemyOcc, aFile, hFile, Direction.South);
        legalMoves |= legalLineGen(pos, sideColor, PieceType.Queen, myOcc, enemyOcc, aFile, hFile, Direction.West);

        legalMoves |= legalLineGen(pos, sideColor, PieceType.Queen, myOcc, enemyOcc, aFile, hFile, Direction.NorthEast);
        legalMoves |= legalLineGen(pos, sideColor, PieceType.Queen, myOcc, enemyOcc, aFile, hFile, Direction.SouthEast);
        legalMoves |= legalLineGen(pos, sideColor, PieceType.Queen, myOcc, enemyOcc, aFile, hFile, Direction.SouthWest);
        legalMoves |= legalLineGen(pos, sideColor, PieceType.Queen, myOcc, enemyOcc, aFile, hFile, Direction.NorthWest);

        return legalMoves;
    }

    private long legalLineGen(long pos, SideColor sideColor, PieceType pieceType, long myOcc, long enemyOcc, long aFile, long hFile, Direction direction) {

        long legalMoves = 0x0L;

        long frontier = step(pos, aFile, hFile, direction);
        boolean legality;

        while (frontier != 0x0L) {

            if ((frontier & myOcc) != 0x0L) break;
            else if ((frontier & enemyOcc) != 0x0L) {

                legality = captureLegality(sideColor, pieceType, pos, frontier);
                legalMoves |= (legality)? frontier : 0x0L;
                break;

            }
            else {

                legality = quietMoveLegality(sideColor, pieceType, pos, frontier);
                legalMoves |= (legality)? frontier : 0x0L;
                frontier = step(frontier, aFile, hFile, direction);
            }
        }

        return legalMoves;
    }

    private long step(long pos, long aFile, long hFile, Direction direction) {

        long step = 0x0L;

        switch (direction) {

            case North -> step = pos << 8;
            case East -> step = (pos & ~hFile) >>> 1;
            case South -> step = pos >>> 8;
            case West -> step = (pos & ~aFile) << 1;

            case NorthEast -> step = (pos & ~hFile) << 7;
            case SouthEast -> step = (pos & ~hFile) >>> 9;
            case SouthWest -> step = (pos & ~aFile) >>> 7;
            case NorthWest -> step = (pos & ~aFile) << 9;
        }

        return step;
    }

    // TODO: create a working kingLegalMoves Method
    private long kingLegalMoves(long pos, SideColor sideColor) {

        long legalMoves = 0x0L;

        long myOcc = bitboards.getColorOcc(sideColor);
        long enemyOcc = bitboards.getColorOcc(sideColor.other());

        long aFile = bitboards.getFile(0);
        long hFile = bitboards.getFile(7);

        boolean kingMoved = bitboards.kingMoved(sideColor);
        boolean aRookMoved = bitboards.rookMoved(sideColor, aFile);
        boolean hRookMoved = bitboards.rookMoved(sideColor, hFile);
        boolean inCheck = checkForCheck(sideColor, sideColor.other());

        for (Direction direction : Direction.values()) {

            boolean legality;

            long oneStep = direction.go(pos, aFile, hFile);

            if ((oneStep & myOcc) != 0) continue;
            if ((oneStep & enemyOcc) != 0) {

                legality = captureLegality(sideColor, PieceType.King, pos, oneStep);
                legalMoves |= (legality)? oneStep : 0x0L;
                continue;
            }
            else {

                legality = quietMoveLegality(sideColor, PieceType.King, pos, oneStep);
                legalMoves |= (legality)? oneStep : 0x0L;
            }

            if (direction == Direction.East && !kingMoved && !hRookMoved && legality && !inCheck) {

                if ((bitboards.getPieces(sideColor, PieceType.Rook) & hFile) == 0x0L) continue;
                long castlingRight = direction.go(oneStep, aFile, hFile) & ~(myOcc | enemyOcc);

                legality = castlingRight != 0x0L && castlingLegality(sideColor, pos, castlingRight, hFile);
                legalMoves |= legality? castlingRight : 0x0L;
            }

            if (direction == Direction.West && !kingMoved && !aRookMoved && legality && !inCheck) {

                if ((bitboards.getPieces(sideColor, PieceType.Rook) & aFile) == 0x0L) continue;

                long castlingLeft = (direction.go(oneStep, aFile, hFile) & ~(myOcc | enemyOcc));
                long rightOfRook = direction.go(castlingLeft, aFile, hFile) & ~(myOcc | enemyOcc);
                boolean emptyPath = (castlingLeft != 0x0L && rightOfRook != 0x0L);

                legality = emptyPath && castlingLegality(sideColor, pos, castlingLeft, aFile);
                legalMoves |= legality? castlingLeft : 0x0L;
            }
        }

         return legalMoves;
    }

    // TODO: collapse all MoveLegality methods into one
    ///true -> legal move | false -> illegal move
    private boolean quietMoveLegality (SideColor sideColor, PieceType pieceType, long from, long to) {

        SideColor enemySideColor = sideColor.other();

        bitboards.setPieces(sideColor, pieceType, from, to);
        boolean legality = !(checkForCheck(sideColor, enemySideColor));
        bitboards.setPieces(sideColor, pieceType, to, from);
        return legality;
    }

    private boolean captureLegality (SideColor sideColor, PieceType pieceType, long from, long to) {

        SideColor enemySideColor = sideColor.other();
        PieceType enemyPieceType = null;

        for (PieceType p : PieceType.values()) {

            long curPiece = bitboards.getPieces(enemySideColor, p);
            if ((to & curPiece) != 0) {
                enemyPieceType = p;
                break;
            }
        }

        if (enemyPieceType == null) throw new WrongMethodTypeException("No Piece is Taken, Wrong Method called!!!");

        //make Move
        bitboards.setPieces(sideColor, pieceType, from, to);
        bitboards.setPieces(enemySideColor, enemyPieceType, to, 0x0L);

        boolean legality = !(checkForCheck(sideColor, enemySideColor));

        //Undo Move
        bitboards.setPieces(sideColor, pieceType, to, from);
        bitboards.setPieces(enemySideColor,enemyPieceType, to);

        return legality;
    }

    /// this method only checks if King is in check after Castling
    /// Precodition: king/rook present / unmoved / valid castling rights, path empty and not attacked
    /// Postcodition: After Castling king is not in check -> true, king is in check -> false
    private boolean castlingLegality (SideColor sideColor, long from, long to, long rookFile) {

        SideColor enemySideColor = sideColor.other();

        long aFile = bitboards.getFile(0);
        long hFile = bitboards.getFile(7);
        long homeRank = (sideColor == SideColor.White)? bitboards.getRank(0) : bitboards.getRank(7);

        if (rookFile != aFile && rookFile != hFile) throw new IllegalArgumentException("file should be a/h-File!");

        if (bitboards.kingMoved(sideColor) || bitboards.rookMoved(sideColor, rookFile)) return false;

        boolean kingSide = rookFile == hFile;

        long rookFrom = bitboards.getPieces(sideColor, PieceType.Rook) & rookFile & homeRank;
        long rookTo = (kingSide)? (to << 1) : (to >>> 1);

        bitboards.setPieces(sideColor, PieceType.King, from, to);
        bitboards.setPieces(sideColor, PieceType.Rook, rookFrom, rookTo);

        boolean legality = !checkForCheck(sideColor, enemySideColor);

        bitboards.setPieces(sideColor, PieceType.King, to, from);
        bitboards.setPieces(sideColor, PieceType.Rook, rookTo, rookFrom);

        return legality;
    }

    private boolean enPassantCaptureLegality (SideColor sideColor, long from, long to) {

        long enPassant = bitboards.getEnPassant();

        if ((enPassant & to) == 0) throw new IllegalStateException("targetSquare is not the current EP target square!!!");

        SideColor enemySideColor = sideColor.other();
        long pawnCaptured = (enemySideColor == SideColor.White)? to << 8: to >>> 8;

        bitboards.setPieces(sideColor, PieceType.Pawn, from, to);
        bitboards.setEnPassant(0x0L);
        bitboards.setPieces(enemySideColor, PieceType.Pawn, pawnCaptured, 0x0L);

        boolean legality = !(checkForCheck(sideColor, enemySideColor));

        bitboards.setPieces(sideColor, PieceType.Pawn, to, from);
        bitboards.setEnPassant(enPassant);
        bitboards.setPieces(enemySideColor, PieceType.Pawn, pawnCaptured);

        return legality;
    }

    ///true -> in check | false -> not in check
    private boolean checkForCheck (SideColor mySideColor, SideColor enemySideColor) {

        long enemyAtkBoard = attackPatterns.getFullAtkBoard(enemySideColor);
        long myKing = bitboards.getPieces(mySideColor, PieceType.King);

        return (enemyAtkBoard & myKing) != 0;
    }

}