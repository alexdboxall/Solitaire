
// must not store any objects, as shallow copies are done to card objects
public class Card {

	public enum Suit {
		Heart,
		Diamond,
		Spade,
		Club,
	}
	
	final public static int RANK_ACE  	= 1;
	final public static int RANK_JACK 	= 11;
	final public static int RANK_QUEEN	= 12;
	final public static int RANK_KING 	= 13;

	protected Suit suit;
	protected int rank;
	
	public Card(Suit _suit, int _rank) {
		suit = _suit;
		rank = _rank;
	}
	
	public Suit getSuit() {
		return suit;
	}
	
	public int getRank() {
		return rank;
	}
	
	public boolean isRed() {
		return suit == Suit.Heart || suit == Suit.Diamond;
	}
	
	public boolean isBlack() {
		return suit == Suit.Spade || suit == Suit.Club;
	}
	
	public boolean isAlternateColour(Card other) {
		return (isRed() && other.isBlack()) || (isBlack() && other.isRed());
	}
	
	public boolean isRankedOneAbove(Card other) {
		return rank == other.rank + 1;
	}
	
	public boolean isRankedOneBelow(Card other) {
		return rank == other.rank - 1;
	}
	
	public String displayString() {
		char suitChar = suit == Suit.Heart ? 'H' : 
				 		suit == Suit.Club ? 'C' : 
				 		suit == Suit.Spade ? 'S' : 'D';
		
		String rankChar = rank == RANK_ACE   ? " A" : 
						rank == RANK_JACK  ? " J" : 
						rank == RANK_QUEEN ? " Q" :
						rank == RANK_KING  ? " K" : String.format("%2d", rank);
		
		return String.format("%s%s", rankChar, suitChar);
	}
}
