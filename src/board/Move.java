package board;

import static constants.BoardConstants.*;

/**
 * Utility class for encoding and decoding chess moves.
 * <p>
 * Moves are represented as 16-bit {@code short} values for memory efficiency.
 * The bits are laid out as follows:
 * <pre>
 * 15  14  13  12  11  10   9   8   7   6   5   4   3   2   1   0
 * +---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+
 * |   Move Type   |      From Square      |       To Square       |
 * +---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+
 * </pre>
 * <ul>
 * <li><b>0-5:</b> To Square (0-63)</li>
 * <li><b>6-11:</b> From Square (0-63)</li>
 * <li><b>12-15:</b> Move Type (Flags for capture, promotion, castle, etc.)</li>
 * </ul>
 */
public class Move {

    // Private constructor to prevent instantiation
    private Move(){}

    /**
     * Encodes a move into a 16-bit short.
     *
     * @param from the starting square (0-63)
     * @param to the destination square (0-63)
     * @param moveType the flag representing the type of move (e.g., capture, quiet, promotion)
     * @return the encoded move as a {@code short}
     */
    public static short encode(int from, int to, int moveType) {
        return (short) (to | (from << 6) | (moveType << 12));
    }

    /**
     * Extracts the starting square from an encoded move.
     *
     * @param move the encoded move
     * @return the source square index (0-63)
     */
    public static int getFrom(short move) {
        return (move >>> 6) & (0x3F);
    }

    /**
     * Extracts the destination square from an encoded move.
     *
     * @param move the encoded move
     * @return the destination square index (0-63)
     */
    public static int getTo(short move) {
        return move & 0x3F;
    }

    /**
     * Extracts the move type flag from an encoded move.
     *
     * @param move the encoded move
     * @return the move type identifier (bits 12-15)
     */
    public static int getMoveType(short move) {
        return (move >>> 12) & 0xF;
    }

    /**
     * Decodes the promoted piece type from the move type flag.
     * <p>
     * This relies on the promotion flags being ordered such that the lower 2 bits
     * correspond to the piece type (Knight=0, Bishop=1, Rook=2, Queen=3).
     *
     * @param moveType the move type flag (must be a promotion type)
     * @return the base piece constant (e.g., {@code W_QUEEN}) representing the promoted piece
     */
    public static int getPromotedPieceBase(int moveType) {
        // (moveType & 0x3) maps to: 0->Knight, 1->Bishop, 2->Rook, 3->Queen
        return W_KNIGHT + ((moveType & 0x3) * 2);
    }

    /**
     * Returns a string representation of the move in coordinate notation (e.g., "e2e4").
     *
     * @param move the encoded move
     * @return the string representation
     */
    public static String toString(short move) {
        return SQUARE_NAMES[getFrom(move)] + SQUARE_NAMES[getTo(move)];
    }
}
