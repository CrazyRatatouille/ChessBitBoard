public class AttackPatterns {

    private final Bitboards bitboards;
    private final PieceType[] pieces = PieceType.values();

    public AttackPatterns(Bitboards bitboards) {
        this.bitboards = bitboards;
    }

    public long getFullAtkBoard (SideColor sideColor) {

        long fullAtkBoard = 0x0L;

        for (PieceType pieceType : pieces) {
           fullAtkBoard |= getAttackPattern(sideColor, pieceType);
        }

        return fullAtkBoard;
    }

    private long getAttackPattern (SideColor sideColor, PieceType pieceType) {

        if (pieceType == PieceType.Pawn) {
            if (sideColor == SideColor.White) return wPawnAtkPattern(sideColor);
            return bPawnAtkPattern(sideColor);
        }

        if (pieceType == PieceType.Knight) return knightAtkPattern(sideColor);
        if (pieceType == PieceType.Bishop) return bishopAtkPattern(sideColor);
        if (pieceType == PieceType.Rook) return rookAtkPattern(sideColor);
        if (pieceType == PieceType.Queen) return queenAtkPattern(sideColor);
        else return kingAtkPattern(sideColor);
    }

    private long wPawnAtkPattern(SideColor sideColor) {

        long pawns = bitboards.getPieces(sideColor, PieceType.Pawn);

        long aFile = bitboards.getFile(0);
        long hFile = bitboards.getFile(7);

        return  ((pawns & ~aFile) << 9) | ((pawns & ~hFile) << 7);
    }

    private long bPawnAtkPattern(SideColor sideColor) {

        long pawns = bitboards.getPieces(sideColor, PieceType.Pawn);

        long aFile = bitboards.getFile(0);
        long hFile = bitboards.getFile(7);

        return  ((pawns & ~aFile) >>> 7) | ((pawns & ~hFile) >>> 9);
    }

    private long knightAtkPattern(SideColor sideColor) {

        long knights = bitboards.getPieces(sideColor, PieceType.Knight);

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

    private long bishopAtkPattern(SideColor sideColor) {

        long bishops = bitboards.getPieces(sideColor, PieceType.Bishop);
        long bishopsNotA = bishops & ~bitboards.getFile(0);
        long bishopsNotH = bishops & ~bitboards.getFile(7);
        long myOcc = bitboards.getColorOcc(sideColor);
        long occ = bitboards.getOcc();

        return recLineGen(bishopsNotH << 7, myOcc, occ, Direction.NorthEast)
                | recLineGen(bishopsNotH >>> 9, myOcc, occ, Direction.SouthEast)
                | recLineGen(bishopsNotA >>> 7, myOcc, occ, Direction.SouthWest)
                | recLineGen(bishopsNotA << 9, myOcc, occ, Direction.NorthWest);
    }

    private long rookAtkPattern(SideColor sideColor) {

        long rooks = bitboards.getPieces(sideColor, PieceType.Rook);
        long rooksNotA = rooks & ~bitboards.getFile(0);
        long rooksNotH = rooks & ~bitboards.getFile(7);
        long myOcc = bitboards.getColorOcc(sideColor);
        long occ = bitboards.getOcc();

        return recLineGen(rooks << 8, myOcc, occ, Direction.North)
                | recLineGen(rooksNotH >>> 1, myOcc, occ, Direction.East)
                | recLineGen(rooks >>> 8, myOcc, occ, Direction.South)
                | recLineGen(rooksNotA << 1, myOcc, occ, Direction.West);
    }

    private long queenAtkPattern(SideColor sideColor) {

        long queens = bitboards.getPieces(sideColor, PieceType.Queen);
        long queensNotA = queens & ~bitboards.getFile(0);
        long queensNotH = queens & ~bitboards.getFile(7);
        long myOcc = bitboards.getColorOcc(sideColor);
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

    private long kingAtkPattern(SideColor sideColor) {

        long king = bitboards.getPieces(sideColor, PieceType.King);
        long kingNotA = king & ~bitboards.getFile(0);
        long kingNotH = king & ~bitboards.getFile(7);

        return king << 8
                | kingNotH << 7
                | kingNotH >>> 1
                | kingNotH >>> 9
                | king >>> 8
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

}
