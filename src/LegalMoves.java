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

        one = checkLegality(Color.White, PieceType.Pawn, pos, one)? one : 0x0L;
        two = checkLegality(Color.White, PieceType.Pawn, pos, two)? two : 0x0L;
        NE = checkLegality(Color.White, PieceType.Pawn, pos, NE)? NE : 0x0L;
        NW = checkLegality(Color.White, PieceType.Pawn, pos, NW)? NW : 0x0L;

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

    private long bishopLegalMoves(long pos, Color color) {

        long bishopsNotA = pos & ~bitboards.getFile(0);
        long bishopsNotH = pos & ~bitboards.getFile(7);
        long myOcc = bitboards.getColorOcc(color);
        long occ = bitboards.getOcc();

        return recLineGen(bishopsNotH << 7, myOcc, occ, Direction.NorthEast)
                | recLineGen(bishopsNotH >>> 9, myOcc, occ, Direction.SouthEast)
                | recLineGen(bishopsNotA >>> 7, myOcc, occ, Direction.SouthWest)
                | recLineGen(bishopsNotA << 9, myOcc, occ, Direction.NorthWest);
    }

    private long rookLegalMoves(long pos, Color color) {

        long rooksNotA = pos & ~bitboards.getFile(0);
        long rooksNotH = pos & ~bitboards.getFile(7);
        long myOcc = bitboards.getColorOcc(color);
        long occ = bitboards.getOcc();

        return recLineGen(pos << 8, myOcc, occ, Direction.North)
                | recLineGen(rooksNotH >>> 1, myOcc, occ, Direction.East)
                | recLineGen(pos >>> 8, myOcc, occ, Direction.South)
                | recLineGen(rooksNotA << 1, myOcc, occ, Direction.West);
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
    private boolean checkLegality (Color color, PieceType pieceType, long from, long to) {

        Color enemy = (color == Color.White)? Color.Black : Color.White;

        bitboards.setPieces(color, pieceType, from, to);
        boolean legality = !(checkForCheck(color, enemy));
        bitboards.setPieces(color, pieceType, to, from);
        return legality;
    }

    ///true -> in check | false -> not in check
    private boolean checkForCheck (Color myColor, Color enemyColor) {

        long enemyAtkBoard = attackPatterns.getFullAtkBoard(enemyColor);
        long myKing = bitboards.getPieces(myColor, PieceType.King);

        return (enemyAtkBoard & myKing) != 0;
    }


}
