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
import ie.ucd.mscba.qa_rsap.settings.VNSSettings;
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

import de.zib.sndlib.network.Link;
import de.zib.sndlib.network.Network;
import de.zib.sndlib.network.Node;
import de.zib.sndlib.network.Nodes;

/**
 * 
 */
public class SolutionGenerator
{
    private Network         network         = null;
    private NodeAdjacencies nodeAdjacencies = null;
    List<Link>              networkLinks    = null;
    List<Node>              networkNodes    = null;
    private Dijkstra        dijkstra        = null;
    private VNSSettings     vnsSettings     = null;

    /**
     * SolutionGenerator Object which accepts the three elements required to generate initial solutions.
     * These are: 1) The Network Representation
     *            2) The adjacency matrix
     *            3) User define setting for the problem
     */
    public SolutionGenerator( Network network, NodeAdjacencies nodeAdjacencies, VNSSettings vnsSettings )
    {
        super( );
        this.network = network;
        this.nodeAdjacencies = nodeAdjacencies;
        this.networkLinks = network.getNetworkStructure().getLinks().getLink();
        this.networkNodes = network.getNetworkStructure().getNodes().getNode();
        this.dijkstra = new Dijkstra(this.networkNodes, this.nodeAdjacencies);
        this.vnsSettings = vnsSettings;
    }

    /**
     * This method is responsible for generating an initial solution. This process takes 3 stages, 
     *  1) Generating local ring the do not violate the size constraint.
     *  2) Any node that could not be added as ring are added as spurs.
     *  3) Generating a tertiary ring that connects all local ring. 
     */
    public Solution getInitialSolution()
    {
        Solution sol = new Solution(vnsSettings);

        List<Node> allNetworkNodes = network.getNetworkStructure( ).getNodes( ).getNode( );

        List<Node> tempNodeList = new ArrayList<Node>( allNetworkNodes);
        int counter = tempNodeList.size( );
        Random randomGenerator = new Random( );

        // build rings and spurs
        List<Node> spursCandidates = new ArrayList<Node>( );
        while ( counter != 0 )
        {
            int randomInt = randomGenerator.nextInt( counter ); // Get a random node of remaining to start new ring
            Node currentNode = tempNodeList.get( randomInt );

            Ring localRing = createLocalRing(currentNode, allNetworkNodes, 
                                            tempNodeList, spursCandidates, sol.getLocalrings( ) );
            if ( localRing == null )
            {
                // Failed to find a local ring for this node.
                // Save to be added as a spur later
                spursCandidates.add( currentNode );
                tempNodeList.remove( currentNode );
            }
            else
            {
                sol.getLocalrings( ).add( localRing );
            }
            counter = tempNodeList.size( );
        }
        for ( Node node : spursCandidates )
        {
            Spur spur = createSrup( node, allNetworkNodes, sol.getLocalrings( ) );
            if ( spur != null )
            {
                sol.getSpurs( ).add( spur );
            }
            else
            {
                return null; // spur cannot find parent, infeasible solution.
            }
        }

        tempNodeList = new ArrayList<Node>( allNetworkNodes);
        List<Spur> spurs = sol.getSpurs( );
        for ( Spur spur : spurs )
        {
            tempNodeList.remove( spur.getSpurNode( ) );
        }

        // Generate tertiary ring if we have mode than one local ring
        if ( sol.getLocalrings( ).size( ) > 1 )
        {
            Ring tertiaryRing = generateTertiaryRing( sol.getSpurs( ), sol.getLocalrings( ) );
            if ( tertiaryRing != null )
            {
                sol.setTertiaryRing( tertiaryRing );
            }
            else
            {
                return null; // No valid initial sol found;
            }
        }
        sol.calculateTotalCost( network.getNetworkStructure( ).getLinks( ).getLink( ) );
        //System.out.println( "Total solution Cost : " + sol.getTotalCost( ) );
        return sol;
    }

    /**
     * This method is responsible for creating spur objects. Nodes that cannot be added to a ring are spur objcet.
     * This method ensure that a spur is created off it closest node which is already on a ring.
     */
    public Spur createSrup(Node currentNode, List<Node> allNodes, List<Ring> localRings)
    {
        String currentNodeName = currentNode.getId( );

        Spur spur = new Spur( );
        spur.setSpurNode( currentNode );

        // Find closest local ring that spur can be attached to
        List<AdjNode> adjList = nodeAdjacencies.getAdjList( currentNodeName );
        for ( int i = 0; i < adjList.size( ); i++ )
        {
            boolean parentfound = false;
            AdjNode currentAdjNode = adjList.get( i );
            Node currentAdjNodeObj = QaRsapUtils.getNodeById( currentAdjNode.getNodeName( ), allNodes );
            for ( int j = 0; j < localRings.size( ); j++ )
            {
                Ring currentLocalRing = localRings.get( j );
                if ( currentLocalRing.getNodes( ).contains( currentAdjNodeObj ) )
                {
                    spur.setParentNode( currentAdjNodeObj );
                    parentfound = true;
                    break;
                }
            }
            if ( parentfound )
                break;
        }

        if ( spur.getParentNode( ) == null )
            return null;

        return spur;
    }

    /**
     * Given a seed node, this method is responsible for creating a local ring around this node. It is greedy by nature
     * in that it continues to add the closest node to the previous node as long as more adjacent nodes exist and the size 
     * constraints are node breached. When on to these stopping conditions are met, an attempt is made to complete the ring. 
     * Dijkstra with backtracking is used to ensure every possibility of successfully completing the ring.
     */
    public Ring createLocalRing(Node currentNode, List<Node> allNodes, List<Node> tempNodeList, 
                                List<Node> spurCandidates, List<Ring> localRings)
    {
        Ring currentRing = new Ring( );
        String currentNodeName = currentNode.getId( );

        // Add the first node to the ring
        currentRing.addNode( currentNode );
        tempNodeList.remove( currentNode );

        // While the ring Size is within ring size limit
        boolean noMoreNodes = false;
        while ( currentRing.getSize( ) < vnsSettings.getInitMaxLRSize() && !noMoreNodes )
        {
            List<AdjNode> adjList = nodeAdjacencies.getAdjList( currentNodeName ); // Get adjacent node for the current
                                                                                   // node
            String nearestAdjNode = findLowestAdjCost( adjList, tempNodeList ); // Find lowest cost adjacent node that
                                                                                // is still available

            if ( nearestAdjNode != null )
            {
                currentNode = QaRsapUtils.getNodeById( nearestAdjNode, allNodes ); // Get Nearest Node object
                currentNodeName = currentNode.getId( );
                currentRing.addNode( currentNode );
                tempNodeList.remove( currentNode );
            }
            else
            {
                noMoreNodes = true;
            }
        }
        boolean ringComplete = completeRing( currentRing, allNodes, tempNodeList, spurCandidates, localRings);
        if ( ringComplete == true )
        {
            // do nothing
        }
        else
        {
            currentRing = null;
        }
        return currentRing;
    }

    /**
     * This method returns the closet node to selected node node that has not already been used
     * as part of the solution.
     */
    public String findLowestAdjCost(List<AdjNode> adjList, List<Node> tempNodeList)
    {
        String node = null;

        for ( int i = 0; i < adjList.size( ); i++ )
        {
            AdjNode adjNode = adjList.get( i );
            if ( QaRsapUtils.getNodeById( adjNode.getNodeName( ), tempNodeList ) != null )
            {
                node = adjNode.getNodeName( );
                break;
            }
        }
        return node;
    }

    /**
     * This method is responsible from completing a local ring when stopping constrains have been met.
     */
    public boolean completeRing(Ring currentRing, List<Node> allNodes, List<Node> tempNodeList, List<Node> spurCandidates,
                    List<Ring> rings)
    {
        boolean ringComplete = false;
        List<Node> allNetworkNodes = network.getNetworkStructure( ).getNodes( ).getNode( );
        String lastNodeName = null;
        Node lastNode = null;

        int ringSize = currentRing.getSize( );
        while ( ringSize > 2 )
        {
            lastNodeName = currentRing.getSpecificNodeName( ringSize - 1 );
            List<AdjNode> adjList = nodeAdjacencies.getAdjList( lastNodeName );
            lastNode = QaRsapUtils.getNodeById( lastNodeName, allNodes );

            if ( QaRsapUtils.isAdj( currentRing.getSpecificNodeName( 0 ), adjList ) )
            {
                currentRing.addNode( currentRing.getSpecificNode( 0 ) );
                ringComplete = true;
                break;
            }
            else if ( ringSize < vnsSettings.getInitMaxLRSize()-1 )
            {
                List<String> nodesToRemove = QaRsapUtils.nodesToRemove( currentRing, null, rings, new String[] {
                        lastNodeName, currentRing.getSpecificNodeName( 0 ) } );
                List<DijkstraNode> returnedNodes = dijkstra.runDijkstra( lastNode, currentRing.getSpecificNode( 0 ),
                                                                            nodesToRemove);

                if ( returnedNodes != null
                                && returnedNodes.get( 0 ).getPathFromRoot( ).size( ) < (vnsSettings.getMaxLocalRingSize() - ringSize) )
                {
                    DijkstraNode returnedNode = returnedNodes.get( 0 );
                    List<String> pathList = returnedNode.getPathFromRoot( );

                    for ( int j = 0; j < pathList.size( ) - 1; j++ )
                    {
                        Node thisNode = QaRsapUtils.getNodeById( pathList.get( j ), allNetworkNodes );
                        currentRing.addNode( thisNode );
                        tempNodeList.remove( thisNode );
                        if(spurCandidates.contains( thisNode ))
                        {
                            spurCandidates.remove( thisNode );
                        }
                    }
                    currentRing.addNode( currentRing.getSpecificNode( 0 ) );
                    ringComplete = true;
                    break;
                }
                else
                {
                    currentRing.removeNode( lastNode );
                    tempNodeList.add( lastNode );
                    ringSize = currentRing.getSize( );
                    continue;
                }
            }
            else
            {
                currentRing.removeNode( lastNode );
                tempNodeList.add( lastNode );
                ringSize = currentRing.getSize( );
            }
        }
        if ( !ringComplete ) // Ring cannot be complete, disassamble ring.
        {
            while ( ringSize > 0 )
            {
                lastNodeName = currentRing.getSpecificNodeName( ringSize - 1 );
                lastNode = QaRsapUtils.getNodeById( lastNodeName, allNodes );
                currentRing.removeNode( lastNode );
                tempNodeList.add( lastNode );
                ringSize = currentRing.getSize( );
            }
            currentRing = null;
        }
        return ringComplete;
    }
    /**
     * The generateTertiaryRing method is responsible for joining all local ring together. Firstly it pick a local ring 
     * at random and then finds its closest neighbouring ring. Once these two rings are connected, the process is repeated,
     * using Dijkstra to one ring to the next. When all local rings have been visited, Dijkstra is used again to connect the 
     * start and end nodes to complete the ring.
     * 
     */
    public Ring generateTertiaryRing(List<Spur> spurs, List<Ring> localRings)
    {
        List<Node> allNetworkNodes = network.getNetworkStructure( ).getNodes( ).getNode( );

        boolean validTertiaryRingFound = true;
        List<Ring> nonvisitedRings = new ArrayList<Ring>( localRings );
        List<Ring> visitedRings = new ArrayList<Ring>( );

        // Create new tertiary ring instance.
        Ring tertiaryRing = new Ring( );

        // Pick a starting ring at random
        Random randomGenerator = new Random( );
        int rand = randomGenerator.nextInt( localRings.size( ) );
        Ring currentRing = localRings.get( rand );
        // nonvisitedRings.remove(currentRing);
        // visitedRings.add(currentRing);

        int counter = 0;

        Node bestLeftChoice = null;
        Node bestRightChoice = null;
        DijkstraNode bestDNode = null;

        while ( nonvisitedRings.size( ) > 1 )
        {
            if ( counter == 0 )
            {
                double shortestDistance = Double.POSITIVE_INFINITY;
                for ( int k = 0; k < currentRing.getNodes( ).size( ) - 1; k++ )
                {
                    Node node = currentRing.getNodes( ).get( k );
                    // First try to connect to the closest ring not yet connected
                    // Remove all node that are:
                    // 1)Already on the tertiary ring
                    // 2)On this ring
                    // 3)Spur nodes
                    List<String> nodesToRemove = QaRsapUtils.nodesToRemove( tertiaryRing, spurs, visitedRings,
                                    new String[] { node.getId( ) } );

                    DijkstraNode dn = getBestconnectors( node, nodesToRemove, currentRing );
                    if ( dn == null )
                    {
                        continue;
                    }
                    else
                    {
                        if ( shortestDistance > dn.getDistanceToRoot( ) )
                        {
                            shortestDistance = dn.getDistanceToRoot( );
                            bestLeftChoice = node;
                            bestRightChoice = (QaRsapUtils.getNodeById( dn.getNodeName( ), allNetworkNodes ));
                            bestDNode = dn;
                        }
                    }
                }
                if ( bestDNode == null || bestLeftChoice == null )
                {
                    return null; // no solution possible from here.
                }
                visitedRings.add( currentRing );
                nonvisitedRings.remove( currentRing );

                tertiaryRing.addNode( bestLeftChoice );
                for ( String nodeName : bestDNode.getPathFromRoot( ) )
                {
                    tertiaryRing.getNodes( ).add( QaRsapUtils.getNodeById( nodeName, allNetworkNodes ) );
                }

                counter++;
            }
            else
            {
                currentRing = QaRsapUtils.getRingByNode( bestRightChoice, localRings );

                List<String> nodesToRemove = QaRsapUtils.nodesToRemove( tertiaryRing, spurs, visitedRings,
                                new String[] { bestRightChoice.getId( ) } );
                
                DijkstraNode dn = getBestconnectors( bestRightChoice, nodesToRemove, currentRing );
                if ( dn == null )
                {
                    nodesToRemove = QaRsapUtils.nodesToRemove( tertiaryRing, spurs, null,
                                    new String[] { bestRightChoice.getId( ) } );
                    dn = getBestconnectors( bestLeftChoice, nodesToRemove, currentRing );
                    if ( dn == null )
                    {
                        // Failed to genreate a vlaid tertiary ring
                        return null;
                    }
                }
                for ( String nodeName : dn.getPathFromRoot( ) )
                {
                    tertiaryRing.getNodes( ).add( QaRsapUtils.getNodeById( nodeName, allNetworkNodes ) );
                }
                bestRightChoice = tertiaryRing.getNodes( ).get( tertiaryRing.getSize( ) - 1 );
                visitedRings.add( currentRing );
                nonvisitedRings.remove( currentRing );
            }
        }
        List<Node> tempNodeList = new ArrayList<Node>( allNetworkNodes );
        Iterator<Node> tertiaryRingIter = tertiaryRing.getNodes( ).iterator( );
        while ( tertiaryRingIter.hasNext( ) )
        {
            tempNodeList.remove( tertiaryRingIter.next( ) );
        }
        if ( tertiaryRing.getSize( ) >= 3 )
        {
            // Complete the ring
            List<String> nodesToRemove = QaRsapUtils.nodesToRemove( tertiaryRing, spurs, null, new String[] {
                    tertiaryRing.getSpecificNodeName( 0 ), bestRightChoice.getId( ) } );
            // call dijsktra
            List<DijkstraNode> returnedNodes = dijkstra.runDijkstra(
                            tertiaryRing.getSpecificNode( tertiaryRing.getSize( ) - 1 ),
                            tertiaryRing.getSpecificNode( 0 ), nodesToRemove);

            if ( returnedNodes != null && returnedNodes.size( ) == 1 )
            {
                DijkstraNode returnedNode = returnedNodes.get( 0 );
                for ( String nodeName : returnedNode.getPathFromRoot( ) )
                {
                    tertiaryRing.getNodes( ).add(
                                    QaRsapUtils.getNodeById( nodeName, network.getNetworkStructure( ).getNodes( )
                                                    .getNode( ) ) );
                }
            }
            else
            {
                // cannot find path back. Failed to build tertiray Ring
                return null;
            }
        }
        else
        // handle ring of two nodes
        {
            // Complete the ring
            List<String> nodesToRemove = QaRsapUtils.nodesToRemove( tertiaryRing, spurs, null,
                            new String[] { bestRightChoice.getId( ) } );
            // call dijsktra
            List<DijkstraNode> returnedNodes = dijkstra.runDijkstra(
                            tertiaryRing.getSpecificNode( tertiaryRing.getSize( ) - 1 ), null, nodesToRemove);

            if ( returnedNodes != null )
            {
                double shorestDistance = Double.POSITIVE_INFINITY;
                DijkstraNode bestFirstNode = null;
                DijkstraNode bestSecondNode = null;
                for ( int i = 0; i < returnedNodes.size( ); i++ )
                {
                    DijkstraNode firstNode = returnedNodes.get( i );
                    Node node = QaRsapUtils.getNodeById( firstNode.getNodeName( ), allNetworkNodes );
                    if ( !QaRsapUtils.isNodeOnRing( node, tertiaryRing ) )
                    {
                        nodesToRemove = QaRsapUtils.nodesToRemove( tertiaryRing, spurs, null,
                                        new String[] { tertiaryRing.getSpecificNodeName( 0 ) } );
                        // call dijsktra
                        List<DijkstraNode> returnedNodes2 = dijkstra.runDijkstra( node,
                                        tertiaryRing.getSpecificNode( 0 ), nodesToRemove);
                        if ( returnedNodes2 != null && returnedNodes2.size( ) == 1 )
                        {
                            DijkstraNode secondNode = returnedNodes2.get( 0 );
                            if ( shorestDistance > firstNode.getDistanceToRoot( ) + secondNode.getDistanceToRoot( ) )
                            {
                                shorestDistance = firstNode.getDistanceToRoot( ) + secondNode.getDistanceToRoot( );
                                bestFirstNode = firstNode;
                                bestSecondNode = secondNode;
                            }
                        }
                        else
                        {
                            // cannot find path back. Failed to build tertiray Ring
                            return null;
                        }
                    }
                }
                // Add paths of both dijkstra nodes to complete the ring
                for ( String nodeName : bestFirstNode.getPathFromRoot( ) )
                {
                    tertiaryRing.getNodes( ).add(
                                    QaRsapUtils.getNodeById( nodeName, network.getNetworkStructure( ).getNodes( )
                                                    .getNode( ) ) );
                }
                for ( String nodeName : bestSecondNode.getPathFromRoot( ) )
                {
                    tertiaryRing.getNodes( ).add(
                                    QaRsapUtils.getNodeById( nodeName, network.getNetworkStructure( ).getNodes( )
                                                    .getNode( ) ) );
                }
            }
            else
            {
                // cannot find path back. Failed to build tertiary Ring
                return null;
            }
        }
        return tertiaryRing;
    }
    
    /**
     * This method uses Dijkstra to find the best connection point (local ring) to selected node.
     */
    private DijkstraNode getBestconnectors(Node node, List<String> nodesToRemove, Ring currentRing)
    {
       List<Node> allNetworkNodes = network.getNetworkStructure( ).getNodes( ).getNode( );

        // call dijsktra
        List<DijkstraNode> returnedNodes = dijkstra.runDijkstra( node, null, nodesToRemove);
                     
        DijkstraNode bestRightChoice = null;

        if ( returnedNodes != null && returnedNodes.size( ) > 0 )
        {
            for ( int i = 0; i < returnedNodes.size( ); i++ )
            {
                DijkstraNode dn = returnedNodes.get( i );
                Node closestNode = QaRsapUtils.getNodeById( dn.getNodeName( ), allNetworkNodes );
                if ( QaRsapUtils.isNodeOnRing( closestNode, currentRing ) )
                {
                    continue;
                }
                else
                {
                    bestRightChoice = dn;
                    // As they are ordered, we have found the closest on another ring
                    break;
                }
            }
        }
        else
        {
            // No path found. No solution possible
            return null;
        }

        return bestRightChoice;
    }
}
