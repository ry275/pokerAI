import java.io.File;
import HandEvaluator.*;
import java.util.*;

public class NB_AI {
	private Player player;
	private Hand hand;
	private Game game;
	private double[][] NB_position;
	private double[][] NB_previousAction;
	private double[][] NB_strength;
	private double[][] NB_action;
	private double[][] NB_label;

	public NB_AI(String NB_flopDataAddress, 
			String NB_turnDataAddress, 
			String NB_riverDataAddress)
	{
		this.NB_position = new double[2][6];
		this.NB_previousAction = new double[2][6];
		this.NB_strength = new double[10][6];
		this.NB_action = new double[4][6];
		this.NB_label = new double[2][3];
		
		try 
		{
			for (int i = 0; i < 5; i += 2)
			{
				Scanner scanner;
				if (i == 0) scanner = new Scanner(new File(NB_flopDataAddress));
				else if (i == 2) scanner = new Scanner(new File(NB_turnDataAddress));
				else scanner = new Scanner(new File(NB_riverDataAddress));
				
				String header = scanner.nextLine();
				String[] line = new String[5];
		
				int wins = 0;
				int losses = 0;
				while (scanner.hasNextLine())
				{
					line = scanner.nextLine().split(",");
					int label;
					if (Integer.parseInt(line[4]) >= 0) 
					{
						wins++;
						label = i + 1;
					}
					else 
					{
						losses++;
						label = i;
					}
					
					if (line[0].toUpperCase().equals("SB")) this.NB_position[0][label]++;
					else this.NB_position[1][label]++;		
					if (line[1].equals("0")) this.NB_previousAction[0][label]++;
					else this.NB_previousAction[1][label]++;
					
					switch(line[2].toUpperCase())
					{
					case "ROYAL FLUSH":
						this.NB_strength[9][label]++;
						break;
					case "STRAIGHT_FLUSH":
						this.NB_strength[8][label]++;
						break;
					case "FOUR_OF_A_KIND":
						this.NB_strength[7][label]++;
						break;
					case "FULL_HOUSE":
						this.NB_strength[6][label]++;
						break;
					case "FLUSH":
						this.NB_strength[5][label]++;
						break;
					case "STRAIGHT":
						this.NB_strength[4][label]++;
						break;
					case "THREE_OF_A_KIND":
						this.NB_strength[3][label]++;
						break;
					case "TWO_PAIR":
						this.NB_strength[2][label]++;
						break;
					case "ONE_PAIR":
						this.NB_strength[1][label]++;
						break;
					default:
						this.NB_strength[0][label]++;
						break;
					}
					
					String action = line[3].toUpperCase();
					if (action.equals("FOLD")) this.NB_action[0][label]++;
					else if (action.equals("CHECK")) this.NB_action[1][label]++;
					else if (action.equals("CALL")) this.NB_action[2][label]++;
					else if (action.equals("RAISE")) this.NB_action[3][label]++;	
				}					
				scanner.close();
				
				// calculate the conditional probabilities 
				double total = wins + losses;
				int stage = i / 2;
				this.NB_label[0][stage] = losses / total;
				this.NB_label[1][stage] = wins / total;
				 
				total = this.NB_position[0][i] + this.NB_position[1][i];
				this.NB_position[0][i] = this.NB_position[0][i] / total;
				this.NB_position[1][i] = this.NB_position[1][i] / total;
				total = this.NB_position[0][i + 1] + this.NB_position[1][i + 1];
				this.NB_position[0][i + 1] = this.NB_position[0][i + 1] / total;
				this.NB_position[1][i + 1] = this.NB_position[1][i + 1] / total;
				
				total = this.NB_previousAction[0][i] + this.NB_previousAction[1][i];
				this.NB_previousAction[0][i] = this.NB_previousAction[0][i] / total;
				this.NB_previousAction[1][i] = this.NB_previousAction[1][i] / total;
				total = this.NB_previousAction[0][i + 1] + this.NB_previousAction[1][i + 1];
				this.NB_previousAction[0][i + 1] = this.NB_previousAction[0][i + 1] / total;
				this.NB_previousAction[1][i + 1] = this.NB_previousAction[1][i + 1] / total;
				
				for (int l = 0; l < 2; l++)
				{
					total = 0;
					for (int str = 0; str < 10; str++)
					{
						total += this.NB_strength[str][i + l];
					}
					for (int str = 0; str < 10; str++)
					{
						this.NB_strength[str][i + l] = this.NB_strength[str][i + l] / total;
					}
				}
				
				for (int l = 0; l < 2; l++)
				{
					total = 0;
					for (int act = 0; act < 4; act++)
					{
						total += this.NB_action[act][i + l];
					}
					for (int act = 0; act < 4; act++)
					{
						this.NB_action[act][i + l] = this.NB_action[act][i + l] / total;
					}
				}				
			}
		}
		catch (Exception e)
		{
			System.out.println(e);
		}				
	}
	
	public void newGame(Player player, Hand hand, Game game)
	{
		this.player = player;
		this.hand = hand;
		this.game = game;
	}
	
	public List<Action> actionsPreflop()
	{
		if (this.game.currentStage != Stage.PREFLOP) return null;
		List<Action> actions = new ArrayList<Action>();
		
		int[] features = Util_AI.preflopFeatures(this.game, this.hand);
		int sum = features[0] + features[1] + features[2];
		if (sum == 3 || sum == 2) 
		{
			actions.add(new Action(Move.RAISE, this.player, this.game.currentBet));
			actions.add(new Action(Move.CALL, this.player, this.game.currentBet - this.game.lastStageTotalBets.get(this.player)));
			actions.add(new Action(Move.CHECK, this.player, 0));
			actions.add(new Action(Move.FOLD, this.player, 0));
		}
		else if (sum == 1) 
		{
			actions.add(new Action(Move.CALL, this.player, this.game.currentBet - this.game.lastStageTotalBets.get(this.player)));
			actions.add(new Action(Move.CHECK, this.player, 0));
			actions.add(new Action(Move.FOLD, this.player, 0));
		}
		else
		{
			actions.add(new Action(Move.CHECK, this.player, 0));
			actions.add(new Action(Move.FOLD, this.player, 0));
		}
		
		return actions;
	}
	
	public List<Action> actionsFlopTurnRiver()
	{
		if (game.currentStage == Stage.PREFLOP || game.currentStage == Stage.SHOWDOWN) return null;
		int[] features = Util_AI.flopTurnRiverFeatures(game, this.hand, this.player);
		int position = features[0];
		int previousAction = features[1];
		int strength = features[2];
		
		double[][] condProbs = new double[4][2];
		condProbs[0][1] = 0;
		condProbs[1][1] = 1;
		condProbs[2][1] = 2;
		condProbs[3][1] = 3;
		
		int stage;
		if (game.currentStage == Stage.FLOP) stage = 0;
		else if (game.currentStage == Stage.TURN) stage = 2;
		else stage = 4;
		condProbs[0][0] = (this.NB_position[position][stage + 1] * this.NB_previousAction[previousAction][stage + 1] *
				this.NB_strength[strength][stage + 1] * this.NB_action[0][stage + 1] * this.NB_label[1][stage / 2]) -
				(this.NB_position[position][stage] * this.NB_previousAction[previousAction][stage] *
				this.NB_strength[strength][stage] * this.NB_action[0][stage] * this.NB_label[0][stage / 2]);
		condProbs[1][0] = (this.NB_position[position][stage + 1] * this.NB_previousAction[previousAction][stage + 1] *
				this.NB_strength[strength][stage + 1] * this.NB_action[1][stage + 1] * this.NB_label[1][stage / 2]) - 
				(this.NB_position[position][stage] * this.NB_previousAction[previousAction][stage] *
						this.NB_strength[strength][stage] * this.NB_action[1][stage] * this.NB_label[0][stage / 2]);
		condProbs[2][0] = (this.NB_position[position][stage + 1] * this.NB_previousAction[previousAction][stage + 1] *
				this.NB_strength[strength][stage + 1] * this.NB_action[2][stage + 1] * this.NB_label[1][stage / 2]) - 
				this.NB_position[position][stage] * this.NB_previousAction[previousAction][stage] *
				this.NB_strength[strength][stage] * this.NB_action[2][stage] * this.NB_label[0][stage / 2];
		condProbs[3][0] = (this.NB_position[position][stage + 1] * this.NB_previousAction[previousAction][stage + 1] *
				this.NB_strength[strength][stage + 1] * this.NB_action[3][stage + 1] * this.NB_label[1][stage / 2]) - 
				this.NB_position[position][stage] * this.NB_previousAction[previousAction][stage] *
				this.NB_strength[strength][stage] * this.NB_action[3][stage] * this.NB_label[0][stage / 2];
		
		Arrays.sort(condProbs, new Comparator<double[]>() {
			public int compare(double[] a, double[] b)
			{
				return -1 * Double.compare(a[0], b[0]);
			}
		});
		
		List<Action> actions = new ArrayList<Action>();
		
		for (int i = 0; i < 4; i++)
		{
			if (condProbs[i][1] == 0) actions.add(new Action(Move.FOLD, this.player, 0));
			else if (condProbs[i][1] == 1) actions.add(new Action(Move.CHECK, this.player, 0));
			else if (condProbs[i][1] == 2) actions.add(new Action(Move.CALL, this.player, game.currentBet - game.lastStageTotalBets.get(this.player)));
			else if (condProbs[i][1] == 3) actions.add(new Action(Move.RAISE, this.player, game.currentBet));
		}
		
		return actions;
	}
	
	// Returned list is sorted. The actions with the highest chances of winning are first
	public List<Action> actions()
	{
		if (this.game.currentStage == Stage.PREFLOP) return this.actionsPreflop();
		else if (this.game.currentStage != Stage.SHOWDOWN) return this.actionsFlopTurnRiver();
		else return null;
	}
}
