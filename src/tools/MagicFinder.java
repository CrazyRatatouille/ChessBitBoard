package tools;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

import static constants.BitboardMasks.*;
import static constants.BoardConstants.*;

public class MagicFinder {

    private MagicFinder(){}

    public static long[] findRookMagics() {


        long[] allMagics = new long[BOARD_SIZE];
        boolean[] indices = new boolean[1 << 12];

        for (int sq = 0; sq < BOARD_SIZE; sq++) {

            long[] allBlockerSubSet = getAllRookBlockerSubsets(sq);

            int shift = BOARD_SIZE - Long.bitCount(ROOK_BLOCKER_MASK[sq]);
            long magicCandidate = 0;
            boolean magicFound = false;

            while (!magicFound) {

                Arrays.fill(indices, false);

                magicFound = true;

                magicCandidate = ThreadLocalRandom.current().nextLong() &
                        ThreadLocalRandom.current().nextLong() &
                        ThreadLocalRandom.current().nextLong();

                for (int i = 0; i < allBlockerSubSet.length; i++) {

                    int index = (int)((magicCandidate * allBlockerSubSet[i]) >>> shift);

                    if (indices[index]) {
                        magicFound = false;
                        break;
                    }
                    indices[index] = true;
                }
            }

            allMagics[sq] = magicCandidate;
        }

        System.out.println(Arrays.toString(allMagics));
        return allMagics;
    }

    public static long[] findBishopMagics() {

        long[] allMagics = new long[BOARD_SIZE];
        boolean[] indices = new boolean[1 << 10];

        for (int sq = 0; sq < BOARD_SIZE; sq++) {

            int shift = BOARD_SIZE - Long.bitCount(BISHOP_BLOCKER_MASK[sq]);
            long magicCandidate = 0;
            boolean magicFound = false;


            long[] blockerSubsets = getAllBishopBlockerSubsets(sq);


            while (!magicFound) {

                Arrays.fill(indices, false);

                magicFound = true;

                magicCandidate = ThreadLocalRandom.current().nextLong() &
                        ThreadLocalRandom.current().nextLong() &
                        ThreadLocalRandom.current().nextLong();

                for (int i = 0; i < blockerSubsets.length; i++) {

                    int index = (int) ((magicCandidate * BISHOP_BLOCKER_MASK[sq]) >>> shift);

                    if (indices[index]) {
                        magicFound = false;
                        break;
                    }
                    indices[i] = true;
                }
            }
            allMagics[sq] = magicCandidate;
        }

        System.out.println(Arrays.toString(allMagics));
        return allMagics;
    }

    public static int[] RookMBBOffsets() {

        int[] out = new int[BOARD_SIZE];
        int offset = 0;

        for (int sq = 0; sq < BOARD_SIZE; sq++) {
            out[sq] = offset;
            offset += 1 << Long.bitCount(ROOK_BLOCKER_MASK[sq]);
        }

        return out;
    }

    public static int[] BishopMBBOffsets() {

        int[] out = new int[BOARD_SIZE];
        int offset = 0;

        for (int sq = 0; sq < BOARD_SIZE; sq++) {
            out[sq] = offset;
            offset += 1 << Long.bitCount(BISHOP_BLOCKER_MASK[sq]);
        }
        return out;
    }

    public static long[] populatedRookMBB() {

        int size = 0;

        for (int sq = 0; sq < BOARD_SIZE; sq++) {
            size += 1 << Long.bitCount(ROOK_BLOCKER_MASK[sq]);
        }

        long[] out = new long[size];

        for (int sq = 0; sq < BOARD_SIZE; sq++) {

            long origin = 1L << sq;
            long mask = ROOK_BLOCKER_MASK[sq];
            long magic = ROOK_MAGICS[sq];
            int shift = BOARD_SIZE - Long.bitCount(mask);
            int offSet = ROOK_MBB_OFFSETS[sq];


            long subset = 0;

            for (int i = 0; i < (1 << Long.bitCount(ROOK_BLOCKER_MASK[sq])); i++) {

                long N = (origin & ~subset) << 8;
                long E = (origin & ~(subset | H_FILE)) << 1;
                long S = (origin & ~subset) >>> 8;
                long W = (origin & ~(subset | A_FILE)) >>> 1;

                for (int j = 0; j < 7; j++) {
                    N |= (N & ~subset) << 8;
                    E |= (E & ~(subset | H_FILE)) << 1;
                    S |= (S & ~subset) >>> 8;
                    W |= (W & ~(subset | A_FILE)) >>> 1;
                }

                long index = (subset * magic) >>> shift;
                subset = (subset - mask) & mask;

                out[(int) (offSet + index)] = (N | E | S | W);
            }
        }

        return out;
    }

    public static int[] populatedBishopMBB() {
        return new int[0];
    }

    private static long[] getAllRookBlockerSubsets(int sq) {

        long mask = ROOK_BLOCKER_MASK[sq];

        long[] out = new long[1 << Long.bitCount(mask)];

        int index = 0;
        //Cool bitwise trick see: https://www.chessprogramming.org/Traversing_Subsets_of_a_Set
        long subset = 0;
        do {
            out[index++] = subset;
            subset = (subset - mask) & mask;
        } while (subset != 0);

        return out;
    }

    private static long[] getAllBishopBlockerSubsets(int sq) {

        long mask = BISHOP_BLOCKER_MASK[sq];

        long[] out = new long[1 << Long.bitCount(mask)];

        int index = 0;
        long subset = 0;
        do {
            out[index++] = subset;
            subset = (subset - mask) & mask;
        } while (subset != 0);

        return out;
    }
}
