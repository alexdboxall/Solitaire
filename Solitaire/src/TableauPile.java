import java.util.LinkedList;

public class TableauPile extends Pile {

	@Override
	protected boolean canAdd(Card newCard) {
		if (getHeight() == 0) {
			// only kings are allowed to be placed on an empty pile
			return newCard.rank == Card.RANK_KING;
			
		} else {
			// otherwise ensure that the top card is of a different colour
			// and has a value one above the new card
			Card top = getTopCard();
			return top.isAlternateColour(newCard) && top.isRankedOneAbove(newCard);	
		}
	}

	@Override
	protected boolean canAdd(Pile p) {
		// we can add a pile as long as we can add its bottom card
		return canAdd(p.getBottomCard());
	}
	
	public TableauPile(Pile p, int count) {
		super(p, count);
	}

	public TableauPile() {
		super();
	}
	
	public TableauPile(Pile p) {
		super(p);
	}
}
