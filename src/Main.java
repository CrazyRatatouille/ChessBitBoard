import constants.BitboardMasks;
import tools.MagicFinder;

import java.util.Arrays;

public class Main {

    public static void main(String[] args){

        long[] magics = MagicFinder.findBishopMagics();
        StringBuilder str = new StringBuilder("{\n");

        for (long magic : magics) {
            str.append("0x").append((Long.toHexString(magic))).append("L, ");
        }
        str.append("\n}");

        System.out.println(str);
    }
}