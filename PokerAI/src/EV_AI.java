import java.io.*;
import java.util.*;
import HandEvaluator.*;

import HandEvaluator.Hand_Evaluator;

public class EV_AI {

    /** [potOdds] calculates the pot odds when faced with a bet.*/
    public static double potOdds(int raise, int pot) {
        return (raise / (raise + pot));
    }
    
    public static List<Action> actionsPreFlop(Hand hand, Game game)
    {
    		List<Action> actions = new ArrayList<Action>();
		
		int[] features = Util_AI.preflopFeatures(game, hand);
		int sum = features[0] + features[1] + features[2];
		if (sum == 3 || sum == 2) 
		{
			actions.add(new Action(Move.RAISE, game.currentTurn, game.currentBet));
			actions.add(new Action(Move.CALL, game.currentTurn, game.currentBet - game.lastStageTotalBets.get(game.currentTurn)));
			actions.add(new Action(Move.CHECK, game.currentTurn, 0));
			actions.add(new Action(Move.FOLD, game.currentTurn, 0));
		}
		else if (sum == 1) 
		{
			actions.add(new Action(Move.CALL, game.currentTurn, game.currentBet - game.lastStageTotalBets.get(game.currentTurn)));
			actions.add(new Action(Move.CHECK, game.currentTurn, 0));
			actions.add(new Action(Move.FOLD, game.currentTurn, 0));
		}
		else
		{
			actions.add(new Action(Move.CHECK, game.currentTurn, 0));
			actions.add(new Action(Move.FOLD, game.currentTurn, 0));
		}
		
		return actions;
    }

    /** [optimalEquityMoveFlop h1 h2 c1 c2 c3 pot raise] returns the optimal action on the flop given current equity 
     * and stage of the game. Note that this can be integrated given that the code stores the current
     * state of the game i.e. cards shown, action faced, players turn, pot size, etc.
     */
    public static List<Action> optimalEquityMoveFlop(Hand h1, Hand h2, Game game, Deck deck) {

    		Hand_Evaluator HE = new HandEvaluator.Hand_Evaluator();
    		int c1 = HE.bitCardRep(game.cardsFaceUp.get(0).toStringEvaluator());
    		int c2 = HE.bitCardRep(game.cardsFaceUp.get(1).toStringEvaluator());
    		int c3 = HE.bitCardRep(game.cardsFaceUp.get(2).toStringEvaluator());
    		int pot = game.pot;
    		int raise = game.currentBet;
        // this is the basic idea that needs to be integrated
        // this is stating if equity > raise faced, then call or raise 
        double equity = equityEstimateFullInfoFlop(h1, h2, c1, c2, c3, deck);
        // this can be integrated based on stage of the game. This means either first to act or checked to
        Random rand = new Random();
        int mixedStrat = rand.nextInt(100);
        int raiseVal = (int)(pot * (mixedStrat / 100.0)) + game.bigBlind;

        // if faced with a raise 
        if (raise > 0) {
            // if probability given price is good
            if (equity > potOdds(raise, pot)) {
                if ((mixedStrat / 100.0) > equity) {
                		List<Action> actions = new ArrayList<Action>();
                		actions.add(new Action(Move.CALL, game.currentTurn, game.currentBet - game.lastStageTotalBets.get(game.currentTurn)));
                		actions.add(new Action(Move.RAISE, game.currentTurn, raiseVal));
                		actions.add(new Action(Move.RAISE, game.currentTurn, game.currentBet));
                		actions.add(new Action(Move.CHECK, game.currentTurn, 0));
                		actions.add(new Action(Move.FOLD, game.currentTurn, 0));
                		return actions;
                }
                else {
                		List<Action> actions = new ArrayList<Action>();
                		actions.add(new Action(Move.RAISE, game.currentTurn, raiseVal));
                		actions.add(new Action(Move.RAISE, game.currentTurn, game.currentBet));
                		actions.add(new Action(Move.CALL, game.currentTurn, game.currentBet - game.lastStageTotalBets.get(game.currentTurn)));
            			actions.add(new Action(Move.CHECK, game.currentTurn, 0));
            			actions.add(new Action(Move.FOLD, game.currentTurn, 0));
            			return actions;
                }
            }
            // room for bluffing
            else {
                if ((mixedStrat / 100.0) < equity) {
                		List<Action> actions = new ArrayList<Action>();
            			actions.add(new Action(Move.RAISE, game.currentTurn, raiseVal));
            			actions.add(new Action(Move.RAISE, game.currentTurn, game.currentBet));
            			actions.add(new Action(Move.CALL, game.currentTurn, game.currentBet - game.lastStageTotalBets.get(game.currentTurn)));
            			actions.add(new Action(Move.CHECK, game.currentTurn, 0));
            			actions.add(new Action(Move.FOLD, game.currentTurn, 0));
            			return actions;
                    
                }
                // fold lol
                else {
                		List<Action> actions = new ArrayList<Action>();
                		actions.add(new Action(Move.FOLD, game.currentTurn, 0));
            			actions.add(new Action(Move.RAISE, game.currentTurn, raiseVal));
            			actions.add(new Action(Move.RAISE, game.currentTurn, game.currentBet));
            			actions.add(new Action(Move.CALL, game.currentTurn, game.currentBet - game.lastStageTotalBets.get(game.currentTurn)));
            			actions.add(new Action(Move.CHECK, game.currentTurn, 0));
            			return actions;
                }
            }
        }
        else {
            if (equity == 1) {
                // Raise 75% of the time
                if (mixedStrat < 75) {
                		List<Action> actions = new ArrayList<Action>();                		
                		actions.add(new Action(Move.RAISE, game.currentTurn, raiseVal));
                		actions.add(new Action(Move.RAISE, game.currentTurn, game.currentBet));
                		actions.add(new Action(Move.CHECK, game.currentTurn, 0));
                		actions.add(new Action(Move.CALL, game.currentTurn, game.currentBet - game.lastStageTotalBets.get(game.currentTurn)));                		
                		actions.add(new Action(Move.FOLD, game.currentTurn, 0));
                		return actions;                   
                }
                else {
                		List<Action> actions = new ArrayList<Action>();                		
                		actions.add(new Action(Move.CHECK, game.currentTurn, 0));
                		actions.add(new Action(Move.RAISE, game.currentTurn, raiseVal));
                		actions.add(new Action(Move.RAISE, game.currentTurn, game.currentBet));               		
                		actions.add(new Action(Move.CALL, game.currentTurn, game.currentBet - game.lastStageTotalBets.get(game.currentTurn)));                		
                		actions.add(new Action(Move.FOLD, game.currentTurn, 0));
                		return actions;
                    
                }
            }
            // room for bluffing 
            else {
                if ((mixedStrat / 100.0) < equity) {
                		List<Action> actions = new ArrayList<Action>();  
                		actions.add(new Action(Move.RAISE, game.currentTurn, raiseVal));
                		actions.add(new Action(Move.RAISE, game.currentTurn, game.currentBet));
                		actions.add(new Action(Move.CALL, game.currentTurn, game.currentBet - game.lastStageTotalBets.get(game.currentTurn))); 
                		actions.add(new Action(Move.CHECK, game.currentTurn, 0));                       		
                		actions.add(new Action(Move.FOLD, game.currentTurn, 0));
                		return actions;
                    
                }
            }
        }
        List<Action> actions = new ArrayList<Action>();  
        actions.add(new Action(Move.CHECK, game.currentTurn, 0));
        actions.add(new Action(Move.FOLD, game.currentTurn, 0));
        actions.add(new Action(Move.CALL, game.currentTurn, game.currentBet - game.lastStageTotalBets.get(game.currentTurn)));  
		actions.add(new Action(Move.RAISE, game.currentTurn, raiseVal));
		actions.add(new Action(Move.RAISE, game.currentTurn, game.currentBet));                    		
		return actions;
    }

    /** [optimalEquityMoveTurn h1 h2 c1 c2 c3 c4 pot raise] returns the optimal action on the flop given current equity 
     * and stage of the game. Note that this can be integrated given that the code stores the current
     * state of the game i.e. cards shown, action faced, players turn, pot size, etc.
     */
    public static List<Action> optimalEquityMoveTurn(Hand h1, Hand h2, Game game, Deck deck) {

    		Hand_Evaluator HE = new HandEvaluator.Hand_Evaluator();
		int c1 = HE.bitCardRep(game.cardsFaceUp.get(0).toStringEvaluator());
		int c2 = HE.bitCardRep(game.cardsFaceUp.get(1).toStringEvaluator());
		int c3 = HE.bitCardRep(game.cardsFaceUp.get(2).toStringEvaluator());
		int c4 = HE.bitCardRep(game.cardsFaceUp.get(3).toStringEvaluator());
		int pot = game.pot;
		int raise = game.currentBet;
		
        // this is the basic idea that needs to be integrated
        // this is stating if equity > raise faced, then call or raise 
        double equity = equityEstimateFullInfoTurn(h1, h2, c1, c2, c3, c4, deck);
        // this can be integrated based on stage of the game. This means either first to act or checked to
        Random rand = new Random();
        int mixedStrat = rand.nextInt(100);
        int raiseVal = (int)(pot * (mixedStrat / 100.0)) + game.bigBlind;

        // if faced with a raise 
        if (raise > 0) {
            // if probability given price is good
            if (equity > potOdds(raise, pot)) {
                if ((mixedStrat / 100.0) > equity) {
                		List<Action> actions = new ArrayList<Action>(); 
                		actions.add(new Action(Move.CALL, game.currentTurn, game.currentBet - game.lastStageTotalBets.get(game.currentTurn))); 
                		actions.add(new Action(Move.RAISE, game.currentTurn, raiseVal));
            			actions.add(new Action(Move.RAISE, game.currentTurn, game.currentBet));
                    actions.add(new Action(Move.CHECK, game.currentTurn, 0));
                    actions.add(new Action(Move.FOLD, game.currentTurn, 0));                               		
            			return actions;
                }
                else {
                		List<Action> actions = new ArrayList<Action>();  
            			actions.add(new Action(Move.RAISE, game.currentTurn, raiseVal));
        				actions.add(new Action(Move.RAISE, game.currentTurn, game.currentBet));
        				actions.add(new Action(Move.CALL, game.currentTurn, game.currentBet - game.lastStageTotalBets.get(game.currentTurn)));
        				actions.add(new Action(Move.CHECK, game.currentTurn, 0));
        				actions.add(new Action(Move.FOLD, game.currentTurn, 0));                               		
        				return actions;
                }
            }
            // room for bluffing
            else {
                if ((mixedStrat / 100.0) < equity) {
                		List<Action> actions = new ArrayList<Action>();  
        				actions.add(new Action(Move.RAISE, game.currentTurn, raiseVal));
    					actions.add(new Action(Move.RAISE, game.currentTurn, game.currentBet));
    					actions.add(new Action(Move.CALL, game.currentTurn, game.currentBet - game.lastStageTotalBets.get(game.currentTurn)));
    					actions.add(new Action(Move.CHECK, game.currentTurn, 0));
    					actions.add(new Action(Move.FOLD, game.currentTurn, 0));                               		
    					return actions;
                }
                // fold 
                else {
                		List<Action> actions = new ArrayList<Action>();  
                		actions.add(new Action(Move.FOLD, game.currentTurn, 0));
                		actions.add(new Action(Move.RAISE, game.currentTurn, raiseVal));
                		actions.add(new Action(Move.RAISE, game.currentTurn, game.currentBet));
                		actions.add(new Action(Move.CALL, game.currentTurn, game.currentBet - game.lastStageTotalBets.get(game.currentTurn)));
                		actions.add(new Action(Move.CHECK, game.currentTurn, 0));                               		
                		return actions;                    
                }
            }
        }
        else {
            if (equity == 1) {
                // Raise 75% of the time
                if (mixedStrat < 75) {
                		List<Action> actions = new ArrayList<Action>(); 
                		actions.add(new Action(Move.RAISE, game.currentTurn, raiseVal));
                		actions.add(new Action(Move.RAISE, game.currentTurn, game.currentBet));          		                   		
                		actions.add(new Action(Move.CHECK, game.currentTurn, 0));    
                		actions.add(new Action(Move.CALL, game.currentTurn, game.currentBet - game.lastStageTotalBets.get(game.currentTurn)));
                		actions.add(new Action(Move.FOLD, game.currentTurn, 0));  
                		return actions; 
                }
                else {
                		List<Action> actions = new ArrayList<Action>(); 
                		actions.add(new Action(Move.CHECK, game.currentTurn, 0));
                		actions.add(new Action(Move.RAISE, game.currentTurn, raiseVal));
                		actions.add(new Action(Move.RAISE, game.currentTurn, game.currentBet));          		                   		   
                		actions.add(new Action(Move.CALL, game.currentTurn, game.currentBet - game.lastStageTotalBets.get(game.currentTurn)));
                		actions.add(new Action(Move.FOLD, game.currentTurn, 0));  
                		return actions;                   
                }
            }
            // room for bluffing 
            else {
                if ((mixedStrat / 100.0) < equity) {
                		List<Action> actions = new ArrayList<Action>(); 
                		actions.add(new Action(Move.RAISE, game.currentTurn, raiseVal));
            			actions.add(new Action(Move.RAISE, game.currentTurn, game.currentBet));
            			actions.add(new Action(Move.CALL, game.currentTurn, game.currentBet - game.lastStageTotalBets.get(game.currentTurn)));
            			actions.add(new Action(Move.CHECK, game.currentTurn, 0));            			          		                   		              			
            			actions.add(new Action(Move.FOLD, game.currentTurn, 0));  
            			return actions;
                }
            }
        }
        List<Action> actions = new ArrayList<Action>(); 
        actions.add(new Action(Move.CHECK, game.currentTurn, 0));
        actions.add(new Action(Move.FOLD, game.currentTurn, 0));
        actions.add(new Action(Move.CALL, game.currentTurn, game.currentBet - game.lastStageTotalBets.get(game.currentTurn)));
		actions.add(new Action(Move.RAISE, game.currentTurn, raiseVal));
		actions.add(new Action(Move.RAISE, game.currentTurn, game.currentBet));		            			          		                   		              		
		return actions;        
    }

    /** [optimalEquityMoveRiver h1 h2 c1 c2 c3 c4 c5 pot raise] returns the optimal action on the flop given current equity 
     * and stage of the game. Note that this can be integrted given that the code stores the current
     * state of the game i.e. cards shown, action faced, players turn, pot size, etc.
     */
    public static List<Action> optimalEquityMoveRiver(Hand h1, Hand h2, Game game) {

    		Hand_Evaluator HE = new HandEvaluator.Hand_Evaluator();
		int c1 = HE.bitCardRep(game.cardsFaceUp.get(0).toStringEvaluator());
		int c2 = HE.bitCardRep(game.cardsFaceUp.get(1).toStringEvaluator());
		int c3 = HE.bitCardRep(game.cardsFaceUp.get(2).toStringEvaluator());
		int c4 = HE.bitCardRep(game.cardsFaceUp.get(3).toStringEvaluator());
		int c5 = HE.bitCardRep(game.cardsFaceUp.get(4).toStringEvaluator());
		int pot = game.pot;
		int raise = game.currentBet;

        Random rand = new Random();
        int mixedStrat = rand.nextInt(100);
        int raiseVal = (int)(pot * (mixedStrat / 100.0)) + game.bigBlind;
        int equity = 0;
        int raiseChance = rand.nextInt(100);
        
        int h1c1 = HE.bitCardRep(h1.cardOne.toStringEvaluator());
        int h1c2 = HE.bitCardRep(h1.cardTwo.toStringEvaluator());
        int h2c1 = HE.bitCardRep(h2.cardOne.toStringEvaluator());
        int h2c2 = HE.bitCardRep(h2.cardTwo.toStringEvaluator());
        
        if (Util_AI.strength(c1, c2, c3, c4, c5, h1c1, h1c2) > Util_AI.strength(c1, c2, c3, c4, c5, h2c1, h2c2)) {
            equity = 1;
        }   
        // this can be integrated based on stage of the game. This means either first to act or checked to

        if (equity == 1) {
            if (raiseChance > 50) {
            		List<Action> actions = new ArrayList<Action>(); 
            		actions.add(new Action(Move.RAISE, game.currentTurn, raiseVal));
            		actions.add(new Action(Move.RAISE, game.currentTurn, game.currentBet));
            		actions.add(new Action(Move.CALL, game.currentTurn, game.currentBet - game.lastStageTotalBets.get(game.currentTurn)));	
                actions.add(new Action(Move.CHECK, game.currentTurn, 0));
                actions.add(new Action(Move.FOLD, game.currentTurn, 0));                       			          		                   		              		
        			return actions;
            }
            else {
            		List<Action> actions = new ArrayList<Action>(); 
            		actions.add(new Action(Move.CALL, game.currentTurn, game.currentBet - game.lastStageTotalBets.get(game.currentTurn)));	
        			actions.add(new Action(Move.RAISE, game.currentTurn, raiseVal));
        			actions.add(new Action(Move.RAISE, game.currentTurn, game.currentBet));        			
        			actions.add(new Action(Move.CHECK, game.currentTurn, 0));
        			actions.add(new Action(Move.FOLD, game.currentTurn, 0));                       			          		                   		              		
        			return actions;                
            }
        }
        // equity = 0 with raise
        else if (raise > 0) {
            if (raiseChance < 20) {
            		List<Action> actions = new ArrayList<Action>();             		
            		actions.add(new Action(Move.RAISE, game.currentTurn, raiseVal));
            		actions.add(new Action(Move.RAISE, game.currentTurn, game.currentBet));        	
            		actions.add(new Action(Move.FOLD, game.currentTurn, 0));
            		actions.add(new Action(Move.CALL, game.currentTurn, game.currentBet - game.lastStageTotalBets.get(game.currentTurn)));	
            		actions.add(new Action(Move.CHECK, game.currentTurn, 0));           		                       			          		                   		              		
            		return actions;               
            }
            else {
            		List<Action> actions = new ArrayList<Action>();
            		actions.add(new Action(Move.FOLD, game.currentTurn, 0));
            		actions.add(new Action(Move.RAISE, game.currentTurn, raiseVal));
            		actions.add(new Action(Move.RAISE, game.currentTurn, game.currentBet));        	       		
            		actions.add(new Action(Move.CALL, game.currentTurn, game.currentBet - game.lastStageTotalBets.get(game.currentTurn)));	
            		actions.add(new Action(Move.CHECK, game.currentTurn, 0));           		                       			          		                   		              		
            		return actions;
            }
        }
        // equity = 0 without raise
        else {
            if (raiseChance < 20) {
            		List<Action> actions = new ArrayList<Action>();           		
            		actions.add(new Action(Move.RAISE, game.currentTurn, raiseVal));
            		actions.add(new Action(Move.RAISE, game.currentTurn, game.currentBet));
            		actions.add(new Action(Move.CHECK, game.currentTurn, 0));
            		actions.add(new Action(Move.FOLD, game.currentTurn, 0));
            		actions.add(new Action(Move.CALL, game.currentTurn, game.currentBet - game.lastStageTotalBets.get(game.currentTurn)));	            		           		                       			          		                   		              		
            		return actions;
            }
            else {
            		List<Action> actions = new ArrayList<Action>();    
            		actions.add(new Action(Move.CHECK, game.currentTurn, 0));
        			actions.add(new Action(Move.RAISE, game.currentTurn, raiseVal));
        			actions.add(new Action(Move.RAISE, game.currentTurn, game.currentBet));       			
        			actions.add(new Action(Move.FOLD, game.currentTurn, 0));
        			actions.add(new Action(Move.CALL, game.currentTurn, game.currentBet - game.lastStageTotalBets.get(game.currentTurn)));	            		           		                       			          		                   		              		
        			return actions;
            }
        }
        }


    /** [equityEstimateFullInfoFlop h1 h2 c1 c2 c3] is the equity estimate in percentage form with full information. Note that
     * this is specifically for the flop, and can be easily integrated with compareHands in Dealer.java
     * the flop cards will be hard coded for the sake of testing.
     */
    public static double equityEstimateFullInfoFlop(Hand h1, Hand h2, int c1, int c2, int c3, Deck deck) {

        //this should be toggled based on stage of game (we only care about Flop or Turn as River is either a win or loss)
    		Hand_Evaluator HE = new HandEvaluator.Hand_Evaluator();
        int h1c1 = HE.bitCardRep(h1.cardOne.toStringEvaluator());
        int h1c2 = HE.bitCardRep(h1.cardTwo.toStringEvaluator());
        int h2c1 = HE.bitCardRep(h2.cardOne.toStringEvaluator());
        int h2c2 = HE.bitCardRep(h2.cardTwo.toStringEvaluator());
        
        Evaluator e1 = new Evaluator(h1c1, h1c2, c1, c2, c3);
        Evaluator e2 = new Evaluator(h2c1, h2c2, c1, c2, c3);

        int val1 = e1.evaluate();
        int val2 = e2.evaluate();
        
        List<Card> cards = deck.getCards();

        double equity = 0;
        
        // If strength of P1 hand is better or just as good as P2
        if (val1 <= val2) {
            return 1;   // This means we have 100% equity. We will interpret this number later as
                        // mixing a call, raise, or check depending on board texture
        }
        // sift through rest of the deck to see what percentage of cards will make the cards better
        else {
            for (int i = 0; i < cards.size(); i++) {
                int c4 = HE.bitCardRep(cards.get(i).toStringEvaluator());
                if (ifIncreasesStrengthFlop(h1, h2, c1, c2, c3, c4)) {
                		equity = equity + (1/45.0);
                }
            }
        }
        return equity * 1.878070984;					// derived number from table in excel
    }
    /** [equityEstimateFullInfoTurn h1 h2 c1 c2 c3] is the equity estimate in percentage form with full information. Note that
     * this is specifically for the flop, and can be easily integrated with compareHands in Dealer.java
     * the flop cards will be hard coded for the sake of testing.
     */
    public static double equityEstimateFullInfoTurn(Hand h1, Hand h2, int c1, int c2, int c3, int c4, Deck deck) {

        //this should be toggled based on stage of game (we only care about Flop or Turn as River is either a win or loss)
    		Hand_Evaluator HE = new HandEvaluator.Hand_Evaluator();
        int h1c1 = HE.bitCardRep(h1.cardOne.toStringEvaluator());
        int h1c2 = HE.bitCardRep(h1.cardTwo.toStringEvaluator());
        int h2c1 = HE.bitCardRep(h2.cardOne.toStringEvaluator());
        int h2c2 = HE.bitCardRep(h2.cardTwo.toStringEvaluator());
        
        // you already have code on this, this should be picking the evaluator that returns the highest card strength 
        // i.e. lowest number given c4. See code below in ifIncreasesStrength for what I am talking about 
        Evaluator e1 = new Evaluator(h1c1, h1c2, c1, c2, c3);
        Evaluator e2 = new Evaluator(h2c1, h2c2, c1, c2, c3);

        int val1 = e1.evaluate();
        int val2 = e2.evaluate();

        List<Card> cards = deck.getCards();

        double equity = 0;
        
        // If strength of P1 hand is better or just as good as P2
        if (val1 <= val2) {
            return 1;   // This means we have 100% equity. We will interpret this number later as
                        // mixing a call, raise, or check depending on board texture
        }
        // sift through rest of the deck to see what percentage of cards will make the cards better
        else {
            for (int i = 0; i < cards.size(); i++) {
            		int c5 = HE.bitCardRep(cards.get(i).toStringEvaluator());
                if (ifIncreasesStrengthTurn(h1, h2, c1, c2, c3, c4, c5)) {
                    equity = equity + (1/44.0);
                }
            }
        }
        return equity;
    }
    
   /** for the flop*/
    public static boolean ifIncreasesStrengthFlop(Hand h1, Hand h2, int c1, int c2, int c3, int c4) {

    		Hand_Evaluator HE = new HandEvaluator.Hand_Evaluator();
        int h1c1 = HE.bitCardRep(h1.cardOne.toStringEvaluator());
        int h1c2 = HE.bitCardRep(h1.cardTwo.toStringEvaluator());
        int h2c1 = HE.bitCardRep(h2.cardOne.toStringEvaluator());
        int h2c2 = HE.bitCardRep(h2.cardTwo.toStringEvaluator());

        Evaluator e1 = new Evaluator(h1c2, h1c2, c1, c2, c3);
        Evaluator e2 = new Evaluator(h2c1, h2c2, c1, c2, c3);

        int val1 = e1.evaluate();
        int val2 = e2.evaluate();

        int P1Score = val1;
        int P2Score = val2;

        for (int i = 0; i < 5; i++) {
            int[] input = new int[5];
            input[0] = h1c1;
            input[1] = h1c2;
            input[2] = c1;
            input[3] = c2;
            input[4] = c3;
            input[i] = c4;
            P1Score = Math.min(P1Score, new Evaluator(input[0], input[1], input[2], input[3], input[4]).evaluate());
        }

        for (int i = 0; i < 5; i++) {
            int[] input = new int[5];
            input[0] = h2c1;
            input[1] = h2c2;
            input[2] = c1;
            input[3] = c2;
            input[4] = c3;
            input[i] = c4;
            P2Score = Math.min(P2Score, new Evaluator(input[0], input[1], input[2], input[3], input[4]).evaluate());
        }
//        
//        if (P1Score > 399 && P1Score <= 1599) {
//            return "flush";
//        }
        if (P1Score > P2Score) {
        		return true;
        }
        return false;
    }

    /** for the turn */
    public static boolean ifIncreasesStrengthTurn(Hand h1, Hand h2, int c1, int c2, int c3, int c4, int c5) {
    	
    	
    	
    		Hand_Evaluator HE = new HandEvaluator.Hand_Evaluator();
        int h1c1 = HE.bitCardRep(h1.cardOne.toStringEvaluator());
        int h1c2 = HE.bitCardRep(h1.cardTwo.toStringEvaluator());
        int h2c1 = HE.bitCardRep(h2.cardOne.toStringEvaluator());
        int h2c2 = HE.bitCardRep(h2.cardTwo.toStringEvaluator());

        Evaluator e1 = new Evaluator(h1c2, h1c2, c1, c2, c3);
        Evaluator e2 = new Evaluator(h2c1, h2c2, c1, c2, c3);

        int val1 = e1.evaluate();
        int val2 = e2.evaluate();

        int P1Score = val1;
        int P2Score = val2;

        
        for (int i = 0; i < 4; i++)
        {
            for (int j = i + 1; j < 5; j++)
            {
                int[] input = new int[5];
                input[0] = c1;
                input[1] = c2;
                input[2] = c3;
                input[3] = c4;
                input[4] = c5;
                input[i] = h1c1;
                input[j] = h1c2;
                P1Score = Math.min(P1Score, new Evaluator(input[0], input[1], input[2], input[3], input[4]).evaluate());
            }
        }

        for (int i = 0; i < 4; i++)
        {
            for (int j = i + 1; j < 5; j++)
            {
                int[] input = new int[5];
                input[0] = c1;
                input[1] = c2;
                input[2] = c3;
                input[3] = c4;
                input[4] = c5;
                input[i] = h2c1;
                input[j] = h2c2;
                P2Score = Math.min(P2Score, new Evaluator(input[0], input[1], input[2], input[3], input[4]).evaluate());
            }
        }
        if (P1Score < P2Score) {
            return true;
        }
        return false;
    }
}