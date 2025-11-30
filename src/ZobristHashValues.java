import java.util.Random;

/**
 * Generates and stores the Zobrist hash values used for board state hashing.
 *
 * <p>Zobrist Hashing provides a highly efficient and collision-resistant method for uniquely identifying
 * a chess board position with a single {@code long} value. It is essential for implementing a
 * Transposition Table to detect repeated positions (3-fold repetition).</p>
 *
 * <p>Each unique combination of (PieceType, SideColor, Square) is assigned a single, cryptographically random {@code long}.
 * The hash for any given board is computed by XORing the random values for all pieces on the board.</p>
 */
public class ZobristHashValues {

    private static final long[] zobristHashValues = new long[12 * 64];

    /**
     * Initializes a unique, cryptographically random long for every possible
     * {@code PieceType}-{@code SideColor}-{@code Square} combination.
     */
    public ZobristHashValues() {

        long seed = 0x5F8C9A72D3B1E4C7L;
        Random random = new Random(seed);

        for (int i = 0; i < zobristHashValues.length; i++) {
            zobristHashValues[i] = random.nextLong();
        }
    }

    /**
     * Returns the unique Zobrist hash value for a specific piece occupying a specific square.
     *
     * @param sideColor the color of the piece.
     * @param pieceType the type of the piece.
     * @param square the position of the piece.
     * @return The 64-bit random long corresponding to this unique Piece-Square configuration.
     */
    public static long getHashCode(SideColor sideColor, PieceType pieceType, Square square) {

        long pos = square.pos();

        int colorAdj = (sideColor == SideColor.White)? 0 : 6;
        int pieceAdj = pieceType.ordinal();

        int index = (pieceAdj + colorAdj) * 64;
        index += Long.numberOfTrailingZeros(pos);

        return zobristHashValues[index];
    }
}
