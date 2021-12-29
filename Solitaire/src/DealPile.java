import java.util.Collections;

public class DealPile extends Pile {

	@Override
	protected boolean canAdd(Card c) {
		// should never be called
		return false;
	}

	@Override
	protected boolean canAdd(Pile p) {
		// should never be called
		return false;
	}
	
	public void fill() {
		// add one card of every suit
		for (int rank = Card.RANK_ACE; rank <= Card.RANK_KING; ++rank) {
			cards.add(new Card(Card.Suit.Club, rank));
			cards.add(new Card(Card.Suit.Spade, rank));
			cards.add(new Card(Card.Suit.Diamond, rank));
			cards.add(new Card(Card.Suit.Heart, rank));
		}
		
		// ensure it is shuffled
		Collections.shuffle(cards);
	}
}
