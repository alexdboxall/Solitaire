import java.util.Iterator;

public class Tableau {
	protected TableauPile visiblePile;
	protected TableauPile hiddenPile;
	
	public TableauPile getVisiblePile() {
		return visiblePile;
	}
	
	public TableauPile getHiddenPile() {
		return hiddenPile;
	}
	
	Tableau(int initialCardCount, DealPile dealPile) {
		visiblePile = new TableauPile();
		hiddenPile = new TableauPile();

		while (initialCardCount-- != 0) {
			hiddenPile.forceAddCard(dealPile.removeTopCard());
		}
		
		flipOverCard();
	}
	
	void flipOverCard() {
		if (hiddenPile.getHeight() != 0 && visiblePile.getHeight() == 0) {
			visiblePile.forceAddCard(hiddenPile.removeTopCard());
		}
	}
	
	void debugPrint() {		
		Iterator<Card> hiddenIter = hiddenPile.cards.iterator();
		Iterator<Card> visibleIter = visiblePile.cards.iterator();

		while (hiddenIter.hasNext()) {
			System.out.printf(" -  %s\n", hiddenIter.next().displayString());
		}
		while (visibleIter.hasNext()) {
			System.out.printf(" >  %s\n", visibleIter.next().displayString());
		}
		
		System.out.printf("\n");
	}
}
