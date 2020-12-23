import java.util.*;
import HandEvaluator.*;

public class Dealer {
	
	public Game game;
	public Deck deck;
	
	// need to call newGame() before starting any gameplay
	public Dealer()
	{
		this.deck = new Deck();
	}
	
	// invoke this method after invoking reset()
	private Hand dealHand()
	{
		try 
		{
			Hand hand = new Hand(this.deck.getOneCard(), this.deck.getOneCard());
			return hand;
		}
		catch (Exception e)
		{
			System.out.println(e);
		}
		
		return null;
	}
	
	// does not modify this.game if the Flop has already been dealt for the current game.
	private List<Card> dealFlop()
	{
		try
		{
			return this.deck.getThreeCards();
		}
		catch (Exception e)
		{
			System.out.println(e);
		}
		return null;
	}
	
	// does not modify this.game if both the Turn and River have already been dealt for the current game. 
	private Card dealTurnRiver()
	{
		try
		{
			return this.deck.getOneCard();
		}
		catch (Exception e)
		{
			System.out.println(e);
		}
		return null;
	}
	
	// doesn't do anything if the current stage is NA
	private void showdown()
	{
		switch(this.game.currentStage)
		{
		case PREFLOP:
			this.game.cardsFaceUp.addAll(this.dealFlop());
			this.game.cardsFaceUp.add(this.dealTurnRiver());
			this.game.cardsFaceUp.add(this.dealTurnRiver());
			break;
		case FLOP:
			this.game.cardsFaceUp.add(this.dealTurnRiver());
			this.game.cardsFaceUp.add(this.dealTurnRiver());
			break;
		case TURN:
			this.game.cardsFaceUp.add(this.dealTurnRiver());
			break;
		}	
		this.game.currentStage = Stage.SHOWDOWN;
	}
	
	// doesn't do anything if the current stage is NA
	private void advanceStage()
	{
		if (this.game.currentStage == Stage.PREFLOP ||
				this.game.currentStage == Stage.FLOP ||
				this.game.currentStage == Stage.TURN)
		{
			Iterator<Player> gamePlayers = this.game.lastStageAction.keySet().iterator();
			while (gamePlayers.hasNext())
			{
				Player p = gamePlayers.next();
				this.game.lastStageAction.put(p, null);
				this.game.lastStageTotalBets.put(p,  0);
			}			
		}
		switch (this.game.currentStage)
		{
		case PREFLOP:
			this.game.cardsFaceUp.addAll(this.dealFlop());
			this.game.currentStage = Stage.FLOP;
			this.game.currentBet = 0;
			if (this.game.dealer == Player.P1) this.game.currentTurn = Player.P2;
			else this.game.currentTurn = Player.P1;
			break;
		case FLOP:
			this.game.cardsFaceUp.add(this.dealTurnRiver());
			this.game.currentStage = Stage.TURN;
			this.game.currentBet = 0;
			if (this.game.dealer == Player.P1) this.game.currentTurn = Player.P2;
			else this.game.currentTurn = Player.P1;
			break;
		case TURN:
			this.game.cardsFaceUp.add(this.dealTurnRiver());
			this.game.currentStage = Stage.RIVER;
			this.game.currentBet = 0;
			if (this.game.dealer == Player.P1) this.game.currentTurn = Player.P2;
			else this.game.currentTurn = Player.P1;
			break;
		case RIVER:
			this.game.currentStage = Stage.SHOWDOWN;
			break;
		}
	}
	
	// returns 1 if handOne beats handTwo, 0 if both hands are equal, or -1 if handOne loses to handTwo
	private int compareHands(Hand handOne, Hand handTwo)
	{
			Hand_Evaluator HE = new HandEvaluator.Hand_Evaluator();
			int c_1 = HE.bitCardRep(this.game.cardsFaceUp.get(0).toStringEvaluator());
			int c_2 = HE.bitCardRep(this.game.cardsFaceUp.get(1).toStringEvaluator());
			int c_3 = HE.bitCardRep(this.game.cardsFaceUp.get(2).toStringEvaluator());
			int c_4 = HE.bitCardRep(this.game.cardsFaceUp.get(3).toStringEvaluator());
			int c_5 = HE.bitCardRep(this.game.cardsFaceUp.get(4).toStringEvaluator());
			
			int h1c1 = HE.bitCardRep(handOne.cardOne.toStringEvaluator());
			int h1c2 = HE.bitCardRep(handOne.cardTwo.toStringEvaluator());
			
			int h2c1 = HE.bitCardRep(handTwo.cardOne.toStringEvaluator());
			int h2c2 = HE.bitCardRep(handTwo.cardTwo.toStringEvaluator());
			
			int P1Score = new Evaluator(c_1, c_2, c_3, c_4, c_5).evaluate();
			int P2Score = new Evaluator(c_1, c_2, c_3, c_4, c_5).evaluate();
			
			// determine score of HandOne
			// handOne single card
			for (int i = 0; i < 5; i++)
			{
				int[] input = new int[5];
				input[0] = c_1;
				input[1] = c_2;
				input[2] = c_3;
				input[3] = c_4;
				input[4] = c_5;
				input[i] = h1c1;
				P1Score = Math.min(P1Score, new Evaluator(input).evaluate());
				
			    input = new int[5];
				input[0] = c_1;
				input[1] = c_2;
				input[2] = c_3;
				input[3] = c_4;
				input[4] = c_5;
				input[i] = h1c2;
				P1Score = Math.min(P1Score, new Evaluator(input).evaluate());
			}
			
			// handOne both cards
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
					input[i] = h1c1;
					input[j] = h1c2;
					P1Score = Math.min(P1Score, new Evaluator(input).evaluate());
				}
			}
			
			// determine score of HandTwo
			// handTwo single card
			for (int i = 0; i < 5; i++)
			{
				int[] input = new int[5];
				input[0] = c_1;
				input[1] = c_2;
				input[2] = c_3;
				input[3] = c_4;
				input[4] = c_5;
				input[i] = h2c1;
				P2Score = Math.min(P2Score, new Evaluator(input).evaluate());
				
			    input = new int[5];
				input[0] = c_1;
				input[1] = c_2;
				input[2] = c_3;
				input[3] = c_4;
				input[4] = c_5;
				input[i] = h2c2;
				P2Score = Math.min(P2Score, new Evaluator(input).evaluate());
			}
			
			// handTwo both cards
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
					input[i] = h2c1;
					input[j] = h2c2;
					P2Score = Math.min(P2Score, new Evaluator(input).evaluate());
				}
			}
			
			if (P2Score > P1Score) return 1;
			else if (P2Score == P1Score) return 0;
			else return -1;			
	}
	
	// returns true if action was successfully evaluated, false otherwise.
	public boolean evaluateAction(ActionStage AS)
	{
		if (this.game.status != Status.IN_PROGRESS ||  
				this.game.currentStage == Stage.SHOWDOWN) return false;
		if (AS.action.player != this.game.currentTurn) return false;
		
		switch (AS.action.move)
		{
		case FOLD:
			if (AS.action.player == Player.P1) this.game.status = Status.P2_WINS;
			else this.game.status = Status.P1_WINS;
			this.game.currentStage = Stage.SHOWDOWN;
			break;
		case CHECK:
			if (AS.action.player == Player.P1)
			{
				// only P1 CHECKs
				if (this.game.lastStageAction.get(Player.P2) == null)
				{				
					this.game.currentTurn = Player.P2;
					this.game.lastStageAction.put(AS.action.player, AS);
				}
				// both players CHECK
				else if (this.game.lastStageAction.get(Player.P2).action.move == Move.CHECK || this.game.lastStageAction.get(Player.P2).action.move == Move.CALL)
				{
					this.game.lastStageAction.put(AS.action.player, AS);
					this.advanceStage();
				}
				// ILLEGAL: Cannot check if the other person bet
				else return false;
			}
			else
			{
				// only P2 CHECKs
				if (this.game.lastStageAction.get(Player.P1) == null)
				{
					this.game.currentTurn = Player.P1;
					this.game.lastStageAction.put(AS.action.player, AS);
				}
				// both players CHECK
				else if (this.game.lastStageAction.get(Player.P1).action.move == Move.CHECK || this.game.lastStageAction.get(Player.P1).action.move == Move.CALL)
				{
					this.game.lastStageAction.put(AS.action.player, AS);
					this.advanceStage();
				}
				// ILLEGAL: Cannot check if the other person bet
				else return false;
			}
			break;
		case CALL:
			if (this.game.currentBet == 0 || this.game.lastStageTotalBets.get(AS.action.player) >= this.game.currentBet) return false;
			this.game.pot += AS.action.bet;
			this.game.gameTotalBets.put(AS.action.player, this.game.gameTotalBets.get(AS.action.player) + AS.action.bet);
			this.game.balances.put(AS.action.player, this.game.balances.get(AS.action.player) - AS.action.bet);
			this.game.lastStageAction.put(AS.action.player, AS);
			this.game.lastStageTotalBets.put(AS.action.player, this.game.lastStageTotalBets.get(AS.action.player) + AS.action.bet);
			if ((AS.action.player == Player.P1 && this.game.allInBets.get(Player.P2) != null) || 
					(AS.action.player == Player.P2 && this.game.allInBets.get(Player.P1) != null)) showdown();
			if (!(AS.action.player == Player.P1 && this.game.lastStageAction.get(Player.P2).action.move == Move.BLIND ||
					AS.action.player == Player.P2 && this.game.lastStageAction.get(Player.P1).action.move == Move.BLIND)) this.advanceStage();
			// current turn can change after CALL only during PREFLOP
			else if (AS.action.player == Player.P1) this.game.currentTurn = Player.P2;
			else this.game.currentTurn = Player.P1;
			break;
		case RAISE:
			if (AS.action.bet + this.game.lastStageTotalBets.get(AS.action.player) <= this.game.currentBet) return false;
			this.game.currentBet = AS.action.bet + this.game.lastStageTotalBets.get(AS.action.player);
			this.game.pot += AS.action.bet;
			this.game.gameTotalBets.put(AS.action.player, this.game.gameTotalBets.get(AS.action.player) + AS.action.bet);
			this.game.balances.put(AS.action.player, this.game.balances.get(AS.action.player) - AS.action.bet);
			if (AS.action.player == Player.P1) this.game.currentTurn = Player.P2;
			else this.game.currentTurn = Player.P1;
			this.game.lastStageAction.put(AS.action.player, AS);
			this.game.lastStageTotalBets.put(AS.action.player, this.game.lastStageTotalBets.get(AS.action.player) + AS.action.bet);
			break;
		case ALL_IN:
			if (AS.action.player == Player.P1)
			{
				this.game.allInBets.put(Player.P1, AS.action.bet);
				// Both players go ALL_IN
				if (this.game.allInBets.get(Player.P2) != null)
				{
					this.game.pot += AS.action.bet;
					this.game.gameTotalBets.put(AS.action.player, this.game.gameTotalBets.get(AS.action.player) + AS.action.bet);
					this.game.balances.put(AS.action.player, this.game.balances.get(AS.action.player) - AS.action.bet);
					this.game.currentBet = Math.max(this.game.currentBet, AS.action.bet + this.game.lastStageTotalBets.get(AS.action.player));
					this.game.lastStageAction.put(AS.action.player, AS);
					this.showdown();
				}
				else if (this.game.currentBet >= AS.action.bet)
				{
					this.game.pot += AS.action.bet;
					this.game.gameTotalBets.put(AS.action.player, this.game.gameTotalBets.get(AS.action.player) + AS.action.bet);
					this.game.balances.put(AS.action.player, this.game.balances.get(AS.action.player) - AS.action.bet);
					this.game.currentBet = Math.max(this.game.currentBet, AS.action.bet + this.game.lastStageTotalBets.get(AS.action.player));
					this.game.lastStageAction.put(AS.action.player, AS);
					this.showdown();
				}
				else 
				{
					this.game.currentBet = AS.action.bet + this.game.lastStageTotalBets.get(AS.action.player);
					this.game.pot += AS.action.bet;
					this.game.gameTotalBets.put(AS.action.player, this.game.gameTotalBets.get(AS.action.player) + AS.action.bet);
					this.game.balances.put(AS.action.player, this.game.balances.get(AS.action.player) - AS.action.bet);
					this.game.lastStageAction.put(AS.action.player, AS);
					this.game.currentTurn = Player.P2;
				}
			}
			else
			{
				this.game.allInBets.put(Player.P2, AS.action.bet);
				// Both players go ALL_IN
				if (this.game.allInBets.get(Player.P1) != null)
				{
					this.game.pot += AS.action.bet;
					this.game.gameTotalBets.put(AS.action.player, this.game.gameTotalBets.get(AS.action.player) + AS.action.bet);
					this.game.balances.put(AS.action.player, this.game.balances.get(AS.action.player) - AS.action.bet);
					this.game.currentBet = Math.max(this.game.currentBet, AS.action.bet + this.game.lastStageTotalBets.get(AS.action.player));
					this.game.lastStageAction.put(AS.action.player, AS);
					this.showdown();
				}
				else if (this.game.currentBet >= AS.action.bet)
				{
					this.game.pot += AS.action.bet;
					this.game.gameTotalBets.put(AS.action.player, this.game.gameTotalBets.get(AS.action.player) + AS.action.bet);
					this.game.balances.put(AS.action.player, this.game.balances.get(AS.action.player) - AS.action.bet);
					this.game.currentBet = Math.max(this.game.currentBet, AS.action.bet + this.game.lastStageTotalBets.get(AS.action.player));
					this.game.lastStageAction.put(AS.action.player, AS);
					this.showdown();
				}
				else 
				{
					this.game.currentBet = AS.action.bet + this.game.lastStageTotalBets.get(AS.action.player);
					this.game.pot += AS.action.bet;
					this.game.gameTotalBets.put(AS.action.player, this.game.gameTotalBets.get(AS.action.player) + AS.action.bet);
					this.game.balances.put(AS.action.player, this.game.balances.get(AS.action.player) - AS.action.bet);
					this.game.lastStageAction.put(AS.action.player, AS);
					this.game.currentTurn = Player.P1;
				} 
			}
			this.game.lastStageTotalBets.put(AS.action.player, this.game.lastStageTotalBets.get(AS.action.player) + AS.action.bet);
			break;
		}
			
		this.game.history.add(AS);
		return true;
	}


	// returns null if the currentStage isn't Showdown
	public Outcome outcome(Hand handOne, Hand handTwo)
	{
		// occurs when a player had Folded
		if (this.game.status == Status.P1_WINS) return new Outcome(this.game.pot, 0, Status.P1_WINS);
		if (this.game.status == Status.P2_WINS) return new Outcome(0, this.game.pot, Status.P2_WINS);
		
		if (this.game.currentStage != Stage.SHOWDOWN || this.game.status == Status.RESET) return null;
		int result = this.compareHands(handOne, handTwo);
		int betDifference = 0;
		switch (result)
		{
		// P2 wins
		case -1: 
			betDifference = this.game.gameTotalBets.get(Player.P1) - this.game.gameTotalBets.get(Player.P2);
			if (betDifference > 0)
			{
				int P1Reward = betDifference;
				int P2Reward = this.game.pot - betDifference;
				return new Outcome(P1Reward, P2Reward, Status.P2_WINS);
			}
			return new Outcome(0, this.game.pot, Status.P2_WINS);
		// draw, split Pot
		case 0:
			return new Outcome(this.game.gameTotalBets.get(Player.P1), this.game.gameTotalBets.get(Player.P2), Status.DRAW);						
		// P1 Wins
		default: 
			betDifference = this.game.gameTotalBets.get(Player.P1) - this.game.gameTotalBets.get(Player.P2);
			if (betDifference > 0)
			{
				int P2Reward = betDifference;
				int P1Reward = this.game.pot - betDifference;
				return new Outcome(P1Reward, P2Reward, Status.P1_WINS);
			}
			return new Outcome(this.game.pot, 0, Status.P1_WINS);	
		}	
	}
	
	public GameSetup newGame(int startingBalance, int bigBlind, int smallBlind)
	{
		this.game = new Game(startingBalance, bigBlind, smallBlind);
		this.game.status = Status.IN_PROGRESS;
		this.deck.reset();
		Hand P1Hand = this.dealHand();
		Hand P2Hand = this.dealHand();
		List<Hand> hands = new ArrayList<Hand>();
		hands.add(P1Hand);
		hands.add(P2Hand);
		return new GameSetup(hands, this.game);
	}
	
	public GameSetup nextGame()
	{
		this.game.resetBoard();
		this.game.status = Status.IN_PROGRESS;
		this.deck.reset();
		Hand P1Hand = this.dealHand();
		Hand P2Hand = this.dealHand();
		List<Hand> hands = new ArrayList<Hand>();
		hands.add(P1Hand);
		hands.add(P2Hand);
		return new GameSetup(hands, this.game);
	}
}


