import java.util.*;

public class Random_AI {
	public static List<Action> actions(Game game)
	{
		List<Action> actions = new ArrayList<Action>();
		actions.add(new Action(Move.CALL, game.currentTurn, game.currentBet - game.lastStageTotalBets.get(game.currentTurn)));
		actions.add(new Action(Move.RAISE, game.currentTurn, game.currentBet));
		actions.add(new Action(Move.CHECK, game.currentTurn, 0));
		actions.add(new Action(Move.FOLD, game.currentTurn, 0));
		Collections.shuffle(actions);
		return actions;
	}
}
