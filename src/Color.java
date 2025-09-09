public enum Color {
    White, Black;

    public Color other() {
        return switch (this) {
            case White -> Black;
            case Black -> White;
        };
    }

}