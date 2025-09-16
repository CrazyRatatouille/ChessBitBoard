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
        NE = enPassantCaptureLegality(Color.White, PieceType.Pawn, pos, NE)? NE : 0x0L;
        NW = enPassantCaptureLegality(Color.White, PieceType.Pawn, pos, NW)? NW : 0x0L;

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

        one = quietMoveLegality(Color.Black, PieceType.Pawn, pos, one)? one : 0x0L;
        two = quietMoveLegality(Color.Black, PieceType.Pawn, pos, two)? two : 0x0L;
        SE = enPassantCaptureLegality(Color.Black, PieceType.Pawn, pos, SE)? SE : 0x0L;
        SW = enPassantCaptureLegality(Color.Black, PieceType.Pawn, pos, SW)? SW : 0x0L;

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

    private long bishopLegalMoves(long pos, Color color) {

        long legalMoves = 0x0L;
        long aFile = bitboards.getFile(0);
        long hFile = bitboards.getFile(7);

        long myOcc = bitboards.getColorOcc(color);
        long enemyOcc = bitboards.getColorOcc(color.other());

        legalMoves |= legalLineGen(pos, color, PieceType.Bishop, myOcc, enemyOcc, aFile, hFile, Direction.NorthEast);
        legalMoves |= legalLineGen(pos, color, PieceType.Bishop, myOcc, enemyOcc, aFile, hFile, Direction.SouthEast);
        legalMoves |= legalLineGen(pos, color, PieceType.Bishop, myOcc, enemyOcc, aFile, hFile, Direction.SouthWest);
        legalMoves |= legalLineGen(pos, color, PieceType.Bishop, myOcc, enemyOcc, aFile, hFile, Direction.NorthWest);

        return legalMoves;
    }

    private long rookLegalMoves(long pos, Color color) {

        long legalMoves = 0x0L;
        long aFile = bitboards.getFile(0);
        long hFile = bitboards.getFile(7);

        long myOcc = bitboards.getColorOcc(color);
        long enemyOcc = bitboards.getColorOcc(color.other());

        legalMoves |= legalLineGen(pos, color, PieceType.Rook, myOcc, enemyOcc, aFile, hFile, Direction.North);
        legalMoves |= legalLineGen(pos, color, PieceType.Rook, myOcc, enemyOcc, aFile, hFile, Direction.East);
        legalMoves |= legalLineGen(pos, color, PieceType.Rook, myOcc, enemyOcc, aFile, hFile, Direction.South);
        legalMoves |= legalLineGen(pos, color, PieceType.Rook, myOcc, enemyOcc, aFile, hFile, Direction.West);

        return legalMoves;
    }

    private long queenLegalMoves(long pos, Color color) {

        long legalMoves = 0x0L;
        long aFile = bitboards.getFile(0);
        long hFile = bitboards.getFile(7);

        long myOcc = bitboards.getColorOcc(color);
        long enemyOcc = bitboards.getColorOcc(color.other());

        legalMoves |= legalLineGen(pos, color, PieceType.Queen, myOcc, enemyOcc, aFile, hFile, Direction.North);
        legalMoves |= legalLineGen(pos, color, PieceType.Queen, myOcc, enemyOcc, aFile, hFile, Direction.East);
        legalMoves |= legalLineGen(pos, color, PieceType.Queen, myOcc, enemyOcc, aFile, hFile, Direction.South);
        legalMoves |= legalLineGen(pos, color, PieceType.Queen, myOcc, enemyOcc, aFile, hFile, Direction.West);

        legalMoves |= legalLineGen(pos, color, PieceType.Queen, myOcc, enemyOcc, aFile, hFile, Direction.NorthEast);
        legalMoves |= legalLineGen(pos, color, PieceType.Queen, myOcc, enemyOcc, aFile, hFile, Direction.SouthEast);
        legalMoves |= legalLineGen(pos, color, PieceType.Queen, myOcc, enemyOcc, aFile, hFile, Direction.SouthWest);
        legalMoves |= legalLineGen(pos, color, PieceType.Queen, myOcc, enemyOcc, aFile, hFile, Direction.NorthWest);

        return legalMoves;
    }

    private long legalLineGen(long pos, Color color, PieceType pieceType, long myOcc, long enemyOcc, long aFile, long hFile, Direction direction) {

        long legalMoves = 0x0L;

        long frontier = step(pos, aFile, hFile, direction);
        boolean legality;

        while (frontier != 0x0L) {

            if ((frontier & myOcc) != 0x0L) break;
            else if ((frontier & enemyOcc) != 0x0L) {

                legality = captureLegality(color, pieceType, pos, frontier);
                legalMoves |= (legality)? frontier : 0x0L;
                break;

            }
            else {

                legality = quietMoveLegality(color, pieceType, pos, frontier);
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
    private long kingLegalMoves(long pos, Color color) {

        long legalMoves = 0x0L;

        long myOcc = bitboards.getColorOcc(color);
        long enemyOcc = bitboards.getColorOcc(color.other());

        long aFile = bitboards.getFile(0);
        long hFile = bitboards.getFile(7);

        boolean kingMoved = bitboards.kingMoved(color);
        boolean aRookMoved = bitboards.rookMoved(color, aFile);
        boolean hRookMoved = bitboards.rookMoved(color, hFile);
        boolean inCheck = checkForCheck(color, color.other());

        for (Direction direction : Direction.values()) {

            boolean legality;

            long oneStep = direction.go(pos, aFile, hFile);

            if ((oneStep & myOcc) != 0) continue;
            if ((oneStep & enemyOcc) != 0) {

                legality = captureLegality(color, PieceType.King, pos, oneStep);
                legalMoves |= (legality)? oneStep : 0x0L;
                continue;
            }
            else {

                legality = quietMoveLegality(color, PieceType.King, pos, oneStep);
                legalMoves |= (legality)? oneStep : 0x0L;
            }

            if (direction == Direction.East && !kingMoved && !hRookMoved && legality && !inCheck) {

                if ((bitboards.getPieces(color, PieceType.Rook) & hFile) == 0x0L) continue;
                long castlingRight = direction.go(oneStep, aFile, hFile) & ~(myOcc | enemyOcc);

                legality = castlingRight != 0x0L && castlingLegality(color, pos, castlingRight, hFile);
                legalMoves |= legality? castlingRight : 0x0L;
            }

            if (direction == Direction.West && !kingMoved && !aRookMoved && legality && !inCheck) {

                if ((bitboards.getPieces(color, PieceType.Rook) & aFile) == 0x0L) continue;

                long castlingLeft = (direction.go(oneStep, aFile, hFile) & ~(myOcc | enemyOcc));
                long rightOfRook = direction.go(castlingLeft, aFile, hFile) & ~(myOcc | enemyOcc);
                boolean emptyPath = (castlingLeft != 0x0L && rightOfRook != 0x0L);

                legality = emptyPath && castlingLegality(color, pos, castlingLeft, aFile);
                legalMoves |= legality? castlingLeft : 0x0L;
            }
        }

         return legalMoves;
    }

    // TODO: collapse all MoveLegality methods into one
    ///true -> legal move | false -> illegal move
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