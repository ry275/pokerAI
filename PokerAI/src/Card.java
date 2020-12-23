public class Card {
	public final Suit suit;
	public final Value value;
	
	public Card(Suit s, Value v)
	{
		this.suit = s;
		this.value = v;
	}
	
	@Override
	public String toString()
	{
		String s = "";
		String v = "";
		switch (this.suit)
		{
		case CLUB:
			s = "CLUB";
			break;
		case DIAMOND: 
			s = "DIAMOND";
			break;
		case HEART:
			s = "HEART";
			break;
		case SPADE:
			s = "SPADE";
			break;
		}
		switch (this.value)
		{
		case ACE:
			v = "ACE";
			break;
		case TWO:
			v = "2";
			break;
		case THREE:
			v = "3";
			break;
		case FOUR:
			v = "4";
			break;
		case FIVE:
			v = "5";
			break;
		case SIX:
			v = "6";
			break;
		case SEVEN:
			v = "7";
			break;
		case EIGHT:
			v = "8";
			break;
		case NINE:
			v = "9";
			break;
		case TEN:
			v = "10";
			break;
		case JACK:
			v = "JACK";
			break;
		case QUEEN:
			v = "QUEEN";
			break;
		case KING:
			v = "KING";
			break;
		}
		
		return (v + "." + s);
	}
	
	public String toStringEvaluator()
	{
		String s = "";
		String v = "";
		switch (this.suit)
		{
		case CLUB:
			s = "c";
			break;
		case DIAMOND: 
			s = "d";
			break;
		case HEART:
			s = "h";
			break;
		case SPADE:
			s = "s";
			break;
		}
		switch (this.value)
		{
		case ACE:
			v = "A";
			break;
		case TWO:
			v = "2";
			break;
		case THREE:
			v = "3";
			break;
		case FOUR:
			v = "4";
			break;
		case FIVE:
			v = "5";
			break;
		case SIX:
			v = "6";
			break;
		case SEVEN:
			v = "7";
			break;
		case EIGHT:
			v = "8";
			break;
		case NINE:
			v = "9";
			break;
		case TEN:
			v = "T";
			break;
		case JACK:
			v = "J";
			break;
		case QUEEN:
			v = "Q";
			break;
		case KING:
			v = "K";
			break;
		}
		
		return (v + s);
	}
}
