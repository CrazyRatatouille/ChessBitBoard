package board;

import static constants.BoardConstants.*;

public class Move {

    private Move(){}

    //TODO: refactor later once specifics are known
    public static short encode(int from, int to, int moveType) {
        return (short) (to | (from << 6) | (moveType << 12));
    }

    public static int getFrom(short move) {
        return (move >>> 6) & (0x3F);
    }

    public static int getTo(short move) {
        return move & 0x3F;
    }

    public static int getMoveType(short move) {
        return (move >>> 12) & 0xF;
    }

    public static int getPromotedPieceBase(int moveType) {
        return W_KNIGHT + ((moveType & 0x3) << 1);
    }

    public static String toString(short move) {
        return SQUARE_NAMES[getFrom(move)] + " - " + SQUARE_NAMES[getTo(move)];
    }
}
