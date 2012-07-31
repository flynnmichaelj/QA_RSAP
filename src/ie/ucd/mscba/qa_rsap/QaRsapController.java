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
import ie.ucd.mscba.qa_rsap.valueobjects.OCC;
import ie.ucd.mscba.qa_rsap.valueobjects.OCCChangeHolder;
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
    //Global Variables
    OCC occ = new OCC();
    Solution[] perSearchBest = new Solution[Constants.NUM_SEARCHES];
    
    public void runQaRsap(String fileName)
    {
        //Set a default file name if none exists
        if(fileName == null)
            fileName = "resources/dfn-bwin.xml";
            //fileName = "resources/atlanta.xml";
       
        //===========================================================
        //(1)    Build Network Object representation from input file
        //===========================================================
        //Build Network Object representation from input file
        Network network = InputFileHandler.loadInput(fileName);
        List<Link> networkLinks = network.getNetworkStructure( ).getLinks( ).getLink( );
        List<Node> networkNodes = network.getNetworkStructure( ).getNodes( ).getNode( );
        
        //Assign a number to each node
        for(int i=0; i<networkNodes.size( ); i++)
        {
            networkNodes.get(i).setNodeNumber(i);
        }
        
        //Genrate Node adjancies
        NodeAdjacencies nodeAdjacencies = new NodeAdjacencies(networkLinks);
        
        //===================================
        //(2)   Generate initial Solutions
        //===================================
        
        SolutionGenerator qaRsapProcessor = new SolutionGenerator(network, nodeAdjacencies);
        //Solution initSol = null;
        Solution[] initialSolutions = new Solution[Constants.NUM_SEARCHES];
        for(int i=0; i<Constants.NUM_SEARCHES; i++ )
        {
            Solution thisInitSol  = null;
            int initSolCounter = 1;
            while(thisInitSol == null && initSolCounter <= 10)
            {
                System.out.println("Generate Initital Soluotion: ATTEMPT:" + initSolCounter);
                thisInitSol = qaRsapProcessor.getInitialSolution();
                initSolCounter++;
            }
            initialSolutions[i] = thisInitSol; //TODO
            perSearchBest[i] = thisInitSol;
            System.out.println( "============= INIT SOL =============" );
            thisInitSol.printLocalRing( );
            thisInitSol.printSpurs( );
            thisInitSol.printTertiaryRing( );
            System.out.println( "============= END INIT SOL =============" );
        }
        
        //=====================================
        //(3)   Call anneal on initial solution
        //=====================================
       
        //Annealing properteis
        int n_ann_q = (int)(Constants.INITIAL_QUANTUM_FLUCTUATION/Constants.QUANTUM_FLUCTUATION_STEP);
        int n_ann_c = (int)(Constants.INITIAL_TEMPERATURE/Constants.TEMPERATURE_STEP);
        int n_ann = 0;
        double temp = 0.0;
        double gam = 0.0;

        if(Constants.TROTTER_NUMBER != 1) 
        {
            n_ann = n_ann_q;
            temp = Constants.INITIAL_TEMPERATURE;
        }
        else
        {
            n_ann = n_ann_c;
            gam = 0.0;
        }
        
        for(int iSearch=0; iSearch< Constants.NUM_SEARCHES; iSearch++)
        {
            Solution[] solutionsForAnnealing = new Solution[Constants.TROTTER_NUMBER];
            
            for(int j =0; j< Constants.TROTTER_NUMBER; j++)
            {
                solutionsForAnnealing[j] = perSearchBest[iSearch];              
            }
            
            //TODO May need array[trotterSlices] to assign cost for each slice of a solution
                     
            if(Constants.TROTTER_NUMBER != 1) 
            {
                occ.getOCC( perSearchBest[iSearch], networkNodes.size());
            }
            
            int iann = n_ann;
            Solution annealedSol = null;
            while( iann > 0) /// main annealing loop
            {  
                if(Constants.TROTTER_NUMBER != 1) 
                {
                   gam = Constants.INITIAL_QUANTUM_FLUCTUATION*(double)(iann/(double)n_ann);   //! quantum annealing (gam->0);
                }
                else
                {
                   temp = Constants.INITIAL_TEMPERATURE*(double)(iann/(double)n_ann);  //! classical annealing (temp->0);
                }
                //System.out.println("Called anneal for iann:" + iann + "@ temp: " + temp);
                anneal(networkLinks, solutionsForAnnealing, nodeAdjacencies,  network, temp, gam, Constants.MCS_AT_STEP, iSearch) ; 
               // System.out.println("Finished anneal for iann:" + iann);
                
                iann--;
            } 
            if(Constants.TROTTER_NUMBER != 1) 
            {
                gam = 0.000000001;     // last step of quantum annealing at gam=0 (Well, almost zero)
                //System.out.println("Called final anneal for iSearch:" + iSearch);
                anneal(networkLinks, solutionsForAnnealing, nodeAdjacencies,  network, temp, gam, Constants.MCS_AT_STEP, iSearch) ;
                //System.out.println("Finished final anneal for iSearch:" + iSearch);
            }
        }
        
        //finished Annealing
        //find the best of each search for one overall best
        Solution overAllBestSol = null;
        double overAllBestCost = Double.POSITIVE_INFINITY;
        for(int i = 0; i<Constants.NUM_SEARCHES; i++)
        {
            if(perSearchBest[i].getTotalCost( ) < overAllBestCost)
            {
                overAllBestSol = perSearchBest[i];
            }
        }
        //===================================
        //(4)   Output final results
        //===================================
        
        System.out.println( "============= Overall Best Sol =============" );
        overAllBestSol.printLocalRing( );
        overAllBestSol.printSpurs( );
        overAllBestSol.printTertiaryRing( );
        System.out.println( "Cost:" + overAllBestSol.getTotalCost( ) );
        System.out.println( "============= Overall Best Sol  =============" );

        GraphView view = new GraphView();
        view.drawGraph(overAllBestSol);
    }
    
    private void anneal(List<Link> networkLinks, Solution[] currrentSliceSolutions, 
                            NodeAdjacencies nodeAdjacencies, Network network, double temp, double gam, int nmcs, int iSearch)
    {
        double VnsDistDiff = 0.0;
        double betaTrotter = 0.0;
        double acc = 0.0;        
        double beta = 1.0/((Constants.TROTTER_NUMBER) * temp);
        
        if(Constants.TROTTER_NUMBER != 1) 
        {
            betaTrotter = -Math.log(Math.tanh(gam/((Constants.TROTTER_NUMBER) * temp)));
            //!b_tr = -0.5*LOG(COSH(gam/(DBLE(trot) * temp)))        //experiment with slightly modified interaction between slices  
        }
        else
        {
            betaTrotter = 0.0;
        }
        
        //Setup Neighbourhood search generator
        NeighbourGenerator ng = new NeighbourGenerator(nodeAdjacencies, network);

        for (int istep = 0; istep<nmcs ; istep++)                 // loop over number of searches
        {
            //for (int j = 0; j<network.getNetworkStructure( ).getNodes( ).getNode( ).size( ) ; j++) //TODO Our Run by time within VNS may cater for this       
            //{
                for (int k = 0; k<Constants.TROTTER_NUMBER ; k++)           // loop over Trotter slices      
                {
                    //System.out.println("Entered Trotter slice: " + k + "For nmcs: " + istep);
                    Solution bestSolSoFar = currrentSliceSolutions[k];                
                    Solution vnsResult = null;
            
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
                        vnsResult = invokeVNS(bestSolSoFar,runBytimes, 1 , networkLinks, ng, nodeAdjacencies);
                    }
                    else if((probPickNeighbourhood > Constants.prob_LR_DeleteInsert) && (probPickNeighbourhood < Constants.prob_LR_NodeSwap))
                    {
                        if(bestSolSoFar.getLocalrings( ).size( ) > 1)
                        {
                            vnsResult = invokeVNS(bestSolSoFar,runBytimes, 2 , networkLinks, ng, nodeAdjacencies);
                        }
                    }
                    else if((probPickNeighbourhood > Constants.prob_LR_NodeSwap) && (probPickNeighbourhood < Constants.prob_LR_DeleteSmallRing))
                    {
                        vnsResult = invokeVNS(bestSolSoFar,runBytimes, 3 , networkLinks, ng, nodeAdjacencies); 
                    }
                    else if((probPickNeighbourhood > Constants.prob_LR_DeleteSmallRing) && (probPickNeighbourhood < Constants.prob_LR_Split))
                    {
                        vnsResult = invokeVNS(bestSolSoFar,runBytimes, 4 , networkLinks, ng, nodeAdjacencies);  
                    }
                    else if((probPickNeighbourhood > Constants.prob_LR_Split) && (probPickNeighbourhood < Constants.prob_TR_perturbe))
                    {
                        if(bestSolSoFar.getTertiaryRing( ) != null)
                        {
                            vnsResult = invokeVNS(bestSolSoFar,runBytimes, 5 , networkLinks, ng, nodeAdjacencies);
                        }          
                    }
                    
                    /**System.out.println( "============= VNS SOL =============" );
                    bestSolSoFar.printLocalRing( );
                    bestSolSoFar.printSpurs( );
                    bestSolSoFar.printTertiaryRing( );
                    System.out.println( "============= END VNS SOL =============" );*/
                    
                    VnsDistDiff = vnsResult.getTotalCost() - bestSolSoFar.getTotalCost( );
                    if(VnsDistDiff > 0.0)
                    {
                        String h = "dsdfs";
                    }
                    else if(VnsDistDiff < 0.0)
                    {
                        String h= "sdsdf";
                    }
                    double change = 0;
                    
                    OCCChangeHolder changHolder = null;
                    if(Constants.TROTTER_NUMBER != 1) 
                    {
                       changHolder = occ.occChange( k, bestSolSoFar, vnsResult );
                       change = changHolder.getChangem( ) + changHolder.getChangep( );
                    } 
                    
                    double decisionFactor = Math.random( );
                    if(decisionFactor < Math.pow( 1.05, (-beta*VnsDistDiff - betaTrotter*(double)change))) 
                    {
                        currrentSliceSolutions[k] = vnsResult;
                        
                        // in case of QA update the occupation numbers
                        if(Constants.TROTTER_NUMBER != 1) 
                        {
                            occ.updateOCC( vnsResult, network.getNetworkStructure( ).getNodes( ).getNode( ).size( ), k );
                            if(k!=0) 
                            {
                                occ.getDiffOcc( )[k-1] = occ.getDiffOcc( )[k-1] + changHolder.getChangem();
                            }
                            if(k!=Constants.TROTTER_NUMBER-1) 
                            {
                                occ.getDiffOcc( )[k]   = occ.getDiffOcc( )[k]   + changHolder.getChangep( );
                            }
                        }
                        if(perSearchBest[iSearch].getTotalCost( ) < vnsResult.getTotalCost( ))
                        {
                            perSearchBest[iSearch] = vnsResult;
                        }
                        acc = acc + 1.0;
                    }
                }
            //}           
        }
        
        acc = acc/((double)nmcs*(double)Constants.TROTTER_NUMBER);  
    }
    
    private Solution invokeVNS(Solution inputSol, long runTimes, int neighbourhoodSearch, List<Link> networkLinks, NeighbourGenerator ng, NodeAdjacencies adjList)
    {
        System.out.println(neighbourhoodSearch);
        Solution nsHolder = null;
        Solution bestVNSSol = null;
        double bestVNSCost = Double.POSITIVE_INFINITY;
        
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
            if(bestVNSCost > nsHolder.getTotalCost( ))
            {
                bestVNSSol = nsHolder;
                bestVNSCost = nsHolder.getTotalCost();
            }                  
        }      
        return bestVNSSol;
    }
}
