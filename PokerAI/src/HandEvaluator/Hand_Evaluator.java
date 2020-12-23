package HandEvaluator;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class Hand_Evaluator {
    
    int first_card;
    int second_card;
    HashMap<String, Integer> primeMap;
    HashMap<String, Integer> handMap;

    /** [Hand i j] creates a Hand object with card [i] as the first card and [j] as they second
    */
    public Hand_Evaluator(int i, int j) {
        this.first_card = i;
        this.second_card = j;
    }

    /** */
    public Hand_Evaluator() {
        this.primeMap = new HashMap<String, Integer>();
        this.handMap = new HashMap<String, Integer>();
    }
    
    /** */
    public void populateMap() {

        primeMap.put("2", 2);
        primeMap.put("3", 3);
        primeMap.put("4", 5);
        primeMap.put("5", 7);
        primeMap.put("6", 11);
        primeMap.put("7", 13);
        primeMap.put("8", 17);
        primeMap.put("9", 19);
        primeMap.put("T", 23);
        primeMap.put("J", 29);
        primeMap.put("Q", 31);
        primeMap.put("K", 37);
        primeMap.put("A", 41);
        
        handMap.put("2", 0);
        handMap.put("3", 1);
        handMap.put("4", 2);
        handMap.put("5", 3);
        handMap.put("6", 4);
        handMap.put("7", 5);
        handMap.put("8", 6);
        handMap.put("9", 7);
        handMap.put("T", 8);
        handMap.put("J", 9);
        handMap.put("Q", 10);
        handMap.put("K", 11);
        handMap.put("A", 12);
    }
    /**
    [bitHandRep s] is the integer bit representation of the string representation
    [s] of the current hand. Reference Cactus Kev's hand evaluator algorithm

    +--------+--------+--------+--------+
    |xxxbbbbb|bbbbbbbb|cdhsrrrr|xxpppppp|
    +--------+--------+--------+--------+
    
    p = prime number of rank (deuce=2,trey=3,four=5,five=7,...,ace=41) 
    r = rank of card (deuce=0,trey=1,four=2,five=3,...,ace=12)
    cdhs = suit of card
    b = bit turned on depending on rank of card

    */
    public Hand_Evaluator bitHandRep(String s) {
                
        char first_card = s.charAt(0);
        char second_card = s.charAt(2);
        
        populateMap();

        int primeRank1 = primeMap.get(Character.toString(first_card));
        int rank1 = handMap.get(Character.toString(first_card));
        int primeRank2 = primeMap.get(Character.toString(second_card));
        int rank2 = handMap.get(Character.toString(second_card));

        int first, second, suitrank1, suitrank2;
        
        if (s.charAt(1) == 's') {
            suitrank1 = 1;
        }
        else if (s.charAt(1) == 'h') {
            suitrank1 = 2;
        }
        else if (s.charAt(1) == 'd') {
            suitrank1 = 3;
        }
        else {
            suitrank1 = 4;
        }
        if (s.charAt(3) == 's') {
            suitrank2 = 1;
        }
        else if (s.charAt(3) == 'h') {
            suitrank2 = 2;
        }
        else if (s.charAt(3) == 'd') {
            suitrank2 = 3;
        }
        else {
            suitrank2 = 4;
        }
        
        System.out.println("rank1: " + rank1);
        System.out.println("rank2: " + rank2);
        first = primeRank1 + (rank1 << 8) + (1 << (11 + suitrank1)) 
                    + (1 << (16 + rank1));
        second = primeRank2 + (rank2 << 8) + (1 << (11 + suitrank2)) 
                    + (1 << (16 + rank2));
        
        return new Hand_Evaluator(first, second);
    } 
    
    // Returns the integer represent for a single card
    public int bitCardRep(String s) {
        
        char first_card = s.charAt(0);
        
        populateMap();

        int primeRank1 = primeMap.get(Character.toString(first_card));
        int rank1 = handMap.get(Character.toString(first_card));

        int first, suitrank1;
        
        if (s.charAt(1) == 's') {
            suitrank1 = 1;
        }
        else if (s.charAt(1) == 'h') {
            suitrank1 = 2;
        }
        else if (s.charAt(1) == 'd') {
            suitrank1 = 3;
        }
        else {
            suitrank1 = 4;
        }
    
        first = primeRank1 + (rank1 << 8) + (1 << (11 + suitrank1)) 
                    + (1 << (16 + rank1));
        
        return first;
    } 

}