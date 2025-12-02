public record Move(
        SideColor color,
        PieceType pieceType,
        Square From,
        Square To,
        PieceType promotionTo,
        PieceType capturedPieceType,
        MoveType moveType
) {
    public enum MoveType {CAPTURE, KING_SIDE_CASTLE, QUEEN_SIDE_CASTLE, PROMOTION, PAWN_DOUBLE_MOVE, ENPASSANT}
}
