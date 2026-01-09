package board;

import static constants.BitboardMasks.*;
import static constants.BoardConstants.*;

public class Attacks {

    private Attacks(){}

    public static boolean isInCheck(BoardState boardState, int side) {

        int oppSide = 1 ^ side;

        long oppOcc = boardState.getColorOccupancy(oppSide);
        long fullOcc = boardState.getOccupancy();

        long king = boardState.getPieceBB(W_KING + side);

        long pawnAtkMaks = (side == WHITE)? bPawnAtk(boardState) : wPawnAtk(boardState);
        long oppAtkMask = pawnAtkMaks | knightAtk(boardState, oppSide) | bishopAtk(boardState, oppSide, oppOcc, fullOcc)
                | rookAtk(boardState, oppSide, oppOcc, fullOcc) | queenAtk(boardState, oppSide, oppOcc, fullOcc) | kingAtk(boardState, oppSide);

        return ((king & oppAtkMask) != 0);
    }

    public static long wPawnAtk(BoardState boardState) {
        long pawns = boardState.getPieceBB(W_PAWN);
        return ((pawns & ~A_FILE) << 7) | ((pawns & ~H_FILE) << 9);
    }

    public static long bPawnAtk(BoardState boardState) {
        long pawns = boardState.getPieceBB(B_PAWN);
        return ((pawns & ~A_FILE) >>> 9) | ((pawns & ~H_FILE) >>> 7);
    }

    public static long knightAtk(BoardState boardState, int side) {

        long knights = boardState.getPieceBB(W_KNIGHT + side);
        long fullAtkMask = 0;

        while (knights != 0) {

            long fromMask = (-knights) & knights;
            int from = Long.numberOfTrailingZeros(fromMask);

            fullAtkMask |= KNIGHT_MASK[from];

            knights -= fromMask;
        }

        return fullAtkMask;
    }

    public static long bishopAtk(BoardState boardState, int side, long myOcc, long fullOcc) {

        long bishops = boardState.getPieceBB(W_BISHOP + side);
        long fullAtkMask = 0;

        while (bishops != 0) {

            long mask = (-bishops) & bishops;
            int from = Long.numberOfTrailingZeros(mask);

            long atkMask = lookUpBishop(from, myOcc, fullOcc);
            fullAtkMask |= atkMask;

            bishops -= mask;
        }

        return fullAtkMask;
    }

    public static long rookAtk(BoardState boardState, int side, long myOcc, long fullOcc) {

        long rooks = boardState.getPieceBB(W_ROOK + side);
        long fullAtkMask = 0;

        while (rooks != 0) {

            long mask = (-rooks) & rooks;
            int from = Long.numberOfTrailingZeros(mask);

            long atkMask = lookUpRook(from, myOcc, fullOcc);
            fullAtkMask |= atkMask;

            rooks -= mask;
        }

        return fullAtkMask;
    }

    public static long queenAtk(BoardState boardState, int side, long myOcc, long fullOcc) {

        long queens = boardState.getPieceBB(W_QUEEN + side);
        long fullAtkMask = 0;

        while (queens != 0) {

            long mask = (-queens) & queens;
            int from = Long.numberOfTrailingZeros(mask);

            long atkMask = lookUpBishop(from, myOcc, fullOcc) | lookUpRook(from, myOcc, fullOcc);
            fullAtkMask |= atkMask;

            queens -= mask;
        }

        return fullAtkMask;
    }

    public static long kingAtk(BoardState boardState, int side) {

        long king = boardState.getPieceBB(W_KING + side);
        int from = Long.numberOfTrailingZeros(king);

        return KING_MASK[from];
    }

    public static long lookUpBishop(int sq, long myOcc ,long fullOcc) {

        long magic = BISHOP_MAGICS[sq];
        long potBlockers = BISHOP_BLOCKER_MASK[sq];
        long blockers = potBlockers & fullOcc;
        int shift = BOARD_SIZE - Long.bitCount(potBlockers);

        int offset = BISHOP_MBB_OFFSETS[sq];
        int index = (int) (offset + ((blockers * magic) >>> shift));

        return (BISHOP_MASK[index] & ~myOcc);
    }

    public static long lookUpRook(int sq, long myOcc, long fullOcc) {

        long magic = ROOK_MAGICS[sq];
        long potBlockers = ROOK_BLOCKER_MASK[sq];
        long blockers = potBlockers & fullOcc;
        int shift = BOARD_SIZE - Long.bitCount(potBlockers);

        int offset = ROOK_MBB_OFFSETS[sq];
        int index = (int) (offset + ((blockers * magic) >>> shift));

        return (ROOK_MASK[index] & ~myOcc);
    }
}
