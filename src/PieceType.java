public enum PieceType {
    Pawn, Knight, Bishop, Rook, Queen, King;


    public PieceType getPieceType(int i) {

        if (i < 0 || i > 5) throw new IndexOutOfBoundsException("i has to be an element of [0, 6)");

        PieceType p = Pawn;

        switch (i) {
            case 1 -> p = Knight;
            case 2 -> p = Bishop;
            case 3 -> p = Rook;
            case 4 -> p = Queen;
            case 5 -> p = King;
        }

        return p;
    }
}
