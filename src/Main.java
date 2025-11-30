public class Main {

    public static void main(String[] args) throws InterruptedException {

        BitboardsOld bitboardsOld = new BitboardsOld();
        AttackPatternsOld attackPatternsOld = new AttackPatternsOld(bitboardsOld);
        BoardDrawerOld boardDrawerOld = new BoardDrawerOld(bitboardsOld, 600);
        LegalMovesOld legalMovesOld = new LegalMovesOld(bitboardsOld);
        ControllerOld controllerOld = new ControllerOld(bitboardsOld, boardDrawerOld, legalMovesOld);
        boolean gameOver = false;

        boardDrawerOld.drawBoard();


        try {

            while (!gameOver) {
                boardDrawerOld.drawBoard();
                controllerOld.makeTurn();
                gameOver = legalMovesOld.gameOver(SideColor.White);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        boardDrawerOld.close();
    }
}