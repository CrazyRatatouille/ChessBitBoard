package board;

import static constants.BoardConstants.*;
import static constants.BitboardMasks.*;
import static board.Attacks.lookUpBishop;
import static board.Attacks.lookUpRook;

public class MoveGen {

    private static final short[] moves = new short[MAX_MOVES * MAX_GAME_LENGTH]; //~500kB could fully fit in my L2

    private MoveGen() {}

    public static int moves(BoardState boardState, int index) {

        int side = boardState.getSide();
        int oppSide = 1 ^ side;

        long myOcc = boardState.getColorOccupancy(side);
        long oppOcc = boardState.getColorOccupancy(oppSide);
        long fullOcc = myOcc | oppOcc;

        index = (side == WHITE)?
                addWPMoves(boardState, index, oppOcc, fullOcc) : addBPawnMoves(boardState, index, oppOcc, fullOcc);
        index = addKnightMoves(boardState, index, side, myOcc, oppOcc);
        index = addBishopMoves(boardState, index, side, myOcc, oppOcc, fullOcc);
        index = addRookMoves(boardState, index, side, myOcc, oppOcc, fullOcc);
        index = addQueenMoves(boardState, index, side, myOcc, oppOcc, fullOcc);
        index = addKingMoves(boardState, index, side, myOcc, oppOcc, fullOcc);

        return index;
    }

    public static short getMove(int index) {return moves[index];}

    private static int addWPMoves(BoardState boardState, int index, long oppOcc, long fullOcc) {

        long wPawns = boardState.getPieceBB(W_PAWN);
        long enPassantTarget = boardState.getEnPassantTarget();

        while (wPawns != 0) {

            //isolates the lsb
            long mask = (-wPawns) & wPawns;
            int from = Long.numberOfTrailingZeros(mask);

            long moveSet = (mask << 8) & ~fullOcc;
            moveSet |= ((((mask & SECOND_RANK) << 8) & moveSet) << 8) & ~fullOcc;

            moveSet |= (PAWN_MASK[from] & (oppOcc | enPassantTarget));

            while (moveSet != 0) {

                long moveMask = (-moveSet) & moveSet;
                int to = Long.numberOfTrailingZeros(moveMask);
                moveSet -= moveMask;

                int moveType = 0;
                short move;

                if ((moveMask & oppOcc) != 0) moveType |= CAPTURE;
                if ((moveMask & enPassantTarget) != 0) moveType |= EP_CAPTURE;

                if ((moveMask >>> 16) == mask) moveType |= DOUBLE_PAWN_PUSH;

                if ((moveMask & EIGHT_RANK) != 0) {
                    moveType |= PROMOTION;

                    for (int i = 0; i < 4; i++) {
                        moveType = (moveType & 0xC) | i;
                        move = Move.encode(from, to, moveType);
                        moves[index++] = move;
                    }

                } else {
                    move = Move.encode(from, to, moveType);
                    moves[index++] = move;
                }
            }

            wPawns -= mask;
        }

        return index;
    }

    private static int addBPawnMoves(BoardState boardState, int index, long oppOcc, long fullOcc) {

        long bPawns = boardState.getPieceBB(B_PAWN);
        long enPassantTarget = boardState.getEnPassantTarget();

        while (bPawns != 0) {

            //isolates the lsb
            long mask = (-bPawns) & bPawns;
            int from = Long.numberOfTrailingZeros(mask);

            long moveSet = (mask >>> 8) & ~fullOcc;
            moveSet |= ((((mask & SEVENTH_RANK) >>> 8) & moveSet) >>> 8) & ~fullOcc;

            moveSet |= (PAWN_MASK[BLACK * BOARD_SIZE + from] & (oppOcc | enPassantTarget));

            while (moveSet != 0) {

                long moveMask = (- moveSet) & moveSet;
                int to = Long.numberOfTrailingZeros(moveMask);
                moveSet -= moveMask;

                int moveType = 0;
                short move;

                if ((moveMask & oppOcc) != 0) moveType |= CAPTURE;
                if ((moveMask & enPassantTarget) != 0) moveType |= EP_CAPTURE;

                if ((moveMask << 16) == mask) moveType |= DOUBLE_PAWN_PUSH;

                if ((moveMask & FIRST_RANK) != 0) {
                    moveType |= PROMOTION;

                    for (int i = 0; i < 4; i++) {
                        moveType = (moveType & 0xC) | i;
                        move = Move.encode(from, to, moveType);
                        moves[index++] = move;
                    }

                } else {
                    move = Move.encode(from, to, moveType);
                    moves[index++] = move;
                }
            }

            bPawns -= mask;
        }

        return index;
    }

    private static int addKnightMoves(BoardState boardState, int index, int side, long myOcc, long oppOcc) {

        long knights = boardState.getPieceBB(W_KNIGHT + side);

        while (knights != 0) {

            long mask = (-knights) & knights;
            int from = Long.numberOfTrailingZeros(mask);

            long moveSet = KNIGHT_MASK[from] & ~myOcc;

            while (moveSet != 0) {

                long moveMask = (-moveSet) & moveSet;
                int to = Long.numberOfTrailingZeros(moveMask);

                int moveType = ((moveMask & oppOcc) != 0)? 0x4 : 0;

                short move = Move.encode(from, to, moveType);
                moves[index++] = move;

                moveSet -= moveMask;
            }

            knights -= mask;
        }

        return index;
    }

    private static int addBishopMoves(BoardState boardState, int index, int side, long myOcc, long oppOcc, long fullOcc) {

        long bishops = boardState.getPieceBB(W_BISHOP + side);

        while (bishops != 0) {

            long mask = (-bishops) & bishops;
            int from = Long.numberOfTrailingZeros(mask);

            long atkMask = lookUpBishop(from, myOcc, fullOcc);

            while (atkMask != 0) {

                long moveMask = (-atkMask) & atkMask;
                int to = Long.numberOfTrailingZeros(moveMask);

                int moveType = ((moveMask & oppOcc) != 0)? 0x4 : 0;

                short move = Move.encode(from, to, moveType);
                moves[index++] = move;

                atkMask -= moveMask;
            }

            bishops -= mask;
        }

        return index;
    }

    private static int addRookMoves(BoardState boardState, int index, int side, long myOcc, long oppOcc, long fullOcc) {

        long rooks = boardState.getPieceBB(W_ROOK + side);

        while (rooks != 0) {

            long mask = (-rooks) & rooks;
            int from = Long.numberOfTrailingZeros(mask);

            long atkMask = lookUpRook(from, myOcc, fullOcc);

            while (atkMask != 0) {

                long moveMask = (-atkMask) & atkMask;
                int to = Long.numberOfTrailingZeros(moveMask);

                int moveType = ((moveMask & oppOcc) != 0)? 0x4 : 0;

                short move = Move.encode(from, to, moveType);
                moves[index++] = move;

                atkMask -= moveMask;
            }

            rooks -= mask;
        }

        return index;
    }

    private static int addQueenMoves(BoardState boardState, int index, int side, long myOcc, long oppOcc, long fullOcc) {

        long queens = boardState.getPieceBB(W_QUEEN + side);

        while (queens != 0) {

            long mask = (-queens) & queens;
            int from = Long.numberOfTrailingZeros(mask);

            long atkMask = lookUpBishop(from, myOcc, fullOcc) | lookUpRook(from, myOcc, fullOcc);

            while (atkMask != 0) {

                long moveMask = (-atkMask) & atkMask;
                int to = Long.numberOfTrailingZeros(moveMask);

                int moveType = ((moveMask & oppOcc) != 0)? 0x4 : 0;

                short move = Move.encode(from, to, moveType);
                moves[index++] = move;

                atkMask -= moveMask;
            }

            queens -= mask;
        }

        return index;
    }

    private static int addKingMoves(BoardState boardState, int index, int side, long myOcc, long oppOcc, long fullOcc) {

        long king = boardState.getPieceBB(W_KING + side);
        int from = Long.numberOfTrailingZeros(king);

        long atkMask = KING_MASK[from] & ~myOcc;

        while (atkMask != 0) {

            long moveMask = (-atkMask) & atkMask;
            int to = Long.numberOfTrailingZeros(moveMask);

            int moveType = ((moveMask & oppOcc) != 0)? 0x4 : 0;

            short move = Move.encode(from, to, moveType);
            moves[index++] = move;

            atkMask -= moveMask;
        }

        //isolates the castling rights to the two LSB for each color
        long castlingRights = (boardState.castlingRights() & (0x3L << (2 * side))) >>> (2 * side);

        long relevantRank = FIRST_RANK << (side * 56);

        if (Attacks.isInCheck(boardState, side)) return index;

        if ((castlingRights & 0x2) != 0) {
            int to = from + 2;
            int moveType = 0x2;

            long relFSq = (F_FILE & relevantRank);
            long relGSq = (G_FILE & relevantRank);

            if (((relFSq | relGSq) & fullOcc) == 0) {
                if (!Attacks.isSqareAttacked(boardState, relFSq, side) && !Attacks.isSqareAttacked(boardState, relGSq, side)) {
                    short move = Move.encode(from, to, moveType);
                    moves[index++] = move;
                }
            }

        }

        if ((castlingRights & 0x1) != 0) {
            int to = from - 2;
            int moveType = 0x3;

            long relBSq = (B_FILE & relevantRank);
            long relCSq = (C_FILE & relevantRank);
            long relDSq = (D_FILE & relevantRank);

            if (((relBSq | relCSq | relDSq) & fullOcc) == 0) {
                if (!Attacks.isSqareAttacked(boardState, relCSq, side) && !Attacks.isSqareAttacked(boardState, relDSq, side)) {
                    short move = Move.encode(from, to, moveType);
                    moves[index++] = move;
                }
            }
        }

        return index;
    }
}
