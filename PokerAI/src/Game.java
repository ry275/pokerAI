import java.util.*;


public class Game {
	
	public List<Card> cardsFaceUp;
	public int pot;
	public int currentBet;
	public Player currentTurn;
	public Player dealer;
	public Player opponent;
	public Status status;
	public Stage currentStage;
	public List<ActionStage> history;
	public Map<Player, ActionStage> lastStageAction;
	public Map<Player, Integer> lastStageTotalBets;
	public Map<Player, Integer> allInBets;
	public Map<Player, Integer> balances;
	public Map<Player, Integer> gameTotalBets;
	public int bigBlind;
	public int smallBlind;
	
	public Game(int startingBalance, int bigBlind, int smallBlind)
	{
		this.cardsFaceUp = new ArrayList<Card>();
		this.history = new ArrayList<ActionStage>();
		this.lastStageAction = new HashMap<Player, ActionStage>();
		this.pot = 0;
		this.currentBet = 0;
		this.allInBets = new HashMap<Player, Integer>();
		this.bigBlind = bigBlind;
		this.smallBlind = smallBlind;
		this.lastStageTotalBets = new HashMap<Player, Integer>();
		this.gameTotalBets = new HashMap<Player, Integer>();
		resetBoard();		
		this.balances = new HashMap<Player, Integer>();
		for (Player p : Player.values())
		{
			this.balances.put(p, startingBalance);
		}
	}
	
	public void resetBoard()
	{
		this.cardsFaceUp.clear();
		this.pot = 0;
		this.currentBet = 0;
		Random rn = new Random();
		this.currentTurn = rn.nextInt(2 - 1 + 1) + 1 == 1 ? Player.P1 : Player.P2;
		this.dealer = this.currentTurn;
		
		if (this.currentTurn == Player.P1) this.opponent = Player.P2;
		else this.opponent = Player.P1;
		
		this.status = Status.RESET;
		this.currentStage = Stage.PREFLOP;
		this.history.clear();
		this.lastStageAction.clear();
		for (Player p : Player.values())
		{
			this.lastStageAction.put(p, null);
			this.lastStageTotalBets.put(p,  0);
			this.allInBets.put(p,  null);		
			this.gameTotalBets.put(p,  0);
		}
	}
}
