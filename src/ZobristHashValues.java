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
}
