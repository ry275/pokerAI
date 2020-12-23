import HandEvaluator.*;
import java.util.*;

public class Util_AI {
	// Returns an integer 0...9 representing hand rank. 0 is High Card. 9 is Royal Flush.
	public static int strength(int c_1, int c_2, int c_3, int c_4, int c_5, int hc1, int hc2)
	{
		int lowest = new Evaluator(c_1, c_2, c_3, c_4, c_5).evaluate();

		for (int i = 0; i < 5; i++)
		{
			int[] input = new int[5];
			input[0] = c_1;
			input[1] = c_2;
			input[2] = c_3;
			input[3] = c_4;
			input[4] = c_5;
			input[i] = hc1;
			lowest = Math.min(lowest, new Evaluator(input).evaluate());
			
		    input = new int[5];
			input[0] = c_1;
			input[1] = c_2;
			input[2] = c_3;
			input[3] = c_4;
			input[4] = c_5;
			input[i] = hc2;
			lowest = Math.min(lowest, new Evaluator(input).evaluate());
		}
		
		for (int i = 0; i < 4; i++)
		{
			for (int j = i + 1; j < 5; j++)
			{
				int[] input = new int[5];
				input[0] = c_1;
				input[1] = c_2;
				input[2] = c_3;
				input[3] = c_4;
				input[4] = c_5;
				input[i] = hc1;
				input[j] = hc2;
				lowest = Math.min(lowest, new Evaluator(input).evaluate());
			}
		}
		
		return Evaluator.handRank(lowest) - 1;
	}
	
	public static int strength(Game game, Hand hand)
	{
		if (game.currentStage == Stage.PREFLOP || game.currentStage == Stage.SHOWDOWN) return -1;
		
		Hand_Evaluator HE = new HandEvaluator.Hand_Evaluator();
		int c_1 = HE.bitCardRep(game.cardsFaceUp.get(0).toStringEvaluator());
		int c_2 = HE.bitCardRep(game.cardsFaceUp.get(1).toStringEvaluator());
		int c_3 = HE.bitCardRep(game.cardsFaceUp.get(2).toStringEvaluator());
		int hc1 = HE.bitCardRep(hand.cardOne.toStringEvaluator());
		int hc2 = HE.bitCardRep(hand.cardTwo.toStringEvaluator());
		
		if (game.currentStage == Stage.FLOP) return Evaluator.handRank(new Evaluator(c_1, c_2, c_3, hc1, hc2).evaluate()) - 1;
		else if (game.currentStage == Stage.TURN)
		{
			int c_4 = HE.bitCardRep(game.cardsFaceUp.get(3).toStringEvaluator());
			int lowest = new Evaluator(c_1, c_2, c_3, c_4, hc1).evaluate();
			lowest = Math.min(lowest, new Evaluator(c_1, c_2, c_3, c_4, hc2).evaluate());
			return Evaluator.handRank(lowest) - 1;
		}
		else 
		{
			int c_4 = HE.bitCardRep(game.cardsFaceUp.get(3).toStringEvaluator());
			int c_5 = HE.bitCardRep(game.cardsFaceUp.get(4).toStringEvaluator());
			int lowest = new Evaluator(c_1, c_2, c_3, c_4, c_5).evaluate();

			for (int i = 0; i < 5; i++)
			{
				int[] input = new int[5];
				input[0] = c_1;
				input[1] = c_2;
				input[2] = c_3;
				input[3] = c_4;
				input[4] = c_5;
				input[i] = hc1;
				lowest = Math.min(lowest, new Evaluator(input).evaluate());
				
			    input = new int[5];
				input[0] = c_1;
				input[1] = c_2;
				input[2] = c_3;
				input[3] = c_4;
				input[4] = c_5;
				input[i] = hc2;
				lowest = Math.min(lowest, new Evaluator(input).evaluate());
			}
			
			for (int i = 0; i < 4; i++)
			{
				for (int j = i + 1; j < 5; j++)
				{
					int[] input = new int[5];
					input[0] = c_1;
					input[1] = c_2;
					input[2] = c_3;
					input[3] = c_4;
					input[4] = c_5;
					input[i] = hc1;
					input[j] = hc2;
					lowest = Math.min(lowest, new Evaluator(input).evaluate());
				}
			}
			
			return Evaluator.handRank(lowest) - 1;		
		}
		
	}
	
	// Returns an integer array; arr[0] is Pair?, arr[1] is Matching Suits?, arr[2] is High Card > 8?
	public static int[] preflopFeatures(Game game, Hand hand)
	{
		if (game.currentStage != Stage.PREFLOP) return null;
		int[] features = new int[3];
		if (hand.cardOne.value == hand.cardTwo.value) features[0] = 1;
		if (hand.cardOne.suit == hand.cardTwo.suit) features[1] = 1;
		if (hand.cardOne.value == Value.NINE ||
				hand.cardOne.value == Value.TEN ||
				hand.cardOne.value == Value.JACK ||
				hand.cardOne.value == Value.QUEEN ||
				hand.cardOne.value == Value.KING ||
				hand.cardOne.value == Value.ACE ||
				hand.cardTwo.value == Value.NINE ||
				hand.cardTwo.value == Value.TEN ||
				hand.cardTwo.value == Value.JACK ||
				hand.cardTwo.value == Value.QUEEN ||
				hand.cardTwo.value == Value.KING ||
				hand.cardTwo.value == Value.ACE) features[2] = 1;
		return features;
	}
	
	// Returns an integer array; arr[0] is position, arr[1] is previousAction, arr[2] is strength
	public static int[] flopTurnRiverFeatures(Game game, Hand hand, Player player)
	{
		int position = -1, previousAction = -1, strength = -1;
		if (game.currentStage == Stage.PREFLOP || game.currentStage == Stage.SHOWDOWN) return null;
		if (game.dealer == player) position = 0;
		else position = 1;
		if (game.currentStage == Stage.FLOP)
		{
			for (int i = game.history.size() - 1; i >= 0; i--)
			{
				if (game.history.get(i).action.player == player && game.history.get(i).stage == Stage.PREFLOP)
				{
					if (game.history.get(i).action.move == Move.RAISE) previousAction = 1;
					else previousAction = 0;
					break;
				}
			}
		}
		else if (game.currentStage == Stage.TURN)
		{
			for (int i = game.history.size() - 1; i >= 0; i--)
			{
				if (game.history.get(i).action.player == player && game.history.get(i).stage == Stage.FLOP)
				{
					if (game.history.get(i).action.move == Move.RAISE) previousAction = 1;
					else previousAction = 0;
					break; 
				}
			}
		}
		else 
		{
			for (int i = game.history.size() - 1; i >= 0; i--)
			{
				if (game.history.get(i).action.player == player && game.history.get(i).stage == Stage.TURN)
				{
					if (game.history.get(i).action.move == Move.RAISE) previousAction = 1;
					else previousAction = 0;
					break; 
				}
			}	
		}
		
		strength = Util_AI.strength(game, hand);
		
		int[] features = new int[3];
		features[0] = position;
		features[1] = previousAction;
		features[2] = strength;
		return features;
	}
	
}


