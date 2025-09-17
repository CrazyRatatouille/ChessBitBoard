public class Main {

    public static void main(String[] args) {

        Bitboards lol = new Bitboards();
        BoardDrawer myDraw = new BoardDrawer(lol, 600);
        myDraw.drawBoard();
    }
}