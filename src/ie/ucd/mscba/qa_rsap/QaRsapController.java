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

import ie.ucd.mscba.qa_rsap.processor.NeighbourGenerator;
import ie.ucd.mscba.qa_rsap.processor.SolutionGenerator;
import ie.ucd.mscba.qa_rsap.settings.AnnealSettings;
import ie.ucd.mscba.qa_rsap.settings.AppSettings;
import ie.ucd.mscba.qa_rsap.settings.VNSSettings;
import ie.ucd.mscba.qa_rsap.valueobjects.NodeAdjacencies;
import ie.ucd.mscba.qa_rsap.valueobjects.OCC;
import ie.ucd.mscba.qa_rsap.valueobjects.OCCChangeHolder;
import ie.ucd.mscba.qa_rsap.valueobjects.Ring;
import ie.ucd.mscba.qa_rsap.valueobjects.Solution;
import ie.ucd.mscba.qa_rsap.valueobjects.Spur;

import java.awt.BasicStroke;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Text;
import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;
import org.openstreetmap.gui.jmapviewer.MapPolygonImpl;
import org.openstreetmap.gui.jmapviewer.events.JMVCommandEvent;

import de.zib.sndlib.network.Link;
import de.zib.sndlib.network.Network;
import de.zib.sndlib.network.Node;

/**
 * 
 */
public class QaRsapController
{
    //Global Variables
    private OCC occ                 = null;
    Solution[] perSearchBest        = null;
    AnnealSettings annealSettings   = null;
    VNSSettings vnsSetting          = null;
    Text outputArea                 = null; 
    Display display                 = null; 
    ProgressBar progressBarO         = null;
    ProgressBar progressBarS         = null;
    JMapViewer map                  = null;
    
    private boolean stopRequested = false;
    
    public void setUIComponents(Text outputArea, Display display, ProgressBar progressBarO,  ProgressBar progressBarS,  JMapViewer map)
    {
        this.outputArea = outputArea;
        this.display = display;
        this.progressBarO = progressBarO;
        this.progressBarS = progressBarS;
        this.map = map;
    }
    
    public void runQaRsap(Network network, AppSettings appSettings)
    {
        annealSettings = appSettings.getAnnealSettings();
        vnsSetting = appSettings.getVnsSettings();
        occ = new OCC(annealSettings.getTrotterSlices());
        
        perSearchBest = new Solution[annealSettings.getNumSearches()];
        
        //===========================================================
        //(1)    Build Network Object representation from input file
        //===========================================================
        //Build Network Object representation from input file
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
        
        SolutionGenerator qaRsapProcessor = new SolutionGenerator(network, nodeAdjacencies, vnsSetting);
        
        updateResultsPanel("===================================================================", false, display, outputArea);
        updateResultsPanel("Genrating initial Soltutions.", true, display, outputArea);
        
        Solution[] initialSolutions = new Solution[annealSettings.getNumSearches()];
        for(int i=0; i<annealSettings.getNumSearches(); i++ )
        {
            
            Solution thisInitSol  = null;
            int initSolCounter = 1;
            while(thisInitSol == null && initSolCounter <= 10 && !stopRequested)
            {
                thisInitSol = qaRsapProcessor.getInitialSolution();
                if(thisInitSol != null)
                {
                    //Validate soluton
                    boolean valid = thisInitSol.validate(nodeAdjacencies, networkNodes.size( ));
                    if(!valid)
                    {
                        System.out.println("ERROR: INIT SOLUTION NOT VALID");
                        updateResultsPanel("Ininial Solution invalid, Attempting retry.", true, display, outputArea);
                        thisInitSol = null;
                    }
                }
                else
                    updateResultsPanel("Ininial Solution not found, Attempting retry.", true, display, outputArea);
                initSolCounter++;
            }
            if(stopRequested)
                return;
            
            if(thisInitSol != null)
            {
                initialSolutions[i] = thisInitSol; //TODO
                perSearchBest[i] = thisInitSol;
                updateResultsPanel("Initial Solution " + (i+1) + " Found", true, display, outputArea);
                
            }
            else
                i--;
        }
        
        //=====================================
        //(3)   Call anneal on initial solution
        //=====================================
       
        //Annealing properteis
        int n_ann_q = (int)(annealSettings.getInitQAfluct()/annealSettings.getQaFluctSteps());
        int n_ann_c = (int)(annealSettings.getInitSATemp()/annealSettings.getSaDeltaTemp());
        int n_ann = 0;
        double temp = 0.0;
        double gam = 0.0;

        if(annealSettings.getTrotterSlices() != 1) 
        {
            n_ann = n_ann_q;
            temp = annealSettings.getQaTemp();
        }
        else
        {
            n_ann = n_ann_c;
            gam = 0.0;
        }
        
        for(int iSearch=0; iSearch< annealSettings.getNumSearches(); iSearch++)
        {
            //Stops the proccess if requested by the user
            if(stopRequested)
                return;
            
            Solution[] solutionsForAnnealing = new Solution[annealSettings.getTrotterSlices()];
            
            for(int j = 0; j < annealSettings.getTrotterSlices(); j++)
            {
                solutionsForAnnealing[j] = perSearchBest[iSearch];              
            }
            
            //TODO May need array[trotterSlices] to assign cost for each slice of a solution
                     
            if(annealSettings.getTrotterSlices() != 1) 
            {
                occ.getOCC( perSearchBest[iSearch], networkNodes.size());
            }
            
            int iann = n_ann;
            Solution annealedSol = null;
            updateResultsPanel("Search " + (iSearch+1) , true, display, outputArea);
            while( iann > 0) /// main annealing loop
            {  
                if(annealSettings.getTrotterSlices() != 1) 
                {
                   gam = annealSettings.getInitQAfluct()*(double)(iann/(double)n_ann);   //! quantum annealing (gam->0);
                }
                else
                {
                   temp = annealSettings.getInitSATemp()*(double)(iann/(double)n_ann);  //! classical annealing (temp->0);
                }
                anneal(networkLinks, solutionsForAnnealing, nodeAdjacencies,  network, temp, gam, annealSettings.getMcsSteps(), iSearch) ; 
                
                iann--;
                double calcForProgessBar = ((double)n_ann-iann)/n_ann;
                updateProgressBar((calcForProgessBar*100) , display, progressBarS);
            } 
            if(annealSettings.getTrotterSlices() != 1) 
            {
                gam = 0.000000001;     // last step of quantum annealing at gam=0 (Well, almost zero)
                anneal(networkLinks, solutionsForAnnealing, nodeAdjacencies,  network, temp, gam, annealSettings.getMcsSteps(), iSearch) ;
            }
            
            double calcForProgessBar = (iSearch+1)/(double)annealSettings.getNumSearches();
            updateProgressBar((calcForProgessBar*100) , display, progressBarO);
            
            updateMap(perSearchBest[iSearch], map, network.getNetworkStructure().getNodes().getCoordinatesType());
        }
        
        //finished Annealing
        //find the best of each search for one overall best
        Solution overAllBestSol = null;
        double overAllBestCost = Double.POSITIVE_INFINITY;
        for(int i = 0; i<annealSettings.getNumSearches(); i++)
        {
            if(perSearchBest[i].getTotalCost( ) < overAllBestCost)
            {
                overAllBestSol = perSearchBest[i];
            }
        }
        //===================================
        //(4)   Output final results
        //===================================
        updateMap(overAllBestSol, map, network.getNetworkStructure().getNodes().getCoordinatesType());
        
        if(!stopRequested)
        {
            updateResultsPanel("============= Overall Best Sol =============", true, display, outputArea);
            updateResultsPanel(overAllBestSol.printLocalRing( ), true, display, outputArea);
            updateResultsPanel(overAllBestSol.printSpurs( ), true, display, outputArea);
            updateResultsPanel("Total Local Rings and Spurs Cost:" + overAllBestSol.getLocalRingAndSpursCost( ), true, display, outputArea);
            updateResultsPanel(overAllBestSol.printTertiaryRing( ), true, display, outputArea);
            updateResultsPanel("Overall Cost:" + overAllBestSol.getTotalCost( ), true, display, outputArea);
            updateResultsPanel("============= Overall Best Sol  =============", true, display, outputArea);
        }
    }
    
    private void anneal(List<Link> networkLinks, Solution[] currrentSliceSolutions, 
                            NodeAdjacencies nodeAdjacencies, Network network, double temp, double gam, int nmcs, int iSearch)
    {
        double VnsDistDiff = 0.0;
        double betaTrotter = 0.0;
        double totalAcc = 0.0; 
        double badAcc = 0.0;
        double badGenerated = 0.0;
        double beta = 1.0/((annealSettings.getTrotterSlices()) * temp);
        
        if(annealSettings.getTrotterSlices() != 1) 
        {
            betaTrotter = -Math.log(Math.tanh(gam/((annealSettings.getTrotterSlices()) * temp)));
            //!b_tr = -0.5*LOG(COSH(gam/(DBLE(trot) * temp)))        //experiment with slightly modified interaction between slices  
        }
        else
        {
            betaTrotter = 0.0;
        }
        
        //Setup Neighbourhood search generator
        NeighbourGenerator ng = new NeighbourGenerator(nodeAdjacencies, network, vnsSetting);

        for (int istep = 0; istep<nmcs ; istep++)                 // loop over number of searches
        {
           //Stops the proccess if requested by the user
            if(stopRequested)
                return;
            
            //for (int j = 0; j<network.getNetworkStructure( ).getNodes( ).getNode( ).size( ) ; j++) //TODO Our Run by time within VNS may cater for this       
            //{
                for (int k = 0; k<annealSettings.getTrotterSlices(); k++)           // loop over Trotter slices      
                {
                   //Stops the proccess if requested by the user
                    if(stopRequested)
                        return;
                              
                    Solution vnsResult = currrentSliceSolutions[k];
            
                    //Pick the neighbourhood by probability.
                    double probRunBytimes = Math.random( );
                    double probPickNeighbourhood = Math.abs(probRunBytimes - 0.5);
                    
                    //Determine how many time we want to run this neighbourhood search on this iteration 
                    long runBytimes = 1;
                    if(probRunBytimes > Constants.PROB_MULTIPLE_RUNS)
                    {
                        //Number of times to run is based on a proportion of the number of nodes
                        runBytimes = Math.round((network.getNetworkStructure( ).getNodes( ).getNode( ).size( ))*Math.random( ));
                        if(runBytimes < 1)
                        {
                            runBytimes = 1;
                        }
                    }
                    
                    if(probPickNeighbourhood <= Constants.PORB_LR_DELETE_INSERT)
                    {
                        vnsResult = invokeVNS(currrentSliceSolutions[k],runBytimes, 1 , networkLinks, ng, nodeAdjacencies);
                    }
                    else if((probPickNeighbourhood > Constants.PORB_LR_DELETE_INSERT) && (probPickNeighbourhood < Constants.PROB_LR_NODE_SWAP))
                    {
                        if(currrentSliceSolutions[k].getLocalrings( ).size( ) > 1)
                        {
                            vnsResult = invokeVNS(currrentSliceSolutions[k],runBytimes, 2 , networkLinks, ng, nodeAdjacencies);
                        }
                    }
                    else if((probPickNeighbourhood > Constants.PROB_LR_NODE_SWAP) && (probPickNeighbourhood < Constants.PROB_LR_DELETE_SMALL_RING))
                    {
                        vnsResult = invokeVNS(currrentSliceSolutions[k],runBytimes, 3 , networkLinks, ng, nodeAdjacencies); 
                    }
                    else if((probPickNeighbourhood > Constants.PROB_LR_DELETE_SMALL_RING) && (probPickNeighbourhood < Constants.PROB_LR_SPLIT))
                    {
                        vnsResult = invokeVNS(currrentSliceSolutions[k],runBytimes, 4 , networkLinks, ng, nodeAdjacencies);  
                    }
                    else if((probPickNeighbourhood > Constants.PROB_LR_SPLIT) && (probPickNeighbourhood < Constants.PROB_LR_INSERT_EDGE))
                    {
                        if(currrentSliceSolutions[k].getSpurs( ) != null && currrentSliceSolutions[k].getSpurs( ).size( ) > 1)
                        {
                            vnsResult = invokeVNS(currrentSliceSolutions[k], runBytimes, 5 , networkLinks, ng, nodeAdjacencies);
                        }          
                    }
                    else if((probPickNeighbourhood > Constants.PROB_LR_INSERT_EDGE) && (probPickNeighbourhood < Constants.PROB_TR_PERTURBE))
                    {
                        if(currrentSliceSolutions[k].getTertiaryRing( ) != null)
                        {
                            vnsResult = invokeVNS(currrentSliceSolutions[k],runBytimes, 6 , networkLinks, ng, nodeAdjacencies);
                        }          
                    }
                                        
                    VnsDistDiff = vnsResult.getTotalCost() - currrentSliceSolutions[k].getTotalCost( );
                    double scaledDiff = VnsDistDiff*nodeAdjacencies.getScaleRatio( );
                    
                    if(VnsDistDiff > 0.0)
                    {
                        badGenerated ++;
                    }
                    
                    double change = 0;
                    
                    OCCChangeHolder changHolder = null;
                    if(annealSettings.getTrotterSlices() != 1) 
                    {
                       changHolder = occ.occChange( k, currrentSliceSolutions[k], vnsResult );
                       change = changHolder.getChangem( ) + changHolder.getChangep( );
                    } 
                    
                    double decisionFactor = Math.random( );
                    if(decisionFactor < Math.pow( Math.E, (-beta*scaledDiff - betaTrotter*(double)change))) 
                    {
                        currrentSliceSolutions[k] = vnsResult;
                        
                        // in case of QA update the occupation numbers
                        if(annealSettings.getTrotterSlices() != 1) 
                        {
                            occ.updateOCC( vnsResult, network.getNetworkStructure( ).getNodes( ).getNode( ).size( ), k );
                            if(k!=0) 
                            {
                                occ.getDiffOcc( )[k-1] = occ.getDiffOcc( )[k-1] + changHolder.getChangem();
                            }
                            if(k!=annealSettings.getTrotterSlices()-1) 
                            {
                                occ.getDiffOcc( )[k]   = occ.getDiffOcc( )[k]   + changHolder.getChangep( );
                            }
                        }
                        if(perSearchBest[iSearch].getTotalCost( ) > vnsResult.getTotalCost( ))
                        {
                            perSearchBest[iSearch] = vnsResult;
                        }
                        totalAcc = totalAcc + 1.0;
                        if(VnsDistDiff > 0)
                        {
                            badAcc = badAcc + 1.0;
                        }
                    }
                }
            //}           
        }
        
        totalAcc = totalAcc/((double)nmcs*(double)annealSettings.getTrotterSlices());  
        badAcc = badAcc/badGenerated;  
        //System.out.println("Total acceptance:" + totalAcc + " | Bad Sol acceptance:" + badAcc);
    }
    
    private Solution invokeVNS(Solution inputSol, long runTimes, int neighbourhoodSearch, List<Link> networkLinks, NeighbourGenerator ng, NodeAdjacencies adjList)
    {
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
                    nsHolder = ng.edgeInsertionSearch(inputSol);
                    break;
                case 6:  
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
    
    private void updateResultsPanel(final String text, final boolean newLine, Display display, final Text outputArea)
    {
        //We do not depend on the result so we can use aasyncExec.
        //This just dispatched the event and does not wait for a response
        display.asyncExec (new Runnable () {
           public void run () {
              if (!outputArea.isDisposed())
              {
                  if(newLine)
                  {
                      outputArea.append("\r\n");
                  }
                  outputArea.append(text);
              }
           }
        });
    }
    
    private void updateProgressBar(final double value, Display display, final ProgressBar progressBar)
    {
        //We do not depend on the result so we can use aasyncExec.
        //This just dispatched the event and does not wait for a response
        display.asyncExec (new Runnable () {
           public void run () {
              if (!progressBar.isDisposed())
              {
                  progressBar.setSelection((int)value);
              }
           }
        });
    }
    
    private void updateMap(Solution bestSol, final JMapViewer map, String coordType)
    {
        map.removeAllMapPolygons();
        
        if(!"pixel".equalsIgnoreCase(coordType))
        {
            List<Ring> rings = bestSol.getLocalrings( );
            List<Spur> spurs = bestSol.getSpurs();
            Ring tertiaryRing = bestSol.getTertiaryRing();
            
           
            //Handle Local rings
            for(int i=0; i<rings.size( ); i++)
            {
                List<Coordinate> localRingRoute = new ArrayList<Coordinate>();
                Ring currentRing = rings.get(i);
                List<Node> nodes = currentRing.getNodes();
                for(int j=0; j<nodes.size( ); j++)
                {   
                    Node currentNode = nodes.get( j );
                    double yCoord = currentNode.getCoordinates().getY().doubleValue();
                    double xCoord = currentNode.getCoordinates().getX().doubleValue();
                    localRingRoute.add(new Coordinate(yCoord, xCoord));                          
                }
                map.addMapPolygon(new MapPolygonImpl(localRingRoute, Color.BLACK, new BasicStroke(3.0f)));
            }
            
            //Handle spurs
            for(int i=0; i<spurs.size( ); i++)
            {
                Spur currentSpur = spurs.get(i);
                Node parentNode = currentSpur.getParentNode();
                Node spurNode = currentSpur.getSpurNode();
                double parYCoord = parentNode.getCoordinates().getY().doubleValue();
                double parXCoord = parentNode.getCoordinates().getX().doubleValue();
                double spurYCoord = spurNode.getCoordinates().getY().doubleValue();
                double spurXCoord = spurNode.getCoordinates().getX().doubleValue();
                Coordinate parentNc = new Coordinate(parYCoord, parXCoord);
                Coordinate sourNc = new Coordinate(spurYCoord, spurXCoord);
                
                List<Coordinate> spurRoute = new ArrayList<Coordinate>(Arrays.asList(parentNc, sourNc, sourNc));
                map.addMapPolygon(new MapPolygonImpl(spurRoute, Color.BLACK, new BasicStroke(3.0f)));
            }
            
            //Handle tertiary Ring
            if(tertiaryRing!=null && tertiaryRing.getNodes().size() > 0)
            {
                List<Coordinate> tertiaryRingRoute = new ArrayList<Coordinate>();
                List<Node> nodes = tertiaryRing.getNodes();
                for(int j=0; j<nodes.size( ); j++)
                {   
                    Node currentNode = nodes.get( j );
                    double yCoord = currentNode.getCoordinates().getY().doubleValue();
                    double xCoord = currentNode.getCoordinates().getX().doubleValue();
                    tertiaryRingRoute.add(new Coordinate(yCoord, xCoord));                          
                }
                map.addMapPolygon(new MapPolygonImpl(tertiaryRingRoute, Color.RED, new BasicStroke(1.5f)));
            }
        }
    }
    
    public void stop(boolean stop)
    {
        stopRequested = true;
    }
}
