package board;

import java.util.ArrayList;
import java.util.List;

public class MoveGen {

    //TODO: UNFINISHED, PRETTY MUCH EVERYTHING IS STILL LEFT TO DO

    BoardState boardState;

    public MoveGen(BoardState boardState) {
        this.boardState = boardState;
    }

    public List<Short> moves(int color) {

        List<Short> output = new ArrayList<>();


        return output;
    }

    private void legalCheck(short move) {

    }

    private void addWPMoves(List<Short> all) {

        long wPawns = boardState.getPieces(0);
        long bPawns = boardState.getPieces(1);

        while (wPawns != 0x0L) {

            long singularPawn = 1L << Long.numberOfTrailingZeros(wPawns);

            wPawns &= (wPawns - 1);
        }
    }

    private void addBPawnMoves(List<Short> all, long bPawns) {
    }
}
