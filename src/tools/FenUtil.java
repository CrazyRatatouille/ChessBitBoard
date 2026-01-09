package tools;

import board.BoardState;

import static constants.BoardConstants.*;
import static constants.Zobrist.*;

public class FenUtil {

    /// A FULL LENGTH VALID FEN STRING IS EXPECTED. ELSE IT WILL NOT WORK
    public static void setPos(BoardState boardState, String FEN) {

        int index = 0;

        String[] parts = FEN.trim().split("\\s+");

        //board reset
        boardState.clear();

        int rank = 7;
        int file = 0;
        int square;

        //Piece Placement
        for (int i = 0; i < parts[0].length(); i++) {

            char token = parts[0].charAt(i);

            if (token == '/') {
                rank--;
                file = 0;
            } else if (Character.isDigit(token)) {
                file += Character.getNumericValue(token);
            } else {

                square = file + rank * 8;

                switch (token) {
                    //Black Pieces
                    case 'p' -> setPiece(boardState, square, B_PAWN);
                    case 'n' -> setPiece(boardState, square, B_KNIGHT);
                    case 'b' -> setPiece(boardState, square, B_BISHOP);
                    case 'r' -> setPiece(boardState, square, B_ROOK);
                    case 'q' -> setPiece(boardState, square, B_QUEEN);
                    case 'k' -> setPiece(boardState, square, B_KING);

                    //White Pieces
                    case 'P' -> setPiece(boardState, square, W_PAWN);
                    case 'N' -> setPiece(boardState, square, W_KNIGHT);
                    case 'B' -> setPiece(boardState, square, W_BISHOP);
                    case 'R' -> setPiece(boardState, square, W_ROOK);
                    case 'Q' -> setPiece(boardState, square, W_QUEEN);
                    case 'K' -> setPiece(boardState, square, W_KING);
                }

                file++;
            }
        }



        //Side to move
        int side = (parts[1].charAt(0) == 'w')? 0 : 1;
        boardState.setSide(side);
        if (side == BLACK) boardState.updateZobristHash(SIDE_KEY);



        //Castling ability
        int castlingRights = 0;
        for (char castleRight : parts[2].toCharArray()) {
            switch (castleRight) {
                case 'K' -> castlingRights |= 0b0010;
                case 'Q' -> castlingRights |= 0b0001;
                case 'k' -> castlingRights |= 0b1000;
                case 'q' -> castlingRights |= 0b0100;
            }
        }
        boardState.setCastlingRights((byte)castlingRights);
        boardState.updateZobristHash(CASTLING_KEYS[castlingRights]);



        //En passant target square
        if (parts[3].charAt(0) == '-') {
            boardState.setEnPassantTarget(0);
        }
        else {
            file = parts[3].charAt(0) - 'a';
            rank = parts[3].charAt(1) - '1';
            square = file + rank * 8;

            long enPassantTarget = 1L << (square);
            boardState.setEnPassantTarget(enPassantTarget);
            boardState.updateZobristHash(EN_PASSANT_KEYS[square]);
        }



        //half move Counter
        int halfMoveCounter = Integer.parseInt(parts[4]);
        boardState.setHalfMoveCounter(halfMoveCounter);




        //full move
        int fullMoveCounter = Integer.parseInt(parts[5]);
        boardState.setFullMoveCounter(fullMoveCounter);
    }

    private static void setPiece(BoardState boardState, int square, int piece) {

        boardState.setPieceAt(square, piece);

        long mask = 1L << square;

        boardState.updatePieceBB(piece, mask);
        boardState.updateOccupancy(piece & 1, mask);

        long zobristMask = PIECE_SQUARE_KEYS[piece * BOARD_SIZE + square];
        boardState.updateZobristHash(zobristMask);
    }
}
