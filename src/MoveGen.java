import java.util.ArrayList;
import java.util.List;

public class MoveGen {

    //TODO: UNFINISHED, PRETTY MUCH EVERYTHING IS STILL LEFT TO DO

    BoardState boardState;

    public MoveGen(BoardState boardState) {
        this.boardState = boardState;
    }

    public List<Move> moves(int color) {

        List<Move> output = new ArrayList<>();


        return output;
    }

    private void legalCheck(Move move) {

    }

    private void addWPMoves(List<Move> all) {

        long wPawns = boardState.getPieces(0);
        long bPawns = boardState.getPieces(1);

        while (wPawns != 0x0L) {

            long singularPawn = 1L << Long.numberOfTrailingZeros(wPawns);

            Move move = new Move(SideColor.White, PieceType.Pawn, Square.getSquare(singularPawn),
                    Square.getSquare(singularPawn << 8), null, null, Move.MoveType.QUIET);

            wPawns &= (wPawns - 1);
        }
    }

    private void addBPawnMoves(List<Move> all, long bPawns) {
    }
}
