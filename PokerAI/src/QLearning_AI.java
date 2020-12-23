import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;
import HandEvaluator.*;
import HandEvaluator.*;

/*
   State space abstraction:
   
   * (Preflop, Pair?, Matching Suits?, High Card > 8?)
   * (Flop, Strength, Small Blind?, Previous Street Aggressor?) 
   * (Turn, Strength, Small Blind?, Previous Street Aggressor?)
   * (River, Strength, Small Blind?, Previous Street Aggressor?)
   * WIN
   * LOSE  
   
   Reward function:
   * R(s,a,s') = winReward whenever s' = (WIN)
   * R(s,a,s') = loseReward whenever s' = (LOSE)
   * R(s,a,s') = otherReward for all other s'
  
 */

public class QLearning_AI {
	private Player player;
	private Hand hand;
	private Game game;
	private Map<String, double[]> QFunction;
	private Map<String, int[]> NFunction;
	private double winReward;
	private double loseReward;
	private double otherReward;
	private double alpha; 
	private double gamma;
	private int N;
	public QLearning_AI(double winReward, double loseReward, double otherReward, double alpha, double gamma, int N)
	{
		this.QFunction = new HashMap<String, double[]>();	
		this.NFunction = new HashMap<String, int[]>();
		this.winReward = winReward;
		this.loseReward = loseReward;
		this.otherReward = otherReward;
		this.alpha = alpha;
		this.gamma = gamma;
		this.N = N;
		
		QFunction.put("PREFLOP", new double[4]);
		QFunction.put("PREFLOP, PAIR", new double[4]);
		QFunction.put("PREFLOP, MATCHING", new double[4]);
		QFunction.put("PREFLOP, HIGH", new double[4]);
		QFunction.put("PREFLOP, PAIR, MATCHING", new double[4]);
		QFunction.put("PREFLOP, PAIR, HIGH", new double[4]);
		QFunction.put("PREFLOP, MATCHING, HIGH", new double[4]);
		QFunction.put("PREFLOP, PAIR, MATCHING, HIGH", new double[4]);
			
		QFunction.put("FLOP, 0", new double[4]);
		QFunction.put("FLOP, 0, SB", new double[4]);
		QFunction.put("FLOP, 0, AGG", new double[4]);
		QFunction.put("FLOP, 0, SB, AGG", new double[4]);
		
		QFunction.put("FLOP, 1", new double[4]);
		QFunction.put("FLOP, 1, SB", new double[4]);
		QFunction.put("FLOP, 1, AGG", new double[4]);
		QFunction.put("FLOP, 1, SB, AGG", new double[4]);
		
		QFunction.put("FLOP, 2", new double[4]);
		QFunction.put("FLOP, 2, SB", new double[4]);
		QFunction.put("FLOP, 2, AGG", new double[4]);
		QFunction.put("FLOP, 2, SB, AGG", new double[4]);
		
		QFunction.put("FLOP, 3", new double[4]);
		QFunction.put("FLOP, 3, SB", new double[4]);
		QFunction.put("FLOP, 3, AGG", new double[4]);
		QFunction.put("FLOP, 3, SB, AGG", new double[4]);
		
		QFunction.put("FLOP, 4", new double[4]);
		QFunction.put("FLOP, 4, SB", new double[4]);
		QFunction.put("FLOP, 4, AGG", new double[4]);
		QFunction.put("FLOP, 4, SB, AGG", new double[4]);
		
		QFunction.put("FLOP, 5", new double[4]);
		QFunction.put("FLOP, 5, SB", new double[4]);
		QFunction.put("FLOP, 5, AGG", new double[4]);
		QFunction.put("FLOP, 5, SB, AGG", new double[4]);
		
		QFunction.put("FLOP, 6", new double[4]);
		QFunction.put("FLOP, 6, SB", new double[4]);
		QFunction.put("FLOP, 6, AGG", new double[4]);
		QFunction.put("FLOP, 6, SB, AGG", new double[4]);
		
		QFunction.put("FLOP, 7", new double[4]);
		QFunction.put("FLOP, 7, SB", new double[4]);
		QFunction.put("FLOP, 7, AGG", new double[4]);
		QFunction.put("FLOP, 7, SB, AGG", new double[4]);
		
		QFunction.put("FLOP, 8", new double[4]);
		QFunction.put("FLOP, 8, SB", new double[4]);
		QFunction.put("FLOP, 8, AGG", new double[4]);
		QFunction.put("FLOP, 8, SB, AGG", new double[4]);
		
		QFunction.put("FLOP, 9", new double[4]);
		QFunction.put("FLOP, 9, SB", new double[4]);
		QFunction.put("FLOP, 9, AGG", new double[4]);
		QFunction.put("FLOP, 9, SB, AGG", new double[4]);
		
		QFunction.put("TURN, 0", new double[4]);
		QFunction.put("TURN, 0, SB", new double[4]);
		QFunction.put("TURN, 0, AGG", new double[4]);
		QFunction.put("TURN, 0, SB, AGG", new double[4]);
		
		QFunction.put("TURN, 1", new double[4]);
		QFunction.put("TURN, 1, SB", new double[4]);
		QFunction.put("TURN, 1, AGG", new double[4]);
		QFunction.put("TURN, 1, SB, AGG", new double[4]);
		
		QFunction.put("TURN, 2", new double[4]);
		QFunction.put("TURN, 2, SB", new double[4]);
		QFunction.put("TURN, 2, AGG", new double[4]);
		QFunction.put("TURN, 2, SB, AGG", new double[4]);
		
		QFunction.put("TURN, 3", new double[4]);
		QFunction.put("TURN, 3, SB", new double[4]);
		QFunction.put("TURN, 3, AGG", new double[4]);
		QFunction.put("TURN, 3, SB, AGG", new double[4]);
		
		QFunction.put("TURN, 4", new double[4]);
		QFunction.put("TURN, 4, SB", new double[4]);
		QFunction.put("TURN, 4, AGG", new double[4]);
		QFunction.put("TURN, 4, SB, AGG", new double[4]);
		
		QFunction.put("TURN, 5", new double[4]);
		QFunction.put("TURN, 5, SB", new double[4]);
		QFunction.put("TURN, 5, AGG", new double[4]);
		QFunction.put("TURN, 5, SB, AGG", new double[4]);
		
		QFunction.put("TURN, 6", new double[4]);
		QFunction.put("TURN, 6, SB", new double[4]);
		QFunction.put("TURN, 6, AGG", new double[4]);
		QFunction.put("TURN, 6, SB, AGG", new double[4]);
		
		QFunction.put("TURN, 7", new double[4]);
		QFunction.put("TURN, 7, SB", new double[4]);
		QFunction.put("TURN, 7, AGG", new double[4]);
		QFunction.put("TURN, 7, SB, AGG", new double[4]);
		
		QFunction.put("TURN, 8", new double[4]);
		QFunction.put("TURN, 8, SB", new double[4]);
		QFunction.put("TURN, 8, AGG", new double[4]);
		QFunction.put("TURN, 8, SB, AGG", new double[4]);
		
		QFunction.put("TURN, 9", new double[4]);
		QFunction.put("TURN, 9, SB", new double[4]);
		QFunction.put("TURN, 9, AGG", new double[4]);
		QFunction.put("TURN, 9, SB, AGG", new double[4]);
		
		QFunction.put("RIVER, 0", new double[4]);
		QFunction.put("RIVER, 0, SB", new double[4]);
		QFunction.put("RIVER, 0, AGG", new double[4]);
		QFunction.put("RIVER, 0, SB, AGG", new double[4]);
		
		QFunction.put("RIVER, 1", new double[4]);
		QFunction.put("RIVER, 1, SB", new double[4]);
		QFunction.put("RIVER, 1, AGG", new double[4]);
		QFunction.put("RIVER, 1, SB, AGG", new double[4]);
		
		QFunction.put("RIVER, 2", new double[4]);
		QFunction.put("RIVER, 2, SB", new double[4]);
		QFunction.put("RIVER, 2, AGG", new double[4]);
		QFunction.put("RIVER, 2, SB, AGG", new double[4]);
		
		QFunction.put("RIVER, 3", new double[4]);
		QFunction.put("RIVER, 3, SB", new double[4]);
		QFunction.put("RIVER, 3, AGG", new double[4]);
		QFunction.put("RIVER, 3, SB, AGG", new double[4]);
		
		QFunction.put("RIVER, 4", new double[4]);
		QFunction.put("RIVER, 4, SB", new double[4]);
		QFunction.put("RIVER, 4, AGG", new double[4]);
		QFunction.put("RIVER, 4, SB, AGG", new double[4]);
		
		QFunction.put("RIVER, 5", new double[4]);
		QFunction.put("RIVER, 5, SB", new double[4]);
		QFunction.put("RIVER, 5, AGG", new double[4]);
		QFunction.put("RIVER, 5, SB, AGG", new double[4]);
		
		QFunction.put("RIVER, 6", new double[4]);
		QFunction.put("RIVER, 6, SB", new double[4]);
		QFunction.put("RIVER, 6, AGG", new double[4]);
		QFunction.put("RIVER, 6, SB, AGG", new double[4]);
		
		QFunction.put("RIVER, 7", new double[4]);
		QFunction.put("RIVER, 7, SB", new double[4]);
		QFunction.put("RIVER, 7, AGG", new double[4]);
		QFunction.put("RIVER, 7, SB, AGG", new double[4]);
		
		QFunction.put("RIVER, 8", new double[4]);
		QFunction.put("RIVER, 8, SB", new double[4]);
		QFunction.put("RIVER, 8, AGG", new double[4]);
		QFunction.put("RIVER, 8, SB, AGG", new double[4]);
		
		QFunction.put("RIVER, 9", new double[4]);
		QFunction.put("RIVER, 9, SB", new double[4]);
		QFunction.put("RIVER, 9, AGG", new double[4]);
		QFunction.put("RIVER, 9, SB, AGG", new double[4]);	
		
		NFunction.put("PREFLOP", new int[4]);
		NFunction.put("PREFLOP, PAIR", new int[4]);
		NFunction.put("PREFLOP, MATCHING", new int[4]);
		NFunction.put("PREFLOP, HIGH", new int[4]);
		NFunction.put("PREFLOP, PAIR, MATCHING", new int[4]);
		NFunction.put("PREFLOP, PAIR, HIGH", new int[4]);
		NFunction.put("PREFLOP, MATCHING, HIGH", new int[4]);
		NFunction.put("PREFLOP, PAIR, MATCHING, HIGH", new int[4]);
			
		NFunction.put("FLOP, 0", new int[4]);
		NFunction.put("FLOP, 0, SB", new int[4]);
		NFunction.put("FLOP, 0, AGG", new int[4]);
		NFunction.put("FLOP, 0, SB, AGG", new int[4]);
		
		NFunction.put("FLOP, 1", new int[4]);
		NFunction.put("FLOP, 1, SB", new int[4]);
		NFunction.put("FLOP, 1, AGG", new int[4]);
		NFunction.put("FLOP, 1, SB, AGG", new int[4]);
		
		NFunction.put("FLOP, 2", new int[4]);
		NFunction.put("FLOP, 2, SB", new int[4]);
		NFunction.put("FLOP, 2, AGG", new int[4]);
		NFunction.put("FLOP, 2, SB, AGG", new int[4]);
		
		NFunction.put("FLOP, 3", new int[4]);
		NFunction.put("FLOP, 3, SB", new int[4]);
		NFunction.put("FLOP, 3, AGG", new int[4]);
		NFunction.put("FLOP, 3, SB, AGG", new int[4]);
		
		NFunction.put("FLOP, 4", new int[4]);
		NFunction.put("FLOP, 4, SB", new int[4]);
		NFunction.put("FLOP, 4, AGG", new int[4]);
		NFunction.put("FLOP, 4, SB, AGG", new int[4]);
		
		NFunction.put("FLOP, 5", new int[4]);
		NFunction.put("FLOP, 5, SB", new int[4]);
		NFunction.put("FLOP, 5, AGG", new int[4]);
		NFunction.put("FLOP, 5, SB, AGG", new int[4]);
		
		NFunction.put("FLOP, 6", new int[4]);
		NFunction.put("FLOP, 6, SB", new int[4]);
		NFunction.put("FLOP, 6, AGG", new int[4]);
		NFunction.put("FLOP, 6, SB, AGG", new int[4]);
		
		NFunction.put("FLOP, 7", new int[4]);
		NFunction.put("FLOP, 7, SB", new int[4]);
		NFunction.put("FLOP, 7, AGG", new int[4]);
		NFunction.put("FLOP, 7, SB, AGG", new int[4]);
		
		NFunction.put("FLOP, 8", new int[4]);
		NFunction.put("FLOP, 8, SB", new int[4]);
		NFunction.put("FLOP, 8, AGG", new int[4]);
		NFunction.put("FLOP, 8, SB, AGG", new int[4]);
		
		NFunction.put("FLOP, 9", new int[4]);
		NFunction.put("FLOP, 9, SB", new int[4]);
		NFunction.put("FLOP, 9, AGG", new int[4]);
		NFunction.put("FLOP, 9, SB, AGG", new int[4]);
		
		NFunction.put("TURN, 0", new int[4]);
		NFunction.put("TURN, 0, SB", new int[4]);
		NFunction.put("TURN, 0, AGG", new int[4]);
		NFunction.put("TURN, 0, SB, AGG", new int[4]);
		
		NFunction.put("TURN, 1", new int[4]);
		NFunction.put("TURN, 1, SB", new int[4]);
		NFunction.put("TURN, 1, AGG", new int[4]);
		NFunction.put("TURN, 1, SB, AGG", new int[4]);
		
		NFunction.put("TURN, 2", new int[4]);
		NFunction.put("TURN, 2, SB", new int[4]);
		NFunction.put("TURN, 2, AGG", new int[4]);
		NFunction.put("TURN, 2, SB, AGG", new int[4]);
		
		NFunction.put("TURN, 3", new int[4]);
		NFunction.put("TURN, 3, SB", new int[4]);
		NFunction.put("TURN, 3, AGG", new int[4]);
		NFunction.put("TURN, 3, SB, AGG", new int[4]);
		
		NFunction.put("TURN, 4", new int[4]);
		NFunction.put("TURN, 4, SB", new int[4]);
		NFunction.put("TURN, 4, AGG", new int[4]);
		NFunction.put("TURN, 4, SB, AGG", new int[4]);
		
		NFunction.put("TURN, 5", new int[4]);
		NFunction.put("TURN, 5, SB", new int[4]);
		NFunction.put("TURN, 5, AGG", new int[4]);
		NFunction.put("TURN, 5, SB, AGG", new int[4]);
		
		NFunction.put("TURN, 6", new int[4]);
		NFunction.put("TURN, 6, SB", new int[4]);
		NFunction.put("TURN, 6, AGG", new int[4]);
		NFunction.put("TURN, 6, SB, AGG", new int[4]);
		
		NFunction.put("TURN, 7", new int[4]);
		NFunction.put("TURN, 7, SB", new int[4]);
		NFunction.put("TURN, 7, AGG", new int[4]);
		NFunction.put("TURN, 7, SB, AGG", new int[4]);
		
		NFunction.put("TURN, 8", new int[4]);
		NFunction.put("TURN, 8, SB", new int[4]);
		NFunction.put("TURN, 8, AGG", new int[4]);
		NFunction.put("TURN, 8, SB, AGG", new int[4]);
		
		NFunction.put("TURN, 9", new int[4]);
		NFunction.put("TURN, 9, SB", new int[4]);
		NFunction.put("TURN, 9, AGG", new int[4]);
		NFunction.put("TURN, 9, SB, AGG", new int[4]);
		
		NFunction.put("RIVER, 0", new int[4]);
		NFunction.put("RIVER, 0, SB", new int[4]);
		NFunction.put("RIVER, 0, AGG", new int[4]);
		NFunction.put("RIVER, 0, SB, AGG", new int[4]);
		
		NFunction.put("RIVER, 1", new int[4]);
		NFunction.put("RIVER, 1, SB", new int[4]);
		NFunction.put("RIVER, 1, AGG", new int[4]);
		NFunction.put("RIVER, 1, SB, AGG", new int[4]);
		
		NFunction.put("RIVER, 2", new int[4]);
		NFunction.put("RIVER, 2, SB", new int[4]);
		NFunction.put("RIVER, 2, AGG", new int[4]);
		NFunction.put("RIVER, 2, SB, AGG", new int[4]);
		
		NFunction.put("RIVER, 3", new int[4]);
		NFunction.put("RIVER, 3, SB", new int[4]);
		NFunction.put("RIVER, 3, AGG", new int[4]);
		NFunction.put("RIVER, 3, SB, AGG", new int[4]);
		
		NFunction.put("RIVER, 4", new int[4]);
		NFunction.put("RIVER, 4, SB", new int[4]);
		NFunction.put("RIVER, 4, AGG", new int[4]);
		NFunction.put("RIVER, 4, SB, AGG", new int[4]);
		
		NFunction.put("RIVER, 5", new int[4]);
		NFunction.put("RIVER, 5, SB", new int[4]);
		NFunction.put("RIVER, 5, AGG", new int[4]);
		NFunction.put("RIVER, 5, SB, AGG", new int[4]);
		
		NFunction.put("RIVER, 6", new int[4]);
		NFunction.put("RIVER, 6, SB", new int[4]);
		NFunction.put("RIVER, 6, AGG", new int[4]);
		NFunction.put("RIVER, 6, SB, AGG", new int[4]);
		
		NFunction.put("RIVER, 7", new int[4]);
		NFunction.put("RIVER, 7, SB", new int[4]);
		NFunction.put("RIVER, 7, AGG", new int[4]);
		NFunction.put("RIVER, 7, SB, AGG", new int[4]);
		
		NFunction.put("RIVER, 8", new int[4]);
		NFunction.put("RIVER, 8, SB", new int[4]);
		NFunction.put("RIVER, 8, AGG", new int[4]);
		NFunction.put("RIVER, 8, SB, AGG", new int[4]);
		
		NFunction.put("RIVER, 9", new int[4]);
		NFunction.put("RIVER, 9, SB", new int[4]);
		NFunction.put("RIVER, 9, AGG", new int[4]);
		NFunction.put("RIVER, 9, SB, AGG", new int[4]);
	}

	public void newGame(Player player, Hand hand, Game game)
	{
		this.player = player;
		this.hand = hand;
		this.game = game;
	}
	
	private String getState()
	{
		String state = "";
		if (this.game.currentStage == Stage.PREFLOP)
		{
			int[] features = Util_AI.preflopFeatures(this.game, this.hand);
			int pair = features[0], matching = features[1], high = features[2];
			state = "PREFLOP";
			if (pair == 1) state += ", PAIR";
			if (matching == 1) state += ", MATCHING";
			if (high == 1) state += ", HIGH";
		}
		else 
		{
			int[] features = Util_AI.flopTurnRiverFeatures(this.game, this.hand, this.player);
			int position = features[0], previousAction = features[1], strength = features[2];
			if (this.game.currentStage == Stage.FLOP) state = "FLOP, " + strength;
			else if (this.game.currentStage == Stage.TURN) state = "TURN, " + strength;
			else if (this.game.currentStage == Stage.RIVER) state = "RIVER, " + strength;
			if (position == 1) state += ", SB";
			if (previousAction == 1) state += ", AGG";
		}
		return state;
	}
	
	// The Action List is ordered from best to worst. 
	public QLearningActions actions()
	{
		if (this.game.currentStage == Stage.SHOWDOWN) return null;
		List<Action> actions = new ArrayList<Action>();
	
		String state = this.getState();
		int[] nCounts = this.NFunction.get(state);
		// Check if we should explore new actions
		for (int i = 0; i < 4; i++)
		{
			if (nCounts[i] < this.N)
			{
				if (i == 0) actions.add(new Action(Move.FOLD, this.player, 0));
				else if (i == 1) actions.add(new Action(Move.CHECK, this.player, 0));
				else if (i == 2) actions.add(new Action(Move.CALL, this.player, game.currentBet - game.lastStageTotalBets.get(this.player)));
				else if (i == 3) actions.add(new Action(Move.RAISE, this.player, game.currentBet));
			}
		}
		// Check which action yields greatest QFunction value
		double[] QValues = this.QFunction.get(state);
		double[][] Qcopy = new double[4][2];
		Qcopy[0][1] = 0;
		Qcopy[1][1] = 1;
		Qcopy[2][1] = 2;
		Qcopy[3][1] = 3;
		
		Qcopy[0][0] = QValues[0];
		Qcopy[1][0] = QValues[1];
		Qcopy[2][0] = QValues[2];
		Qcopy[3][0] = QValues[3];
		
		Arrays.sort(Qcopy, new Comparator<double[]>() {
			public int compare(double[] a, double[] b)
			{
				return -1 * Double.compare(a[0], b[0]);
			}
		});
		
		for (int i = 0; i < 4; i++)
		{
			if (Qcopy[i][1] == 0) actions.add(new Action(Move.FOLD, this.player, 0));
			else if (Qcopy[i][1] == 1) actions.add(new Action(Move.CHECK, this.player, 0));
			else if (Qcopy[i][1] == 2) actions.add(new Action(Move.CALL, this.player, game.currentBet - game.lastStageTotalBets.get(this.player)));
			else if (Qcopy[i][1] == 3) actions.add(new Action(Move.RAISE, this.player, game.currentBet));
		}
		
		return new QLearningActions(actions, state);
	}
	
	// To be called after dealer evaluates the action.
	public void updateFunctions(String state, Action a, boolean win, boolean loss)
	{
		int action = -1;
		if (a.move == Move.FOLD) action = 0;
		if (a.move == Move.CHECK) action = 1;
		if (a.move == Move.CALL) action = 2;
		if (a.move == Move.RAISE) action = 3;
		int[] nCounts = this.NFunction.get(state);
		nCounts[action] += 1;
		double[] QValues = this.QFunction.get(state);
		double r = -1;
		if (win) r = this.winReward;
		else if (loss) r = this.loseReward;
		else 
		{
			double[] nextQValues = this.QFunction.get(this.getState());
			double max = Double.MIN_VALUE;
			for (int i = 0; i < nextQValues.length; i++)
			{
				max = Math.max(max, nextQValues[i]);
			}
			r = this.otherReward + (this.gamma * max);
		}
		QValues[action] = (1 - this.alpha) * QValues[action] + (this.alpha * r);
	}
	
	public void loadModel(String filename)
	{
		File toRead=new File(filename);
		try {
        FileInputStream fis=new FileInputStream(toRead);
        ObjectInputStream ois=new ObjectInputStream(fis);

        Map<String,double[]> QFuncInFile=(Map<String,double[]>)ois.readObject();
        Map<String, int[]> NFuncInFile = (Map<String, int[]>)ois.readObject();
        this.QFunction = QFuncInFile;
        this.NFunction = NFuncInFile;

        ois.close();
        fis.close();   
		}
        catch(Exception e) {}
    } 
	
	public void saveModel(String filename)
	{
		File saveFile = new File(filename);
		try {
			FileOutputStream fos = new FileOutputStream(saveFile);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(this.QFunction);
			oos.writeObject(this.NFunction);
			oos.flush();
			oos.close();
			fos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
}
