public enum DirectionOld {
    North, NorthEast, East, SouthEast, South, SouthWest, West, NorthWest;

    public int DirectionToIndex() {

        int idx = Integer.MIN_VALUE;

        switch (this) {
            case North -> idx = 0;
            case East -> idx = 2;
            case South -> idx = 4;
            case West -> idx = 6;

            case NorthEast -> idx = 1;
            case SouthEast -> idx = 3;
            case SouthWest -> idx = 5;
            case NorthWest -> idx = 7;
        }

        return idx;
    }

    public long go(long pos, long aFile, long hFile) {

        long moved = 0x0L;

        switch (this) {
            case North -> moved = pos << 8;
            case East -> moved = (pos & ~hFile) >>> 1;
            case South -> moved = pos >>> 8;
            case West -> moved = (pos & ~aFile) << 1;

            case NorthEast -> moved = (pos & ~hFile) << 7;
            case SouthEast -> moved = (pos & ~hFile) >>> 9;
            case SouthWest -> moved = (pos & ~aFile) >>> 7;
            case NorthWest -> moved = (pos & ~aFile) << 9;
        }

        return moved;
    }
}
