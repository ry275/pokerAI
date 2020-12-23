package HandEvaluator;

import java.io.*;
import java.util.*;

public class Deck {

    public static final int[] primes = new int[]{2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41};
    public List<Integer> deck;

    public Deck() {
        deck = new ArrayList<Integer>();
        // shuffleDeck();
    }

    /** [initDeck] populates deck with all 52 cards in order where each card is represented 
     * by some integer */
    public void initDeck() {
        int suit = 32768;

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 13; j++) {
                int card = primes[j] | (j << 8) | suit | (1 << (16+j));
                deck.add(card);
            }
        }
    }
    /** [findCard c] is the indice of card [c] in the deck, used as a test method.
    * Returns -1 if not found */
    public int findCard(int c) {
        for (int i = 0; i < deck.size(); i++) {
            if (deck.get(i) == c) {
                return i;
            }
        }
        return -1;
    }  
    /** [shuffleDeck] shuffles and updates deck randomly before the start of each game */
    public void shuffleDeck() {
        // this should be random
        Collections.shuffle(deck);
    }

}