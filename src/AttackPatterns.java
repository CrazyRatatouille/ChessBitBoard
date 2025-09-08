public class AttackPatterns {

    private final Bitboards bitboards;
    private final PieceType[] pieces = PieceType.values();

    public AttackPatterns(Bitboards bitboards) {
        this.bitboards = bitboards;
    }

    public long getFullAtkBoard (Color color) {

        long fullAtkBoard = 0x0L;

        for (PieceType pieceType : pieces) {
           fullAtkBoard |= getAttackPattern(color, pieceType);
        }

        return fullAtkBoard;
    }

    private long getAttackPattern (Color color, PieceType pieceType) {

        if (pieceType == PieceType.Pawn) {
            if (color == Color.White) return wPawnAtkPattern(color);
            return bPawnAtkPattern(color);
        }

        if (pieceType == PieceType.Knight) return knightAtkPattern(color);
        if (pieceType == PieceType.Bishop) return bishopAtkPattern(color);
        if (pieceType == PieceType.Rook) return rookAtkPattern(color);
        if (pieceType == PieceType.Queen) return queenAtkPattern(color);
        else return kingAtkPattern(color);
    }

    private long wPawnAtkPattern(Color color) {

        long pawns = bitboards.getPieces(color, PieceType.Pawn);

        long aFile = bitboards.getFile(0);
        long hFile = bitboards.getFile(7);

        return  ((pawns & ~aFile) << 9) | ((pawns & ~hFile) << 7);
    }

    private long bPawnAtkPattern(Color color) {

        long pawns = bitboards.getPieces(color, PieceType.Pawn);

        long aFile = bitboards.getFile(0);
        long hFile = bitboards.getFile(7);

        return  ((pawns & ~aFile) >>> 7) | ((pawns & ~hFile) >>> 9);
    }

    private long knightAtkPattern(Color color) {

        long knights = bitboards.getPieces(color, PieceType.Knight);

        long aFile = bitboards.getFile(0);
        long bFile = bitboards.getFile(1);
        long gFile = bitboards.getFile(6);
        long hFile = bitboards.getFile(7);


        return (knights & ~aFile) << 17
                | (knights & ~hFile) << 15
                | (knights & ~(aFile | bFile)) << 10
                | (knights & ~(gFile | hFile)) << 6
                | (knights & ~(aFile | bFile)) >>> 6
                | (knights & ~(gFile | hFile)) >>> 10
                | (knights & ~aFile) >>> 15
                | (knights & ~hFile) >>> 17;
    }

    private long bishopAtkPattern(Color color) {

        long bishops = bitboards.getPieces(color, PieceType.Bishop);
        long bishopsNotA = bishops & ~bitboards.getFile(0);
        long bishopsNotH = bishops & ~bitboards.getFile(7);
        long myOcc = bitboards.getColorOcc(color);
        long occ = bitboards.getOcc();

        return recLineGen(bishopsNotH << 7, myOcc, occ, Direction.NorthEast)
                | recLineGen(bishopsNotH >>> 9, myOcc, occ, Direction.SouthEast)
                | recLineGen(bishopsNotA >>> 7, myOcc, occ, Direction.SouthWest)
                | recLineGen(bishopsNotA << 9, myOcc, occ, Direction.NorthWest);
    }

    private long rookAtkPattern(Color color) {

        long rooks = bitboards.getPieces(color, PieceType.Rook);
        long rooksNotA = rooks & ~bitboards.getFile(0);
        long rooksNotH = rooks & ~bitboards.getFile(7);
        long myOcc = bitboards.getColorOcc(color);
        long occ = bitboards.getOcc();

        return recLineGen(rooks << 8, myOcc, occ, Direction.North)
                | recLineGen(rooksNotH >>> 1, myOcc, occ, Direction.East)
                | recLineGen(rooks >>> 8, myOcc, occ, Direction.South)
                | recLineGen(rooksNotA << 1, myOcc, occ, Direction.West);
    }

    private long queenAtkPattern(Color color) {

        long queens = bitboards.getPieces(color, PieceType.Queen);
        long queensNotA = queens & ~bitboards.getFile(0);
        long queensNotH = queens & ~bitboards.getFile(7);
        long myOcc = bitboards.getColorOcc(color);
        long occ = bitboards.getOcc();

        return recLineGen(queens << 8, myOcc, occ, Direction.North)
                | recLineGen(queensNotH >>> 1, myOcc, occ, Direction.East)
                | recLineGen(queens >>> 8, myOcc, occ, Direction.South)
                | recLineGen(queensNotA << 1, myOcc, occ, Direction.West)

                |recLineGen(queensNotH << 7, myOcc, occ, Direction.NorthEast)
                | recLineGen(queensNotH >>> 9, myOcc, occ, Direction.SouthEast)
                | recLineGen(queensNotA >>> 7, myOcc, occ, Direction.SouthWest)
                | recLineGen(queensNotA << 9, myOcc, occ, Direction.NorthWest);
    }

    private long kingAtkPattern(Color color) {

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

}
