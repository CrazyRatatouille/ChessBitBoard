package tools;

import board.BoardState;
import static constants.BoardConstants.*;
import codedraw.CodeDraw;
import codedraw.Image;
import codedraw.Palette;

import java.awt.*;

public class PosVisualiser {

    public static void main(String[] args){

        BoardState boardState = new BoardState();
        boardState.setPos("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1");

        visualizeBitboard(boardState,0b1001);
    }

    private static final int canvasSize = 600;
    private static final int tileSize = canvasSize / 8;

    private static final CodeDraw myObj = new CodeDraw(canvasSize, canvasSize);

    private static final Color myWhite = Palette.fromRGB(241, 217, 176);
    private static final Color myBlack = Palette.fromRGB(178, 128, 94);
    private static final Color myGreen = Palette.fromRGBA(3, 75, 18, 150);

    static {
        for (int square = 0; square < BOARD_SIZE; square++) {
            // Calculate grid positions
            int col = square % 8;
            int row = square / 8;

            int x = col * tileSize;
            int y = row * tileSize;

            // Determine if the square is light or dark
            // (col + row) % 2 == 0 is standard for 'light' squares starting at top-left
            boolean isLightSquare = (col + row) % 2 == 0;

            // 1. Draw the Square Background
            if (isLightSquare) {
                myObj.setColor(myWhite);
            } else {
                myObj.setColor(myBlack);
            }
            myObj.fillSquare(x, y, tileSize);

            // 2. Draw the File and Rank Label
            // Set color to contrast the background (e.g., Black text on White square)
            if (isLightSquare) {
                myObj.setColor(myBlack);
            } else {
                myObj.setColor(myWhite);
            }

            if (row != 7 && col != 7) continue;

            if (row == 7) {
                char file = (char) ('a' + col);
                String label = "" + file;
                myObj.drawText(x + 5, y + 55, label);
            }

            if (col == 7) {
                char rank = (char) ('8' - row);
                String label = "" + rank;
                myObj.drawText(x + 55, y + 5, label);
            }
        }
    }


    public static void visualizeBitboard(BoardState boardState, long bitboard) {

        for (int square = 0; square < BOARD_SIZE; square++) {

            int row = (square / 8);
            int col = (square % 8);

            int x = col * tileSize;
            int y = canvasSize - (row + 1) * tileSize;

            int piece = boardState.pieceAt(square);
            String path = "assets/";

            switch (piece) {
                case W_PAWN -> path += "WhitePawn.png";
                case W_KNIGHT -> path += "WhiteKnight.png";
                case W_BISHOP -> path += "WhiteBishop.png";
                case W_ROOK -> path += "WhiteRook.png";
                case W_QUEEN -> path += "WhiteQueen.png";
                case W_KING -> path += "WhiteKing.png";

                case B_PAWN -> path += "BlackPawn.png";
                case B_KNIGHT -> path += "BlackKnight.png";
                case B_BISHOP -> path += "BlackBishop.png";
                case B_ROOK -> path += "BlackRook.png";
                case B_QUEEN -> path += "BlackQueen.png";
                case B_KING -> path += "BlackKing.png";
            }

            if(piece >= 0) myObj.drawImage(x, y, tileSize, tileSize, Image.fromResource(path));
        }

        while (bitboard != 0) {

            long mask = (-bitboard & bitboard);
            int square = Long.numberOfTrailingZeros(mask);

            int row = (square / 8);
            int col = (square % 8);

            int x = col * tileSize;
            int y = canvasSize - (row + 1) * tileSize;

            myObj.setColor(myGreen);
            myObj.fillSquare(x, y, tileSize);

            bitboard -= mask;
        }


        myObj.show();
    }
}
