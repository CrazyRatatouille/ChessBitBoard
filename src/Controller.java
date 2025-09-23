import codedraw.EventScanner;
import codedraw.MouseClickEvent;

import java.time.Duration;

public class Controller {

    private BoardDrawer boardDrawer;
    private Bitboards bitboards;
    private final int canvasSize;
    private SideColor turnColor = SideColor.White;
    private EventScanner myEventSC;
    private LegalMoves legalMoves;


    public Controller (Bitboards bitboards, BoardDrawer boardDrawer, LegalMoves legalMoves) {

        this.bitboards = bitboards;
        this.boardDrawer = boardDrawer;
        canvasSize = boardDrawer.getCanvasSize();
        myEventSC = boardDrawer.getEventScanner();
        this.legalMoves = legalMoves;
    }

    /// This Method waits for a piece of ones color to be selected and moved and moves the selected Piece to its
    /// selected Position
    public void makeTurn() throws Exception {

        boolean hasMoved = false;

        while (!hasMoved) {

            myEventSC.removeEventsOlderThan(Duration.ofMillis(0));
            MouseClickEvent mouseClickEvent = myEventSC.nextMouseClickEvent();

            int x = mouseClickEvent.getX(); int y = mouseClickEvent.getY();

            long pos = boardDrawer.getPos(x, y);

            PieceType[] myPiece = new PieceType[0];
            SideColor sideColor = bitboards.findPosInfo(pos, myPiece);

            //handles emptyTiles and wrong color
            if (sideColor != turnColor) {
                continue;
            }

            long allLegalMoves;

            try {
                allLegalMoves = legalMoves.legalMoves(pos, sideColor);
            } catch (NoPieceAtSquareException E){
                throw new Exception("Logical Error, bitboards.findPosInfo should have given a valid Square");
            }

            //Now makes the move or cancels selection
            myEventSC.removeEventsOlderThan(Duration.ofMillis(0));

            mouseClickEvent = myEventSC.nextMouseClickEvent();
            x = mouseClickEvent.getX(); y = mouseClickEvent.getY();

            pos = boardDrawer.getPos(x, y);

            if ((pos & allLegalMoves) != 0) {
                //4 Cases, quiteMove, Capture, Castling and EnPassant

            }


        }



    }

}
