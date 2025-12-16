public record Move(
        SideColor color,
        PieceType pieceType,
        Square From,
        Square To,
        PieceType promotionTo,
        PieceType capturedPieceType,
        MoveType moveType
) {
    public enum MoveType {STANDARD, CAPTURE, PAWN_DOUBLE_MOVE, KING_SIDE_CASTLE, QUEEN_SIDE_CASTLE, PROMOTION, PROMOTION_AND_CAPTURE ,ENPASSANT}
}
