package board;

import constants.BitboardMasks;

import static constants.BitboardMasks.*;
import static constants.BoardConstants.*;

public class Attacks {

    //private constructor to ensure no Object creation
    private Attacks(){}

    /**
     * Determines whether the king of the specified side is currently in check.
     *
     * @param boardState the current state of the board
     * @param side the side (color) of the king to check
     * @return {@code true} if the king is under attack, {@code false} otherwise
     */
    public static boolean isInCheck(BoardState boardState, int side) {

        long king = boardState.getPieceBB(W_KING + side);
        return isSqareAttacked(boardState, king, side);
    }

    /**
     * Determines whether a specific square is currently being attacked by any opponent pieces.
     *
     * @param boardState the current state of the board
     * @param sqMask a bitboard mask with exactly one bit set, representing the square to evaluate
     * @param side the side (color) of the defending piece
     * @return {@code true} if the square is under attack by the opponent, {@code false} otherwise
     */
    public static boolean isSqareAttacked(BoardState boardState, long sqMask, int side) {

        int sq = Long.numberOfTrailingZeros(sqMask);
        int oppSide = 1 ^ side;

        long relevantPawns = BitboardMasks.PAWN_MASK[side * BOARD_SIZE + sq];

        long kingAtkMask = kingAtk(boardState, oppSide);

        long AtkByPawns = relevantPawns & boardState.getPieceBB(W_PAWN + oppSide);
        long AtkByKnights = KNIGHT_MASK[sq] & boardState.getPieceBB(W_KNIGHT + oppSide);
        long AtkByKing = kingAtkMask & sqMask;
        if ((AtkByPawns | AtkByKnights | AtkByKing) != 0) return true;

        long fullOcc = boardState.getOccupancy();

        long AtkOnDiagonals = lookUpBishop(sq, 0L, fullOcc)
                & (boardState.getPieceBB(W_BISHOP + oppSide) | boardState.getPieceBB(W_QUEEN + oppSide));
        long AtkOnStraights = lookUpRook(sq, 0L, fullOcc)
                & (boardState.getPieceBB(W_ROOK + oppSide) | boardState.getPieceBB(W_QUEEN + oppSide));

        return (AtkOnDiagonals | AtkOnStraights) != 0;
    }

    /**
     * Calculates the combined attack mask for all white pawns on the board.
     *
     * @param boardState the current state of the board
     * @return a bitboard representing all squares currently attacked by white pawns
     */
    public static long wPawnAtk(BoardState boardState) {
        long pawns = boardState.getPieceBB(W_PAWN);

        //all pawns that are not on the A file are shifted by 7 bits to the left to calculate all valid NW attacks
        //all pawns that are not on the H file are shifted by 9 bits to the left to calculate all valid NE attacks
        return ((pawns & ~A_FILE) << 7) | ((pawns & ~H_FILE) << 9);
    }

    /**
     * Calculates the combined attack mask for all black pawns on the board.
     *
     * @param boardState the current state of the board
     * @return a bitboard representing all squares currently attacked by black pawns
     */
    public static long bPawnAtk(BoardState boardState) {
        long pawns = boardState.getPieceBB(B_PAWN);

        //all pawns that are not on the A file are shifted by 9 bits to the right to calculate all valid SW attacks
        //all pawns that are not on the H file are shifted by 7 bits to the right to calculate all valid SE attacks
        return ((pawns & ~A_FILE) >>> 9) | ((pawns & ~H_FILE) >>> 7);
    }

    /**
     * Calculates the combined attack mask for all knights of the specified side.
     *
     * @param boardState the current state of the board
     * @param side the side (color) of the knights
     * @return a bitboard representing all squares currently attacked by the knights
     */
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

    /**
     * Calculates the combined attack mask for all bishops of the specified side.
     *
     * @param boardState the current state of the board
     * @param side the side (color) of the bishops
     * @param myOcc the occupancy of friendly pieces (used for masking)
     * @param fullOcc the combined occupancy of all pieces (used for blocking)
     * @return a bitboard representing all squares currently attacked by the bishops
     */
    public static long bishopAtk(BoardState boardState, int side, long myOcc, long fullOcc) {

        long bishops = boardState.getPieceBB(W_BISHOP + side);
        long fullAtkMask = 0;

        while (bishops != 0) {

            long fromMask = (-bishops) & bishops;
            int from = Long.numberOfTrailingZeros(fromMask);

            long atkMask = lookUpBishop(from, myOcc, fullOcc);
            fullAtkMask |= atkMask;

            bishops -= fromMask;
        }

        return fullAtkMask;
    }

    /**
     * Calculates the combined attack mask for all rooks of the specified side.
     *
     * @param boardState the current state of the board
     * @param side the side (color) of the rooks
     * @param myOcc the occupancy of friendly pieces (used for masking)
     * @param fullOcc the combined occupancy of all pieces (used for blocking)
     * @return a bitboard representing all squares currently attacked by the rooks
     */
    public static long rookAtk(BoardState boardState, int side, long myOcc, long fullOcc) {

        long rooks = boardState.getPieceBB(W_ROOK + side);
        long fullAtkMask = 0;

        while (rooks != 0) {

            long fromMask = (-rooks) & rooks;
            int from = Long.numberOfTrailingZeros(fromMask);

            long atkMask = lookUpRook(from, myOcc, fullOcc);
            fullAtkMask |= atkMask;

            rooks -= fromMask;
        }

        return fullAtkMask;
    }

    /**
     * Calculates the combined attack mask for all queens of the specified side.
     *
     * @param boardState the current state of the board
     * @param side the side (color) of the queens
     * @param myOcc the occupancy of friendly pieces (used for masking)
     * @param fullOcc the combined occupancy of all pieces (used for blocking)
     * @return a bitboard representing all squares currently attacked by the queens
     */
    public static long queenAtk(BoardState boardState, int side, long myOcc, long fullOcc) {

        long queens = boardState.getPieceBB(W_QUEEN + side);
        long fullAtkMask = 0;

        while (queens != 0) {

            long fromMask = (-queens) & queens;
            int from = Long.numberOfTrailingZeros(fromMask);

            long atkMask = lookUpBishop(from, myOcc, fullOcc) | lookUpRook(from, myOcc, fullOcc);
            fullAtkMask |= atkMask;

            queens -= fromMask;
        }

        return fullAtkMask;
    }

    /**
     * Retrieves the precomputed attack mask for the king based on its
     * current position on the board.
     *
     * @param boardState the current state of the board
     * @param side the side (color) of the king
     * @return a bitboard representing all squares the king attacks
     * (geometrically), ignoring friendly occupancy
     */
    public static long kingAtk(BoardState boardState, int side) {

        long king = boardState.getPieceBB(W_KING + side);
        int from = Long.numberOfTrailingZeros(king);

        return KING_MASK[from];
    }

    /**
     * Looks up a precomputed bitboard with bits active on all squares
     * a bishop on the specified square can occupy within one move
     *
     * @param square the starting square (0-63)
     * @param myOcc the occupancy of friendly pieces
     * @param fullOcc the combined occupancy of all pieces
     * @return a bitboard representation of all squares, which a bishop on the specified
     * square can occupy within one move
     *
     * @implNote
     * This method uses a pre-computed "magic" constant to hash sparse blocker bits.
     * The multiplication ({@code blockers * magic}) "smears" the relevant bits into
     * the upper regions of the 64-bit integer. The shift ({@code >>>}) then extracts
     * the top N bits to form a compact, dense index, avoiding collisions.
     */
    public static long lookUpBishop(int square, long myOcc, long fullOcc) {

        long magic = BISHOP_MAGICS[square];

        //bitboard of all relevant blocker squares for this bishop
        long potBlockers = BISHOP_BLOCKER_MASK[square];
        long blockers = potBlockers & fullOcc;
        int shift = BOARD_SIZE - Long.bitCount(potBlockers);

        int offset = BISHOP_MBB_OFFSETS[square];

        //Magic hashing (see @implNote)
        int index = (int) (offset + ((blockers * magic) >>> shift));

        return (BISHOP_MASK[index] & ~myOcc);
    }

    /**
     * Looks up a precomputed bitboard with bits active on all squares
     * a rook on the specified square can occupy within one move
     *
     * @param square the starting square (0-63)
     * @param myOcc the occupancy of friendly pieces
     * @param fullOcc the combined occupancy of all pieces
     * @return a bitboard representation of all squares, which a rook on the specified
     * square can occupy within one move
     *
     * @implNote
     * Uses the same Magic Bitboard hashing algorithm as {@link #lookUpBishop(int, long, long)}.
     */
    public static long lookUpRook(int square, long myOcc, long fullOcc) {

        long magic = ROOK_MAGICS[square];

        //bitboard of all relevant blocker squares for this rook
        long potBlockers = ROOK_BLOCKER_MASK[square];
        long blockers = potBlockers & fullOcc;
        int shift = BOARD_SIZE - Long.bitCount(potBlockers);

        int offset = ROOK_MBB_OFFSETS[square];

        //Magic hashing (see @implNote)
        int index = (int) (offset + ((blockers * magic) >>> shift));

        return (ROOK_MASK[index] & ~myOcc);
    }
}
