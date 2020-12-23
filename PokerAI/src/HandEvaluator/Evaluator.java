package HandEvaluator;

import java.io.*;
import java.util.*;

public class Evaluator implements Constants{

    int c1;
    int c2;
    int c3;
    int c4;
    int c5;
    
    /** [Evaluator c1 c2 c3 c4 c5] inits 5 cards to be evaluated in terms of strength using 
     * a linear system based on uniqueness of hand. However, these numbers will need to be 
     * altered given our NB setup for evaluating expected strength and values of hands later on
    */
    public Evaluator(int c1, int c2, int c3, int c4, int c5) {
        this.c1 = c1;
        this.c2 = c2;
        this.c3 = c3;
        this.c4 = c4;
        this.c5 = c5;
    }
    
    public Evaluator(int[] cs)
    {
    		this.c1 = cs[0];
    		this.c2 = cs[1];
    		this.c3 = cs[2];
    		this.c4 = cs[3];
    		this.c5 = cs[4];
    }

    /** [handRank val] simply returns ranking of hand given strength [val] where 1 is highcard
     * and 9 is straight flush. Ordering is subject to change
    */
    public static int handRank(int val) {
        if (val > 6185) return HIGH_CARD;        
        if (val > 3325) return ONE_PAIR;    
        if (val > 2467) return TWO_PAIR;         
        if (val > 1609) return THREE_OF_A_KIND; 
        if (val > 1599) return STRAIGHT;         
        if (val > 322)  return FLUSH;            
        if (val > 166)  return FULL_HOUSE;       
        if (val > 10)   return FOUR_OF_A_KIND;   
        return STRAIGHT_FLUSH;                   
    }

    /** [binSearchProducts x] is a binary search algorithm that returns the indice of the
     * specified 
    */
    public static int binSearchProducts(int x) {
        int low = 0;
        int high = 4888;
        int mid = 2444;
        Tables tables = new Tables();
        while (low <= high) {
            mid = (low + high) / 2;
            if (x < tables.products.get(mid)) {
                high = mid - 1;
            }
            else if (x > tables.products.get(mid)) {
                low = mid + 1;
            }
            else {
                return mid;
            }
        }
        return -1; // should never happen else we are fucked
    }
    /**  */
    public int evaluate() {

        Tables tables = new Tables();
        int flushConst = 61440; // add to constants later 
        int twoByteConstant = 255;
        int x = (c1 | c2 | c3 | c4 | c5) >> 16;

        // Flush check
        if (tables.flushes.containsKey(x)) {
            return tables.flushes.get(x);
        }
                // Straight and High Card check 
        else if (tables.strAndHC.containsKey(x)) {
            return tables.strAndHC.get(x);
        }
        // The rest

        else {
            x = (c1 & twoByteConstant) * (c2 & twoByteConstant) * 
                (c3 & twoByteConstant) * (c4 & twoByteConstant) * (c5 & twoByteConstant);
            int xValue = binSearchProducts(x);
            return tables.values.get(xValue);
        }


    }
}