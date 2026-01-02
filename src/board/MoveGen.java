package board;

import static constants.BoardConstants.*;
import static constants.BitboardMasks.*;
import board.Move;
import constants.BitboardMasks;

public class MoveGen {

    //TODO: UNFINISHED, PRETTY MUCH EVERYTHING IS STILL LEFT TO DO
    private MoveGen() {}


    //TODO: potentially remove paramater and change BoardState to static class
    public static short[] moves(BoardState boardState) {

        short[] moves = new short[MAX_MOVES];

        int side = boardState.getSide();
        int oppSide = 1 ^ side;

        long myOcc = boardState.getColorOccupancy(side);
        long oppOcc = boardState.getColorOccupancy(oppSide);
        long fullOcc = myOcc | oppOcc;

        int index = (side == WHITE)?
                addWPMoves(boardState, moves, oppOcc, fullOcc) : addBPawnMoves(boardState, moves, oppOcc, fullOcc);
        index = addKnightMoves(boardState, moves, index, side, myOcc, oppOcc);
        index = addBishopMoves(boardState, moves,  index, side, myOcc, oppOcc, fullOcc);
        index = addRookMoves(boardState, moves,  index, side, myOcc, oppOcc, fullOcc);


        return moves;
    }

//    private void legalCheck(short move) {
//    }

    private static int addWPMoves(BoardState boardState, short[] moves, long oppOcc, long fullOcc) {

        long wPawns = boardState.getPieces(W_PAWN);
        long enPassantTarget = boardState.getEnPassantTarget();

        int index = 0;

        while (wPawns != 0) {

            //isolates the lsb
            long mask = (-wPawns) & wPawns;
            int from = Long.numberOfTrailingZeros(mask);

            long moveSet = (mask << 8) & ~fullOcc;
            moveSet |= ((((mask & SECOND_RANK) << 8) & moveSet) << 8) & ~fullOcc;

            moveSet |= (PAWN_MASK[WHITE * BOARD_SIZE + from] & (oppOcc | enPassantTarget));

            while (moveSet != 0) {

                long moveMask = (- moveSet) & moveSet;
                int to = Long.numberOfTrailingZeros(moveMask);
                moveSet -= moveMask;

                int moveType = 0;
                short move;

                if ((moveMask & oppOcc) != 0) moveType |= 0x4;
                if ((moveMask & enPassantTarget) != 0) moveType |= 0x5;

                if ((moveMask & EIGHT_RANK) != 0) {
                    moveType |= 0x8;

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

    private static int addBPawnMoves(BoardState boardState, short[] moves, long oppOcc, long fullOcc) {

        long bPawns = boardState.getPieces(B_PAWN);
        long enPassantTarget = boardState.getEnPassantTarget();

        int index = 0;

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

                if ((moveMask & oppOcc) != 0) moveType |= 0x4;
                if ((moveMask & enPassantTarget) != 0) moveType |= 0x5;

                if ((moveMask & FIRST_RANK) != 0) {
                    moveType |= 0x8;

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

    private static int addKnightMoves(BoardState boardState, short[] moves, int index, int side, long myOcc, long oppOcc) {

        long knights = boardState.getPieces(W_KNIGHT + side);

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
    
    private static int addBishopMoves(BoardState boardState, short[] moves, int index, int side, long myOcc, long oppOcc, long fullOcc) {

        long bishops = boardState.getPieces(W_BISHOP + side);

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

    private static int addRookMoves(BoardState boardState, short[] moves, int index, int side, long myOcc, long oppOcc, long fullOcc) {

        long rooks = boardState.getPieces(W_ROOK + side);

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

    private static int addQueenMoves(BoardState boardState, short[] moves, int index, int side, long myOcc, long oppOcc, long fullOcc) {

        long queens = boardState.getPieces(W_QUEEN + side);

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

}
