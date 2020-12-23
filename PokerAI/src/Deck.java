import java.util.*;

public class Deck {
	private List<Card> cards;
	public boolean isFresh;
	
	public Deck()
	{
		this.cards = new ArrayList<Card>();
		reset();
	}
	
	public void reset()
	{
		this.cards.clear();
		
		for (Suit s : Suit.values())
		{
			for (Value v : Value.values())
			{
				this.cards.add(new Card(s, v));
			}
		}
		
		Collections.shuffle(this.cards);
		
		this.isFresh = true;
	}
	
	public List<Card> getCards()
	{
		return this.cards;
	}
	
	// removes card from deck
	public Card getOneCard() throws Exception
	{
		if (this.cards.size() <= 0) throw new Exception("Empty deck.");
		Card card = this.cards.get(0);
		this.cards.remove(0);
		this.isFresh = false;
		return card;
	}
	
	// removes cards from deck
	public List<Card> getThreeCards() throws Exception
	{
		if (this.cards.size() <= 2) throw new Exception("Not enough cards in deck.");
		List<Card> cards = new ArrayList<Card>();
		cards.add(this.cards.get(0));
		cards.add(this.cards.get(1));
		cards.add(this.cards.get(2));
		this.cards.remove(0);
		this.cards.remove(0);
		this.cards.remove(0);
		this.isFresh = false;
		return cards;
	}
}
