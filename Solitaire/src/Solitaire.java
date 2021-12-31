
public class Solitaire {
	
	public enum ScoringMode {
		Standard,
		Vegas,
		None
	}
	
	Tableau tableau[];								//stores the 7 columns of cards
	TableauPile holding;							//the pile of cards held under the mouse
	DealPile dealPile;								//the pile of cards that can be dealt/turned over. also used to initialise the game
	DealPile showingPile;							//holds the cards just dealt (up to 3)
	DealPile discardPile;							//holds cards that have been dealt, but can no longer be used until recycled
	FoundationPile foundations[];					//stores the 4 foundation piles
		
	//each pile is given an index for use in hold, release, flip, etc.
	//the tableaus get indicies 0 thru 6
	static final int FOUNDATION_COLUMN_BASE = 7;	//base index for the foundations (7 thru 10)
	static final int HAND_COLUMN_BASE = 11;			//index for the showingPile (11)
	static final int DRAW_PILE_BASE = 12;			//dummy index used for clicking on the dealPile (used by GUI routines)
	
	protected long firstMoveTimestamp = 0;			//milliseconds since Unix epoch from when first move made (release or flip)
	protected int winSeconds = 0;					//zero before game is won, if game is won then how long it took to win 
	protected int score = 0;						//score (may not reflect latest time penalty, that is gone in getScore)
	protected int previousPenaltyTime = 0;			//used to determine when penalties are applied (ie. to not double count them)
	protected boolean draw3 = true;					//deal 1 or draw 3 mode
	protected ScoringMode scoringMode;
	protected int resets = 0;						//how many times the player has gone through the entire dealPile (used for scoring in deal 3 mode)
	protected int holdOrigin;						//the pile index (see above constants) where the pile under the mouse came from (so cards can be
													//moved back where they were if a move is illegal)
	protected int numberOfUndos = 0;
	
	static protected int seed = 0;
	
	Solitaire undoState;
	
	//copy constructor, allows us to save board state so the undo functionality can work
	public Solitaire(Solitaire other) {		
		firstMoveTimestamp = other.firstMoveTimestamp;
		winSeconds = other.winSeconds;
		score = other.score;
		previousPenaltyTime = other.previousPenaltyTime;
		draw3 = other.draw3;
		resets = other.resets;
		holdOrigin = other.holdOrigin;
		undoState = other.undoState;
		scoringMode = other.scoringMode;
				
		holding = new TableauPile(other.holding);
		
		dealPile = new DealPile(other.dealPile);
		showingPile = new DealPile(other.showingPile);
		discardPile = new DealPile(other.discardPile);
		
		foundations = new FoundationPile[4];
		tableau = new Tableau[7];
		
		numberOfUndos = 0;
		
		for (int i = 0; i < 4; ++i) {
			foundations[i] = new FoundationPile(other.foundations[i]);
		}
		for (int i = 0; i < 7; ++i) {
			tableau[i] = new Tableau(other.tableau[i]);
		}
	}
	
	//constructor. starts game in either draw1 or draw3 mode
	public Solitaire(boolean _draw3, ScoringMode scoreMode, int initialScore) {
		draw3 = _draw3;
		holding = new TableauPile();
		resets = 0;
		undoState = null;
		scoringMode = scoreMode;
		
		foundations = new FoundationPile[4];
		for (int i = 0; i < 4; ++i) {
			foundations[i] = new FoundationPile();
		}
		
		dealPile = new DealPile();
		showingPile = new DealPile();
		discardPile = new DealPile();
		dealPile.fill(seed++);
		
		tableau = new Tableau[7];
		for (int i = 0; i < 7; ++i) {
			tableau[i] = new Tableau(i + 1, dealPile);
		}
		
		score = initialScore;
		winSeconds = 0;
		firstMoveTimestamp = 0;
		previousPenaltyTime = 0;
	}
	
	
	public void undo() {
		if (!canUndo()) {
			return;
		}
		
		tableau = undoState.tableau;
		holding = undoState.holding;
		dealPile = undoState.dealPile;
		showingPile = undoState.showingPile;
		discardPile = undoState.discardPile;
		foundations = undoState.foundations;
		
		scoringMode = undoState.scoringMode;
		firstMoveTimestamp = undoState.firstMoveTimestamp;
		winSeconds = undoState.winSeconds;
		score = undoState.score;
		previousPenaltyTime = undoState.previousPenaltyTime;
		draw3 = undoState.draw3;
		resets = undoState.resets;
		holdOrigin = undoState.holdOrigin;
		
		numberOfUndos = undoState.numberOfUndos + 1;		
		
		//allows for infinite undos, but only if there is something to undo
		undoState = undoState == null ? null : undoState.undoState;
	}
	
	public void saveStateForUndo() {
		undoState = new Solitaire(this);
	}
	
	//adds (or subtracts) an amount from the score, ensuring the final
	//score is not negative (unless playing in Vegas mode, where you can lose money)
	protected void changeScore(int amount) {
		if (scoringMode == ScoringMode.None) {
			return;
		}
		
		score += amount;
		if (score < 0 && scoringMode == ScoringMode.Standard) {
			score = 0;
		}
	}
	
	//returns the score, and applies time penalties
	public int getScore() {
		int time = getTime();
		
		if (scoringMode == ScoringMode.Standard) {
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
		if (isWon() && winSeconds == 0 && scoringMode == ScoringMode.Standard) {
			//record time when win occured
			winSeconds = getTime();
			
			//add bonus points as described here: https://en.wikipedia.org/wiki/Klondike_(solitaire)
			//but with a new penalty for each undo
			if (winSeconds >= 30) {
				score += (20000 / winSeconds) * 35 / (numberOfUndos + 1);
			}
		}
	}
	
	//pick up a pile of cards from a given pile ID
	//numCards determines how many cards from the parent pile are grabbed (from the front)
	public void hold(int column, int numCards) {
		saveStateForUndo();

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
	
	//forces cards to be released on a certain pile, even if under 
	//normal rules it wouldn't be allowed
	//used to allow cards to return to their original positions if a 
	//move is cancelled or illegal
	public void forceRelease(int column) {
		if (column >= HAND_COLUMN_BASE) {
			showingPile.forceAddPile(holding);
			
		} else if (column >= FOUNDATION_COLUMN_BASE) {
			foundations[column - FOUNDATION_COLUMN_BASE].forceAddPile(holding);
			
		} else {
			tableau[column].visiblePile.forceAddPile(holding);
		}
	}
	
	//sets the start of game time if it has not yet been set
	public void setTimestampIfNeeded() {
		if (firstMoveTimestamp == 0) {
			firstMoveTimestamp = System.currentTimeMillis();
		}
	}
	
	public void release(int column) {
		boolean couldRelease = false;
			
		if (column >= HAND_COLUMN_BASE) {
			couldRelease = false;
			
		} else if (column >= FOUNDATION_COLUMN_BASE) {
			couldRelease = foundations[column - FOUNDATION_COLUMN_BASE].addPile(holding);
			
			if (couldRelease && (holdOrigin < FOUNDATION_COLUMN_BASE || holdOrigin >= HAND_COLUMN_BASE)) {
				if (scoringMode == ScoringMode.Standard) changeScore(10);
				else if (scoringMode == ScoringMode.Vegas) changeScore(5); 
			}
			
		} else if (column != -1) {
			// ensure kings can only be moved to empty tableaus, not just empty piles
			if (holding.getBottomCard().rank == Card.RANK_KING && tableau[column].visiblePile.getHeight() == 0 && tableau[column].hiddenPile.getHeight() != 0) {
				couldRelease = false;
			} else {
				couldRelease = tableau[column].visiblePile.addPile(holding);
			}
			
			if (couldRelease && holdOrigin == HAND_COLUMN_BASE && scoringMode == ScoringMode.Standard) {
				changeScore(5);
			} else if (couldRelease && holdOrigin == FOUNDATION_COLUMN_BASE) {
				if (scoringMode == ScoringMode.Standard) changeScore(-15);
				else if (scoringMode == ScoringMode.Vegas) changeScore(-5);
			}
		}
		
		if (!couldRelease) {
			// put holding back where it came from
			forceRelease(holdOrigin);
		} else {
			setTimestampIfNeeded();
			
			if (holdOrigin == HAND_COLUMN_BASE && showingPile.getHeight() == 0 && discardPile.getHeight() != 0) {
				showingPile.forceAddCard(discardPile.removeTopCard());
			}
		}
		
		holding = new TableauPile();
		checkForWin();
	}
	
	protected void resetHand() {
		dealPile = discardPile;
		showingPile = new DealPile();
		discardPile = new DealPile();
		
		resets++;
		
		if (scoringMode == ScoringMode.Standard) {
			if (draw3) {
				if (resets >= 4) changeScore(-20);
			} else {
				changeScore(-100);
			}
		}
	}
	
	public boolean canFlipHand() {
		return !(dealPile.getHeight() == 0 && scoringMode == ScoringMode.Vegas && resets == (draw3 ? 2 : 0));
	}
	
	public void flipHand(int count) {		
		setTimestampIfNeeded();
		saveStateForUndo();
		
		if (!canFlipHand()) {
			return;
		}
		
		while (showingPile.getHeight() != 0) {
			discardPile.forceAddCard(showingPile.removeBottomCard());
		}
		
		if (dealPile.getHeight() == 0) {
			resetHand();
			return;
		}
		
		for (int i = 0; i < count && dealPile.getHeight() != 0; ++i) {
			showingPile.forceAddCard(dealPile.removeBottomCard());
		}
	}
	
	public void flipColumn(int column) {
		setTimestampIfNeeded();
		saveStateForUndo();

		tableau[column].flipOverCard();
		
		if (scoringMode == ScoringMode.Standard) {
			changeScore(5);
		}
	}
}
