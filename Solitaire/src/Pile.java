
import java.util.LinkedList;

public abstract class Pile {	
	abstract protected boolean canAdd(Card c);
	abstract protected boolean canAdd(Pile p);
	
	protected LinkedList<Card> cards;
	
	//copy constructor
	public Pile(Pile other) {
		cards = new LinkedList<Card>();
		
		for (Card card : other.cards) {
			cards.add(card);
		}
	}
	
	Card getBottomCard() {
		return cards.getLast();
	}
	
	Card getTopCard() {
		return cards.getFirst();
	}
	
	Card removeTopCard() {
		return cards.removeFirst();
	}
	
	Card removeBottomCard() {
		return cards.removeLast();
	}
	
	int getHeight() {
		return cards.size();
	}
	
	void forceAddCard(Card c) {
		// used to initialise the pile
		cards.addFirst(c);
	}
	
	boolean addCard(Card c) {
		if (!canAdd(c)) {
			return false;
		}
		
		cards.addFirst(c);
		
		return true;
	}
	
	void forceAddPile(Pile p) {
		while (p.getHeight() != 0) {
			cards.addFirst(p.removeBottomCard());
		}
	}
	
	boolean addPile(Pile p) {
		if (p.getHeight() == 1) {
			return addCard(p.getTopCard());
		}
		if (!canAdd(p)) {
			return false;
		}
		
		while (p.getHeight() != 0) {
			cards.addFirst(p.removeBottomCard());
		}
		
		return true;
	}
	
	Pile() {
		cards = new LinkedList<Card>();	
	}
	
	Pile(Pile other, int amount) {
		cards = new LinkedList<Card>();
		
		while (amount-- != 0 && other.getHeight() != 0) {
			cards.addLast(other.removeTopCard());
		}
	}
}
