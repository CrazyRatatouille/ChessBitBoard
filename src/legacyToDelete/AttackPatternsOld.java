//import legacyToDelete.PieceType;
//
//public class AttackPatternsOld {
//
//    private final BitboardsOld bitboardsOld;
//    private final PieceType[] pieces = PieceType.values();
//
//    public AttackPatternsOld(BitboardsOld bitboardsOld) {
//        this.bitboardsOld = bitboardsOld;
//    }
//
//    public long getFullAtkBoard (SideColor sideColor) {
//
//        long fullAtkBoard = 0x0L;
//
//        for (PieceType pieceType : pieces) {
//           fullAtkBoard |= getAttackPattern(sideColor, pieceType);
//        }
//
//        return fullAtkBoard;
//    }
//
//    private long getAttackPattern (SideColor sideColor, PieceType pieceType) {
//
//        if (pieceType == PieceType.Pawn) {
//            if (sideColor == SideColor.White) return wPawnAtkPattern(sideColor);
//            return bPawnAtkPattern(sideColor);
//        }
//
//        if (pieceType == PieceType.Knight) return knightAtkPattern(sideColor);
//        if (pieceType == PieceType.Bishop) return bishopAtkPattern(sideColor);
//        if (pieceType == PieceType.Rook) return rookAtkPattern(sideColor);
//        if (pieceType == PieceType.Queen) return queenAtkPattern(sideColor);
//        else return kingAtkPattern(sideColor);
//    }
//
//    private long wPawnAtkPattern(SideColor sideColor) {
//
//        long pawns = bitboardsOld.getPieces(sideColor, PieceType.Pawn);
//
//        long aFile = bitboardsOld.getFile(0);
//        long hFile = bitboardsOld.getFile(7);
//
//        return  ((pawns & ~aFile) << 9) | ((pawns & ~hFile) << 7);
//    }
//
//    private long bPawnAtkPattern(SideColor sideColor) {
//
//        long pawns = bitboardsOld.getPieces(sideColor, PieceType.Pawn);
//
//        long aFile = bitboardsOld.getFile(0);
//        long hFile = bitboardsOld.getFile(7);
//
//        return  ((pawns & ~aFile) >>> 7) | ((pawns & ~hFile) >>> 9);
//    }
//
//    private long knightAtkPattern(SideColor sideColor) {
//
//        long knights = bitboardsOld.getPieces(sideColor, PieceType.Knight);
//
//        long aFile = bitboardsOld.getFile(0);
//        long bFile = bitboardsOld.getFile(1);
//        long gFile = bitboardsOld.getFile(6);
//        long hFile = bitboardsOld.getFile(7);
//
//
//        return (knights & ~aFile) << 17
//                | (knights & ~hFile) << 15
//                | (knights & ~(aFile | bFile)) << 10
//                | (knights & ~(gFile | hFile)) << 6
//                | (knights & ~(aFile | bFile)) >>> 6
//                | (knights & ~(gFile | hFile)) >>> 10
//                | (knights & ~aFile) >>> 15
//                | (knights & ~hFile) >>> 17;
//    }
//
//    private long bishopAtkPattern(SideColor sideColor) {
//
//        long bishops = bitboardsOld.getPieces(sideColor, PieceType.Bishop);
//        long bishopsNotA = bishops & ~bitboardsOld.getFile(0);
//        long bishopsNotH = bishops & ~bitboardsOld.getFile(7);
//        long myOcc = bitboardsOld.getColorOcc(sideColor);
//        long occ = bitboardsOld.getOcc();
//
//        return recLineGen(bishopsNotH << 7, myOcc, occ, DirectionOld.NorthEast)
//                | recLineGen(bishopsNotH >>> 9, myOcc, occ, DirectionOld.SouthEast)
//                | recLineGen(bishopsNotA >>> 7, myOcc, occ, DirectionOld.SouthWest)
//                | recLineGen(bishopsNotA << 9, myOcc, occ, DirectionOld.NorthWest);
//    }
//
//    private long rookAtkPattern(SideColor sideColor) {
//
//        long rooks = bitboardsOld.getPieces(sideColor, PieceType.Rook);
//        long rooksNotA = rooks & ~bitboardsOld.getFile(0);
//        long rooksNotH = rooks & ~bitboardsOld.getFile(7);
//        long myOcc = bitboardsOld.getColorOcc(sideColor);
//        long occ = bitboardsOld.getOcc();
//
//        return recLineGen(rooks << 8, myOcc, occ, DirectionOld.North)
//                | recLineGen(rooksNotH >>> 1, myOcc, occ, DirectionOld.East)
//                | recLineGen(rooks >>> 8, myOcc, occ, DirectionOld.South)
//                | recLineGen(rooksNotA << 1, myOcc, occ, DirectionOld.West);
//    }
//
//    private long queenAtkPattern(SideColor sideColor) {
//
//        long queens = bitboardsOld.getPieces(sideColor, PieceType.Queen);
//        long queensNotA = queens & ~bitboardsOld.getFile(0);
//        long queensNotH = queens & ~bitboardsOld.getFile(7);
//        long myOcc = bitboardsOld.getColorOcc(sideColor);
//        long occ = bitboardsOld.getOcc();
//
//        return recLineGen(queens << 8, myOcc, occ, DirectionOld.North)
//                | recLineGen(queensNotH >>> 1, myOcc, occ, DirectionOld.East)
//                | recLineGen(queens >>> 8, myOcc, occ, DirectionOld.South)
//                | recLineGen(queensNotA << 1, myOcc, occ, DirectionOld.West)
//
//                |recLineGen(queensNotH << 7, myOcc, occ, DirectionOld.NorthEast)
//                | recLineGen(queensNotH >>> 9, myOcc, occ, DirectionOld.SouthEast)
//                | recLineGen(queensNotA >>> 7, myOcc, occ, DirectionOld.SouthWest)
//                | recLineGen(queensNotA << 9, myOcc, occ, DirectionOld.NorthWest);
//    }
//
//    private long kingAtkPattern(SideColor sideColor) {
//
//        long king = bitboardsOld.getPieces(sideColor, PieceType.King);
//        long kingNotA = king & ~bitboardsOld.getFile(0);
//        long kingNotH = king & ~bitboardsOld.getFile(7);
//
//        return king << 8
//                | kingNotH << 7
//                | kingNotH >>> 1
//                | kingNotH >>> 9
//                | king >>> 8
//                | kingNotA >>> 7
//                | kingNotA << 1
//                | kingNotA << 9;
//    }
//
//    private long recLineGen(long movedPieces, long myOcc, long occ, DirectionOld direction) {
//
//        if (movedPieces  == 0x0L) return 0x0L;
//
//        long goFurther = movedPieces & ~occ;
//        long notAtkMyPieces = movedPieces & ~myOcc;
//
//        long piecesNotA = goFurther & ~bitboardsOld.getFile(0);
//        long piecesNotH = goFurther & ~bitboardsOld.getFile(7);
//
//        if (direction == DirectionOld.North) return notAtkMyPieces | recLineGen(goFurther << 8, myOcc, occ, direction);
//        if (direction == DirectionOld.East) return notAtkMyPieces | recLineGen(piecesNotH >>> 1, myOcc, occ, direction);
//        if (direction == DirectionOld.South) return notAtkMyPieces | recLineGen(goFurther >>> 8, myOcc, occ, direction);
//        if (direction == DirectionOld.West) return notAtkMyPieces | recLineGen(piecesNotA << 1, myOcc, occ, direction);
//
//        if (direction == DirectionOld.NorthEast) return notAtkMyPieces | recLineGen(piecesNotH << 7, myOcc, occ, direction);
//        if (direction == DirectionOld.SouthEast) return notAtkMyPieces | recLineGen(piecesNotH >>> 9, myOcc, occ, direction);
//        if (direction == DirectionOld.SouthWest) return notAtkMyPieces | recLineGen(piecesNotA >>> 7, myOcc, occ, direction);
//        else return notAtkMyPieces | recLineGen(piecesNotA << 9, myOcc, occ, direction);
//    }
//
//}
