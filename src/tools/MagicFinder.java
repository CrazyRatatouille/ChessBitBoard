package tools;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static constants.BitboardMasks.ROOK_BLOCKER_MASK;
import static constants.BitboardMasks.BISHOP_BLOCKER_MASK;
import static constants.BoardConstants.BOARD_SIZE;

public class MagicFinder {

    private MagicFinder(){}

    public static long[] findRookMagic() {

        boolean[] usedIndices = new boolean[1 << 12];
        long[] allMagics = new long[BOARD_SIZE];

        for (int sq = 0; sq < BOARD_SIZE; sq++) {

            long[] allBlockerSubSet = getAllRookBlockerSubsets(sq);

            int bitShift = BOARD_SIZE - Long.bitCount(ROOK_BLOCKER_MASK[sq]);
            long magicCandidate = 0;
            boolean magicFound = false;

            while (!magicFound) {

                Arrays.fill(usedIndices, false);

                magicFound = true;

                magicCandidate = ThreadLocalRandom.current().nextLong() &
                        ThreadLocalRandom.current().nextLong() &
                        ThreadLocalRandom.current().nextLong();

                for (int i = 0; i < allBlockerSubSet.length; i++) {

                    int index = (int)((magicCandidate * allBlockerSubSet[i]) >>> bitShift);

                    if (usedIndices[index]) {
                        magicFound = false;
                        break;
                    }
                    usedIndices[index] = true;
                }
            }

            allMagics[sq] = magicCandidate;
        }

        System.out.println(Arrays.toString(allMagics));
        return allMagics;
    }

    private static long[] getAllRookBlockerSubsets(int sq) {

        long mask = ROOK_BLOCKER_MASK[sq];

        long[] output = new long[1 << Long.bitCount(mask)];

        int index = 0;
        //Cool bitwise trick see: https://www.chessprogramming.org/Traversing_Subsets_of_a_Set
        long subset = 0;
        do {
            output[index++] = subset;
            subset = (subset - mask) & mask;
        } while (subset != 0);

        return output;
    }
}
