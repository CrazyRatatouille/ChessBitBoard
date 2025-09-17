import codedraw.CodeDraw;
import codedraw.Image;
import codedraw.Palette;

import java.awt.Color;

public class BoardDrawer {

    private Bitboards bitboard;
    private CodeDraw myObj;
    private final int canvasSize;
    private final int tileSize;

    private final Color myWhite = Palette.fromRGB(241, 217, 176);
    private final Color myBlack = Palette.fromRGB(178, 128, 94);
    private final Color myGreen = Palette.fromRGBA(3, 75, 18, 117);

    public BoardDrawer(Bitboards bitboard, int canvasSize) {

        this.bitboard = bitboard;
        this.canvasSize = canvasSize;
        myObj = new CodeDraw(canvasSize, canvasSize);

        tileSize = (int)(canvasSize / 8d);
    }

    public void drawBoard () {

        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {

                Color color = ((y + x) % 2 == 0)? myWhite : myBlack;
                myObj.setColor(color);

                myObj.fillSquare(x *  tileSize, y * tileSize, tileSize);

                long pos = 0x8000000000000000L >>> (x + 8 * y);

                PieceType[] p = new PieceType[1];
                SideColor c = bitboard.findPosInfo(pos, p);

                if (c != null) {

                    String path = "assets/" + c + p[0] + ".png";

                    myObj.drawImage(x * tileSize, y * tileSize, tileSize, tileSize, Image.fromResource(path));
                }

            }
        }

        myObj.show();

    }

}
