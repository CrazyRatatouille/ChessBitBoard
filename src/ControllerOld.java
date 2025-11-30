import codedraw.EventScanner;
import codedraw.MouseClickEvent;

import java.time.Duration;

public class ControllerOld {

    private BoardDrawerOld boardDrawerOld;
    private BitboardsOld bitboardsOld;
    private SideColor turnColor = SideColor.White;
    private EventScanner myEventSC;
    private LegalMovesOld legalMovesOld;


    public ControllerOld(BitboardsOld bitboardsOld, BoardDrawerOld boardDrawerOld, LegalMovesOld legalMovesOld) {

        this.bitboardsOld = bitboardsOld;
        this.boardDrawerOld = boardDrawerOld;
        myEventSC = boardDrawerOld.getEventScanner();
        this.legalMovesOld = legalMovesOld;
    }

    /// This Method waits for a piece of ones color to be selected and moved and moves the selected Piece to its
    /// selected Position
    public void makeTurn() throws InterruptedException {

        boolean hasMoved = false;
        myEventSC.removeEventsOlderThan(Duration.ofMillis(0));

        while (!hasMoved) {
            Thread.sleep(200);

            if (!myEventSC.hasMouseClickEvent()) continue;
            MouseClickEvent mouseClickEvent = myEventSC.nextMouseClickEvent();

            int x = mouseClickEvent.getX();
            int y = mouseClickEvent.getY();

            long pos = boardDrawerOld.getPos(x, y);

            PieceType[] myPiece = new PieceType[1];
            SideColor sideColor = bitboardsOld.findPosInfo(pos, myPiece);

            //handles emptyTiles and wrong color
            if (sideColor != turnColor) {
                continue;
            }

            long allLegalMoves;
            allLegalMoves = legalMovesOld.legalMoves(pos, sideColor);

            long from = pos;
            boardDrawerOld.drawPieceMoves(allLegalMoves);

            //Now makes the move or cancels selection

            while (!myEventSC.hasMouseClickEvent()) Thread.sleep(50);
            mouseClickEvent = myEventSC.nextMouseClickEvent();

            x = mouseClickEvent.getX();
            y = mouseClickEvent.getY();

            pos = boardDrawerOld.getPos(x, y);
            long to = pos;


            if ((pos & allLegalMoves) == 0) continue;

            bitboardsOld.setPieces(turnColor, myPiece[0], from, to);
            turnColor = turnColor.other();
            hasMoved = true;
        }
    }
}
