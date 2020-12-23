public class Hand {
	public final Card cardOne;
	public final Card cardTwo;
	
	public Hand(Card cardOne, Card cardTwo)
	{
		this.cardOne = cardOne;
		this.cardTwo = cardTwo;
	}
	
	public int preFlopStrength()
	{
		return -1;
	}
	
	public int strength()
	{
		return -1;
	}
}
