public enum Square {

    A1, B1, C1, D1, E1, F1, G1, H1,
    A2, B2, C2, D2, E2, F2, G2, H2,
    A3, B3, C3, D3, E3, F3, G3, H3,
    A4, B4, C4, D4, E4, F4, G4, H4,
    A5, B5, C5, D5, E5, F5, G5, H5,
    A6, B6, C6, D6, E6, F6, G6, H6,
    A7, B7, C7, D7, E7, F7, G7, H7,
    A8, B8, C8, D8, E8, F8, G8, H8;


    /**
     * The call of this method returns a bitboard(long) representation of the Square {@code this}. This method
     * is mainly used to keep Code readable by using Squares instead of arbitrary long numbers.
     *
     * @return the bitboard representation of {@code this} Square with the LSB being A1
     */
    public long pos() {
        return 1L << this.ordinal();
    }

    /**
     * A static utility method that converts a single-set-bit bitboard representation back to its corresponding Square
     * enum. This method is mainly used to keep code readable for humans by using a Square representation for Variables
     * and the bitboards for computation.
     *
     * @param bitBoard the bitboard whose Square representation is to be computed
     * @return null if the provided bitboard has more/less activated bits than 1, otherwise
     * the Square which the bitboard {@code bitBoard} describes with the LSB being A1
     */
    public static Square getSquare(long bitBoard) {

        if (Long.bitCount(bitBoard) != 1) return null;

        int index = Long.numberOfTrailingZeros(bitBoard);
        return Square.values()[index];
    }
}
