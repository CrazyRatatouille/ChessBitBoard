public class Main {

    public static void main(String[] args) {

        long wPawns = 0x000000000000FF00L; //a2 - h2
        long wKnights = 0x0000000000000042L; //b1 & g1
        long wBishops = 0x0000000000000024L; //c1 & f1
        long wRooks = 0x0000000000000081L; //a1 & h1
        long wQueen = 0x0000000000000010L; //d1
        long wKing = 0x0000000000000008L; //e1

        long bPawns = 0x00FF000000000000L; //a7 - g7
        long bKnights = 0x4200000000000000L; //b8 & g8
        long bBishops = 0x2400000000000000L; //c8 & f8
        long bRooks = 0x8100000000000000L; //a8 & h8
        long bQueen = 0x1000000000000000L; //d8
        long bKing = 0x0800000000000000L; //e8

    }
}