public enum SideColor {
    White, Black;

    public SideColor other() {
        return switch (this) {
            case White -> Black;
            case Black -> White;
        };
    }

}