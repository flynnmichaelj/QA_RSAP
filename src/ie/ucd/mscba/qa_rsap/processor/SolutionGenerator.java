/**
 * Created on 7 Jul 2012
 *
 * @author Michael Flynn
 * @version 1.0
 *
 * File: QaRsapProcessor.java
 * Package ie.ucd.mscba.qa_rsap.processor
 * Project QA_RSAP
 */
package ie.ucd.mscba.qa_rsap.processor;

import ie.ucd.mscba.qa_rsap.Constants;
import ie.ucd.mscba.qa_rsap.dijkstra.Dijkstra;
import ie.ucd.mscba.qa_rsap.dijkstra.DijkstraNode;
import ie.ucd.mscba.qa_rsap.utils.QaRsapUtils;
import ie.ucd.mscba.qa_rsap.valueobjects.AdjNode;
import ie.ucd.mscba.qa_rsap.valueobjects.BestConnectors;
import ie.ucd.mscba.qa_rsap.valueobjects.NodeAdjacencies;
import ie.ucd.mscba.qa_rsap.valueobjects.Ring;
import ie.ucd.mscba.qa_rsap.valueobjects.Solution;
import ie.ucd.mscba.qa_rsap.valueobjects.Spur;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;

import de.zib.sndlib.network.Network;
import de.zib.sndlib.network.Node;
import de.zib.sndlib.network.Nodes;

/**
 * 
 */
public class SolutionGenerator
{
    private Network network = null;
    private NodeAdjacencies nodeAdjacencies = null;

    public SolutionGenerator( Network network, NodeAdjacencies  nodeAdjacencies)
    {
        super( );
        this.network = network;
        this.nodeAdjacencies = nodeAdjacencies;
    }


    public Solution getInitialSolution()
    {
        Solution sol = new Solution();
        
        Nodes nodes = network.getNetworkStructure( ).getNodes( );
   
        List<Node> tempNodeList = new ArrayList<Node>(nodes.getNode());
        int counter = tempNodeList.size( );
        Random randomGenerator = new Random();
        
        //build rings and spurs
        List<Node> spursFound = new ArrayList<Node>();
        while(counter != 0)
        {
            int randomInt = randomGenerator.nextInt(counter); //Get a random node of remaining to start new ring
            Node currentNode = tempNodeList.get(randomInt);

            Ring localRing = createLocalRing(currentNode, nodes.getNode(), tempNodeList);
            if(localRing == null)
            {
                //Failed to find a local ring for this node. 
                //Save to be added as a spur later
                spursFound.add(currentNode);
                tempNodeList.remove(currentNode);
            }
            else
            {
                sol.getLocalrings().add(localRing) ;
            }
            counter = tempNodeList.size( ); 
        }
        for(Node node : spursFound)
        {
            Spur spur = createSrup(node, nodes.getNode(), sol.getLocalrings());
            if(spur != null)
            {
                sol.getSpurs().add(spur);
            }
            else
            {
                return null; //spur cannot find parent, infeasible solution.
            }
        }
        
       
        tempNodeList = new ArrayList<Node>(nodes.getNode());
        List<Spur> spurs = sol.getSpurs();
        for(Spur spur : spurs)
        {
            tempNodeList.remove(spur.getSpurNode());
        }
        
        //Generate tertiary ring if we have mode than one local ring
        if(sol.getLocalrings( ).size( ) >1 )
        {
            Ring tertiaryRing = generateTertiaryRing(sol.getSpurs( ), sol.getLocalrings());
            if(tertiaryRing!=null)
            {
                 sol.setTertiaryRing(tertiaryRing);
            }
            else
            {
                return null; //No valid initial sol found;
            }
        }

        ///---------------------------------------------------------
        ///Calculate Total cost.
        ///----------------------------------------------------------
        sol.calculateTotalCost( network.getNetworkStructure( ).getLinks( ).getLink( ));
        System.out.println( "Total solution Cost : "  + sol.getTotalCost( ));
        return sol;
    }
    
    //=======================================
    //GenerateTertiaryRing
    //======================================
    public Ring generateTertiaryRing(List<Spur> spurs, List<Ring> localRings)
    {
        List<Node> allNetworkNodes = network.getNetworkStructure( ).getNodes( ).getNode( );
        
        boolean validTertiaryRingFound = true;
        List<Ring> nonvisitedRings = new ArrayList<Ring>(localRings);
        List<Ring> visitedRings = new ArrayList<Ring>();
        
        //Create new tertiary ring instance.
        Ring tertiaryRing = new Ring();
        
        //Pick a starting ring at random
        Random randomGenerator = new Random();
        int rand = randomGenerator.nextInt(localRings.size( ));
        Ring currentRing  = localRings.get(rand);
        //nonvisitedRings.remove(currentRing);
        //visitedRings.add(currentRing);
        
        Dijkstra dijkstra = new Dijkstra();
        int counter = 0;
        
        Node bestLeftChoice = null;
        Node bestRightChoice = null;
        DijkstraNode bestDNode = null;
        
        while(nonvisitedRings.size( ) > 1)
        {         
            if(counter == 0)
            {
                double shortestDistance = Double.POSITIVE_INFINITY;                
                for(int k=0; k<currentRing.getNodes( ).size( )-1; k++)
                {
                    Node node = currentRing.getNodes( ).get( k );
                    //First try to connect to the closest ring not yet connected
                    //Remove all node that are:
                    //  1)Already on the tertiary ring 
                    //  2)On this ring
                    //  3)Spur nodes
                    List<String> nodesToRemove = QaRsapUtils.nodesToRemove(tertiaryRing, spurs, visitedRings, new String[]{node.getId()});
                    List<Node> reducedAllNodes = new ArrayList<Node>(allNetworkNodes);
                    List<Node> origAllNode = network.getNetworkStructure( ).getNodes( ).getNode( );
                    for(int i=0; i<nodesToRemove.size( ); i++)
                    {
                        Node thisNode = QaRsapUtils.getNodeById( nodesToRemove.get(i), origAllNode);
                        reducedAllNodes.remove( thisNode );
                    }
                    
                    DijkstraNode dn = getBestconnectors( node, reducedAllNodes, nodesToRemove, currentRing );
                    if(dn == null)
                    {
                       continue;
                    }
                    else
                    {
                        if(shortestDistance > dn.getDistanceToRoot( ))
                        {
                            shortestDistance = dn.getDistanceToRoot( );
                            bestLeftChoice = node;
                            bestRightChoice = (QaRsapUtils.getNodeById( dn.getNodeName( ), allNetworkNodes ));
                            bestDNode = dn;
                        }
                    }                    
                } 
                if(bestDNode==null || bestLeftChoice == null)
                {
                    return  null; //no solution possible from here.
                }
                visitedRings.add( currentRing );
                nonvisitedRings.remove(currentRing);
                
                tertiaryRing.addNode( bestLeftChoice );
                for(String nodeName : bestDNode.getPathFromRoot( ))
                {
                    tertiaryRing.getNodes( ).add(QaRsapUtils.getNodeById( nodeName, allNetworkNodes));
                }
                
                counter ++;
            }
            else
            {
                currentRing = QaRsapUtils.getRingByNode( bestRightChoice, localRings );
                
                List<String> nodesToRemove = QaRsapUtils.nodesToRemove(tertiaryRing, spurs, visitedRings, new String[]{bestRightChoice.getId()});
                List<Node> reducedAllNodes = new ArrayList<Node>(allNetworkNodes);
                List<Node> origAllNode = network.getNetworkStructure( ).getNodes( ).getNode( );
                for(int i=0; i<nodesToRemove.size( ); i++)
                {
                    Node thisNode = QaRsapUtils.getNodeById( nodesToRemove.get(i), origAllNode);
                    reducedAllNodes.remove( thisNode );
                }
                DijkstraNode dn = getBestconnectors( bestRightChoice, reducedAllNodes, nodesToRemove, currentRing );
                if(dn == null)
                {
                    nodesToRemove = QaRsapUtils.nodesToRemove(tertiaryRing, spurs, null, new String[]{bestRightChoice.getId()});
                    reducedAllNodes = new ArrayList<Node>(allNetworkNodes);
                    origAllNode = network.getNetworkStructure( ).getNodes( ).getNode( );
                    for(int i=0; i<nodesToRemove.size( ); i++)
                    {
                        Node thisNode = QaRsapUtils.getNodeById( nodesToRemove.get(i), origAllNode);
                        reducedAllNodes.remove( thisNode );
                    }
                    dn = getBestconnectors( bestLeftChoice, reducedAllNodes, nodesToRemove, currentRing );
                    if(dn == null)
                    {
                        //Failed to genreate a vlaid tertiary ring
                        return null;
                    }
                } 
                for(String nodeName : dn.getPathFromRoot( ))
                {
                    tertiaryRing.getNodes( ).add(QaRsapUtils.getNodeById( nodeName, allNetworkNodes));
                }
                bestRightChoice = tertiaryRing.getNodes( ).get( tertiaryRing.getSize( )-1 );
                visitedRings.add(currentRing);
                nonvisitedRings.remove( currentRing );
            }
        }
        List<Node> tempNodeList = new ArrayList<Node>(allNetworkNodes);
        Iterator<Node> tertiaryRingIter = tertiaryRing.getNodes().iterator( );
        while(tertiaryRingIter.hasNext( ))
        {
            tempNodeList.remove(tertiaryRingIter.next());
        }
        if(tertiaryRing.getSize( ) >= 3) 
        {
            //Complete the ring
            List<String> nodesToRemove = QaRsapUtils.nodesToRemove(tertiaryRing, spurs, null, new String[]{tertiaryRing.getSpecificNodeName(0), bestRightChoice.getId()});
            List<Node> reducedAllNodes = new ArrayList<Node>(allNetworkNodes);
            List<Node> origAllNode = network.getNetworkStructure( ).getNodes( ).getNode( );
            for(int i=0; i<nodesToRemove.size( ); i++)
            {
                Node thisNode = QaRsapUtils.getNodeById( nodesToRemove.get(i), origAllNode);
                reducedAllNodes.remove( thisNode );
            }
            //call dijsktra
            List<DijkstraNode> returnedNodes = dijkstra.runDijkstra( tertiaryRing.getSpecificNode(tertiaryRing.getSize( )-1),
                                      tertiaryRing.getSpecificNode( 0),
                                      reducedAllNodes, 
                                      nodeAdjacencies.reducedClone(nodesToRemove));
              
              if(returnedNodes != null && returnedNodes.size( ) == 1)
              {
                  DijkstraNode returnedNode = returnedNodes.get(0);
                  for(String nodeName : returnedNode.getPathFromRoot( ))
                  {
                      tertiaryRing.getNodes( ).add(QaRsapUtils.getNodeById( nodeName, network.getNetworkStructure( ).getNodes( ).getNode( ) ));
                  }
              }
              else
              {
                  //cannot find path back. Failed to build tertiray Ring
                  return null;
              }
        }
        else //handle ring of two nodes
        {
            //Complete the ring
            List<String> nodesToRemove = QaRsapUtils.nodesToRemove(tertiaryRing, spurs, null, new String[]{bestRightChoice.getId()});
            List<Node> reducedAllNodes = new ArrayList<Node>(allNetworkNodes);
            for(int i=0; i<nodesToRemove.size( ); i++)
            {
                Node thisNode = QaRsapUtils.getNodeById( nodesToRemove.get(i), allNetworkNodes);
                reducedAllNodes.remove( thisNode );
            }
            //call dijsktra
            List<DijkstraNode> returnedNodes = dijkstra.runDijkstra( tertiaryRing.getSpecificNode(tertiaryRing.getSize( )-1),
                                      null,
                                      reducedAllNodes, 
                                      nodeAdjacencies.reducedClone(nodesToRemove));

            if(returnedNodes != null)
            {
                double shorestDistance = Double.POSITIVE_INFINITY;
                DijkstraNode bestFirstNode = null;
                DijkstraNode bestSecondNode = null;
                for(int i=0; i<returnedNodes.size( ); i++ )
                {
                    DijkstraNode firstNode = returnedNodes.get(i);
                    Node node = QaRsapUtils.getNodeById( firstNode.getNodeName( ), allNetworkNodes );
                    if(!QaRsapUtils.isNodeOnRing( node, tertiaryRing ))
                    {
                        nodesToRemove = QaRsapUtils.nodesToRemove(tertiaryRing, spurs, null, new String[]{tertiaryRing.getSpecificNodeName(0)});
                        reducedAllNodes = new ArrayList<Node>(allNetworkNodes);
                        for(int j=0; j<nodesToRemove.size( ); j++)
                        {
                            Node thisNode = QaRsapUtils.getNodeById( nodesToRemove.get(j), allNetworkNodes);
                            reducedAllNodes.remove( thisNode );
                        }
                        //call dijsktra
                        List<DijkstraNode> returnedNodes2 = dijkstra.runDijkstra(node,tertiaryRing.getSpecificNode(0),
                                                  reducedAllNodes, 
                                                  nodeAdjacencies.reducedClone(nodesToRemove));
                        if(returnedNodes2 != null && returnedNodes2.size( ) == 1)
                        {
                            DijkstraNode secondNode = returnedNodes2.get(0);
                            if(shorestDistance > firstNode.getDistanceToRoot( ) + secondNode.getDistanceToRoot( ))
                            {
                                shorestDistance = firstNode.getDistanceToRoot( ) + secondNode.getDistanceToRoot( );
                                bestFirstNode = firstNode;
                                bestSecondNode = secondNode;
                            }
                        }
                        else
                        {
                            //cannot find path back. Failed to build tertiray Ring
                            return null;
                        }
                    }
                }
                //Add paths of both dijkstra nodes to complete the ring
                for(String nodeName : bestFirstNode.getPathFromRoot( ))
                {
                    tertiaryRing.getNodes( ).add(QaRsapUtils.getNodeById( nodeName, network.getNetworkStructure( ).getNodes( ).getNode( ) ));
                }
                for(String nodeName : bestSecondNode.getPathFromRoot( ))
                {
                    tertiaryRing.getNodes( ).add(QaRsapUtils.getNodeById( nodeName, network.getNetworkStructure( ).getNodes( ).getNode( ) ));
                }
            }
            else
            {
                //cannot find path back. Failed to build tertiary Ring
                return null;
            }
        }
        return tertiaryRing;
    }
    
    private DijkstraNode getBestconnectors(Node node, List<Node> reducedAllNodes, List<String> nodesToRemove, Ring currentRing)
    {
        Dijkstra dijkstra = new Dijkstra();
        List<Node> allNetworkNodes = network.getNetworkStructure( ).getNodes( ).getNode( );
        
        //call dijsktra
        List<DijkstraNode> returnedNodes = dijkstra.runDijkstra(node, null, reducedAllNodes, 
                                nodeAdjacencies.reducedClone(nodesToRemove));
        
        DijkstraNode bestRightChoice = null;
        
        if(returnedNodes != null && returnedNodes.size( ) > 0)
        {   
            for(int i=0; i<returnedNodes.size( ); i++)
            {
                DijkstraNode dn = returnedNodes.get(i);
                Node closestNode = QaRsapUtils.getNodeById( dn.getNodeName( ), allNetworkNodes);
                if(QaRsapUtils.isNodeOnRing( closestNode, currentRing ))
                {
                    continue;
                }
                else
                {
                    bestRightChoice = dn;
                    //As they are ordered, we have found the closest on another ring
                    break;
                }
            }
        }
        else
        {
           //No path found. No solution possible  
            return null;
        }
        
        return bestRightChoice;
    }
    
//    public Ring generateTertiaryRingOld(List<Spur> spurs, List<Ring> rings, List<Node> tempNodeList)
//    {
//        boolean validTertiaryRingFound = true;
//        List<Ring> nonvisitedRings = new ArrayList<Ring>(rings);
//        
//        Ring tertiaryRing = new Ring();
//        
//        Random randomGenerator = new Random();
//        int rand = randomGenerator.nextInt(tempNodeList.size());
//        Node pickedNode = tempNodeList.get(rand);
//        tertiaryRing.addNode(pickedNode);
//        tempNodeList.remove(pickedNode);
//        
//        Ring parentRing = QaRsapUtils.getRingByNode(pickedNode, rings );
//        nonvisitedRings.remove(parentRing);
//        
//        while(nonvisitedRings.size( )>0)
//        {
//            List<AdjNode> adjNodes = nodeAdjacencies.getAdjList(pickedNode.getId( ));
//            Ring foundRing = null;
//            Node nextNode = null;
//            for(AdjNode adjNode : adjNodes)
//            {
//                nextNode = QaRsapUtils.getNodeById( adjNode.getNodeName(), tempNodeList );
//                if(nextNode != null)
//                {
//                    foundRing = findApplicableParent(tertiaryRing, nonvisitedRings, nextNode);
//                    if(foundRing != null && nonvisitedRings.contains(foundRing))
//                    {
//                        nonvisitedRings.remove(foundRing); 
//                        pickedNode = nextNode;
//                        break;
//                    }   
//                }       
//            }
//            if(foundRing == null)
//            {
//                //Relax constraint
//                for(AdjNode adjNode : adjNodes)
//                {
//                    nextNode = QaRsapUtils.getNodeById( adjNode.getNodeName(), tempNodeList );
//                    if(foundRing == null) //allow search on all other rings.
//                    {
//                       foundRing = findApplicableParent(tertiaryRing, rings, nextNode);
//                       if(foundRing!=null)
//                       {
//                           pickedNode = nextNode;
//                           break;
//                       }
//                    }
//                }
//                if(foundRing == null)
//                {
//                    validTertiaryRingFound = false;
//                    tertiaryRing = null;
//                    break;
//                }
//            }
//        }
//        
//        if(validTertiaryRingFound)
//        {
//            if(tertiaryRing.getSize( ) < 3)
//            {
//                Node lastNode = tertiaryRing.getNodes( ).get( tertiaryRing.getSize( )-1);
//                List<AdjNode> adjList = nodeAdjacencies.getAdjList( lastNode.getId( ) );
//                boolean notOnTertiaryRing = false;
//                int counter = 0;
//                while(!notOnTertiaryRing)
//                {
//                    Node thisNode = QaRsapUtils.getNodeById(adjList.get(counter).getNodeName( ), network.getNetworkStructure( ).getNodes( ).getNode( ) );
//                    if(!QaRsapUtils.isNodeOnRing( thisNode, tertiaryRing ))
//                    {
//                        tertiaryRing.getNodes( ).add( thisNode );
//                        notOnTertiaryRing = true;
//                    }
//                    counter++;
//                }
//                
//            }
//            //call dijsktra back to start
//            Dijkstra dijkstra = new Dijkstra();
//            //Remove node that are already on tertiary ring
//            List<String> nodesToRemove = QaRsapUtils.nodesToRemove(tertiaryRing, spurs, null, 
//                                                        new String[]{tertiaryRing.getNodes( ).get(0).getId( ),
//                                                        tertiaryRing.getNodes( ).get(tertiaryRing.getSize( )-1).getId( )}
//                                                        );
//            
//            List<Node> reducedAllNodes = new ArrayList<Node>(network.getNetworkStructure( ).getNodes( ).getNode( ));
//            List<Node> origAllNode = network.getNetworkStructure( ).getNodes( ).getNode( );
//            for(int i=0; i<nodesToRemove.size( ); i++)
//            {
//                Node thisNode = QaRsapUtils.getNodeById( nodesToRemove.get(i), origAllNode);
//                reducedAllNodes.remove( thisNode );
//            }
//            
//            //call dijsktra
//            List<DijkstraNode> returnedNodes = dijkstra.runDijkstra( tertiaryRing.getSpecificNode( tertiaryRing.getSize( )-1),
//                                    tertiaryRing.getSpecificNode( 0),
//                                    reducedAllNodes, 
//                                    nodeAdjacencies.reducedClone(nodesToRemove));
//            
//            if(returnedNodes != null && returnedNodes.size( ) == 1)
//            {
//                DijkstraNode returnedNode = returnedNodes.get(0);
//                for(String nodeName : returnedNode.getPathFromRoot( ))
//                {
//                    tertiaryRing.getNodes( ).add(QaRsapUtils.getNodeById( nodeName, network.getNetworkStructure( ).getNodes( ).getNode( ) ));
//                }
//            }
//            else
//            {
//                //cannot find path back. Failed to build tertiray Ring
//                return null;
//            }
//        }
//        
//        return tertiaryRing;
//    }
    
      
    public Ring findApplicableParent(Ring tertiaryRing, List<Ring> searchableRings, Node childNode)
    {
        Ring returnRing = null;
        
        for(Ring ring : searchableRings)
        {
            if(ring.getNodes( ).contains(childNode) && !tertiaryRing.getNodes( ).contains(childNode))
            {
                tertiaryRing.addNode(childNode);
                returnRing = ring;
                break;
            }    
        }
        return returnRing;       
    }
    //=======================================
    //
    //======================================
    public Spur createSrup(Node currentNode, List<Node> allNodes, List<Ring> localRings)
    {
        String currentNodeName = currentNode.getId( );
        
        Spur spur = new Spur();
        spur.setSpurNode(currentNode);
        
        //Find closest local ring that spur can be attached to
        List<AdjNode> adjList = nodeAdjacencies.getAdjList(currentNodeName);
        for (int i=0; i<adjList.size( ); i++)
        {
            boolean parentfound = false;
            AdjNode currentAdjNode = adjList.get(i);
            Node currentAdjNodeObj = QaRsapUtils.getNodeById(currentAdjNode.getNodeName(), allNodes); 
            for (int j=0; j<localRings.size(); j++)
            {
                Ring currentLocalRing  = localRings.get(j);
                if(currentLocalRing.getNodes( ).contains(currentAdjNodeObj))
                {
                    spur.setParentNode(currentAdjNodeObj);
                    parentfound = true;
                    break;
                }
            }
            if(parentfound)
                break;
        }
        
        if(spur.getParentNode( ) == null)
            return null;
        
        return spur;
    }
    
    public Ring createLocalRing(Node currentNode, List<Node> allNodes, List<Node> tempNodeList)
    {
        Ring currentRing = new Ring();
        String currentNodeName = currentNode.getId( );
        
        //Add the first node to the ring
        currentRing.addNode(currentNode);
        tempNodeList.remove(currentNode);
        
        //While the ring Size is within ring size limit
        boolean noMoreNodes = false;
        while (currentRing.getSize() < Constants.initMaxLocalRingSize && !noMoreNodes)
        {
            List<AdjNode> adjList = nodeAdjacencies.getAdjList(currentNodeName); // Get adjacent node for the current node
            String nearestAdjNode = findLowestAdjCost(adjList, tempNodeList);    // Find lowest cost adjacent node that is still available
            
            if(nearestAdjNode != null)
            {    
                currentNode = QaRsapUtils.getNodeById(nearestAdjNode, allNodes);     // Get Nearest Node object
                currentNodeName = currentNode.getId( );
                currentRing.addNode(currentNode);
                tempNodeList.remove(currentNode);
            }
            else
            { 
                 noMoreNodes = true;
            }
        }
        boolean ringComplete = completeRing(currentRing, allNodes, tempNodeList );
        if(ringComplete == true) 
        {
           //do nothing
        }
        else
        {
            currentRing = null; 
        }
        return currentRing;
    }
    
    public String findLowestAdjCost(List<AdjNode> adjList, List<Node> tempNodeList)
    {
        String node = null;
        
        for (int i=0; i<adjList.size( ); i++)
        {
           AdjNode adjNode = adjList.get(i);
           if(QaRsapUtils.getNodeById( adjNode.getNodeName( ), tempNodeList ) != null)
           {
               node = adjNode.getNodeName( );
               break;
           }
        }
        return node;
    }
    
    public boolean completeRing(Ring currentRing,  List<Node> allNodes, List<Node> tempNodeList)
    {
        boolean ringComplete = false;

        String lastNodeName = null;
        Node lastNode = null;
        
        int ringSize = currentRing.getSize();
        while( ringSize > 2)
        {
            lastNodeName = currentRing.getSpecificNodeName(ringSize-1);
            List<AdjNode> adjList = nodeAdjacencies.getAdjList(lastNodeName);
            lastNode = QaRsapUtils.getNodeById(lastNodeName, allNodes);  
            if(QaRsapUtils.isAdj(currentRing.getSpecificNodeName(0), adjList))
            {
                currentRing.addNode(currentRing.getSpecificNode(0));
                ringComplete = true;
                break;
            }
            else
            {
                currentRing.removeNode(lastNode);
                tempNodeList.add(lastNode);
            }
            ringSize = currentRing.getSize();
        }
        if(!ringComplete) //Ring cannot be complete, disassamble ring.
        {
            while( ringSize > 0)
            {
                lastNodeName = currentRing.getSpecificNodeName(ringSize-1);
                lastNode = QaRsapUtils.getNodeById(lastNodeName, allNodes); 
                currentRing.removeNode(lastNode);
                tempNodeList.add(lastNode);    
                ringSize = currentRing.getSize();
            }
            currentRing = null;
        }
        return ringComplete;
    }   
}
