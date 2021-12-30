
public class FoundationPile extends Pile {

	@Override
	protected boolean canAdd(Card newCard) {
		if (getHeight() == 0) {
			// only an ace can be placed on an empty foundation
			return newCard.rank == Card.RANK_ACE;
		} else {
			// otherwise ensure the suit is the same and it is one higher 
			// than the previous card
			Card top = getTopCard();
			return top.getSuit() == newCard.getSuit() && top.isRankedOneBelow(newCard);
		}
	}

	@Override
	protected boolean canAdd(Pile p) {
		// you can only move individual cards to a foundation, not a pile
		// (as they are in the wrong order, and the colours alternate)
		return false;
	}

	public FoundationPile() {
		super();
	}
	
	public FoundationPile(Pile p) {
		super(p);
	}
}
