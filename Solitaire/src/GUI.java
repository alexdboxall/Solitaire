
/*
 *	Solitaire - GUI.jar
 * 
 * 	Copyright Alex Boxall 2021-2022
 * 	See LICENSE for licensing details.
 * 
 */

import java.awt.*;
import java.awt.image.*;
import java.io.File;
import java.io.InputStream;
import java.awt.event.*;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileSystemView;

public class GUI extends JPanel {
	
	private static final long serialVersionUID = 1L;

	protected Solitaire game = null;
	

	//these are recomputed if the window changes dimentions
	static int WINDOW_WIDTH = 1000;
	static int WINDOW_HEIGHT = 700;
	static int DRAW_X_POS = 25;
	static int TABLEAU_X_POS = 25;
	static int TABLEAU_DISTANCE = 137;
	static int FOUNDATION_X_POS = TABLEAU_X_POS + 3 * TABLEAU_DISTANCE;

	static final int CARD_WIDTH = 105;
	static final int CARD_HEIGHT = 150;
	static final int CARD_BORDER = 6;
	static final int DRAW_Y_POS = 20;
	static final int TABLEAU_Y_POS = 200;
	static final int FOUNDATION_Y_POS = DRAW_Y_POS;
	static final int TABLEAU_Y_DISTANCE = 22;
	static final int DEAL_3_SHIFT_DIST = 35;
		
	protected int recentMouseX;
	protected int recentMouseY;
	protected int recentCardX;
	protected int recentCardY;
	
	static protected BufferedImage cardImages[][];
	static final int CARD_IMAGE_JACK = 0;
	static final int CARD_IMAGE_QUEEN = 1;
	static final int CARD_IMAGE_KING = 2;
	
	protected JMenuItem undoBtn;
	protected JMenuItem cumulativeBtn;
	protected JMenuItem timedBtn;
	
	protected TexturePaint cardBackPaint;
	protected TexturePaint pictureCardPaint;
	
	protected GameOptions options;

	class CardClickInfo {
	    public final int tableau;
	    public final int cards;
	    public final int cardX;
	    public final int cardY;

	    public CardClickInfo(int _tableau, int _cards, int _cardX, int _cardY) {
	    	tableau = _tableau;
	    	cards = _cards;
	    	cardX = _cardX;
	    	cardY = _cardY;
	    }
	}
	
	protected void drawBackOfCard(Graphics g, int x, int y) {
		Graphics2D g2 = (Graphics2D) g;

		g.setColor(new Color(0xFFFFFF));
		g.fillRect(x, y, CARD_WIDTH, CARD_HEIGHT);
		
		g.setColor(new Color(0x000000));
		g.drawRect(x, y, CARD_WIDTH - 1, CARD_HEIGHT - 1);
		
		g.setColor(new Color(0x0040C0));
		g.fillRect(x + CARD_BORDER, y + CARD_BORDER, CARD_WIDTH - 2 * CARD_BORDER, CARD_HEIGHT - 2 * CARD_BORDER);
		
		g2.setPaint(cardBackPaint);
		g2.fillRect(x + CARD_BORDER * 2, y + CARD_BORDER * 2, CARD_WIDTH - 4 * CARD_BORDER, CARD_HEIGHT - 4 * CARD_BORDER);
	}
	
	protected void drawSymbol(Graphics g, int x, int y, boolean upsideDown, Card.Suit suit) {
		if (suit == Card.Suit.Diamond) {
			int xpoly[] = {x, x - 8, x, x + 8};
			int ypoly[] = {y - 12, y, y + 12, y};
			g.fillPolygon(new Polygon(xpoly, ypoly, xpoly.length));
			
		} else if (suit == Card.Suit.Heart) {
			if (upsideDown) {
				int xpoly[] = {x, x - 8, x, x + 8};
				int ypoly[] = {y, y, y - 12, y};
				g.fillPolygon(new Polygon(xpoly, ypoly, xpoly.length));
				g.fillOval(x - 9, y - 3, 10, 10);
				g.fillOval(x - 1, y - 3, 10, 10);
				
			} else {
				int xpoly[] = {x, x - 8, x, x + 8};
				int ypoly[] = {y, y, y + 12, y};
				g.fillPolygon(new Polygon(xpoly, ypoly, xpoly.length));
				g.fillOval(x - 9, y - 8, 10, 10);
				g.fillOval(x - 1, y - 8, 10, 10);
			}
		

		} else if (suit == Card.Suit.Spade) {
			if (upsideDown) {
				int xpoly[] = {x, x - 8, x, x + 8};
				int ypoly[] = {y, y, y + 12, y};
				g.fillPolygon(new Polygon(xpoly, ypoly, xpoly.length));
				g.fillOval(x - 9, y - 8, 10, 10);
				g.fillOval(x - 1, y - 8, 10, 10);
				
				g.fillRect(x, y - 10, 1, 10);
				g.fillRect(x - 1, y - 10, 3, 3);
				g.fillRect(x - 3, y - 10, 7, 1);
				
			} else {
				int xpoly[] = {x, x - 8, x, x + 8};
				int ypoly[] = {y, y, y - 12, y};
				g.fillPolygon(new Polygon(xpoly, ypoly, xpoly.length));
				g.fillOval(x - 9, y - 3, 10, 10);
				g.fillOval(x - 1, y - 3, 10, 10);
				g.fillRect(x, y, 1, 10);
				g.fillRect(x - 1, y + 7, 3, 3);
				g.fillRect(x - 3, y + 9, 7, 1);	
			}
			
		} else {
			if (upsideDown) {
				g.fillOval(x - 5, y - 1, 10, 10);
				g.fillOval(x - 9, y - 8, 10, 10);
				g.fillOval(x - 1, y - 8, 10, 10);
				
				g.fillRect(x, y - 10, 1, 10);
				g.fillRect(x - 1, y - 10, 3, 3);
				g.fillRect(x - 3, y - 10, 7, 1);
				
			} else {
				g.fillOval(x - 5, y - 10, 10, 10);
				g.fillOval(x - 9, y - 3, 10, 10);
				g.fillOval(x - 1, y - 3, 10, 10);
				g.fillRect(x, y, 1, 10);
				g.fillRect(x - 1, y + 7, 3, 3);
				g.fillRect(x - 3, y + 9, 7, 1);
			}
			
		}
		
	}
	
	protected void drawCard(Graphics g, int x, int y, Card card) {
		g.setColor(new Color(0xFFFFFF));
		g.fillRect(x, y, CARD_WIDTH, CARD_HEIGHT);
		
		g.setColor(new Color(0x000000));
		g.drawRect(x, y, CARD_WIDTH - 1, CARD_HEIGHT - 1);
		
		String rank = card.rank == Card.RANK_ACE   ? "A" : 
					  card.rank == Card.RANK_JACK  ? "J" : 
				      card.rank == Card.RANK_QUEEN ? "Q" :
					  card.rank == Card.RANK_KING  ? "K" : String.format("%d", card.rank);
		
		g.setColor(new Color(card.isBlack() ? 0x000000 : 0xFF0000));
		if (card.rank == 10) {
			g.setFont(new Font("Courier New", Font.BOLD, 22));
			g.drawString("1", x + 4, y + 22);
			g.drawString("0", x + 12, y + 22);
			g.setFont(new Font("Courier New", Font.BOLD, -22));
			g.drawString("0", x + CARD_WIDTH - 7 - 5, y + CARD_HEIGHT - 22);
			g.drawString("1", x + CARD_WIDTH - 7 + 3, y + CARD_HEIGHT - 22);
			
		} else {
			g.setFont(new Font("Courier New", Font.BOLD, 22));
			g.drawString(rank, x + 5, y + 22);
			g.setFont(new Font("Courier New", Font.BOLD, -22));
			g.drawString(rank, x + CARD_WIDTH - 5, y + CARD_HEIGHT - 22);
		}
		
		
		if (card.rank == 1) {
			if (card.suit == Card.Suit.Spade) {
				int xpoly[] = {x + CARD_WIDTH / 2, x + CARD_WIDTH / 2 - 27, x + CARD_WIDTH / 2, x + CARD_WIDTH / 2 + 27};
				int ypoly[] = {y + CARD_HEIGHT / 2, y + CARD_HEIGHT / 2, y - 36 + CARD_HEIGHT / 2, y + CARD_HEIGHT / 2};
				g.fillPolygon(new Polygon(xpoly, ypoly, xpoly.length));
				g.fillOval(x + CARD_WIDTH / 2 - 27, y + CARD_HEIGHT / 2 - 10, 30, 30);
				g.fillOval(x + CARD_WIDTH / 2 - 3, y + CARD_HEIGHT / 2 - 10, 30, 30);
				g.fillRect(x + CARD_WIDTH / 2, y + CARD_HEIGHT / 2, 3, 30);
				
				int xpoly2[] = {x + CARD_WIDTH / 2 + 1, x + CARD_WIDTH / 2 - 19, x + CARD_WIDTH / 2 + 21};
				int ypoly2[] = {y + CARD_HEIGHT / 2 + 20, y + CARD_HEIGHT / 2 + 30, y + CARD_HEIGHT / 2 + 30};
				g.fillPolygon(new Polygon(xpoly2, ypoly2, xpoly2.length));
				
				g.setFont(new Font("Courier New", Font.BOLD, 8));
				g.drawString("N.INGLIS", x + CARD_WIDTH / 2 - 18, y + CARD_WIDTH / 2 + 62);

			} else {
				drawSymbol(g, x + CARD_WIDTH / 2, y + CARD_HEIGHT / 2, false, card.suit);
			}
			
		} else if (card.rank == 2){
			drawSymbol(g, x + CARD_WIDTH / 2, y + CARD_HEIGHT / 2 - CARD_HEIGHT / 3, false, card.suit);
			drawSymbol(g, x + CARD_WIDTH / 2, y + CARD_HEIGHT / 2 + CARD_HEIGHT / 3, true, card.suit);
		
		} else if (card.rank == 3) {
			drawSymbol(g, x + CARD_WIDTH / 2, y + CARD_HEIGHT / 2 - CARD_HEIGHT / 3, false, card.suit);
			drawSymbol(g, x + CARD_WIDTH / 2, y + CARD_HEIGHT / 2, false, card.suit);
			drawSymbol(g, x + CARD_WIDTH / 2, y + CARD_HEIGHT / 2 + CARD_HEIGHT / 3, true, card.suit);
		
		} else if (card.rank == 4) {
			drawSymbol(g, x + CARD_WIDTH * 7 / 24, y + CARD_HEIGHT / 2 - CARD_HEIGHT / 3, false, card.suit);
			drawSymbol(g, x + CARD_WIDTH * 7 / 24, y + CARD_HEIGHT / 2 + CARD_HEIGHT / 3, true, card.suit);
			drawSymbol(g, x + CARD_WIDTH * 17 / 24, y + CARD_HEIGHT / 2 - CARD_HEIGHT / 3, false, card.suit);
			drawSymbol(g, x + CARD_WIDTH * 17 / 24, y + CARD_HEIGHT / 2 + CARD_HEIGHT / 3, true, card.suit);
		
		} else if (card.rank == 5) {
			drawSymbol(g, x + CARD_WIDTH * 7 / 24, y + CARD_HEIGHT / 2 - CARD_HEIGHT / 3, false, card.suit);
			drawSymbol(g, x + CARD_WIDTH * 7 / 24, y + CARD_HEIGHT / 2 + CARD_HEIGHT / 3, true, card.suit);
			drawSymbol(g, x + CARD_WIDTH * 17 / 24, y + CARD_HEIGHT / 2 - CARD_HEIGHT / 3, false, card.suit);
			drawSymbol(g, x + CARD_WIDTH * 17 / 24, y + CARD_HEIGHT / 2 + CARD_HEIGHT / 3, true, card.suit);
		
			drawSymbol(g, x + CARD_WIDTH / 2, y + CARD_HEIGHT / 2, false, card.suit);

		} else if (card.rank == 6) {
			drawSymbol(g, x + CARD_WIDTH * 7 / 24, y + CARD_HEIGHT / 2 - CARD_HEIGHT / 3, false, card.suit);
			drawSymbol(g, x + CARD_WIDTH * 7 / 24, y + CARD_HEIGHT / 2 + CARD_HEIGHT / 3, true, card.suit);
			drawSymbol(g, x + CARD_WIDTH * 7 / 24, y + CARD_HEIGHT / 2, false, card.suit);
			drawSymbol(g, x + CARD_WIDTH * 17 / 24, y + CARD_HEIGHT / 2 - CARD_HEIGHT / 3, false, card.suit);
			drawSymbol(g, x + CARD_WIDTH * 17 / 24, y + CARD_HEIGHT / 2 + CARD_HEIGHT / 3, true, card.suit);
			drawSymbol(g, x + CARD_WIDTH * 17 / 24, y + CARD_HEIGHT / 2, false, card.suit);
		
		} else if (card.rank == 7) {
			drawSymbol(g, x + CARD_WIDTH * 7 / 24, y + CARD_HEIGHT / 2 - CARD_HEIGHT / 3, false, card.suit);
			drawSymbol(g, x + CARD_WIDTH * 7 / 24, y + CARD_HEIGHT / 2 + CARD_HEIGHT / 3, true, card.suit);
			drawSymbol(g, x + CARD_WIDTH * 7 / 24, y + CARD_HEIGHT / 2, false, card.suit);
			drawSymbol(g, x + CARD_WIDTH * 17 / 24, y + CARD_HEIGHT / 2 - CARD_HEIGHT / 3, false, card.suit);
			drawSymbol(g, x + CARD_WIDTH * 17 / 24, y + CARD_HEIGHT / 2 + CARD_HEIGHT / 3, true, card.suit);
			drawSymbol(g, x + CARD_WIDTH * 17 / 24, y + CARD_HEIGHT / 2, false, card.suit);
			
			drawSymbol(g, x + CARD_WIDTH / 2, y + CARD_HEIGHT * 2 / 3, true, card.suit);
		
		} else if (card.rank == 8) {
			drawSymbol(g, x + CARD_WIDTH * 7 / 24, y + CARD_HEIGHT / 2 - CARD_HEIGHT / 3, false, card.suit);
			drawSymbol(g, x + CARD_WIDTH * 7 / 24, y + CARD_HEIGHT / 2 + CARD_HEIGHT / 3, true, card.suit);
			drawSymbol(g, x + CARD_WIDTH * 7 / 24, y + CARD_HEIGHT / 2, false, card.suit);
			drawSymbol(g, x + CARD_WIDTH * 17 / 24, y + CARD_HEIGHT / 2 - CARD_HEIGHT / 3, false, card.suit);
			drawSymbol(g, x + CARD_WIDTH * 17 / 24, y + CARD_HEIGHT / 2 + CARD_HEIGHT / 3, true, card.suit);
			drawSymbol(g, x + CARD_WIDTH * 17 / 24, y + CARD_HEIGHT / 2, false, card.suit);
			
			drawSymbol(g, x + CARD_WIDTH / 2, y + CARD_HEIGHT / 3, false, card.suit);
			drawSymbol(g, x + CARD_WIDTH / 2, y + CARD_HEIGHT * 2 / 3, true, card.suit);
		
		} else if (card.rank == 9) {
			drawSymbol(g, x + CARD_WIDTH * 7 / 24, y + CARD_HEIGHT / 2 - CARD_HEIGHT / 3, false, card.suit);
			drawSymbol(g, x + CARD_WIDTH * 7 / 24, y + CARD_HEIGHT / 2 - CARD_HEIGHT / 9, false, card.suit);
			drawSymbol(g, x + CARD_WIDTH * 7 / 24, y + CARD_HEIGHT / 2 + CARD_HEIGHT / 9, true, card.suit);
			drawSymbol(g, x + CARD_WIDTH * 7 / 24, y + CARD_HEIGHT / 2 + CARD_HEIGHT / 3, true, card.suit);
			
			drawSymbol(g, x + CARD_WIDTH * 17 / 24, y + CARD_HEIGHT / 2 - CARD_HEIGHT / 3, false, card.suit);
			drawSymbol(g, x + CARD_WIDTH * 17 / 24, y + CARD_HEIGHT / 2 - CARD_HEIGHT / 9, false, card.suit);
			drawSymbol(g, x + CARD_WIDTH * 17 / 24, y + CARD_HEIGHT / 2 + CARD_HEIGHT / 9, true, card.suit);
			drawSymbol(g, x + CARD_WIDTH * 17 / 24, y + CARD_HEIGHT / 2 + CARD_HEIGHT / 3, true, card.suit);
			
			drawSymbol(g, x + CARD_WIDTH / 2, y + CARD_HEIGHT / 2, false, card.suit);
		
		} else if (card.rank == 10) {
			drawSymbol(g, x + CARD_WIDTH * 7 / 24, y + CARD_HEIGHT / 2 - CARD_HEIGHT / 3, false, card.suit);
			drawSymbol(g, x + CARD_WIDTH * 7 / 24, y + CARD_HEIGHT / 2 - CARD_HEIGHT / 9, false, card.suit);
			drawSymbol(g, x + CARD_WIDTH * 7 / 24, y + CARD_HEIGHT / 2 + CARD_HEIGHT / 9, true, card.suit);
			drawSymbol(g, x + CARD_WIDTH * 7 / 24, y + CARD_HEIGHT / 2 + CARD_HEIGHT / 3, true, card.suit);
			
			drawSymbol(g, x + CARD_WIDTH * 17 / 24, y + CARD_HEIGHT / 2 - CARD_HEIGHT / 3, false, card.suit);
			drawSymbol(g, x + CARD_WIDTH * 17 / 24, y + CARD_HEIGHT / 2 - CARD_HEIGHT / 9, false, card.suit);
			drawSymbol(g, x + CARD_WIDTH * 17 / 24, y + CARD_HEIGHT / 2 + CARD_HEIGHT / 9, true, card.suit);
			drawSymbol(g, x + CARD_WIDTH * 17 / 24, y + CARD_HEIGHT / 2 + CARD_HEIGHT / 3, true, card.suit);
			
			drawSymbol(g, x + CARD_WIDTH / 2, y + CARD_HEIGHT / 2 - CARD_HEIGHT * 2 / 9, false, card.suit);
			drawSymbol(g, x + CARD_WIDTH / 2, y + CARD_HEIGHT / 2 + CARD_HEIGHT * 2 / 9, true, card.suit);
		
		} else {
			try {
				BufferedImage image = cardImages[card.suit.ordinal()][card.rank - Card.RANK_JACK];
				if (image != null) {
					g.drawImage(image.getScaledInstance(CARD_WIDTH * 44 / 72, CARD_HEIGHT * 14 / 16, Image.SCALE_DEFAULT), x + CARD_WIDTH * 14 / 72, y + CARD_HEIGHT / 16, null);
				}
			} catch (Exception e) { ; }
			g.drawRect(x + CARD_WIDTH * 14 / 72, y + CARD_HEIGHT / 16, CARD_WIDTH * 44 / 72, CARD_HEIGHT * 14 / 16);

			drawSymbol(g, x + CARD_WIDTH * 7 / 24, y + CARD_HEIGHT / 2 - CARD_HEIGHT / 3, false, card.suit);
			drawSymbol(g, x + CARD_WIDTH * 17 / 24, y + CARD_HEIGHT / 2 + CARD_HEIGHT / 3, true, card.suit);
		}

	}
	
	protected void paintHand(Graphics g) {		
		int dealCards = game.dealPile.getHeight();
		
		if (dealCards == 0) {
			if (game.canFlipHand()) {
				g.setColor(new Color(0x00FF00));
				g.fillOval(DRAW_X_POS + CARD_WIDTH / 8, DRAW_Y_POS + (CARD_HEIGHT - CARD_WIDTH) / 2 + CARD_WIDTH / 8, CARD_WIDTH * 6 / 8, CARD_WIDTH * 6 / 8);
				g.setColor(new Color(0x008000));
				g.fillOval(DRAW_X_POS + CARD_WIDTH / 6, DRAW_Y_POS + (CARD_HEIGHT - CARD_WIDTH) / 2 + CARD_WIDTH / 6, CARD_WIDTH * 4 / 6, CARD_WIDTH * 4 / 6);
			} else {
				Graphics2D g2 = (Graphics2D) g;
				g2.setColor(new Color(0xFF0000));
				g2.setStroke(new BasicStroke(5));
				g2.drawLine(DRAW_X_POS + CARD_WIDTH / 8, DRAW_Y_POS + (CARD_HEIGHT - CARD_WIDTH) / 2 + CARD_WIDTH / 8, DRAW_X_POS + CARD_WIDTH * 7 / 8, DRAW_Y_POS + (CARD_HEIGHT - CARD_WIDTH) / 2 + CARD_WIDTH * 7 / 8);
				g2.drawLine(DRAW_X_POS + CARD_WIDTH / 8, DRAW_Y_POS + (CARD_HEIGHT - CARD_WIDTH) / 2 + CARD_WIDTH * 7 / 8, DRAW_X_POS + CARD_WIDTH * 7 / 8, DRAW_Y_POS + (CARD_HEIGHT - CARD_WIDTH) / 2 + CARD_WIDTH / 8);
				g2.setStroke(new BasicStroke(1));
			}
			
		} else {
			for (int i = 0; i < dealCards / 8 + 1; ++i) {
				drawBackOfCard(g, DRAW_X_POS + 2 * i, DRAW_Y_POS + i);
			}
		}
		
		int shown = game.showingPile.getHeight() + game.discardPile.getHeight();
		
		if (shown != 0) {			
			int xpos = DRAW_X_POS + TABLEAU_DISTANCE;
			int ypos = DRAW_Y_POS;
			int i = 0;
			
			Iterator<Card> reverseIter = game.discardPile.cards.descendingIterator();
			while (reverseIter.hasNext()) {
				Card card = reverseIter.next();
				drawCard(g, xpos, ypos, card);
				
				if (i % 8 == 7) {
					xpos += 2;
					ypos += 1;
				}
				++i;
			}
			
			reverseIter = game.showingPile.cards.descendingIterator();
			while (reverseIter.hasNext()) {
				Card card = reverseIter.next();
				drawCard(g, xpos, ypos, card);
				xpos += 28;
			}
		}

	}
	
	protected void paintTableau(Graphics g) {
		for (int t = 0; t < 7; ++t) {
			int ypos = TABLEAU_Y_POS;
			for (@SuppressWarnings("unused") Card card : game.tableau[t].hiddenPile.cards) {
				drawBackOfCard(g, TABLEAU_X_POS + t * TABLEAU_DISTANCE, ypos);
				ypos += TABLEAU_Y_DISTANCE;
			}
			
			Iterator<Card> reverseIter = game.tableau[t].visiblePile.cards.descendingIterator();
			while (reverseIter.hasNext()) {
				Card card = reverseIter.next();
				drawCard(g, TABLEAU_X_POS + t * TABLEAU_DISTANCE, ypos, card);
				ypos += TABLEAU_Y_DISTANCE;
			}
		}
	}
	
	protected void paintFoundations(Graphics g) {		
		for (int i = 0; i < 4; ++i) {
			g.setColor(new Color(0x000000));
			g.drawRect(FOUNDATION_X_POS + i * TABLEAU_DISTANCE, FOUNDATION_Y_POS, CARD_WIDTH, CARD_HEIGHT);
			
			if (game.foundations[i].getHeight() != 0) {
				drawCard(g, FOUNDATION_X_POS + i * TABLEAU_DISTANCE, FOUNDATION_Y_POS, game.foundations[i].getTopCard());
			}
		}
	}
	
	protected void paintHolding(Graphics g) {
		if (game.holding.getHeight() == 0) {
			return;
		}
		
		int ypos = recentMouseY + recentCardY;
		
		Iterator<Card> reverseIter = game.holding.cards.descendingIterator();
		while (reverseIter.hasNext()) {
			Card card = reverseIter.next();
			drawCard(g, recentMouseX + recentCardX, ypos, card);
			ypos += 15;
		}
	}
	
	protected void paintStatusBar(Graphics g) {		
		g.setColor(new Color(0xFFFFFF));
		g.fillRect(0, WINDOW_HEIGHT - 27, WINDOW_WIDTH, 27);

		g.setColor(new Color(0x000000));
		g.setFont(new Font("Arial", Font.BOLD, 14));
		
		String seedString = String.format("Seed: 0x%08X", game.seed);
		//g.drawString(seedString, 9, WINDOW_HEIGHT - 9);
		
		String timeString = String.format("Time: %d", game.getTime());
		int timeWidth = g.getFontMetrics().stringWidth(timeString);
		if (game.options.timed) {
			g.drawString(timeString, WINDOW_WIDTH - 9 - timeWidth, WINDOW_HEIGHT - 9);
		} else {
			timeWidth = 0;
		}
		
		if (game.options.scoring != Solitaire.ScoringMode.None) {
			String scoreString = "";
			if (game.options.scoring == Solitaire.ScoringMode.Standard) {
				scoreString = String.format("%d", game.getScore());
			} else if (game.options.scoring == Solitaire.ScoringMode.Vegas) {
				int score = game.getScore();
				scoreString = score < 0 ? String.format("-$%d", -score) : String.format("$%d", score);
			} else {
				assert(false);
			}
			
			int labelWidth = g.getFontMetrics().stringWidth("Score: ");
			int scoreWidth = g.getFontMetrics().stringWidth(scoreString);
			g.drawString("Score: ", WINDOW_WIDTH - 16 - timeWidth - scoreWidth - labelWidth, WINDOW_HEIGHT - 9);
	
			if (game.getScore() < 0) {
				g.setColor(new Color(0xFF0000));
			}
			g.drawString(scoreString, WINDOW_WIDTH - 16 - timeWidth - scoreWidth, WINDOW_HEIGHT - 9);	
		}
	}
	
	@Override
	public void paint(Graphics g) {
		if (game == null) {
			return;
		}
				
		super.paint(g);
		g.setColor(new Color(0x008000));
		g.fillRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
		
		paintHand(g);
		paintTableau(g);
		paintFoundations(g);
		paintHolding(g);
		paintStatusBar(g);
		
		if (game.isWon()) {
			g.setColor(new Color(0xFFFF00));
			g.setFont(new Font("Arial", Font.BOLD, 48));
			g.drawString("YOU WON!", WINDOW_WIDTH / 2 - 131, WINDOW_HEIGHT / 2);
		}
		
		updateMenubar();
	}
	
	public GUI.CardClickInfo detectMousePosition(int x, int y) {
		int tableau = -1;
		int cards = 0;
		int cardX = 0;
		int cardY = 0;
				
		if (y >= FOUNDATION_Y_POS && y <= FOUNDATION_Y_POS + CARD_HEIGHT) {
			if (x >= FOUNDATION_X_POS && ((x - FOUNDATION_X_POS) % TABLEAU_DISTANCE) < CARD_WIDTH) {
				cards = 1;
				tableau = Solitaire.FOUNDATION_COLUMN_BASE + ((x - FOUNDATION_X_POS) / TABLEAU_DISTANCE);
				cardX = FOUNDATION_X_POS + ((x - FOUNDATION_X_POS) / TABLEAU_DISTANCE) * TABLEAU_DISTANCE;
				cardY = FOUNDATION_Y_POS;
			}
		}
		
		int draw3Shift = game.options.draw3 ? DEAL_3_SHIFT_DIST * (game.showingPile.getHeight() - 1) : 0;
		if (y >= DRAW_Y_POS && y <= DRAW_Y_POS + CARD_HEIGHT + game.dealPile.getHeight() / 8) {
			if (x >= DRAW_X_POS && x <= DRAW_X_POS + CARD_WIDTH + game.dealPile.getHeight() / 4) {
				cards = 1;
				tableau = Solitaire.DRAW_PILE_BASE;
				cardX = DRAW_X_POS;
				cardY = DRAW_Y_POS;
			}
			if (x >= DRAW_X_POS + TABLEAU_DISTANCE + draw3Shift && x <= DRAW_X_POS + TABLEAU_DISTANCE + CARD_WIDTH + draw3Shift + game.discardPile.getHeight() / 4) {
				cards = 1;
				tableau = Solitaire.HAND_COLUMN_BASE;
				cardX = DRAW_X_POS + TABLEAU_DISTANCE + draw3Shift;
				cardY = DRAW_Y_POS;
			}
		}
		
		if (y >= TABLEAU_Y_POS) {
			if (x >= TABLEAU_X_POS && ((x - TABLEAU_X_POS) % TABLEAU_DISTANCE) < CARD_WIDTH) {
				tableau = ((x - TABLEAU_X_POS) / TABLEAU_DISTANCE);
				if (tableau < 7) {
					int hidden = game.tableau[tableau].hiddenPile.getHeight();
					
					int cardsIn = (y - TABLEAU_Y_POS) / TABLEAU_Y_DISTANCE;
					
					if (cardsIn - hidden >= 0) {
						cards = game.tableau[tableau].visiblePile.getHeight() + hidden - cardsIn;
						if (cards < 1) {
							cards = 1;
							cardsIn =  game.tableau[tableau].visiblePile.getHeight() + hidden;
						}
						cardX = TABLEAU_X_POS + ((x - TABLEAU_X_POS) / TABLEAU_DISTANCE) * TABLEAU_DISTANCE;
						cardY = TABLEAU_Y_POS + cardsIn * TABLEAU_Y_DISTANCE;
						
					} else {
						tableau = -1;
					}
				} else {
					tableau = -1;
				}
			}
		}
		
		return new CardClickInfo(tableau, cards, cardX, cardY);
	}
	
	void recomputeDistances(int w, int h) {
		WINDOW_WIDTH = w;
		WINDOW_HEIGHT = h;
		TABLEAU_DISTANCE = WINDOW_WIDTH * 138 / 1000;
		if (TABLEAU_DISTANCE <= CARD_WIDTH) {
			TABLEAU_DISTANCE = CARD_WIDTH + 1;
		}
		DRAW_X_POS = (WINDOW_WIDTH - 6 * TABLEAU_DISTANCE - CARD_WIDTH) / 2;
		TABLEAU_X_POS = DRAW_X_POS;
		FOUNDATION_X_POS = TABLEAU_X_POS + 3 * TABLEAU_DISTANCE;
	}
	
	void start(GameOptions opt) {
		opt.setInitialScore(game.score);
		game = new Solitaire(opt);
		opt.useSeed = false;
		repaint();
	}
	
	void startFromSeed(int seed) {
		GameOptions opt = game.options;
		opt.useSeed = true;
		opt.initialSeed = seed;
		opt.setInitialScore(game.score);
		
		if (opt.cumulative && opt.scoring == Solitaire.ScoringMode.Vegas) {
			opt.initialScore = game.score;
		}
		
		start(opt);
	}
	
	GUI() {
		game = null;
		
		cardImages = new BufferedImage[4][];
		
		cardImages[Card.Suit.Club.ordinal()] = new BufferedImage[3];
		cardImages[Card.Suit.Diamond.ordinal()] = new BufferedImage[3];
		cardImages[Card.Suit.Spade.ordinal()] = new BufferedImage[3];
		cardImages[Card.Suit.Heart.ordinal()] = new BufferedImage[3];
		
		try {
			cardImages[Card.Suit.Club.ordinal()][CARD_IMAGE_JACK] = ImageIO.read(getClass().getResourceAsStream("/img/clubjack.png"));
			cardImages[Card.Suit.Club.ordinal()][CARD_IMAGE_QUEEN] = ImageIO.read(getClass().getResourceAsStream("/img/clubqueen.png"));
			cardImages[Card.Suit.Club.ordinal()][CARD_IMAGE_KING] = ImageIO.read(getClass().getResourceAsStream("/img/clubking.png"));

			cardImages[Card.Suit.Diamond.ordinal()][CARD_IMAGE_JACK] = ImageIO.read(getClass().getResourceAsStream("/img/diamondjack.png"));
			cardImages[Card.Suit.Diamond.ordinal()][CARD_IMAGE_QUEEN] = ImageIO.read(getClass().getResourceAsStream("/img/diamondqueen.png"));
			cardImages[Card.Suit.Diamond.ordinal()][CARD_IMAGE_KING] = ImageIO.read(getClass().getResourceAsStream("/img/diamondking.png"));
			
			cardImages[Card.Suit.Spade.ordinal()][CARD_IMAGE_JACK] = ImageIO.read(getClass().getResourceAsStream("/img/spadejack.png"));
			cardImages[Card.Suit.Spade.ordinal()][CARD_IMAGE_QUEEN] = ImageIO.read(getClass().getResourceAsStream("/img/spadequeen.png"));
			cardImages[Card.Suit.Spade.ordinal()][CARD_IMAGE_KING] = ImageIO.read(getClass().getResourceAsStream("/img/spadeking.png"));
			
			cardImages[Card.Suit.Heart.ordinal()][CARD_IMAGE_JACK] = ImageIO.read(getClass().getResourceAsStream("/img/heartjack.png"));
			cardImages[Card.Suit.Heart.ordinal()][CARD_IMAGE_QUEEN] =ImageIO.read(getClass().getResourceAsStream("/img/heartqueen.png"));
			cardImages[Card.Suit.Heart.ordinal()][CARD_IMAGE_KING] = ImageIO.read(getClass().getResourceAsStream("/img/heartking.png"));

			
		} catch (Exception e) { ; }
		
		BufferedImage bf = new BufferedImage(2, 2, BufferedImage.TYPE_INT_BGR);
		bf.setRGB(0, 0, 0x0000FF);
		bf.setRGB(1, 1, 0x0000FF);
		bf.setRGB(0, 1, 0x000080);
		bf.setRGB(1, 0, 0x000080);
		cardBackPaint = new TexturePaint(bf, new Rectangle(0, 0, 5, 5));
		
		
		bf = new BufferedImage(4, 4, BufferedImage.TYPE_INT_BGR);
		for (int i = 0; i < 4; ++i) {
			bf.setRGB((i + 0) % 4, i, 0xFFFFFF);
			bf.setRGB((i + 1) % 4, i, 0xFFFF00);
			bf.setRGB((i + 2) % 4, i, 0x000000);
			bf.setRGB((i + 3) % 4, i, 0xFF0000);
		}
		pictureCardPaint = new TexturePaint(bf, new Rectangle(0, 0, 5, 5));
		
		
		setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));

		JPanel gamePanel = new JPanel();
		gamePanel.setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
		add(gamePanel);
		
		(new Timer(200, new ActionListener() {
		    @Override
		    public void actionPerformed(ActionEvent e) {
		        repaint();
		    }
		})).start();
		
		gamePanel.addMouseMotionListener(new MouseMotionListener() {
			@Override
			public void mouseDragged(MouseEvent e) {
				if (game.holding.getHeight() == 0) {
					return;
				}
				
				recentMouseX = e.getX();
				recentMouseY = e.getY();
				
				repaint();
			}

			@Override
			public void mouseMoved(MouseEvent e) {
				
			}			
		});
		
		gamePanel.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
			}

			@Override
			public void mousePressed(MouseEvent e) {
				if (game.isWon()) {
					return;
				}
												
				recentMouseX = e.getX();
				recentMouseY = e.getY();
								
				CardClickInfo clickInfo = detectMousePosition(recentMouseX, recentMouseY);
				
				if (clickInfo.tableau == Solitaire.DRAW_PILE_BASE) {
					game.flipHand(game.options.draw3 ? 3 : 1);
					repaint();
				
				} else if (clickInfo.tableau != -1 && game.holding.getHeight() == 0) {
					
					if (clickInfo.tableau < 7) {
						Tableau tab = game.tableau[clickInfo.tableau];
						if (tab.visiblePile.getHeight() == 0 && tab.hiddenPile.getHeight() != 0) {
							game.flipColumn(clickInfo.tableau);
							repaint();
							return;
						}
					}
					
					game.hold(clickInfo.tableau, clickInfo.cards);
					recentCardX = clickInfo.cardX - recentMouseX;
					recentCardY = clickInfo.cardY - recentMouseY;
					repaint();
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				CardClickInfo clickInfo = detectMousePosition(recentMouseX, recentMouseY);
				
				if (game.holding.getHeight() != 0) {
					game.release(clickInfo.tableau);
					repaint();
				}
			}

			@Override
			public void mouseEntered(MouseEvent e) {				
			}

			@Override
			public void mouseExited(MouseEvent e) {				
			}
		});
		
		game = new Solitaire(new GameOptions());
	}
	
	public void updateMenubar() {
		undoBtn.setEnabled(game.canUndo());
		cumulativeBtn.setEnabled(game.options.scoring == Solitaire.ScoringMode.Vegas);
	}
	
	public void createOptionsDialog(JFrame frame) {
		final JDialog dialog = new JDialog(frame, "Options", Dialog.ModalityType.DOCUMENT_MODAL);
		dialog.setBounds(150, 150, 250, 200);
		dialog.setResizable(false);
		
		ActionListener escListener = new ActionListener() {
	        @Override
	        public void actionPerformed(ActionEvent e) {
	            dialog.setVisible(false);
	        }
	    };

	    dialog.getRootPane().registerKeyboardAction(escListener,
	            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
	            JComponent.WHEN_IN_FOCUSED_WINDOW);
	    
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		dialog.add(panel);
		
		JPanel drawPanel = new JPanel();
		drawPanel.setBorder(new TitledBorder("Draw"));
		drawPanel.setLayout(new BoxLayout(drawPanel, BoxLayout.Y_AXIS));
		ButtonGroup drawButtonGroup = new ButtonGroup();
		JRadioButton draw1Btn = new JRadioButton("Draw 1");
		JRadioButton draw3Btn = new JRadioButton("Draw 3");
		drawButtonGroup.add(draw1Btn);
		drawButtonGroup.add(draw3Btn);
		drawPanel.add(draw1Btn);
		drawPanel.add(draw3Btn);
		
		JPanel scoringPanel = new JPanel();
		scoringPanel.setBorder(new TitledBorder("Scoring"));
		scoringPanel.setLayout(new BoxLayout(scoringPanel, BoxLayout.Y_AXIS));
		ButtonGroup scoringButtonGroup = new ButtonGroup();
		JRadioButton standardScoringBtn = new JRadioButton("Standard");
		JRadioButton vegasScoringBtn = new JRadioButton("Vegas");
		JRadioButton noScoringBtn = new JRadioButton("None");
		scoringButtonGroup.add(standardScoringBtn);
		scoringButtonGroup.add(vegasScoringBtn);
		scoringButtonGroup.add(noScoringBtn);
		scoringPanel.add(standardScoringBtn);
		scoringPanel.add(vegasScoringBtn);
		scoringPanel.add(noScoringBtn);
		
		JPanel sidewaysPanel = new JPanel();
		sidewaysPanel.setLayout(new BoxLayout(sidewaysPanel, BoxLayout.X_AXIS));
		sidewaysPanel.add(drawPanel);
		sidewaysPanel.add(scoringPanel);
		
		panel.add(sidewaysPanel);
		
	    JButton confirmBtn = new JButton("   OK   ");
	    dialog.getRootPane().setDefaultButton(confirmBtn);
	    confirmBtn.addActionListener(new ActionListener() {
	    	@Override
	    	public void actionPerformed(ActionEvent e) {
	    		Solitaire.ScoringMode scoringMode = Solitaire.ScoringMode.Standard;
	    		if (standardScoringBtn.isSelected()) scoringMode = Solitaire.ScoringMode.Standard;
	    		else if (vegasScoringBtn.isSelected()) scoringMode = Solitaire.ScoringMode.Vegas;
	    		else if (noScoringBtn.isSelected()) scoringMode = Solitaire.ScoringMode.None;
	    		else assert false;
	    		
	    		boolean draw3 = draw3Btn.isSelected();
	    		
	    		if (draw3 != game.options.draw3 || scoringMode != game.options.scoring) {
	    			GameOptions opt = game.options;
					opt.scoring = scoringMode;
					opt.draw3 = draw3;
					start(opt);
	    		}
	    		
	    		
	    	}
	    });

	    JButton cancelBtn = new JButton("Cancel");
	    cancelBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dialog.setVisible(false);
			}
	    });

	    JPanel confirmCancelPanel = new JPanel();
		confirmCancelPanel.setLayout(new BoxLayout(confirmCancelPanel, BoxLayout.X_AXIS));
		confirmCancelPanel.add(confirmBtn);
		confirmCancelPanel.add(cancelBtn);
		panel.add(confirmCancelPanel);
	    
	    if (game.options.draw3) {
	    	draw3Btn.setSelected(true);
	    } else {
	    	draw1Btn.setSelected(true);
	    }
	    
	    if (game.options.scoring == Solitaire.ScoringMode.Standard) standardScoringBtn.setSelected(true);
	    else if (game.options.scoring == Solitaire.ScoringMode.Vegas) vegasScoringBtn.setSelected(true);
	    else noScoringBtn.setSelected(true);
	    
		dialog.setVisible(true);
	}
	
	public void addMenus(JFrame frame) {
		JMenuBar menu = new JMenuBar();
		frame.setJMenuBar(menu);
		
		JMenu gameMenu = new JMenu("Game");
		menu.add(gameMenu);
		
		JMenuItem dealBtn = new JMenuItem("Deal                        ");
		dealBtn.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));
		dealBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				GameOptions opt = game.options;
				opt.setInitialScore(game.score);
				start(opt);
			}
		});
		gameMenu.add(dealBtn);
		
		gameMenu.add(new JSeparator());
		
		undoBtn = new JMenuItem("Undo");
		undoBtn.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit ().getMenuShortcutKeyMask()));
		undoBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (game.canUndo() && !game.isWon()) {
					game.undo();
					repaint();
				}
			}
		});
		gameMenu.add(undoBtn);
		
		JMenuItem optBtn = new JMenuItem("Options...");
		optBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				createOptionsDialog(frame);
			}
		});
		//gameMenu.add(optBtn);
		
		JMenuItem deal1Btn = new JMenuItem("Play draw 1");
		deal1Btn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				GameOptions opt = game.options;
				opt.setInitialScore(game.score);
				opt.draw3 = false;
				start(opt);
			}
		});
		gameMenu.add(deal1Btn);

		JMenuItem deal3Btn = new JMenuItem("Play draw 3");
		deal3Btn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				GameOptions opt = game.options;
				opt.setInitialScore(game.score);
				opt.draw3 = true;
				start(opt);
			}
		});
		gameMenu.add(deal3Btn);
		
		gameMenu.add(new JSeparator());
		
		JMenuItem stdBtn = new JMenuItem("Standard scoring");
		stdBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				GameOptions opt = game.options;
				opt.scoring = Solitaire.ScoringMode.Standard;
				start(opt);
			}
		});
		gameMenu.add(stdBtn);
		
		JMenuItem vegasBtn = new JMenuItem("Vegas scoring");
		vegasBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				GameOptions opt = game.options;
				opt.scoring = Solitaire.ScoringMode.Vegas;
				start(opt);
			}
		});
		gameMenu.add(vegasBtn);

		JMenuItem noneBtn = new JMenuItem("No scoring");
		noneBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				GameOptions opt = game.options;
				opt.scoring = Solitaire.ScoringMode.None;
				start(opt);
			}
		});
		gameMenu.add(noneBtn);
		
		gameMenu.add(new JSeparator());
		
		timedBtn = new JMenuItem("Disable timed game");
		timedBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (game.options.timed) {
					game.options.timed = false;
					timedBtn.setText("Enable timed game");
				} else {
					game.options.timed = true;
					timedBtn.setText("Disable timed game");
				}
			}
		});
		gameMenu.add(timedBtn);
		
		cumulativeBtn = new JMenuItem("Enable cumulative scoring");
		cumulativeBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (game.options.cumulative) {
					game.options.cumulative = false;
					cumulativeBtn.setText("Enable cumulative scoring");
				} else {
					game.options.cumulative = true;
					cumulativeBtn.setText("Disable cumulative scoring");
				}
			}
		});
		gameMenu.add(cumulativeBtn);
	
		
		gameMenu.add(new JSeparator());
		
		
		int presetSeeds[] = { 0x567C0882, 0xCCC0A516, 0x9811B537, 0xE864D2BA, 0x66B4C3F5, 0xB178FAD4, 0x74847F14 };
		
		JMenu presetMenu = new JMenu("Preset games");
		gameMenu.add(presetMenu);
		
		for (int i = 0; i < presetSeeds.length; ++i) {
			final int j = i;
			JMenuItem preset = new JMenuItem(String.format("Game #%d          ", j + 1));
			preset.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					startFromSeed(presetSeeds[j]);
				}
			});
			presetMenu.add(preset);
		}
		
		
		
		
		gameMenu.add(new JSeparator());


		JMenuItem closeBtn = new JMenuItem("Exit");
		closeBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				frame.dispose();
			}
		});
		gameMenu.add(closeBtn);
		
		
		JMenu helpMenu = new JMenu("Help");
		menu.add(helpMenu);
		
		JMenuItem howToBtn = new JMenuItem("Help...");
		howToBtn.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
		howToBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
			}
		});
		helpMenu.add(howToBtn);
		
		JMenuItem tipBtn = new JMenuItem("Tip");
		tipBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
			}
		});
		//helpMenu.add(tipBtn);
		
		
		helpMenu.add(new JSeparator());

		JMenuItem aboutBtn = new JMenuItem("About...           ");
		aboutBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final ImageIcon icon = new ImageIcon("C:/Users/Alex/Desktop/icon.png");
		        JOptionPane.showMessageDialog(null, "Solitiare\nVersion 1.3\n\nCopyright Alex Boxall 2021-2022\nBSD 3-clause license", "About", JOptionPane.INFORMATION_MESSAGE, icon);
			}
		});
		helpMenu.add(aboutBtn);

		addComponentListener(new ComponentAdapter() {
		    public void componentResized(ComponentEvent componentEvent) {
		    	recomputeDistances(getBounds().width, getBounds().height);
		    	repaint();
		    }
		});
		
		updateMenubar();
	}
	
	
	public static void createWindow() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e1) {
			e1.printStackTrace();
		}
		
		JFrame frame = new JFrame("Solitaire");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		try {
			ImageIcon icon = (ImageIcon) FileSystemView.getFileSystemView().getSystemIcon(new File("solitaire.exe"));
			frame.setIconImage(icon.getImage());
		} catch (Exception e) { ; }
		
		GUI gui = new GUI();
		gui.addMenus(frame);

		frame.setContentPane(gui);
		frame.pack();
		frame.setVisible(true);
	}
	
	public static void main(String[] args) {
		createWindow();
	}
}
