enum Suit {CLUB, DIAMOND, HEART, SPADE}
enum Value {ACE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING}

public class Card {
	
	private final Suit suit;
	private final Value value;
	
	public Card(Suit s, Value v)
	{
		this.suit = s;
		this.value = v;
	}
	
	public Suit getSuit(){
		return this.suit;
	}
	
	public Value getValue() {
		return this.value;
	}
}
