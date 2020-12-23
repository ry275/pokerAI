import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

enum Type{HumanVsHuman, HumanVsAI, AIVsAI}

public class Engine {
	public final Type type;
	private Dealer dealer;
	public Engine(Type type)
	{
		this.type = type;
		this.dealer = new Dealer();
	}
	
	private Action parseMove(String input, Game game) throws Exception
	{
		String[] splits = input.split("\\s+");
		if (splits.length == 0) throw new Exception("Invalid Command: No input read!");
		if (splits[0].toLowerCase().equals("fold"))
		{
			if (splits.length > 1) throw new Exception("Invalid Command: Too many arguments!");
			return new Action(Move.FOLD, game.currentTurn, 0);
		}
		else if (splits[0].toLowerCase().equals("check"))
		{
			if (splits.length > 1) throw new Exception("Invalid Command: Too many arguments!");
			return new Action(Move.CHECK, game.currentTurn, 0);
		}
		else if (splits[0].toLowerCase().equals("call"))
		{
			if (splits.length > 1) throw new Exception("Invalid Command: Too many arguments!");
			int amountNeededForCall = game.currentBet - game.lastStageTotalBets.get(game.currentTurn);
			return new Action(Move.CALL, game.currentTurn, amountNeededForCall);
		}
		else if (splits[0].toLowerCase().equals("raise"))
		{
			try 
			{
				if (splits.length < 2) throw new Exception("Invalid Command: Please specify raise amount!");
				int bet = Integer.parseInt(splits[1]);
				if (splits.length > 2) throw new Exception("Invalid Command: Too many arguments!");
				if (bet < game.currentBet) throw new Exception("Invalid Command: Raise By amount must be at least the current bet size!");
				int amountNeededForRaise = game.currentBet - game.lastStageTotalBets.get(game.currentTurn) + bet;
				return new Action(Move.RAISE, game.currentTurn, amountNeededForRaise);
			}
			catch (NumberFormatException nfe)
			{
				throw new Exception ("Invalid Command: Bet amount must be an integer!");
			}
		}
		else if (splits[0].toLowerCase().equals("all") && splits[1].toLowerCase().equals("in"))
		{
			if (splits.length > 2) throw new Exception("Invalid Command: Too many arguments!");
			return new Action(Move.ALL_IN, game.currentTurn, game.balances.get(game.currentTurn));
		}
		else 
		{
			throw new Exception("Invalid Command: Unknown move!");
		}
	}
	
	private boolean isValidAction(Action action, Game game)
	{
		if (action == null) return false;
		switch (action.move)
		{
		case FOLD:
			return true;
		case CHECK:
			if (!game.lastStageTotalBets.get(Player.P1).equals(game.lastStageTotalBets.get(Player.P2))) return false;
			return true;
		case CALL:
			if (game.currentBet == 0 || 
			game.balances.get(action.player) <= action.bet) return false;
			return true;
		case RAISE:
			if (game.balances.get(action.player) <= action.bet) return false;
			return true;
		// ALL_IN
		default:
			if (game.balances.get(action.player) != action.bet || 
					game.balances.get(action.player) <= 0) return false;
			return true;
		}
	}
	
	private String printHumanVsHuman(GameSetup setup)
	{
		
		String res = "---------------------------------------------------------------------------\n";
		res += "Dealer: ";
		res += setup.game.dealer == Player.P1 ? "Player 1" : "Player 2";
		res += "    Big Blind: " + setup.game.bigBlind + "    Small Blind: " + setup.game.smallBlind + "\n";
		res += "---------------------------------------------------------------------------\n";
		res += "(Cards On Table)    ";
		for (int i = 0; i < setup.game.cardsFaceUp.size(); i++)
		{
			res += setup.game.cardsFaceUp.get(i).toString() + "  ";
		}
		res += "\n";
		
		if (setup.game.currentTurn == Player.P1)
		{
			Hand P1Hand = setup.hands.get(0);
			res += "(Player 1's Hand)    " + P1Hand.cardOne.toString() + "  " + P1Hand.cardTwo.toString();
		}
		else 
		{
			Hand P2Hand = setup.hands.get(1);
			res += "(Player 2's Hand)    " + P2Hand.cardOne.toString() + "  " + P2Hand.cardTwo.toString();
		}
		res += "\n";
		res += "Pot: " + setup.game.pot + "        Current Bet Size: " + setup.game.currentBet + "\n";
		res += "Player 1's Balance: " + setup.game.balances.get(Player.P1) + 
				"    Player 2's Balance: " + setup.game.balances.get(Player.P2) + "\n";
		res += "---------------------------------------------------------------------------";

		return res;
	}
	
	private String printHumanVsAI(GameSetup setup)
	{
		
		String res = "---------------------------------------------------------------------------\n";
		res += "Dealer: ";
		res += setup.game.dealer == Player.P1 ? "Player" : "AI";
		res += "    Big Blind: " + setup.game.bigBlind + "    Small Blind: " + setup.game.smallBlind + "\n";
		res += "---------------------------------------------------------------------------\n";
		res += "(Cards On Table)    ";
		for (int i = 0; i < setup.game.cardsFaceUp.size(); i++)
		{
			res += setup.game.cardsFaceUp.get(i).toString() + "  ";
		}
		res += "\n";
		
		if (setup.game.currentTurn == Player.P1)
		{
			Hand P1Hand = setup.hands.get(0);
			res += "(Player's Hand)    " + P1Hand.cardOne.toString() + "  " + P1Hand.cardTwo.toString();
		}
		res += "\n";
		res += "Pot: " + setup.game.pot + "        Current Bet Size: " + setup.game.currentBet + "\n";
		res += "Player's Balance: " + setup.game.balances.get(Player.P1) + 
				"    AI's Balance: " + setup.game.balances.get(Player.P2) + "\n";
		res += "---------------------------------------------------------------------------";

		return res;
	}
	
	private QLearning_AI newQL()
	{
		Scanner scanner = new Scanner(System.in);
		double winReward = 0;
		boolean valid = false;
		while (!valid)
		{
			System.out.print("Enter the QL Win Reward: ");
			String input = scanner.nextLine();
			if (!input.matches(".*[a-z].*")) 
			{
				winReward = Double.parseDouble(input);
				valid = true;
			}
		}
		double loseReward = 0;
		valid = false;
		while (!valid)
		{
			System.out.print("Enter the QL Lose Reward: ");
			String input = scanner.nextLine();
			if (!input.matches(".*[a-z].*"))
			{
				loseReward = Double.parseDouble(input);
				valid = true;
			}
		}
		double otherReward = 0;
		valid = false;
		while (!valid)
		{
			System.out.print("Enter the QL Other Reward: ");
			String input = scanner.nextLine();
			if (!input.matches(".*[a-z].*")) 
			{
				otherReward = Double.parseDouble(input);
				valid = true;
			}
		}
		double alpha = 0;
		valid = false;
		while (!valid)
		{
			System.out.print("Enter the QL Alpha: ");
			String input = scanner.nextLine();
			if (!input.matches(".*[a-z].*"))
			{
				alpha = Double.parseDouble(input);
				valid = true;
			}
		}
		double gamma = 0;
		valid = false;
		while (!valid)
		{
			System.out.print("Enter the QL Gamma: ");
			String input = scanner.nextLine();
			if (!input.matches(".*[a-z].*"))
			{
				gamma = Double.parseDouble(input);
				valid = true;
			}
		}
		int N = 0;
		valid = false;
		while (!valid)
		{
			System.out.print("Enter the QL N number (how many times the AI should try to explore each action): ");
			String input = scanner.nextLine();
			if (input.matches("[0-9]+")) 
			{
				N = Integer.parseInt(input);
				valid = true;
			}
		}
		return new QLearning_AI(winReward,loseReward,otherReward,alpha,gamma,N);
	}
	private void humanVsHuman()
	{
		Scanner scanner = new Scanner(System.in);
		boolean play = true;
		GameSetup setup;
		while (play)
		{
			if (this.dealer.game == null)
			{					
				System.out.println("Starting a brand new game (Human vs Human)!");
				
				int balance = 0;
				boolean validBalance = false;
				while (!validBalance)
				{
					System.out.print("Enter each player's starting balance (integer): ");
					String balanceInput = scanner.nextLine();
					if (balanceInput.matches("[0-9]+")) 
					{
						balance = Integer.parseInt(balanceInput);
						validBalance = true;
					}
					else 
					{
						System.out.println("Invalid Input! Please enter only a strictly positive integer.\n");
					}
				}
				
				int bigBlind = 0;
				boolean validBigBlind = false;
				while (!validBigBlind)
				{
					System.out.print("Enter big blind amount (integer): ");
					String bigBlindInput = scanner.nextLine();
					if (bigBlindInput.matches("[0-9]+")) 
					{
						bigBlind = Integer.parseInt(bigBlindInput);
						validBigBlind = true;
					}
					else 
					{
						System.out.println("Invalid Input! Please enter only a strictly positive integer.\n");
					}
				}
				
				int smallBlind = 0;
				boolean validSmallBlind = false;
				while (!validSmallBlind)
				{
					System.out.print("Enter small blind amount (integer): ");
					String smallBlindInput = scanner.nextLine();
					if (smallBlindInput.matches("[0-9]+")) 
					{
						smallBlind = Integer.parseInt(smallBlindInput);
						validSmallBlind = true;
					}
					else 
					{
						System.out.println("Invalid Input! Please enter only a strictly positive integer.\n");
					}
				}
				
				setup = this.dealer.newGame(balance, bigBlind, smallBlind);			
			}
			else setup = this.dealer.nextGame();
			
			System.out.println("");
			
			// Dealer pays small blind
			if (setup.game.smallBlind >= setup.game.balances.get(setup.game.dealer))
			{
				setup.game.pot += setup.game.balances.get(setup.game.dealer);
				setup.game.gameTotalBets.put(setup.game.dealer, setup.game.gameTotalBets.get(setup.game.dealer) + setup.game.balances.get(setup.game.dealer));
				setup.game.currentBet = setup.game.balances.get(setup.game.dealer);
				setup.game.lastStageTotalBets.put(setup.game.dealer, setup.game.currentBet);
				setup.game.currentStage = Stage.SHOWDOWN;
				setup.game.lastStageAction.put(setup.game.dealer, new ActionStage(new Action(Move.ALL_IN, setup.game.dealer, setup.game.balances.get(setup.game.dealer)), Stage.PREFLOP));
				setup.game.balances.put(setup.game.dealer, 0);
			}
			else 
			{
				setup.game.pot += setup.game.smallBlind;
				setup.game.gameTotalBets.put(setup.game.dealer, setup.game.gameTotalBets.get(setup.game.dealer) + setup.game.smallBlind);
				setup.game.currentBet = setup.game.smallBlind;
				setup.game.lastStageTotalBets.put(setup.game.dealer, setup.game.smallBlind);
				setup.game.balances.put(setup.game.dealer, setup.game.balances.get(setup.game.dealer) - setup.game.smallBlind);
				setup.game.lastStageAction.put(setup.game.dealer, new ActionStage(new Action(Move.BLIND, setup.game.dealer, setup.game.smallBlind), Stage.PREFLOP));
			}

			// Dealer has no money left
			if (setup.game.currentStage == Stage.SHOWDOWN)
			{
				if (setup.game.balances.get(setup.game.opponent) > setup.game.currentBet)
				{
					setup.game.balances.put(setup.game.opponent, setup.game.balances.get(setup.game.opponent) - setup.game.currentBet);
					setup.game.lastStageTotalBets.put(setup.game.opponent, setup.game.currentBet);
					setup.game.lastStageAction.put(setup.game.opponent, new ActionStage(new Action(Move.CALL, setup.game.opponent, setup.game.currentBet), Stage.PREFLOP));
					setup.game.pot += setup.game.currentBet;
					setup.game.gameTotalBets.put(setup.game.opponent, setup.game.gameTotalBets.get(setup.game.opponent) + setup.game.currentBet);
				}
				else 
				{
					setup.game.pot += setup.game.balances.get(setup.game.opponent);
					setup.game.gameTotalBets.put(setup.game.opponent, setup.game.gameTotalBets.get(setup.game.opponent) + setup.game.balances.get(setup.game.opponent));
					setup.game.lastStageTotalBets.put(setup.game.opponent, setup.game.balances.get(setup.game.opponent));
					setup.game.lastStageAction.put(setup.game.opponent, new ActionStage(new Action(Move.ALL_IN, setup.game.opponent, setup.game.balances.get(setup.game.opponent)), Stage.PREFLOP));
					setup.game.balances.put(setup.game.opponent, 0);
				}
			}
			// Dealer has money left
			else 
			{
				// Opponent pays big blind
				if (setup.game.bigBlind >= setup.game.balances.get(setup.game.opponent))
				{
					setup.game.pot += setup.game.balances.get(setup.game.opponent);
					setup.game.gameTotalBets.put(setup.game.opponent, setup.game.gameTotalBets.get(setup.game.opponent) + setup.game.balances.get(setup.game.opponent));
					setup.game.lastStageTotalBets.put(setup.game.opponent, setup.game.balances.get(setup.game.opponent));
					setup.game.currentBet = Math.max(setup.game.currentBet, setup.game.balances.get(setup.game.opponent));
					setup.game.lastStageAction.put(setup.game.opponent, new ActionStage(new Action(Move.ALL_IN, setup.game.opponent, setup.game.balances.get(setup.game.opponent)), Stage.PREFLOP));
					setup.game.balances.put(setup.game.opponent, 0);
					setup.game.currentStage = Stage.SHOWDOWN;
				}
				else 
				{
					setup.game.pot += setup.game.bigBlind;
					setup.game.gameTotalBets.put(setup.game.opponent, setup.game.gameTotalBets.get(setup.game.opponent) + setup.game.bigBlind);
					setup.game.lastStageTotalBets.put(setup.game.opponent, setup.game.bigBlind);
					setup.game.currentBet = setup.game.bigBlind;
					setup.game.balances.put(setup.game.opponent, setup.game.balances.get(setup.game.opponent) - setup.game.bigBlind);
					setup.game.lastStageAction.put(setup.game.opponent, new ActionStage(new Action(Move.BLIND, setup.game.opponent, setup.game.bigBlind), Stage.PREFLOP));
				}	
			}
			
			
			while (setup.game.currentStage != Stage.SHOWDOWN)
			{
				System.out.print(this.printHumanVsHuman(setup));
				System.out.println("");
				boolean validAction = false;
				while (!validAction)
				{
					if (setup.game.currentTurn == Player.P1) System.out.print("Possible actions: 'fold', 'check', 'call', 'raise (integer)', 'all in' \n(Player 1)    Enter Command: ");
					else System.out.print("Possible actions: 'fold', 'check', 'call', 'raise (integer)', 'all in' \n(Player 2)    Enter Command: ");
					String command = scanner.nextLine();
					try 
					{
						Action action = this.parseMove(command, setup.game);
						if (this.isValidAction(this.parseMove(command, setup.game), setup.game) && 
								this.dealer.evaluateAction(new ActionStage(action, setup.game.currentStage))) 
						{
							String receipt = "";
							if (action.player == Player.P1) receipt += "\nPlayer 1 ";
							else receipt += "\nPlayer 2 ";
							switch (action.move)
							{
							case FOLD:
								receipt += "folded!";
								break;
							case CHECK:
								receipt += "checked!";
								break;
							case CALL:
								receipt += "called!";
								break;
							case RAISE:
								receipt += "raised!";
								break;
							case ALL_IN:
								receipt += "went all in!";
								break;
							}
							System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
							System.out.println(receipt);
							validAction = true;
						}
						else System.out.println("Illegal Move!\n");
					}
					catch (Exception e)
					{
						System.out.println(e + "\n");
					}
				}
			}
			System.out.println("\n-------------------------------------------------------");
			System.out.println("		    END OF GAME!");
			System.out.println("-------------------------------------------------------");
			
			String tableCards = "Cards on table:    ";
			for (int i = 0; i < setup.game.cardsFaceUp.size(); i++)
			{
				tableCards += setup.game.cardsFaceUp.get(i).toString() + "  ";
			}
			System.out.println(tableCards);
			System.out.println("Player 1's Hand:    " + setup.hands.get(0).cardOne.toString() + "  " + setup.hands.get(0).cardTwo.toString());
			System.out.println("Player 2's Hand:    " + setup.hands.get(1).cardOne.toString() + "  " + setup.hands.get(1).cardTwo.toString() + "\n");

			Outcome outcome = this.dealer.outcome(setup.hands.get(0), setup.hands.get(1));
			switch (outcome.status)
			{
			case P1_WINS:
				System.out.println("Player 1 wins!");
				break;
			case P2_WINS:
				System.out.println("Player 2 wins!");
				break;
			case DRAW:
				System.out.println("Draw!");
				break;
			}
			setup.game.balances.put(Player.P1, setup.game.balances.get(Player.P1) + outcome.P1Reward);
			setup.game.balances.put(Player.P2, setup.game.balances.get(Player.P2) + outcome.P2Reward);
			System.out.println("" + outcome.P1Reward + " awarded to Player 1    (current balance: " 
			+ setup.game.balances.get(Player.P1) + ")");
			System.out.println("" + outcome.P2Reward + " awarded to Player 2    (current balance: " 
					+ setup.game.balances.get(Player.P2) + ")\n");
			
			boolean validCommand = false;
			while (!validCommand)
			{
				System.out.println("Do you want to (1) Keep Playing, (2) Play a New Game, or (3) Quit?\n");
				System.out.print("Enter Command ('1', '2', or '3'): ");
				String input = scanner.nextLine();
				if (input.equals("1"))
				{
					System.out.println("Starting another game!\n");
					validCommand = true;
				}
				else if (input.equals("2"))
				{
					this.dealer = new Dealer();
					validCommand = true;
				}
				else if (input.equals("3"))
				{
					System.out.println("Good bye :-(");
					play = false;
					validCommand = true;
				}
				else 
				{
					System.out.println("Invalid Command!\n");
				}
			}				
		}
		scanner.close();
	}
	
	private void humanVsAI(String AI)
	{
		Scanner scanner = new Scanner(System.in);
		NB_AI NB = null;
		QLearning_AI QL = null;
		System.out.println("\n\n Starting the AI...  \n\n");
		if (AI.equals("NB")) NB = new NB_AI("./files/NB_Flop.csv", "./files/NB_Turn.csv", "./files/NB_River.csv");
		if (AI.equals("QL")) 
		{
			QL = newQL();
			boolean validCommand = false;
			while (!validCommand)
			{
				System.out.println("Do you want to load the Q and N functions of a previously saved QL model?");
				System.out.print("Enter Command ('Y', 'N'): ");
				String input = scanner.nextLine();
				if (input.toUpperCase().equals("Y"))
				{
					System.out.print("Enter the name of the save file (no extension): ");
					String fileName = scanner.nextLine();
					QL.loadModel(fileName);
					validCommand = true;
				}
				else if (input.toUpperCase().equals("N"))
				{
					validCommand = true;
				}
			}
		}
		
		
		boolean play = true;
		GameSetup setup;
		while (play)
		{
			if (this.dealer.game == null)
			{					
				System.out.println("Starting a brand new game (Human vs AI [" + AI+ "])!");
				
				int balance = 0;
				boolean validBalance = false;
				while (!validBalance)
				{
					System.out.print("Enter each player's starting balance (integer): ");
					String balanceInput = scanner.nextLine();
					if (balanceInput.matches("[0-9]+")) 
					{
						balance = Integer.parseInt(balanceInput);
						validBalance = true;
					}
					else 
					{
						System.out.println("Invalid Input! Please enter only a strictly positive integer.\n");
					}
				}
				
				int bigBlind = 0;
				boolean validBigBlind = false;
				while (!validBigBlind)
				{
					System.out.print("Enter big blind amount (integer): ");
					String bigBlindInput = scanner.nextLine();
					if (bigBlindInput.matches("[0-9]+")) 
					{
						bigBlind = Integer.parseInt(bigBlindInput);
						validBigBlind = true;
					}
					else 
					{
						System.out.println("Invalid Input! Please enter only a strictly positive integer.\n");
					}
				}
				
				int smallBlind = 0;
				boolean validSmallBlind = false;
				while (!validSmallBlind)
				{
					System.out.print("Enter small blind amount (integer): ");
					String smallBlindInput = scanner.nextLine();
					if (smallBlindInput.matches("[0-9]+")) 
					{
						smallBlind = Integer.parseInt(smallBlindInput);
						validSmallBlind = true;
					}
					else 
					{
						System.out.println("Invalid Input! Please enter only a strictly positive integer.\n");
					}
				}
				
				setup = this.dealer.newGame(balance, bigBlind, smallBlind);			
			}
			else setup = this.dealer.nextGame();
			
			if (AI.equals("NB")) NB.newGame(Player.P2, setup.hands.get(1), setup.game);
			else if (AI.equals("QL")) QL.newGame(Player.P2, setup.hands.get(1), setup.game);
			
			System.out.println("");
			
			// Dealer pays small blind
			if (setup.game.smallBlind >= setup.game.balances.get(setup.game.dealer))
			{
				setup.game.pot += setup.game.balances.get(setup.game.dealer);
				setup.game.gameTotalBets.put(setup.game.dealer, setup.game.gameTotalBets.get(setup.game.dealer) + setup.game.balances.get(setup.game.dealer));
				setup.game.currentBet = setup.game.balances.get(setup.game.dealer);
				setup.game.lastStageTotalBets.put(setup.game.dealer, setup.game.currentBet);
				setup.game.currentStage = Stage.SHOWDOWN;
				setup.game.lastStageAction.put(setup.game.dealer, new ActionStage(new Action(Move.ALL_IN, setup.game.dealer, setup.game.balances.get(setup.game.dealer)), Stage.PREFLOP));
				setup.game.balances.put(setup.game.dealer, 0);
			}
			else 
			{
				setup.game.pot += setup.game.smallBlind;
				setup.game.gameTotalBets.put(setup.game.dealer, setup.game.gameTotalBets.get(setup.game.dealer) + setup.game.smallBlind);
				setup.game.currentBet = setup.game.smallBlind;
				setup.game.lastStageTotalBets.put(setup.game.dealer, setup.game.smallBlind);
				setup.game.balances.put(setup.game.dealer, setup.game.balances.get(setup.game.dealer) - setup.game.smallBlind);
				setup.game.lastStageAction.put(setup.game.dealer, new ActionStage(new Action(Move.BLIND, setup.game.dealer, setup.game.smallBlind), Stage.PREFLOP));
			}

			// Dealer has no money left
			if (setup.game.currentStage == Stage.SHOWDOWN)
			{
				if (setup.game.balances.get(setup.game.opponent) > setup.game.currentBet)
				{
					setup.game.balances.put(setup.game.opponent, setup.game.balances.get(setup.game.opponent) - setup.game.currentBet);
					setup.game.lastStageTotalBets.put(setup.game.opponent, setup.game.currentBet);
					setup.game.lastStageAction.put(setup.game.opponent, new ActionStage(new Action(Move.CALL, setup.game.opponent, setup.game.currentBet), Stage.PREFLOP));
					setup.game.pot += setup.game.currentBet;
					setup.game.gameTotalBets.put(setup.game.opponent, setup.game.gameTotalBets.get(setup.game.opponent) + setup.game.currentBet);
				}
				else 
				{
					setup.game.pot += setup.game.balances.get(setup.game.opponent);
					setup.game.gameTotalBets.put(setup.game.opponent, setup.game.gameTotalBets.get(setup.game.opponent) + setup.game.balances.get(setup.game.opponent));
					setup.game.lastStageTotalBets.put(setup.game.opponent, setup.game.balances.get(setup.game.opponent));
					setup.game.lastStageAction.put(setup.game.opponent, new ActionStage(new Action(Move.ALL_IN, setup.game.opponent, setup.game.balances.get(setup.game.opponent)), Stage.PREFLOP));
					setup.game.balances.put(setup.game.opponent, 0);
				}
			}
			// Dealer has money left
			else 
			{
				// Opponent pays big blind
				if (setup.game.bigBlind >= setup.game.balances.get(setup.game.opponent))
				{
					setup.game.pot += setup.game.balances.get(setup.game.opponent);
					setup.game.gameTotalBets.put(setup.game.opponent, setup.game.gameTotalBets.get(setup.game.opponent) + setup.game.balances.get(setup.game.opponent));
					setup.game.lastStageTotalBets.put(setup.game.opponent, setup.game.balances.get(setup.game.opponent));
					setup.game.currentBet = Math.max(setup.game.currentBet, setup.game.balances.get(setup.game.opponent));
					setup.game.lastStageAction.put(setup.game.opponent, new ActionStage(new Action(Move.ALL_IN, setup.game.opponent, setup.game.balances.get(setup.game.opponent)), Stage.PREFLOP));
					setup.game.balances.put(setup.game.opponent, 0);
					setup.game.currentStage = Stage.SHOWDOWN;
				}
				else 
				{
					setup.game.pot += setup.game.bigBlind;
					setup.game.gameTotalBets.put(setup.game.opponent, setup.game.gameTotalBets.get(setup.game.opponent) + setup.game.bigBlind);
					setup.game.lastStageTotalBets.put(setup.game.opponent, setup.game.bigBlind);
					setup.game.currentBet = setup.game.bigBlind;
					setup.game.balances.put(setup.game.opponent, setup.game.balances.get(setup.game.opponent) - setup.game.bigBlind);
					setup.game.lastStageAction.put(setup.game.opponent, new ActionStage(new Action(Move.BLIND, setup.game.opponent, setup.game.bigBlind), Stage.PREFLOP));
				}	
			}
			
			
			while (setup.game.currentStage != Stage.SHOWDOWN)
			{
				System.out.print(this.printHumanVsAI(setup));
				System.out.println("");
				if (setup.game.currentTurn == Player.P1)
				{
					boolean validAction = false;
					while (!validAction)
					{
						System.out.print("Possible actions: 'fold', 'check', 'call', 'raise (integer)', 'all in' \n(Player)    Enter Command: ");
						String command = scanner.nextLine();
						try 
						{
							Action action = this.parseMove(command, setup.game);
							if (this.isValidAction(this.parseMove(command, setup.game), setup.game) && 
									this.dealer.evaluateAction(new ActionStage(action, setup.game.currentStage))) 
							{
								String receipt = "\nPlayer ";
								switch (action.move)
								{
								case FOLD:
									receipt += "folded!";
									break;
								case CHECK:
									receipt += "checked!";
									break;
								case CALL:
									receipt += "called!";
									break;
								case RAISE:
									receipt += "raised!";
									break;
								case ALL_IN:
									receipt += "went all in!";
									break;
								}
								System.out.println("\n\n\n\n\n");
								System.out.println(receipt);
								validAction = true;
							}
							else System.out.println("Illegal Move!\n");
						}
						catch (Exception e)
						{
							System.out.println(e + "\n");
						}
					}
				}
				// AI's turn
				else
				{
					if (AI.equals("NB"))
					{
						List<Action> actions = NB.actions();					
						for (int i = 0; i < actions.size(); i++)
						{
							Action action = actions.get(i);
							if (this.isValidAction(action, setup.game) && 
									this.dealer.evaluateAction(new ActionStage(action, setup.game.currentStage))) 
							{
								String receipt = "\nAI ";
								switch (action.move)
								{
								case FOLD:
									receipt += "folded!";
									break;
								case CHECK:
									receipt += "checked!";
									break;
								case CALL:
									receipt += "called!";
									break;
								case RAISE:
									receipt += "raised!";
									break;
								case ALL_IN:
									receipt += "went all in!";
									break;
								}
								System.out.println("\n\n\n\n\n");
								System.out.println(receipt);
							}
						}
						
					}
					else if (AI.equals("QL"))
					{
						QLearningActions QLactions = QL.actions();
						for (int i = 0; i < QLactions.actions.size(); i++)
						{
							Action action = QLactions.actions.get(i);
							if (this.isValidAction(action, setup.game) && 
									this.dealer.evaluateAction(new ActionStage(action, setup.game.currentStage))) 
							{
								String receipt = "\nAI ";
								switch (action.move)
								{
								case FOLD:
									receipt += "folded!";
									break;
								case CHECK:
									receipt += "checked!";
									break;
								case CALL:
									receipt += "called!";
									break;
								case RAISE:
									receipt += "raised!";
									break;
								case ALL_IN:
									receipt += "went all in!";
									break;
								}
								System.out.println("\n\n\n\n\n");
								System.out.println(receipt);
								if (setup.game.currentStage != Stage.SHOWDOWN) QL.updateFunctions(QLactions.state, action, false, false);
								else if (setup.game.currentStage == Stage.SHOWDOWN)
								{
									Outcome outcome = this.dealer.outcome(setup.hands.get(0), setup.hands.get(1));
									switch (outcome.status)
									{
									case P1_WINS:
										QL.updateFunctions(QLactions.state, action, false, true);
										break;
									case P2_WINS:
										QL.updateFunctions(QLactions.state, action, true, false);
										break;
									case DRAW:
										QL.updateFunctions(QLactions.state, action, true, false);
										break;
									}
								}
							}							
						}
					}
					else if (AI.equals("EV"))
					{
						List<Action> actions = null;
						if (setup.game.currentStage == Stage.PREFLOP) actions = EV_AI.actionsPreFlop(setup.hands.get(1), setup.game);
						else if (setup.game.currentStage == Stage.FLOP) actions = EV_AI.optimalEquityMoveFlop(setup.hands.get(0), setup.hands.get(1), setup.game, dealer.deck);
						else if (setup.game.currentStage == Stage.TURN) actions = EV_AI.optimalEquityMoveTurn(setup.hands.get(0), setup.hands.get(1), setup.game, dealer.deck);
						else if (setup.game.currentStage == Stage.RIVER) actions = EV_AI.optimalEquityMoveRiver(setup.hands.get(0), setup.hands.get(1), setup.game);
						for (int i = 0; i <actions.size(); i++)
						{
							Action action = actions.get(i);
							if (this.isValidAction(action, setup.game) && 
									this.dealer.evaluateAction(new ActionStage(action, setup.game.currentStage))) 
							{
								String receipt = "\nAI ";
								switch (action.move)
								{
								case FOLD:
									receipt += "folded!";
									break;
								case CHECK:
									receipt += "checked!";
									break;
								case CALL:
									receipt += "called!";
									break;
								case RAISE:
									receipt += "raised!";
									break;
								case ALL_IN:
									receipt += "went all in!";
									break;
								}
								System.out.println("\n\n\n\n\n");
								System.out.println(receipt);			
							}							
						}
					}
					else if (AI.equals("RANDOM"))
					{
						List<Action> actions = Random_AI.actions(setup.game);						
						for (int i = 0; i <actions.size(); i++)
						{
							Action action = actions.get(i);
							if (this.isValidAction(action, setup.game) && 
									this.dealer.evaluateAction(new ActionStage(action, setup.game.currentStage))) 
							{
								String receipt = "\nAI ";
								switch (action.move)
								{
								case FOLD:
									receipt += "folded!";
									break;
								case CHECK:
									receipt += "checked!";
									break;
								case CALL:
									receipt += "called!";
									break;
								case RAISE:
									receipt += "raised!";
									break;
								case ALL_IN:
									receipt += "went all in!";
									break;
								}
								System.out.println("\n\n\n\n\n");
								System.out.println(receipt);			
							}							
						}
					}
				}
				
			}
			
			System.out.println("\n-------------------------------------------------------");
			System.out.println("		    END OF GAME!");
			System.out.println("-------------------------------------------------------");
			
			String tableCards = "Cards on table:    ";
			for (int i = 0; i < setup.game.cardsFaceUp.size(); i++)
			{
				tableCards += setup.game.cardsFaceUp.get(i).toString() + "  ";
			}
			System.out.println(tableCards);
			System.out.println("Player's Hand:    " + setup.hands.get(0).cardOne.toString() + "  " + setup.hands.get(0).cardTwo.toString());
			System.out.println("AI's Hand:    " + setup.hands.get(1).cardOne.toString() + "  " + setup.hands.get(1).cardTwo.toString() + "\n");
			
			Outcome outcome = this.dealer.outcome(setup.hands.get(0), setup.hands.get(1));
			switch (outcome.status)
			{
			case P1_WINS:
				System.out.println("Player wins!");
				break;
			case P2_WINS:
				System.out.println("AI wins!");
				break;
			case DRAW:
				System.out.println("Draw!");
				break;
			}
			setup.game.balances.put(Player.P1, setup.game.balances.get(Player.P1) + outcome.P1Reward);
			setup.game.balances.put(Player.P2, setup.game.balances.get(Player.P2) + outcome.P2Reward);
			System.out.println("" + outcome.P1Reward + " awarded to Player    (current balance: " 
			+ setup.game.balances.get(Player.P1) + ")");
			System.out.println("" + outcome.P2Reward + " awarded to AI    (current balance: " 
					+ setup.game.balances.get(Player.P2) + ")\n");
			
			if (AI.equals("QL"))
			{
				boolean validCommand = false;
				while (!validCommand)
				{
					System.out.println("Do you want to save the Q and N functions of the current QL model?");
					System.out.print("Enter Command ('Y', 'N'): ");
					String input = scanner.nextLine();
					if (input.toUpperCase().equals("Y"))
					{
						System.out.print("Enter the name of the save file (no extension): ");
						String fileName = scanner.nextLine();
						QL.saveModel(fileName);
						validCommand = true;
					}
					else if (input.toUpperCase().equals("N"))
					{
						validCommand = true;
					}
				}
			}
			boolean validCommand = false;
			while (!validCommand)
			{
				System.out.println("Do you want to (1) Keep Playing, (2) Play a New Game, or (3) Quit?\n");
				System.out.print("Enter Command ('1', '2', or '3'): ");
				String input = scanner.nextLine();
				if (input.equals("1"))
				{
					System.out.println("Starting another game!\n");
					validCommand = true;
				}
				else if (input.equals("2"))
				{
					this.dealer = new Dealer();
					validCommand = true;
				}
				else if (input.equals("3"))
				{
					System.out.println("Good bye :-(");
					play = false;
					validCommand = true;
				}
				else 
				{
					System.out.println("Invalid Command!\n");
				}
			}				
		}
		
	}
	
	private void AIVsAI(String AI1, String AI2, int games, String csv)
	{
		NB_AI NB = null;
		QLearning_AI QL = null;
		NB_AI NB2 = null;
		QLearning_AI QL2 = null;
		String fileName1 = null;
		String fileName2 = null;
		if (AI1.equals("NB")) NB = new NB_AI("./files/NB_Flop.csv", "./files/NB_Turn.csv", "./files/NB_River.csv");
		if (AI1.equals("QL")) 
		{
			QL = newQL();	
			boolean validCommand = false;
			Scanner scanner = new Scanner(System.in);
			while (!validCommand)
			{
				System.out.println("[AI #1] Do you want to load the Q and N functions of a previously saved QL model?");
				System.out.print("Enter Command ('Y', 'N'): ");
				String input = scanner.nextLine();
				if (input.toUpperCase().equals("Y"))
				{
					System.out.print("Enter the name of the save file (no extension): ");
					String fileName = scanner.nextLine();
					QL.loadModel(fileName);
					validCommand = true;
				}
				else if (input.toUpperCase().equals("N"))
				{
					validCommand = true;
				}
			}
			
			validCommand = false;
			while (!validCommand)
			{
				System.out.println("[AI #1] Do you want to save the Q and N functions of the QL model after the completion of " + games + " games?");
				System.out.print("Enter Command ('Y', 'N'): ");
				String input = scanner.nextLine();
				if (input.toUpperCase().equals("Y"))
				{
					System.out.print("[AI #1] Enter the name of the save file (no extension): ");
					fileName1 = scanner.nextLine();
					validCommand = true;
				}
				else if (input.toUpperCase().equals("N"))
				{
					validCommand = true;
				}
			}
		}
		if (AI2.equals("NB")) NB2 = new NB_AI("./files/NB_Flop.csv", "./files/NB_Turn.csv", "./files/NB_River.csv");
		if (AI2.equals("QL")) 
		{
			QL2 = newQL();
			boolean validCommand = false;
			Scanner scanner = new Scanner(System.in);
			while (!validCommand)
			{
				System.out.println("[AI #2] Do you want to load the Q and N functions of a previously saved QL model?");
				System.out.print("Enter Command ('Y', 'N'): ");
				String input = scanner.nextLine();
				if (input.toUpperCase().equals("Y"))
				{
					System.out.print("Enter the name of the save file (no extension): ");
					String fileName = scanner.nextLine();
					QL2.loadModel(fileName);
					validCommand = true;
				}
				else if (input.toUpperCase().equals("N"))
				{
					validCommand = true;
				}
			}
			
			validCommand = false;
			while (!validCommand)
			{
				System.out.println("[AI #2] Do you want to save the Q and N functions of the QL model after the completion of " + games + " games?");
				System.out.print("Enter Command ('Y', 'N'): ");
				String input = scanner.nextLine();
				if (input.toUpperCase().equals("Y"))
				{
					System.out.print("[AI #2] Enter the name of the save file (no extension): ");
					fileName2 = scanner.nextLine();
					validCommand = true;
				}
				else if (input.toUpperCase().equals("N"))
				{
					validCommand = true;
				}
			}
		}
		
		int AI1_wins = 0;
		int AI1_losses = 0;
		int AI1_draws = 0;
		double AI1_win_percentage = 0;
		int AI1_moneyGained = 0;
		double AI1_moneyGained_avg = 0;
		int AI2_wins = 0;
		int AI2_losses = 0;
		int AI2_draws = 0;
		double AI2_win_percentage = 0;
		int AI2_moneyGained = 0;
		double AI2_moneyGained_avg = 0;	
		int i = 1;
		GameSetup setup;
		
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(3);
		
		try {
			FileWriter csvWriter = new FileWriter("./files/" + csv);
			csvWriter.append("Game Number");
			csvWriter.append(",");
			csvWriter.append("AI1 [" + AI1 + "] Wins");
			csvWriter.append(",");
			csvWriter.append("AI1 [" + AI1 + "] Losses");
			csvWriter.append(",");
			csvWriter.append("AI1 [" + AI1 + "] Draws");
			csvWriter.append(",");
			csvWriter.append("AI1 [" + AI1 + "] Win Percentage");
			csvWriter.append(",");
			csvWriter.append("AI1 [" + AI1 + "] Cumulative Game Reward");
			csvWriter.append(",");
			csvWriter.append("AI1 [" + AI1 + "] Reward/Game Average");
			csvWriter.append(",");
			csvWriter.append("AI2 [" + AI2 + "] Wins");
			csvWriter.append(",");
			csvWriter.append("AI2 [" + AI2 + "] Losses");
			csvWriter.append(",");
			csvWriter.append("AI2 [" + AI2 + "] Draws");
			csvWriter.append(",");
			csvWriter.append("AI2 [" + AI2 + "] Win Percentage");
			csvWriter.append(",");
			csvWriter.append("AI2 [" + AI2 + "] Cumulative Game Reward");
			csvWriter.append(",");
			csvWriter.append("AI2 [" + AI2 + "] Reward/Game Average\n");
			
			csvWriter.flush();
			csvWriter.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
				
		while (i <= games)
		{
			if (this.dealer.game == null)
			{									
				int balance = 1000;
				int bigBlind = 200;
				int smallBlind = 100;
				setup = this.dealer.newGame(balance, bigBlind, smallBlind);			
			}
			else setup = this.dealer.nextGame();
			
			if (AI1.equals("NB")) NB.newGame(Player.P1, setup.hands.get(1), setup.game);
			else if (AI1.equals("QL")) QL.newGame(Player.P1, setup.hands.get(1), setup.game);
			
			if (AI2.equals("NB")) NB2.newGame(Player.P2, setup.hands.get(1), setup.game);
			else if (AI2.equals("QL")) QL2.newGame(Player.P2, setup.hands.get(1), setup.game);
		
			
			// Dealer pays small blind
			if (setup.game.smallBlind >= setup.game.balances.get(setup.game.dealer))
			{
				setup.game.pot += setup.game.balances.get(setup.game.dealer);
				setup.game.gameTotalBets.put(setup.game.dealer, setup.game.gameTotalBets.get(setup.game.dealer) + setup.game.balances.get(setup.game.dealer));
				setup.game.currentBet = setup.game.balances.get(setup.game.dealer);
				setup.game.lastStageTotalBets.put(setup.game.dealer, setup.game.currentBet);
				setup.game.currentStage = Stage.SHOWDOWN;
				setup.game.lastStageAction.put(setup.game.dealer, new ActionStage(new Action(Move.ALL_IN, setup.game.dealer, setup.game.balances.get(setup.game.dealer)), Stage.PREFLOP));
				setup.game.balances.put(setup.game.dealer, 0);
			}
			else 
			{
				setup.game.pot += setup.game.smallBlind;
				setup.game.gameTotalBets.put(setup.game.dealer, setup.game.gameTotalBets.get(setup.game.dealer) + setup.game.smallBlind);
				setup.game.currentBet = setup.game.smallBlind;
				setup.game.lastStageTotalBets.put(setup.game.dealer, setup.game.smallBlind);
				setup.game.balances.put(setup.game.dealer, setup.game.balances.get(setup.game.dealer) - setup.game.smallBlind);
				setup.game.lastStageAction.put(setup.game.dealer, new ActionStage(new Action(Move.BLIND, setup.game.dealer, setup.game.smallBlind), Stage.PREFLOP));
			}

			// Dealer has no money left
			if (setup.game.currentStage == Stage.SHOWDOWN)
			{
				if (setup.game.balances.get(setup.game.opponent) > setup.game.currentBet)
				{
					setup.game.balances.put(setup.game.opponent, setup.game.balances.get(setup.game.opponent) - setup.game.currentBet);
					setup.game.lastStageTotalBets.put(setup.game.opponent, setup.game.currentBet);
					setup.game.lastStageAction.put(setup.game.opponent, new ActionStage(new Action(Move.CALL, setup.game.opponent, setup.game.currentBet), Stage.PREFLOP));
					setup.game.pot += setup.game.currentBet;
					setup.game.gameTotalBets.put(setup.game.opponent, setup.game.gameTotalBets.get(setup.game.opponent) + setup.game.currentBet);
				}
				else 
				{
					setup.game.pot += setup.game.balances.get(setup.game.opponent);
					setup.game.gameTotalBets.put(setup.game.opponent, setup.game.gameTotalBets.get(setup.game.opponent) + setup.game.balances.get(setup.game.opponent));
					setup.game.lastStageTotalBets.put(setup.game.opponent, setup.game.balances.get(setup.game.opponent));
					setup.game.lastStageAction.put(setup.game.opponent, new ActionStage(new Action(Move.ALL_IN, setup.game.opponent, setup.game.balances.get(setup.game.opponent)), Stage.PREFLOP));
					setup.game.balances.put(setup.game.opponent, 0);
				}
			}
			// Dealer has money left
			else 
			{
				// Opponent pays big blind
				if (setup.game.bigBlind >= setup.game.balances.get(setup.game.opponent))
				{
					setup.game.pot += setup.game.balances.get(setup.game.opponent);
					setup.game.gameTotalBets.put(setup.game.opponent, setup.game.gameTotalBets.get(setup.game.opponent) + setup.game.balances.get(setup.game.opponent));
					setup.game.lastStageTotalBets.put(setup.game.opponent, setup.game.balances.get(setup.game.opponent));
					setup.game.currentBet = Math.max(setup.game.currentBet, setup.game.balances.get(setup.game.opponent));
					setup.game.lastStageAction.put(setup.game.opponent, new ActionStage(new Action(Move.ALL_IN, setup.game.opponent, setup.game.balances.get(setup.game.opponent)), Stage.PREFLOP));
					setup.game.balances.put(setup.game.opponent, 0);
					setup.game.currentStage = Stage.SHOWDOWN;
				}
				else 
				{
					setup.game.pot += setup.game.bigBlind;
					setup.game.gameTotalBets.put(setup.game.opponent, setup.game.gameTotalBets.get(setup.game.opponent) + setup.game.bigBlind);
					setup.game.lastStageTotalBets.put(setup.game.opponent, setup.game.bigBlind);
					setup.game.currentBet = setup.game.bigBlind;
					setup.game.balances.put(setup.game.opponent, setup.game.balances.get(setup.game.opponent) - setup.game.bigBlind);
					setup.game.lastStageAction.put(setup.game.opponent, new ActionStage(new Action(Move.BLIND, setup.game.opponent, setup.game.bigBlind), Stage.PREFLOP));
				}	
			}
						
			while (setup.game.currentStage != Stage.SHOWDOWN)
			{
				// AI1's turn
				if (setup.game.currentTurn == Player.P1)
				{
					if (AI1.equals("NB"))
					{
						List<Action> actions = NB.actions();					
						for (int j = 0; j < actions.size(); j++)
						{
							Action action = actions.get(j);
							if (this.isValidAction(action, setup.game) && 
									this.dealer.evaluateAction(new ActionStage(action, setup.game.currentStage))) break;							
						}
						
					}
					else if (AI1.equals("QL"))
					{
						QLearningActions QLactions = QL.actions();
						for (int j = 0; j < QLactions.actions.size(); j++)
						{
							Action action = QLactions.actions.get(j);
							if (this.isValidAction(action, setup.game) && 
									this.dealer.evaluateAction(new ActionStage(action, setup.game.currentStage))) 
							{								
								if (setup.game.currentStage != Stage.SHOWDOWN) QL.updateFunctions(QLactions.state, action, false, false);
								else if (setup.game.currentStage == Stage.SHOWDOWN)
								{
									Outcome outcome = this.dealer.outcome(setup.hands.get(0), setup.hands.get(1));
									switch (outcome.status)
									{
									case P1_WINS:
										QL.updateFunctions(QLactions.state, action, false, true);
										break;
									case P2_WINS:
										QL.updateFunctions(QLactions.state, action, true, false);
										break;
									case DRAW:
										QL.updateFunctions(QLactions.state, action, true, false);
										break;
									}
								}
							}							
						}
					}
					else if (AI1.equals("EV"))
					{
						List<Action> actions = null;
						if (setup.game.currentStage == Stage.PREFLOP) actions = EV_AI.actionsPreFlop(setup.hands.get(0), setup.game);
						else if (setup.game.currentStage == Stage.FLOP) actions = EV_AI.optimalEquityMoveFlop(setup.hands.get(0), setup.hands.get(1), setup.game, dealer.deck);
						else if (setup.game.currentStage == Stage.TURN) actions = EV_AI.optimalEquityMoveTurn(setup.hands.get(0), setup.hands.get(1), setup.game, dealer.deck);
						else if (setup.game.currentStage == Stage.RIVER) actions = EV_AI.optimalEquityMoveRiver(setup.hands.get(0), setup.hands.get(1), setup.game);
						for (int j = 0; j <actions.size(); j++)
						{
							Action action = actions.get(j);
							if (this.isValidAction(action, setup.game) && 
									this.dealer.evaluateAction(new ActionStage(action, setup.game.currentStage))) break;
						}
					}
					else if (AI1.equals("RANDOM"))
					{
						List<Action> actions = Random_AI.actions(setup.game);						
						for (int j = 0; j <actions.size(); j++)
						{
							Action action = actions.get(j);
							if (this.isValidAction(action, setup.game) && 
									this.dealer.evaluateAction(new ActionStage(action, setup.game.currentStage))) break;							
						}
					}
					
				}
				// AI2's turn
				else
				{
					if (AI2.equals("NB"))
					{
						List<Action> actions = NB2.actions();					
						for (int j = 0; j < actions.size(); j++)
						{
							Action action = actions.get(j);
							if (this.isValidAction(action, setup.game) && 
									this.dealer.evaluateAction(new ActionStage(action, setup.game.currentStage))) break;
						}
						
					}
					else if (AI2.equals("QL"))
					{
						QLearningActions QLactions = QL2.actions();
						for (int j = 0; j < QLactions.actions.size(); j++)
						{
							Action action = QLactions.actions.get(j);
							if (this.isValidAction(action, setup.game) && 
									this.dealer.evaluateAction(new ActionStage(action, setup.game.currentStage))) 
							{								
								if (setup.game.currentStage != Stage.SHOWDOWN) QL2.updateFunctions(QLactions.state, action, false, false);
								else if (setup.game.currentStage == Stage.SHOWDOWN)
								{
									Outcome outcome = this.dealer.outcome(setup.hands.get(0), setup.hands.get(1));
									switch (outcome.status)
									{
									case P1_WINS:
										QL2.updateFunctions(QLactions.state, action, false, true);
										break;
									case P2_WINS:
										QL2.updateFunctions(QLactions.state, action, true, false);
										break;
									case DRAW:
										QL2.updateFunctions(QLactions.state, action, true, false);
										break;
									}
								}
							}							
						}
					}
					else if (AI2.equals("EV"))
					{
						List<Action> actions = null;
						if (setup.game.currentStage == Stage.PREFLOP) actions = EV_AI.actionsPreFlop(setup.hands.get(1), setup.game);
						else if (setup.game.currentStage == Stage.FLOP) actions = EV_AI.optimalEquityMoveFlop(setup.hands.get(0), setup.hands.get(1), setup.game, dealer.deck);
						else if (setup.game.currentStage == Stage.TURN) actions = EV_AI.optimalEquityMoveTurn(setup.hands.get(0), setup.hands.get(1), setup.game, dealer.deck);
						else if (setup.game.currentStage == Stage.RIVER) actions = EV_AI.optimalEquityMoveRiver(setup.hands.get(0), setup.hands.get(1), setup.game);
						for (int j = 0; j <actions.size(); j++)
						{
							Action action = actions.get(j);
							if (this.isValidAction(action, setup.game) && 
									this.dealer.evaluateAction(new ActionStage(action, setup.game.currentStage))) break;
						}
					}
					else if (AI2.equals("RANDOM"))
					{
						List<Action> actions = Random_AI.actions(setup.game);						
						for (int j = 0; j <actions.size(); j++)
						{
							Action action = actions.get(j);
							if (this.isValidAction(action, setup.game) && 
									this.dealer.evaluateAction(new ActionStage(action, setup.game.currentStage))) break;							
						}
					}
				}
				
			}
			
			Outcome outcome = this.dealer.outcome(setup.hands.get(0), setup.hands.get(1));
			switch (outcome.status)
			{
			case P1_WINS:
				System.out.println("Game " + i + ": AI1 [" + AI1 + "] wins!");
				AI1_wins++;
				AI2_losses++;
				break;
			case P2_WINS:
				AI1_losses++;
				AI2_wins++;
				System.out.println("Game " + i + ": AI2 [" + AI2 + "] wins!");
				break;
			case DRAW:
				AI1_draws++;
				AI2_draws++;
				System.out.println("Game " + i + ": Draw!");
				break;
			}
			setup.game.balances.put(Player.P1, setup.game.balances.get(Player.P1) + outcome.P1Reward);
			setup.game.balances.put(Player.P2, setup.game.balances.get(Player.P2) + outcome.P2Reward);
			

			AI1_win_percentage = (double) AI1_wins / (double) i;
			AI1_moneyGained += setup.game.balances.get(Player.P1) - 1000;
			AI1_moneyGained_avg = (double) AI1_moneyGained/ (double) i;
			AI2_win_percentage = (double) AI2_wins / (double) i;
			AI2_moneyGained += setup.game.balances.get(Player.P2) - 1000;
			AI2_moneyGained_avg = (double) AI2_moneyGained/ (double) i;
			
			try {
				FileWriter csvWriter = new FileWriter("./files/" + csv, true);
				csvWriter.append(i + "," +
						AI1_wins + "," + 
						AI1_losses + "," +
						AI1_draws + "," + 
						df.format(AI1_win_percentage) + "," +
						AI1_moneyGained + "," +
						df.format(AI1_moneyGained_avg) + "," +
						AI2_wins + "," + 
						AI2_losses + "," +
						AI2_draws + "," + 
						df.format(AI2_win_percentage) + "," +
						AI2_moneyGained + "," +
						df.format(AI2_moneyGained_avg) + "\n");
				
				csvWriter.flush();
				csvWriter.close();
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			i++;
			this.dealer = new Dealer();
							
		}
		
		if (fileName1 != null) QL.saveModel(fileName1);
		if (fileName2 != null) QL.saveModel(fileName2);
	}
	
	
	public void Start()
	{
		Scanner scanner = new Scanner(System.in);
		switch (this.type)
		{
		case HumanVsHuman:
			this.humanVsHuman();
			break;
		case HumanVsAI:
			System.out.println("Please enter type of AI to play against ('NB', 'QL', 'EV', 'Random')");
			String AI = scanner.next();
			while (!(AI.toUpperCase().equals("NB") || AI.toUpperCase().equals("QL") || AI.toUpperCase().equals("EV") || AI.toUpperCase().equals("RANDOM")))
			{
				System.out.println("Please enter type of AI to play against ('NB', 'QL', 'EV', 'Random')");
				AI = scanner.next();
			}
			this.humanVsAI(AI.toUpperCase());
			break;
		// AIVsAI
		default:
			System.out.print("Please enter AI type #1 ('NB', 'QL', 'EV', 'Random'):  ");
			String AI1 = scanner.next();
			while (!(AI1.toUpperCase().equals("NB") || AI1.toUpperCase().equals("QL") || AI1.toUpperCase().equals("EV") || AI1.toUpperCase().equals("RANDOM")))
			{
				System.out.print("Please enter AI type #1 ('NB', 'QL', 'EV', 'Random'):  ");
				AI1 = scanner.next();
			}
			System.out.print("Please enter AI type #2 ('NB', 'QL', 'EV', 'Random'):  ");
			String AI2 = scanner.next();
			while (!(AI2.toUpperCase().equals("NB") || AI2.toUpperCase().equals("QL") || AI2.toUpperCase().equals("EV") || AI2.toUpperCase().equals("RANDOM")))
			{
				System.out.print("Please enter AI type #2 ('NB', 'QL', 'EV', 'Random'):  ");
				AI2 = scanner.next();
			}
			int games = 0;
			boolean validGames = false;
			while (!validGames)
			{
				System.out.print("Please enter how many games to play:  ");
				String input = scanner.nextLine();
				if (input.matches("[0-9]+")) 
				{
					games = Integer.parseInt(input);
					validGames = true;
				}
			}
			String csv = "";
			boolean validCSV = false;
			while (!validCSV)
			{
				System.out.print("Please enter how you want to name the output CSV file (with .csv extension):  ");
				String input = scanner.nextLine();
				if (input.length() > 0) 
				{
					csv = input;
					validCSV = true;
				}
			}
			this.AIVsAI(AI1.toUpperCase(), AI2.toUpperCase(), games, csv);
			break;
		}
		scanner.close();
	}
}
