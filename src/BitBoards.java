    import javax.swing.*;

    public class BitBoards {

        private long[] bitboards = {0xFF00L, 0x00FF000000000000L,   //wPawn     bPawn
                                    0x0042L, 0x4200000000000000L,   //wKnight   bKnight
                                    0x0024L, 0x2400000000000000L,   //wBishop   bBishop
                                    0x0081L, 0x8100000000000000L,   //wRook     bRook
                                    0x0008L, 0x0800000000000000L,   //wQueen    bQueen
                                    0x0010L, 0x1000000000000000L    //wKing     bKing
        };

        /**
         * files[0] (aFile) - files[7] (hFile)
         */
        private static final long[] fileMasks = {
                0x0101010101010101L, 0x0202020202020202L, 0x0404040404040404L, 0x0808080808080808L,
                0x1010101010101010L, 0x2020202020202020L, 0x4040404040404040L, 0x8080808080808080L
        };

        /**
         * ranks[0] (1st rank) - ranks[7] (8th rank)
         */
        private static final long[] rankMasks = {
                0xFFL, 0xFF00L, 0xFF0000, 0xFF000000L,
                0xFF00000000L, 0xFF0000000000L, 0xFF000000000000L, 0xFF00000000000000L
        };

        private long enPassant = 0x0L;

        /**
         * bit 1 (LSB): wQueenSide <br>
         * bit 2 : wKingSide <br>
         * bit 3 : bQueenSide <br>
         * bit 4 (MSB) : bKingSide
         */
        private byte castlingRights = 0xF;
        private static final byte[] castlingMasks = {
                0xE, 0xF, 0xF, 0xF, 0xC, 0xF, 0xF, 0xD,
                0xF, 0xF, 0xF, 0xF, 0xF, 0xF, 0xF, 0xF,
                0xF, 0xF, 0xF, 0xF, 0xF, 0xF, 0xF, 0xF,
                0xF, 0xF, 0xF, 0xF, 0xF, 0xF, 0xF, 0xF,
                0xF, 0xF, 0xF, 0xF, 0xF, 0xF, 0xF, 0xF,
                0xF, 0xF, 0xF, 0xF, 0xF, 0xF, 0xF, 0xF,
                0xF, 0xF, 0xF, 0xF, 0xF, 0xF, 0xF, 0xF,
                0xB, 0xF, 0xF, 0xF, 0x3, 0xF, 0xF, 0x7,
        };

        private long[] occs = {0xFFFFL, 0xFFFF000000000000L, 0xFFFF0000000000FFL};

        /**
         * This constructor sets up a new chess board with all Pieces on their home square.
         * A1 is the LSB and H8 being the MSB
         */
        public BitBoards(){}

        /**
         * This constructor sets up a new chess board and copies the game state of {@code other}.
         * The purpose of this constructor is to allow engines to create branches, to analyze the best moves without
         * altering the main bitboards.
         *
         * @param other the {@code BitBoards} to be copied
         */
        public BitBoards(BitBoards other) {

            System.arraycopy(other.bitboards, 0, this.bitboards, 0, this.bitboards.length);
            System.arraycopy(other.occs, 0, this.occs, 0, this.occs.length);

            this.enPassant = other.enPassant;
            this.castlingRights = other.castlingRights;
        }

        public void makeMove(Move move) {

            //TODO: remove codeDuplication as much as possible

            //TODO: refactor Move move into int move to avoid object creation (deletion by GC is slow, cache misses)

            long fromBit = move.From().pos();
            long toBit = move.To().pos();
            int color = move.color().ordinal();
            Move.MoveType moveType = move.moveType();

            enPassant = 0x0L;

            int pIdx = color + (move.pieceType().ordinal() << 1);
            long moveMask = (fromBit | toBit);

            PieceType captured = move.capturedPieceType();
            PieceType promoTo = move.promotionTo();

            switch (moveType) {
                case STANDARD -> standard(fromBit, toBit, color, pIdx);
                case CAPTURE -> capture(fromBit, toBit, color, captured.ordinal(), pIdx);
                case PAWN_DOUBLE_MOVE -> pawnDoubleMove(fromBit, toBit, color, pIdx);
                case KING_SIDE_CASTLE -> kingSideCastle(fromBit, toBit, color, pIdx);
                case QUEEN_SIDE_CASTLE -> queenSideCastle(fromBit, toBit, color, pIdx);
                case PROMOTION -> promotion(fromBit, toBit, color, promoTo.ordinal(), pIdx);
                case ENPASSANT -> enPassant(fromBit, toBit, color, pIdx);
                case PROMOTION_AND_CAPTURE -> promotionAndCapture(fromBit, toBit, color, captured.ordinal(), promoTo.ordinal(), pIdx);
            }

            updateCastlingRights(move.From(), move.To());
        }

        private void standard(long fromBit, long toBit, int color, int pIdx) {

            long moveMask = (fromBit | toBit);

            bitboards[pIdx] ^= moveMask;
            occs[color] ^= moveMask;
            occs[2] ^= moveMask;
        }

        private void capture(long fromBit, long toBit, int color, int captured, int pIdx) {

            int capColor = 0x1 ^ color;
            long moveMask = (fromBit | toBit);

            bitboards[pIdx] ^= (fromBit | toBit);
            bitboards[capColor + (captured << 1)] ^= toBit;

            occs[color] ^= moveMask;
            occs[capColor] ^= toBit;
            occs[2] ^= fromBit;
        }

        private void promotion(long fromBit, long toBit, int color, int promoTo, int pIdx) {

            long moveMask = (fromBit | toBit);

            bitboards[pIdx] ^= fromBit;
            bitboards[color + (promoTo << 1)] ^= toBit;

            occs[color] ^= moveMask;
            occs[2] ^= moveMask;
        }

        private void promotionAndCapture(long fromBit, long toBit, int color, int captured, int promoTo, int pIdx){

            int capColor = 0x1 ^ color;

            bitboards[pIdx] ^= fromBit;
            bitboards[color + (promoTo << 1)] ^= toBit;
            bitboards[capColor + (captured << 1)] ^= toBit;

            occs[color] ^= (fromBit | toBit);
            occs[capColor] ^= toBit;
            occs[2] ^= fromBit;
        }

        private void kingSideCastle(long fromBit, long toBit, int color, int pIdx) {

            int rookIndex = 6 + color;
            long rookPos = Square.H1.pos() << (color * 56);

            long moveMaskK = (fromBit | toBit);
            long moveMaskR = (rookPos | rookPos >>> 2);
            long moveMaskCombined = (moveMaskK | moveMaskR);

            bitboards[pIdx] ^= moveMaskK;
            bitboards[rookIndex] ^= moveMaskR;

            occs[color] ^= moveMaskCombined;
            occs[2] ^= moveMaskCombined;
        }

        private void queenSideCastle(long fromBit, long toBit, int color, int pIdx) {

            int rookIndex = 6 + color;
            long rookPos = Square.A1.pos() << (color * 56);

            long moveMaskK = (fromBit | toBit);
            long moveMaskR = (rookPos | rookPos << 3);
            long moveMaskCombined = (moveMaskK | moveMaskR);

            bitboards[pIdx] ^= moveMaskK;
            bitboards[rookIndex] ^= moveMaskR;

            occs[color] ^= moveMaskCombined;
            occs[2] ^= moveMaskCombined;;
        }

        private void pawnDoubleMove(long fromBit, long toBit, int color, int pIdx) {

            standard(fromBit, toBit, color, pIdx);

            switch (color) {
                case 0 -> enPassant = toBit >>> 8;
                case 1 -> enPassant = toBit << 8;
            }
        }

        private void enPassant(long fromBit, long toBit, int color, int pIdx) {

            int capColor = 0x1 ^ color;
            long moveMask = (fromBit | toBit);
            long capBit = toBit;

            switch (color) {
                case 0 -> capBit >>>= 8;
                case 1 -> capBit <<= 8;
            }

            bitboards[pIdx] ^= moveMask;
            bitboards[capColor] ^= capBit;

            occs[color] ^= moveMask;
            occs[capColor] ^= capBit;
            occs[2] ^= (moveMask | capBit);
        }

        private void updateCastlingRights(Square from, Square to) {
            castlingRights &= (castlingMasks[from.ordinal()] & castlingMasks[to.ordinal()]);
        }

        /**
         * This method returns an occupancyMap of the whole chessBoard with all its Pieces
         *
         * @return the occupancyMap of the whole chessBoard with all its Pieces
         */
        public long getOccupancy() {
            return occs[2];
        }

        /**
         * This method returns an occupancyMap of the whole chessBoard with all its Pieces
         *
         * @param color the color of Pieces, whose occMap is to be returned
         * @return the occupancyMap of the whole chessBoard with all Pieces of the color {@code color}
         */
        public long getColorOccupancy(SideColor color) {
            return occs[color.ordinal()];
        }

        /**
         * This method returns the square which can be attacked due to enPassant. EnPassant is a rule in chess, where a pawn
         * who moved up two square, can be taken by an opposite pawn for the next turn, if and only if the pawn could have
         * been taken if it was only moved 1 square up the board
         *
         * @return the Square which can be attacked due to the enPassant - rule, null if there is no such square
         */
        public Square getEnPassant() {
            return Square.getSquare(enPassant);
        }
    }