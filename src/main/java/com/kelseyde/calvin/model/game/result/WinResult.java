package com.kelseyde.calvin.model.game.result;

import com.kelseyde.calvin.model.Colour;
import com.kelseyde.calvin.model.game.WinType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class WinResult extends GameResult {

    private final ResultType resultType = ResultType.WIN;

    private final Colour winner;

    private final WinType winType;

}
