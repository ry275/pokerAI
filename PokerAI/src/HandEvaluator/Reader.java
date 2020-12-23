package HandEvaluator;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/** 
Hand Evaluator
*/
class Reader {
	Hand_Evaluator hand;              // 32 bit representation of hand
    int pos;                // position number 1-6
    int potsize;            // total amount in pot
    int effstack;           // effective stack size i.e. lowest stack on table
    int stack;              // current stack size
    String flushPairs;          
    

    /** */
    public void input() {
        BufferedReader reader = null;
        try {

            reader = new BufferedReader(new InputStreamReader(System.in));

            Hand_Evaluator start = new Hand_Evaluator();
            Tables t = new Tables();

            // // Redo later for graphics where you can highlight the hand on a GTO table and specify suit
            // System.out.println("What is your hand? Denote hearts as h, spades as s, clubs as c, diamonds as d. Ex: Ah6c");

            // hand = start.bitHandRep(reader.readLine());

            // System.out.println("int first: " + hand.first_card);
            // System.out.println("binary first: " + Integer.toBinaryString(hand.first_card));
            // System.out.println("int second: " + hand.second_card);
            // System.out.println("binary second: " + Integer.toBinaryString(hand.second_card));
            
            // int x = 0;
            // while (x < 1000) {
            //     System.out.println("flushes array?");
            //     String s = reader.readLine();
            //     System.out.println("idx?");
            //     int idx = Integer.parseInt(reader.readLine());
            //     t.flushMap(s, idx);
            //     x++;
            // }

            flushPairs = reader.readLine();

            // Click a button or something
            // System.out.println("What is your position in the hand? Denote 1 as SB up to 6 as BTN");
            // pos = Integer.parseInt(reader.readLine());

            // System.out.println("What is the potsize?");
            // potsize = Integer.parseInt(reader.readLine());
            
            // System.out.println("What is the effective stack size? The lowest stack on the table");
            // effstack = Integer.parseInt(reader.readLine());
            
            // System.out.println("What is your current stack size?");
            // stack = Integer.parseInt(reader.readLine());

            reader.close();
        }   

        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** */
    public void output() {
        try
        {
            PrintWriter writer = new PrintWriter(System.out);

            Tables t = new Tables();

            writer.println(t.mapAuto(flushPairs));

            writer.close();
        }

        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    
    /** */
    public Reader() {
        input();
        output();
    }

    /** */
    public static void main(String [] Args) 
    {
        new Reader();
    }
}
