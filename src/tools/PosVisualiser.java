package tools;

import codedraw.CodeDraw;
import codedraw.Palette;

import java.awt.*;

public class PosVisualiser {

    public static void main(String[] args){
        visualizeBitboard(0b1001);
    }

    private static final int canvasSize = 600;
    private static final int tileSize = canvasSize / 8;

    private static final CodeDraw myObj = new CodeDraw(canvasSize, canvasSize);

    private static final Color myWhite = Palette.fromRGB(241, 217, 176);
    private static final Color myBlack = Palette.fromRGB(178, 128, 94);
    private static final Color myGreen = Palette.fromRGBA(3, 75, 18, 150);

    static {
        for (int i = 0; i < 64; i++) {
            // Calculate grid positions
            int col = i % 8;
            int row = i / 8;

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


    public static void visualizeBitboard(long bitboard) {

        while (bitboard != 0) {

            long mask = (-bitboard & bitboard);
            int sq = Long.numberOfTrailingZeros(mask);

            int row = (sq / 8);
            int col = (sq % 8);

            int x = col * tileSize;
            int y = canvasSize - (row + 1) * tileSize;

            myObj.setColor(myGreen);
            myObj.fillSquare(x, y, tileSize);

            bitboard -= mask;
        }


        myObj.show();
    }
}
