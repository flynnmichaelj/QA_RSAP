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
import ie.ucd.mscba.qa_rsap.valueobjects.NodeAdjacencies;
import ie.ucd.mscba.qa_rsap.valueobjects.Ring;
import ie.ucd.mscba.qa_rsap.valueobjects.Solution;
import ie.ucd.mscba.qa_rsap.valueobjects.Spur;

import java.util.ArrayList;
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

            Ring localRing  = createLocalRing(currentNode, nodes.getNode(), tempNodeList);
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
            Ring tertiaryRing = generateTertiaryRing(sol.getSpurs( ), sol.getLocalrings(), tempNodeList);
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
    public Ring generateTertiaryRing(List<Spur> spurs, List<Ring> rings, List<Node> tempNodeList)
    {
        boolean validTertiaryRingFound = true;
        List<Ring> nonvisitedRings = new ArrayList<Ring>(rings);
        
        Ring tertiaryRing = new Ring();
        
        Random randomGenerator = new Random();
        int rand = randomGenerator.nextInt(tempNodeList.size());
        Node pickedNode = tempNodeList.get(rand);
        tertiaryRing.addNode(pickedNode);
        tempNodeList.remove(pickedNode);
        
        Ring parentRing = QaRsapUtils.getRingByNode(pickedNode, rings );
        nonvisitedRings.remove(parentRing);
        
        while(nonvisitedRings.size( )>0)
        {
            List<AdjNode> adjNodes = nodeAdjacencies.getAdjList(pickedNode.getId( ));
            Ring foundRing = null;
            Node nextNode = null;
            for(AdjNode adjNode : adjNodes)
            {
                nextNode = QaRsapUtils.getNodeById( adjNode.getNodeName(), tempNodeList );
                if(nextNode != null)
                {
                    foundRing = findApplicableParent(tertiaryRing, nonvisitedRings, nextNode);
                    if(foundRing != null && nonvisitedRings.contains(foundRing))
                    {
                        nonvisitedRings.remove(foundRing); 
                        pickedNode = nextNode;
                        break;
                    }   
                }       
            }
            if(foundRing == null)
            {
                //Relax constraint
                for(AdjNode adjNode : adjNodes)
                {
                    nextNode = QaRsapUtils.getNodeById( adjNode.getNodeName(), tempNodeList );
                    if(foundRing == null) //allow search on all other rings.
                    {
                       foundRing = findApplicableParent(tertiaryRing, rings, nextNode);
                       if(foundRing!=null)
                       {
                           pickedNode = nextNode;
                           break;
                       }
                    }
                }
                if(foundRing == null)
                {
                    validTertiaryRingFound = false;
                    tertiaryRing = null;
                    break;
                }
            }
        }
        
        if(validTertiaryRingFound)
        {
            if(tertiaryRing.getSize( ) < 3)
            {
                Node lastNode = tertiaryRing.getNodes( ).get( tertiaryRing.getSize( )-1);
                List<AdjNode> adjList = nodeAdjacencies.getAdjList( lastNode.getId( ) );
                boolean notOnTertiaryRing = false;
                int counter = 0;
                while(!notOnTertiaryRing)
                {
                    Node thisNode = QaRsapUtils.getNodeById(adjList.get(counter).getNodeName( ), network.getNetworkStructure( ).getNodes( ).getNode( ) );
                    if(!QaRsapUtils.isNodeOnRing( thisNode, tertiaryRing ))
                    {
                        tertiaryRing.getNodes( ).add( thisNode );
                        notOnTertiaryRing = true;
                    }
                    counter++;
                }
                
            }
            //call dijsktra back to start
            Dijkstra dijkstra = new Dijkstra();
            //Remove node that are already on tertiary ring
            List<String> nodesToRemove = QaRsapUtils.nodesToRemove(tertiaryRing, spurs, 
                                                        tertiaryRing.getNodes( ).get(0).getId( ),
                                                        tertiaryRing.getNodes( ).get(tertiaryRing.getSize( )-1).getId( )
                                                        );
            
            List<Node> reducedAllNodes = new ArrayList<Node>(network.getNetworkStructure( ).getNodes( ).getNode( ));
            List<Node> origAllNode = network.getNetworkStructure( ).getNodes( ).getNode( );
            for(int i=0; i<nodesToRemove.size( ); i++)
            {
                Node thisNode = QaRsapUtils.getNodeById( nodesToRemove.get(i), origAllNode);
                reducedAllNodes.remove( thisNode );
            }
            
            //call dijsktra
            TreeMap<String, DijkstraNode> returnedNodes = dijkstra.runDijkstra( tertiaryRing.getSpecificNode( tertiaryRing.getSize( )-1),
                                    tertiaryRing.getSpecificNode( 0),
                                    reducedAllNodes, 
                                    nodeAdjacencies.reducedClone(nodesToRemove));
            
            if(returnedNodes != null)
            {
                String key = returnedNodes.firstKey( );
                DijkstraNode returnedNode = returnedNodes.get( key );
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
        
        return tertiaryRing;
    }
    
      
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
