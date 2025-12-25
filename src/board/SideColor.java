package board;

public enum SideColor {
    White, Black;

    /**
     * The call of this method returns the opposite {@code SideColor} of {@code this}. This method only exists
     * to keep Code readable by avoiding if statements.
     *
     * @return the opposite {@code SideColor} of {@code this}
     */
    public SideColor other() {
        return switch (this) {
            case White -> Black;
            case Black -> White;
        };
    }
}