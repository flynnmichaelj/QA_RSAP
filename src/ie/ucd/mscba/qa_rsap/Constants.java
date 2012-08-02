/**
 * Created on 10 Jul 2012
 *
 * @author Michael Flynn
 * @version 1.0
 *
 * File: Constants.java
 * Package ie.ucd.mscba.tcarp
 * Project TCARP_JAVA
 */
package ie.ucd.mscba.qa_rsap;

/**
 * 
 */
public class Constants
{
    public static int initMaxLocalRingSize = 6;
    public static int maxLocalRingSize = 9;
    
    
    public static int localRingCapcacityModule = 0;
    public static int tertiaryRingCapcacityModule = 0;
    
    public static double spurPenaltyCost  = 7;
    
    public static int NUM_SEARCHES = 5;
    public static int loopCount = 1;
    
    public static int MIN_SIZE_FOR_SPLIT = 7;
    
    //Probalilities for Rings
    public static double prob_LR_DeleteInsert = 0.15;
    public static double prob_LR_NodeSwap = 0.25;
    public static double prob_LR_DeleteSmallRing = 0.35;
    public static double prob_LR_Split = 0.4;
    public static double prob_LR_InsertEdge = 0.45;
    public static double prob_TR_perturbe = 0.5;
    
    public static double probMultipleRuns = 0.5;
    
   //Quantum annealing properties
    public static int TROTTER_NUMBER = 3;
    public static double INITIAL_QUANTUM_FLUCTUATION=2.5;
    public static double QUANTUM_FLUCTUATION_STEP=0.1;
    //public static double INITIAL_TEMPERATURE=0.334;
    //public static double TEMPERATURE_STEP=1.0;
    public static double INITIAL_TEMPERATURE=0.334;
    public static double TEMPERATURE_STEP=0.01;
    public static int MCS_AT_STEP=25;
    //CLASSICAL_PREANNEALING=3 1 0.1
    
    
}
