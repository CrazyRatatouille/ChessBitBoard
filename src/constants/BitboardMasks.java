package constants;

import static constants.BoardConstants.*;

/**
 * Contains pre-computed bitboards and lookup tables for piece attacks.
 * <p>
 * This class handles two types of attack generation:
 * <ul>
 * <li><b>Leaper Pieces (Pawn, Knight, King):</b> Simple static lookups based on the square.</li>
 * <li><b>Sliding Pieces (Bishop, Rook):</b> "Magic Bitboards". A perfect hashing technique
 * that allows for O(1) lookup of sliding attacks by hashing the specific blocker configuration
 * on the board.</li>
 * </ul>
 */
public class BitboardMasks {

    private BitboardMasks(){}

    /**
     * Stores pawn attack masks for both colors.
     * <p>
     * Layout:
     * <ul>
     * <li>Indices [0-63]: <b>White</b> pawn captures.</li>
     * <li>Indices [64-127]: <b>Black</b> pawn captures.</li>
     * </ul>
     * Accessed via {@code PAWN_MASK[side * BOARD_SIZE + square]}.
     */
    public static final long[] PAWN_MASK = new long[2 * BOARD_SIZE];

    /**
     * Stores knight attack masks for every square.
     */
    public static final long[] KNIGHT_MASK = new long[BOARD_SIZE];

    /**
     * Stores king attack masks for every square.
     */
    public static final long[] KING_MASK = new long[BOARD_SIZE];

    // Sizes determined by summing 2^bits for all squares (fancy magic bitboards size)
    private static final int BISHOP_MASK_SIZE = 5248;
    private static final int ROOK_MASK_SIZE = 102400;

    /**
     * Masks indicating which squares are "relevant blockers" for a sliding piece on a given square.
     * <p>
     * These masks exclude the outer edges of the board because pieces on the edge cannot blocks
     * a ray any further (the ray has already ended).
     */
    public static final long[] BISHOP_BLOCKER_MASK = new long[BOARD_SIZE];
    public static final long[] ROOK_BLOCKER_MASK = new long[BOARD_SIZE];

    /**
     * The dense lookup table for Bishop attacks.
     * <p>
     * This array holds pre-calculated attack bitboards for every square and every possible
     * relevant blocker configuration. It is indexed using the Magic Number hash calculated
     * by the bishop lookup method {@link board.Attacks#lookUpBishop(int, long, long)}.
     */
    public static final long[] BISHOP_MASK = new long[BISHOP_MASK_SIZE];

    /**
     * The dense lookup table for Rook attacks.
     * <p>
     * This array holds pre-calculated attack bitboards for every square and every possible
     * relevant blocker configuration. It is indexed using the Magic Number hash calculated
     * by the rook lookup method {@link board.Attacks#lookUpRook(int, long, long)}}.
     */
    public static final long[] ROOK_MASK = new long[ROOK_MASK_SIZE];
    
    /**
     * Offsets into the {@link #BISHOP_MASK} array for each square.
     */
    public static final int[] BISHOP_MBB_OFFSETS = {
            0, 64, 96, 128, 160, 192, 224, 256, 320, 352, 384, 416, 448, 480, 512, 544, 576, 608, 640, 768, 896, 1024,
            1152, 1184, 1216, 1248, 1280, 1408, 1920, 2432, 2560, 2592, 2624, 2656, 2688, 2816, 3328, 3840, 3968, 4000,
            4032, 4064, 4096, 4224, 4352, 4480, 4608, 4640, 4672, 4704, 4736, 4768, 4800, 4832, 4864, 4896, 4928, 4992,
            5024, 5056, 5088, 5120, 5152, 5184
    };

    /**
     * Offsets into the {@link #ROOK_MASK} array for each square.
     */
    public static final int[] ROOK_MBB_OFFSETS = {
            0, 4096, 6144, 8192, 10240, 12288, 14336, 16384, 20480, 22528, 23552, 24576, 25600, 26624, 27648, 28672,
            30720, 32768, 33792, 34816, 35840, 36864, 37888, 38912, 40960, 43008, 44032, 45056, 46080, 47104, 48128,
            49152, 51200, 53248, 54272, 55296, 56320, 57344, 58368, 59392, 61440, 63488, 64512, 65536, 66560, 67584,
            68608, 69632, 71680, 73728, 74752, 75776, 76800, 77824, 78848, 79872, 81920, 86016, 88064, 90112, 92160,
            94208, 96256, 98304
    };

    /**
     * Pre-computed Magic Numbers for Bishops.
     * Generated using {@link tools.MagicFinder}
     */
    public static final long[] BISHOP_MAGICS = {
            0x4024010805010200L, 0x1020880200902100L, 0xC068182051880010L, 0x1140408804A2009L, 0x82404A012020004L,
            0x8002080406000880L, 0x403412095008000CL, 0x82010D03100A00L, 0x84400404640040L, 0x2083810009A2040L,
            0x1100C02404400L, 0x2301804A9024404L, 0x5400040420210024L, 0x8600010426400500L, 0x8A40115082000L,
            0xA011105200900802L, 0x592004D002108102L, 0x106000A841040888L, 0xA08D114010200L, 0x10A800410220000L,
            0x3004820080400L, 0x102008B008A0120L, 0x4002004048020800L, 0xD03A021013030L, 0x3011401150020610L,
            0x488024804040800L, 0x80040408C0802080L, 0x20A2080004004008L, 0x40B0C88004002000L, 0x7010002001008820L,
            0x811010841010L, 0x82008002014100L, 0x8C08201903040880L, 0x1011412000100400L, 0x220804050800L,
            0xD22200900080304L, 0x1008020400201010L, 0x4040020041000L, 0x1020C00060100L, 0x4004204504100L,
            0x80901008101000L, 0x2020121400404L, 0x403A0A0804043A00L, 0x201406091000800L, 0x41140102C800100L,
            0x4920204400208040L, 0x2040124040A0100L, 0x82041C0056008140L, 0xC084009410080200L, 0x82050141108000L,
            0x20501C208900020L, 0x110000842061000L, 0x800300410440008L, 0x1608410A48104L, 0x1004A848840084L,
            0x4008080104002810L, 0x40043004B104000L, 0xA002160484042200L, 0x24200021082800L, 0x400100940405L,
            0x1010088A0020484L, 0x40202490128204L, 0x71001004280800C0L, 0x2028600102060058L
    };

    /**
     * Pre-computed Magic Numbers for Rooks.
     * Generated using {@link tools.MagicFinder}
     */
    public static final long[] ROOK_MAGICS = {
            0x80018040022018L, 0x840022000401000L, 0x880086000821004L, 0x900082010010014L, 0x80040002800800L,
            0x20008100E000401L, 0x80018002000100L, 0x8200050821840042L, 0x208001C0008060L, 0x2002401000200048L,
            0x300200100CA10L, 0x21002018100100L, 0x802C00800800L, 0x801400800200L, 0x3000200042100L, 0x800800041000080L,
            0x1240808000C00328L, 0x2016020041002480L, 0x1000808050006000L, 0x22020048401020L, 0x90808004005800L,
            0x200808004000200L, 0x410040002081001L, 0x800200016C0083L, 0x180004040002006L, 0x810005040012000L,
            0x2820002280100088L, 0x10900500090020L, 0xA400840080800800L, 0x106000404001020L, 0x2050080400015012L,
            0x200800420021108CL, 0x8428804008800028L, 0x201000204000C0L, 0x600200080801000L, 0x4001080480803000L,
            0x1100040080803801L, 0x38203408014010L, 0x10027004000803L, 0x10B82000244L, 0x400080258004L,
            0x1020004070004000L, 0x2000A100150040L, 0x800A204200520008L, 0x8A1002800110004L, 0x24002008140110L,
            0x18030601300C0028L, 0x200450A820004L, 0x400420221009200L, 0x1002201140008080L, 0x40900020008080L,
            0x1850001080080080L, 0x140181080080L, 0x1200506C080200L, 0x40030650088400L, 0x82800841003080L,
            0x400410028D08001L, 0xA080304001608101L, 0x400104200010C009L, 0x2002500009006005L, 0x2001020080442L,
            0x842A004C88031002L, 0x6218408E1810110CL, 0x140300840042B2L
    };

    static {

        for (int sq = 0; sq < BOARD_SIZE; sq++) {

            long fromMask = 1L << sq;


            //all pawns that are not on the A file are shifted by 7 bits to the left/right to calculate all valid NW/SE attacks
            //all pawns that are not on the H file are shifted by 9 bits to the left/right to calculate all valid NE/SW attacks
            PAWN_MASK[sq] = ((fromMask & ~FIRST_RANK) & ~A_FILE) << 7 |  ((fromMask & ~FIRST_RANK) & ~H_FILE) << 9;
            PAWN_MASK[BOARD_SIZE + sq] = ((fromMask & ~EIGHT_RANK) & ~A_FILE) >>> 9 |  ((fromMask & ~EIGHT_RANK) & ~H_FILE) >>> 7;

            KNIGHT_MASK[sq] =
                    (fromMask & ~A_FILE) >>> 17 | (fromMask & ~A_FILE) << 15
                    | (fromMask & ~H_FILE) >>> 15 | (fromMask & ~H_FILE) << 17
                    | (fromMask & ~(A_FILE | B_FILE)) >>> 10 | (fromMask & ~(A_FILE | B_FILE)) << 6
                    | (fromMask & ~(G_FILE | H_FILE)) >>> 6 | (fromMask & ~(G_FILE | H_FILE)) << 10;

            KING_MASK[sq] =
                    fromMask >>> 8 | fromMask << 8
                    | (fromMask & ~A_FILE) >>> 9 | (fromMask & ~A_FILE) >>> 1 | (fromMask & ~A_FILE) << 7
                    | (fromMask & ~H_FILE) >>> 7 | (fromMask & ~H_FILE) << 1 | (fromMask & ~H_FILE) << 9;

            // Generate Relevant Blocker Masks for sliding pieces
            ROOK_BLOCKER_MASK[sq] = (fromMask ^ (A_FILE << (sq % 8))) & ~(FIRST_RANK | EIGHT_RANK)
                    | (fromMask ^ (FIRST_RANK << ((sq / 8) * 8))) & ~(A_FILE | H_FILE);
            BISHOP_BLOCKER_MASK[sq] = bishopEmptyAttacks(fromMask) & ~(A_FILE | H_FILE | FIRST_RANK | EIGHT_RANK);
        }

        populateBishopMBB();
        populateRookMBB();
    }

    private static long bishopEmptyAttacks(long fromMask)   {
        long attacks = 0L;

        long NW = (fromMask & ~A_FILE) << 7;
        long SW = (fromMask & ~A_FILE) >>> 9;
        long NE = (fromMask & ~H_FILE) << 9;
        long SE = (fromMask & ~H_FILE) >>> 7;

        for (int i = 0; i < 7; i++) {
            attacks |= NW | SW | NE | SE;
            NW = (NW & ~A_FILE) << 7;
            SW = (SW & ~A_FILE) >>> 9;
            NE = (NE & ~H_FILE) << 9;
            SE = (SE & ~H_FILE) >>> 7;
        }
        return attacks;
    }

    private static void populateBishopMBB() {

        for (int sq = 0; sq < BOARD_SIZE; sq++) {

            long fromMask = 1L << sq;
            long blockerMask = BISHOP_BLOCKER_MASK[sq];

            long magic = BISHOP_MAGICS[sq];
            int shift = BOARD_SIZE - Long.bitCount(blockerMask);

            int offset = BISHOP_MBB_OFFSETS[sq];

            long blockerSubset = 0;

            //iterate all subsets of the blockerMask
            for (int i = 0; i < (1 << Long.bitCount(blockerMask)); i++) {

                long NE = (fromMask & ~H_FILE) << 9;
                long SE = (fromMask & ~H_FILE) >>> 7;
                long SW = (fromMask & ~A_FILE) >>> 9;
                long NW = (fromMask & ~A_FILE) << 7;

                for (int j = 0; j < 6; j++) {

                    NE |= (NE & ~(blockerSubset | H_FILE)) << 9;
                    SE |= (SE & ~(blockerSubset | H_FILE)) >>> 7;
                    SW |= (SW & ~(blockerSubset | A_FILE)) >>> 9;
                    NW |= (NW & ~(blockerSubset | A_FILE)) << 7;
                }

                int index = (int) ((magic * blockerSubset) >>> shift);

                // Carry-Rippler Step
                blockerSubset = (blockerSubset - blockerMask) & blockerMask;

                BISHOP_MASK[offset + index] = (NE | SE | SW | NW);
            }
        }
    }

    private static void populateRookMBB() {

        for (int sq = 0; sq < BOARD_SIZE; sq++) {

            long fromMask = 1L << sq;
            long blockerMask = ROOK_BLOCKER_MASK[sq];

            long magic = ROOK_MAGICS[sq];
            int shift = BOARD_SIZE - Long.bitCount(blockerMask);

            int offset = ROOK_MBB_OFFSETS[sq];

            long blockerSubset = 0;

            //iterate all subsets of the blockerMask
            for (int i = 0; i < (1 << Long.bitCount(ROOK_BLOCKER_MASK[sq])); i++) {

                long N = fromMask << 8;
                long E = (fromMask & ~H_FILE) << 1;
                long S = fromMask >>> 8;
                long W = (fromMask & ~A_FILE) >>> 1;

                for (int j = 0; j < 7; j++) {
                    N |= (N & ~blockerSubset) << 8;
                    E |= (E & ~(blockerSubset | H_FILE)) << 1;
                    S |= (S & ~blockerSubset) >>> 8;
                    W |= (W & ~(blockerSubset | A_FILE)) >>> 1;
                }

                int index = (int) ((blockerSubset * magic) >>> shift);

                // Carry-Rippler Step
                blockerSubset = (blockerSubset - blockerMask) & blockerMask;

                ROOK_MASK[offset + index] = (N | E | S | W);
            }
        }
    }
}
