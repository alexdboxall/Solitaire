
/*
 *	Solitaire - Solitaire.java
 * 
 * 	Copyright Alex Boxall 2021-2022
 * 	See LICENSE for licensing details.
 * 
 */

import java.util.Random;

class GameOptions {
	Solitaire.ScoringMode scoring;
	boolean timed;
	boolean draw3;
	boolean cumulative;
	
	int initialScore;
	int initialSeed;
	boolean useSeed;
	
	GameOptions() {
		scoring = Solitaire.ScoringMode.Standard;
		timed = true;
		draw3 = true;
		cumulative = false;
		
		initialScore = 0;
		initialSeed = 0;
		useSeed = false;
	}
	
	void setInitialScore(int currentScore) {
		if (scoring == Solitaire.ScoringMode.Vegas) {
			initialScore = cumulative ? currentScore - 52 : -52;
		} else {
			initialScore = 0;
		}
	}
};

public class Solitaire {
	
	//used to set how the scoring is done
	public enum ScoringMode {
		Standard,
		Vegas,
		None
	}

	GameOptions options;
	Tableau tableau[];								//stores the 7 columns of cards
	TableauPile holding;							//the pile of cards held under the mouse
	DealPile dealPile;								//the pile of cards that can be dealt/turned over. also used to initialise the game
	DealPile showingPile;							//holds the cards just dealt (up to 3)
	DealPile discardPile;							//holds cards that have been dealt, but can no longer be used until recycled
	FoundationPile foundations[];					//stores the 4 foundation piles
		
	//each pile is given an index for use in hold, release, flip, etc.
	//the tableaus get indicies 0 thru 6
	//these must remain in this order (ie. DRAW_PILE_BASE cannot be less than HAND_COLUMN_BASE, etc.)
	static final int FOUNDATION_COLUMN_BASE = 7;	//base index for the foundations (7 thru 10)
	static final int HAND_COLUMN_BASE = 11;			//index for the showingPile (11)
	static final int DRAW_PILE_BASE = 12;			//dummy index used for clicking on the dealPile (used by GUI routines)
	
	protected long firstMoveTimestamp = 0;			//milliseconds since Unix epoch from when first move made (release or flip)
	protected int winSeconds = 0;					//zero before game is won, if game is won then how long it took to win 
	protected int score = 0;						//score (may not reflect latest time penalty, that is gone in getScore)
	protected int previousPenaltyTime = 0;			//used to determine when penalties are applied (ie. to not double count them)
	protected int resets = 0;						//how many times the player has gone through the entire dealPile (used for scoring in deal 3 mode)
	protected int holdOrigin;						//the pile index (see above constants) where the pile under the mouse came from (so cards can be
													//moved back where they were if a move is illegal)
	protected int numberOfUndos = 0;				//persists between undos, used to reduce bonus score if undos were made
	protected int seed = 0;
	Solitaire undoState;							//stores the state of the game prior to this one. It also contains an undoState,
													//which allows for infinite undos. When an undo is performed, we reload variables from here
	
	//copy constructor, performing a deep copy of the piles
	//this allows us to save the game state, so the undo functionality can work
	public Solitaire(Solitaire other) {		
		//shallow copy value types
		firstMoveTimestamp = other.firstMoveTimestamp;
		winSeconds = other.winSeconds;
		score = other.score;
		previousPenaltyTime = other.previousPenaltyTime;
		resets = other.resets;
		holdOrigin = other.holdOrigin;
		undoState = other.undoState;
		numberOfUndos = other.numberOfUndos;
		options = other.options;
	
		//deep copy the piles
		holding = new TableauPile(other.holding);
		dealPile = new DealPile(other.dealPile);
		showingPile = new DealPile(other.showingPile);
		discardPile = new DealPile(other.discardPile);
		
		//allocate arrays...
		foundations = new FoundationPile[4];
		tableau = new Tableau[7];
		//and then deep copy the rest of the piles
		for (int i = 0; i < 4; ++i) {
			foundations[i] = new FoundationPile(other.foundations[i]);
		}
		for (int i = 0; i < 7; ++i) {
			tableau[i] = new Tableau(other.tableau[i]);
		}
	}
	
	public Solitaire(GameOptions opt) {
		//initialise variables and arrays
		holding = new TableauPile();
		resets = 0;
		undoState = null;
		winSeconds = 0;
		firstMoveTimestamp = 0;
		previousPenaltyTime = 0;
		options = opt;
		
		foundations = new FoundationPile[4];
		for (int i = 0; i < 4; ++i) {
			foundations[i] = new FoundationPile();
		}
		
		score = opt.initialScore;
		
		//create the piles
		dealPile = new DealPile();
		showingPile = new DealPile();
		discardPile = new DealPile();
		
		seed = opt.useSeed ? opt.initialSeed : (new Random(System.currentTimeMillis())).nextInt();
		dealPile.fill(seed);
		
		//create the tableaus (this is not the standard way of constructing them, as it does it by column, not row)
		tableau = new Tableau[7];
		for (int i = 0; i < 7; ++i) {
			tableau[i] = new Tableau(i + 1, dealPile);
		}
	}
	
	public void undo() {
		//cannot undo if no moves have been made
		if (!canUndo()) {
			return;
		}
		
		//reload everything from the save state (deep copy not needed as we are throwing out undoState at the end)
		tableau = undoState.tableau;
		holding = undoState.holding;
		dealPile = undoState.dealPile;
		showingPile = undoState.showingPile;
		discardPile = undoState.discardPile;
		foundations = undoState.foundations;
		firstMoveTimestamp = undoState.firstMoveTimestamp;
		winSeconds = undoState.winSeconds;
		score = undoState.score;
		previousPenaltyTime = undoState.previousPenaltyTime;
		resets = undoState.resets;
		holdOrigin = undoState.holdOrigin;
		options = undoState.options;
		numberOfUndos = undoState.numberOfUndos + 1;		
		
		//allows for infinite undos, but only if there is something to undo
		undoState = undoState == null ? null : undoState.undoState;
		
		//the original undoState is now lost, so no need for deep copy
	}
	
	//save the game state so it can be undone later, and start the timer if needed
	public void preMove() {
		undoState = new Solitaire(this);
		
		if (firstMoveTimestamp == 0) {
			firstMoveTimestamp = System.currentTimeMillis();
		}
	}
	
	//adds (or subtracts) an amount from the score, ensuring the final
	//score is not negative (unless playing in Vegas mode, where you can lose money)
	protected void changeScore(int amount) {
		//don't bother with the score if scoring is off
		if (options.scoring == ScoringMode.None) {
			return;
		}
		
		score += amount;
		
		//in standard mode, it cannot be negative (it can be in Vegas mode though)
		if (score < 0 && options.scoring == ScoringMode.Standard) {
			score = 0;
		}
	}
	
	//returns the score, and applies time penalties
	public int getScore() {
		int time = getTime();
		
		//only standard mode has time penalties
		if (options.scoring == ScoringMode.Standard && options.timed) {
			//if it has been longer than 10 seconds since the last penalty
			//(loops in case it has been e.g. longer than 20 seconds)
			while (time - previousPenaltyTime >= 10) {
				changeScore(-2);					//apply penalty
				previousPenaltyTime += 10;			//adding 10 instead of setting to time ensures that this records the time where the previous penalty SHOULD
													//have been applied, even if it was applied later
			}
		}
		
		return score;
	}
	
	//if the game is in progress, return the seconds since the start, if it hasn't started, return zero, if it has been won,
	//return the time the game took
	public int getTime() {
		return winSeconds == 0 ? (int) (firstMoveTimestamp == 0 ? 0 : (System.currentTimeMillis() - firstMoveTimestamp) / 1000) : winSeconds;
	}
	
	//check if the game has been won
	public boolean isWon() {
		//each foundation needs 13 cards (from ace to king) to win
		//so if any have less than 13, it is not a win
		for (int i = 0; i < 4; ++i) {
			if (foundations[i].getHeight() != 13) {
				return false;
			}
		}
		return true;
	}
	
	public boolean canUndo() {
		return undoState != null;
	}

	public void checkForWin() {
		if (isWon() && winSeconds == 0) {
			//record time when win occured
			winSeconds = getTime();
			
			//time bonus is only applied in standard mode
			if (options.scoring == ScoringMode.Standard) {
				//add bonus points as described here: https://en.wikipedia.org/wiki/Klondike_(solitaire)
				//but with a new penalty for each undo
				if (winSeconds >= 30) {
					score += (20000 / winSeconds) * 35 / (numberOfUndos + 1);
				}
			}
		}
	}
	
	//pick up a pile of cards from a given pile ID
	//numCards determines how many cards from the parent pile are grabbed (from the front)
	public void hold(int column, int numCards) {
		preMove();

		//save where the cards came from in case the move is cancelled or illegal
		holdOrigin = column;
		
		if (column >= HAND_COLUMN_BASE) {
			//grab from the showing pile
			holding = new TableauPile(showingPile, 1);
			
		} else if (column >= FOUNDATION_COLUMN_BASE) {
			//grab from one of the foundations
			holding = new TableauPile(foundations[column - FOUNDATION_COLUMN_BASE], 1);
			
		} else {
			//otherwise it is a tableau
			holding = new TableauPile(tableau[column].visiblePile, numCards);
		}
	}
	
	//forces cards to be released on a certain pile, even if under normal rules it wouldn't be allowed
	//used to allow cards to return to their original positions if a move is cancelled or illegal
	public void forceRelease(int column) {
		if (column >= HAND_COLUMN_BASE) {
			showingPile.forceAddPile(holding);
			
		} else if (column >= FOUNDATION_COLUMN_BASE) {
			foundations[column - FOUNDATION_COLUMN_BASE].forceAddPile(holding);
			
		} else {
			tableau[column].visiblePile.forceAddPile(holding);
		}
	}
	
	//releases the currently held cards onto a given pile
	public void release(int column) {
		boolean couldRelease = false;
			
		if (column >= HAND_COLUMN_BASE) {
			//you cannot put back into the hand
			couldRelease = false;
			
		} else if (column >= FOUNDATION_COLUMN_BASE) {
			couldRelease = foundations[column - FOUNDATION_COLUMN_BASE].addPile(holding);
			
			//apply the scoring, only if it didn't come from the foundation (ie. it was already in the foundation and the user just clicked the card)
			if (couldRelease && (holdOrigin < FOUNDATION_COLUMN_BASE || holdOrigin >= HAND_COLUMN_BASE)) {
				if (options.scoring == ScoringMode.Standard) changeScore(10);
				else if (options.scoring == ScoringMode.Vegas) changeScore(5); 
			}
			
		} else if (column != -1) {
			// ensure kings can only be moved to empty tableaus, not just empty piles
			if (holding.getBottomCard().rank == Card.RANK_KING && tableau[column].visiblePile.getHeight() == 0 && tableau[column].hiddenPile.getHeight() != 0) {
				couldRelease = false;
			} else {
				couldRelease = tableau[column].visiblePile.addPile(holding);
			}
			
			//apply scoring depending on where it comes from and the scoring mode
			if (couldRelease && holdOrigin == HAND_COLUMN_BASE && options.scoring == ScoringMode.Standard) {
				changeScore(5);
			} else if (couldRelease && holdOrigin == FOUNDATION_COLUMN_BASE) {
				if (options.scoring == ScoringMode.Standard) changeScore(-15);
				else if (options.scoring == ScoringMode.Vegas) changeScore(-5);
			}
		}
		
		if (!couldRelease) {
			//put the cards being held back where it came from if they were not placed on a valid pile
			forceRelease(holdOrigin);
		} else {
			//make a previously discarded cards visible if the showing pile is empty
			if (holdOrigin == HAND_COLUMN_BASE && showingPile.getHeight() == 0 && discardPile.getHeight() != 0) {
				showingPile.forceAddCard(discardPile.removeTopCard());
			}
		}
		
		//clear the holding pile
		holding = new TableauPile();
		
		//games can only be won on a card release, so check that here
		checkForWin();
	}
	
	//'recycles' all of the cards in the hand
	//assumes that the showing pile is already cleared (ie. moved into the discard pile)
	protected void resetHand() {
		//move to the deal pile
		dealPile = discardPile;
		
		//clear the other ones
		showingPile = new DealPile();
		discardPile = new DealPile();
		
		resets++;
		
		//apply the points penalty in standard mode if needed
		if (options.scoring == ScoringMode.Standard) {
			if (options.draw3) {
				if (resets >= 4) changeScore(-20);
			} else {
				changeScore(-100);
			}
		}
	}
	
	//determines if you can reset the hand (on Vegas mode you can only do it a set number of times)
	public boolean canFlipHand() {
		return !(dealPile.getHeight() == 0 && options.scoring == ScoringMode.Vegas && resets == (options.draw3 ? 2 : 0));
	}
	
	//flips a card in the hand
	public void flipHand(int count) {	
		preMove();
		
		//ensure it is legal to do so
		if (!canFlipHand()) {
			return;
		}
		
		//we have now committed to turning the card
		
		//put everything that was showing in the discard pile
		while (showingPile.getHeight() != 0) {
			discardPile.forceAddCard(showingPile.removeBottomCard());
		}
		
		//reset/recycle the hand if needed (must be done after the above step)
		if (dealPile.getHeight() == 0) {
			resetHand();
			return;
		}
		
		//deal the cards
		for (int i = 0; i < count && dealPile.getHeight() != 0; ++i) {
			showingPile.forceAddCard(dealPile.removeBottomCard());
		}
	}
	
	//turn over a flipped over card in the tableau
	public void flipColumn(int column) {
		preMove();

		//turn it over and add the ponts
		tableau[column].flipOverCard();
		
		if (options.scoring == ScoringMode.Standard) {
			changeScore(5);
		}
	}
}
