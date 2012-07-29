/**
 * Created on 7 Jul 2012
 *
 * @author Michael Flynn
 * @version 1.0
 *
 * File: QaRsapController.java
 * Package ie.ucd.mscba.qa_rsap
 * Project QA_RSAP
 */
package ie.ucd.mscba.qa_rsap;

import ie.ucd.mscba.qa_rsap.filehandlers.InputFileHandler;
import ie.ucd.mscba.qa_rsap.processor.NeighbourGenerator;
import ie.ucd.mscba.qa_rsap.processor.SolutionGenerator;
import ie.ucd.mscba.qa_rsap.ui.GraphView;
import ie.ucd.mscba.qa_rsap.valueobjects.NodeAdjacencies;
import ie.ucd.mscba.qa_rsap.valueobjects.Solution;

import java.util.List;

import de.zib.sndlib.network.Link;
import de.zib.sndlib.network.Network;
import de.zib.sndlib.network.Node;

/**
 * 
 */
public class QaRsapController
{
    public void runQaRsap(String fileName)
    {
        //Set a default file name if none exists
        if(fileName == null)
            //fileName = "resources/dfn-bwin.xml";
            fileName = "resources/atlanta.xml";
       
        //Build Network Object representation from input file
        Network network = InputFileHandler.loadInput(fileName);
        List<Link> networkLinks = network.getNetworkStructure( ).getLinks( ).getLink( );
        List<Node> networkNodes = network.getNetworkStructure( ).getNodes( ).getNode( );
        
        //Genrate Node adjancies
        NodeAdjacencies nodeAdjacencies = new NodeAdjacencies(networkLinks);
        
        //Generate initial Solution
        SolutionGenerator qaRsapProcessor = new SolutionGenerator(network, nodeAdjacencies);
        Solution initSol = null;
        int initSolCounter = 1;
        while(initSol == null && initSolCounter <= 10)
        {
            System.out.println("Generate Initital Soluotion: ATTEMPT:" + initSolCounter);
            initSol = qaRsapProcessor.getInitialSolution();
            initSolCounter++;
        }
        System.out.println( "============= INIT SOL =============" );
        initSol.printLocalRing( );
        initSol.printSpurs( );
        initSol.printTertiaryRing( );
        System.out.println( "============= END INIT SOL =============" );
        
        //Setup Neighbourhood search generator
        NeighbourGenerator ng = new NeighbourGenerator(nodeAdjacencies, network);
        Solution bestSolSoFar = initSol.clone();
        double bestCostSoFar = bestSolSoFar.getTotalCost();
        
       
        for(int i=0; i < Constants.loopCount; i++)
        {
            System.out.println("============ ITERATION "+i+" ================" );
            
            //Pick the neighbourhood by probability.
            double probRunBytimes = Math.random( );
            double probPickNeighbourhood = Math.abs(probRunBytimes - 0.5);
            
            //Determine how many time we want to run this neighbourhood search on this iteration 
            long runBytimes = 1;
            if(probRunBytimes > Constants.probMultipleRuns)
            {
                //Number of times to run is based on a proportion of the number of nodes
                runBytimes = Math.round((network.getNetworkStructure( ).getNodes( ).getNode( ).size( ))*Math.random( ));
                if(runBytimes < 1)
                {
                    runBytimes = 1;
                }
            }
            

            Solution nsSolHolder = null;
            if(probPickNeighbourhood <= Constants.prob_LR_DeleteInsert)
            {
                invokeVNS(bestSolSoFar,runBytimes, 1 , networkLinks, ng, nodeAdjacencies);
            }
            else if((probPickNeighbourhood > Constants.prob_LR_DeleteInsert) && (probPickNeighbourhood < Constants.prob_LR_NodeSwap))
            {
                if(bestSolSoFar.getLocalrings( ).size( ) > 1)
                {
                    invokeVNS(bestSolSoFar,runBytimes, 2 , networkLinks, ng, nodeAdjacencies);
                }
            }
            else if((probPickNeighbourhood > Constants.prob_LR_NodeSwap) && (probPickNeighbourhood < Constants.prob_LR_DeleteSmallRing))
            {
                invokeVNS(bestSolSoFar,runBytimes, 3 , networkLinks, ng, nodeAdjacencies); 
            }
            else if((probPickNeighbourhood > Constants.prob_LR_DeleteSmallRing) && (probPickNeighbourhood < Constants.prob_LR_Split))
            {
                invokeVNS(bestSolSoFar,runBytimes, 4 , networkLinks, ng, nodeAdjacencies);  
            }
            else if((probPickNeighbourhood > Constants.prob_LR_Split) && (probPickNeighbourhood < Constants.prob_TR_perturbe))
            {
                if(bestSolSoFar.getTertiaryRing( ) != null)
                {
                    invokeVNS(bestSolSoFar,runBytimes, 5 , networkLinks, ng, nodeAdjacencies);
                }          
            }
            

            System.out.println( "============= VNS SOL =============" );
            bestSolSoFar.printLocalRing( );
            bestSolSoFar.printSpurs( );
            bestSolSoFar.printTertiaryRing( );
            System.out.println( "============= END VNS SOL =============" );
            
        }
               
        
        //Draw Graph
        GraphView view = new GraphView();
        view.drawGraph(bestSolSoFar);
    }
    
    private Solution invokeVNS(Solution inputSol, long runTimes, int neighbourhoodSearch, List<Link> networkLinks, NeighbourGenerator ng, NodeAdjacencies adjList)
    {
        Solution nsHolder = null;
        Solution bestSol = null;
        double bestCost = Double.POSITIVE_INFINITY;
        
        for(int j=0; j < runTimes; j++)
        {
            switch (neighbourhoodSearch) 
            {
                case 1:  
                    nsHolder = ng.deleteInsertSearch(inputSol);
                    break;
                case 2:  
                    nsHolder = ng.swapNodeSearch(inputSol);
                    break;
                case 3:  
                    nsHolder = ng.deleteSmallRingSearch(inputSol);
                    break;
                case 4:  
                    nsHolder = ng.splitLocalSearch( inputSol, adjList );
                    break;
                case 5:  
                    nsHolder = ng.tertiaryRingSearch(inputSol);
                    break;
            }
            
            nsHolder.calculateTotalCost(networkLinks);
            if(bestCost > nsHolder.getTotalCost( ))
            {
                bestSol = nsHolder;
                bestCost = nsHolder.getTotalCost();
            }                  
        }    
        return bestSol;
    }
}
