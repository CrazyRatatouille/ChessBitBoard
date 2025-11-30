public record Move(
        SideColor color,
        PieceType pieceType,
        Square From,
        Square To,
        PieceType promotionTo,
        PieceType capturedPieceType
) {
    public enum MoveType {BASIC, CAPTURE, CASTLE, PROMOTION, ENPASSANT}
}
