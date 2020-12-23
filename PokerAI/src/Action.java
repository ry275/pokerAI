public class Action {
	public final Move move;
	public final Player player;
	// value should only be read when move is not FOLD
	public final int bet;
	public Action(Move move, Player player, int bet)
	{
		this.move = move;
		this.player = player;
		this.bet = bet;
	}
}
