package tools;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

import static constants.BitboardMasks.*;
import static constants.BoardConstants.*;

public class MagicFinder {

    public static void main(String[] args) {

        //=============================================================
        //                   MAGIC NUMBERS GENERATOR
        //=============================================================

        System.out.println("MAGIC NUMBERS:\n");

        //========================== BISHOPS ==========================
        System.out.println("BISHOPS:");
        long[] magics = findBishopMagics();
        StringBuilder str = new StringBuilder("{\n");

        for (long magic : magics) {
            str.append("0x").append((Long.toHexString(magic)).toUpperCase()).append("L, ");
        }
        str.delete(str.length() - 2, str.length());
        str.append("\n}");

        System.out.println(str);

        //=========================== ROOKS ===========================
        System.out.println("ROOKS:");
        magics = findRookMagics();
        str = new StringBuilder("{\n");

        for (long magic : magics) {
            str.append("0x").append((Long.toHexString(magic)).toUpperCase()).append("L, ");
        }
        str.delete(str.length() - 2, str.length());
        str.append("\n}");

        System.out.println(str);


        //=============================================================
        //                       Offsets Calculator
        //=============================================================

        System.out.println("\nOFFSET CALCULATOR: \n");

        //========================== BISHOPS ==========================
        System.out.println("BISHOPS:");
        int[] offsets = BishopMBBOffsets();
        str = new StringBuilder("{\n");

        for (int offset : offsets) {
            str.append(offset).append(", ");
        }

        str.delete(str.length() - 2, str.length());
        str.append("\n}");
        System.out.println(str);

        int totalSize = offsets[BOARD_SIZE - 1] + (1 << Long.bitCount(BISHOP_BLOCKER_MASK[BOARD_SIZE - 1]));
        System.out.println("total Array Size:  " + totalSize);

        //=========================== ROOKS ===========================
        System.out.println("ROOKS:");
        offsets = RookMBBOffsets();
        str = new StringBuilder("{\n");

        for (int offset : offsets) {
            str.append(offset).append(", ");
        }

        str.delete(str.length() - 2, str.length());
        str.append("\n}");
        System.out.println(str);

        totalSize = offsets[BOARD_SIZE - 1] + (1 << Long.bitCount(ROOK_BLOCKER_MASK[BOARD_SIZE - 1]));
        System.out.println("total Array Size:  " + totalSize);
    }

    public static long[] findRookMagics() {

        long[] allMagics = new long[BOARD_SIZE];
        boolean[] indices = new boolean[1 << 12];

        for (int sq = 0; sq < BOARD_SIZE; sq++) {

            long[] allBlockerSubSet = getBlockerSubsets(P_ROOK, sq);

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

        return allMagics;
    }

    public static long[] findBishopMagics() {

        long[] allMagics = new long[BOARD_SIZE];
        boolean[] indices = new boolean[1 << 10];

        for (int sq = 0; sq < BOARD_SIZE; sq++) {

            int shift = BOARD_SIZE - Long.bitCount(BISHOP_BLOCKER_MASK[sq]);
            long magicCandidate = 0;
            boolean magicFound = false;

            long[] blockerSubsets = getBlockerSubsets(P_BISHOP, sq);

            while (!magicFound) {

                Arrays.fill(indices, false);

                magicFound = true;

                magicCandidate = ThreadLocalRandom.current().nextLong() &
                        ThreadLocalRandom.current().nextLong() &
                        ThreadLocalRandom.current().nextLong();

                for (int i = 0; i < blockerSubsets.length; i++) {

                    int index = (int) ((magicCandidate * blockerSubsets[i]) >>> shift);

                    if (indices[index]) {
                        magicFound = false;
                        break;
                    }
                    indices[index] = true;
                }
            }
            allMagics[sq] = magicCandidate;
        }

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

    private static long[] getBlockerSubsets(int pieceType, int sq) {

        long mask = (pieceType == P_BISHOP)? BISHOP_BLOCKER_MASK[sq] : ROOK_BLOCKER_MASK[sq];

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
}
