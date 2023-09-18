package com.kelseyde.calvin.service.engine;

import com.kelseyde.calvin.model.Game;
import com.kelseyde.calvin.model.move.Move;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

//@Service
public class RandomMoveEngine implements Engine {

    @Override
    public Move selectMove(Game game) {
        List<Move> legalMoves = new ArrayList<>(game.getLegalMoves().values());
        return legalMoves.get(new Random().nextInt(legalMoves.size()));
    }

}
