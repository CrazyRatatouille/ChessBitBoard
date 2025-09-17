import java.util.Random;

public class ZobristHashValues {

    long[] zobristHashValues = new long[12 * 64];

    public ZobristHashValues() {

        long seed = 0x5F8C9A72D3B1E4C7L;
        Random random = new Random(seed);

        for (int i = 0; i < zobristHashValues.length; i++) {
            zobristHashValues[i] = random.nextLong();
        }
    }

    public long getHashCode(SideColor sideColor, PieceType pieceType, long pos) {

        if (pos == 0L || (pos & (pos - 1)) != 0L) throw new IllegalArgumentException("pos must have exactly one bit set");

        int colorAdj = (sideColor == SideColor.White)? 0 : 6;
        int pieceAdj = pieceType.ordinal();

        int index = (pieceAdj + colorAdj) * 64;
        index += Long.numberOfTrailingZeros(pos);

        return zobristHashValues[index];
    }
}
