import constants.BitboardMasks;
import tools.MagicFinder;

import java.util.Arrays;

public class Main {

    public static void main(String[] args){

        String test = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

        System.out.println(Arrays.toString(test.trim().split("\\s+")));
    }
}