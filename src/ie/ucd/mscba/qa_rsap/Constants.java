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
    public static int initMaxLocalRingSize = 8;
    public static int maxLocalRingSize = 8;
    
    
    public static int localRingCapcacityModule = 0;
    public static int tertiaryRingCapcacityModule = 0;
    
    public static double spurPenaltyCost  = 10;
    
    public static int loopCount = 1;
    
    public static int MIN_SIZE_FOR_SPLIT = 7;
    
    //Probalilities for Rings
    public static double prob_LR_DeleteInsert = 0.15;
    public static double prob_LR_NodeSwap = 0.25;
    public static double prob_LR_DeleteSmallRing = 0.35;
    public static double prob_LR_Split = 0.4;
    public static double prob_TR_perturbe = 0.5;
    
    public static double probMultipleRuns = 0.5;
    
   //Quantum annealing properties
    private static int TROTTER_NUMBER = 5;
    private static double INITIAL_QUANTUM_FLUCTUATION=2.5;
    private static double QUANTUM_FLUCTUATION_STEP=0.1;
    private static double INITIAL_TEMPERATURE=0.334;
    private static double TEMPERATURE_STEP=1.0;
    private static double MCS_AT_STEP=25.0;
    //CLASSICAL_PREANNEALING=3 1 0.1
    
    
}
