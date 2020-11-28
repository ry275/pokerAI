import java.util.*;


public class Dealer {
	
	private List<Card> deck;
	private List<Card> cardsFaceUp;
	
	public Dealer()
	{
		this.deck = new ArrayList<Card>();
		this.cardsFaceUp = new ArrayList<Card>();
		reset();
	}
	
	public void reset()
	{
		this.cardsFaceUp.clear();
		this.deck.clear();
		
		for (Suit s : Suit.values())
		{
			for (Value v : Value.values())
			{
				this.deck.add(new Card(s, v));
			}
		}
		
		
		Collections.shuffle(deck);	
	}
	
	public List<Card> dealHand()
	{
		List<Card> hand = new ArrayList<Card>();
		hand.add(this.deck.get(0));
		this.deck.remove(0);
		hand.add(this.deck.get(0));
		this.deck.remove(0);		
		return hand;
	}
	
	public List<Card> dealFlop()
	{
		List<Card> flop = new ArrayList<Card>();
		this.deck.remove(0);
		flop.add(this.deck.get(0));
		this.deck.remove(0);
		flop.add(this.deck.get(0));
		this.deck.remove(0);
		flop.add(this.deck.get(0));
		this.deck.remove(0);	
		this.cardsFaceUp.addAll(flop);
		return flop;
	}
	
	public Card dealTurnRiver()
	{
		this.deck.remove(0);
		Card TurnOrRiver = this.deck.get(0);
		this.deck.remove(0);
		this.cardsFaceUp.add(TurnOrRiver);
		return TurnOrRiver;
	}
	
	public int compareHands(List<Card> handOne, List<Card> handTwo)
	{
		
	}
	
}
