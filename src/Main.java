public class Main {

    public static void main(String[] args) {

        Bitboards bitboards = new Bitboards();
        AttackPatterns attackPatterns = new AttackPatterns(bitboards);
        BoardDrawer boardDrawer = new BoardDrawer(bitboards, 600);
        LegalMoves legalMoves = new LegalMoves(bitboards);
        Controller controller = new Controller(bitboards, boardDrawer, legalMoves);

        boardDrawer.drawBoard();
        boardDrawer.drawPieceMoves(0x11L);

    }
}