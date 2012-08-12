/**
 * Created on 11 Jul 2012
 *
 * @author Michael Flynn
 * @version 1.0
 *
 * File: NeighbourGenerator.java
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
import ie.ucd.mscba.qa_rsap.valueobjects.NodeAdjacencies;
import ie.ucd.mscba.qa_rsap.valueobjects.Ring;
import ie.ucd.mscba.qa_rsap.valueobjects.Solution;
import ie.ucd.mscba.qa_rsap.valueobjects.Spur;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import de.zib.sndlib.network.Link;
import de.zib.sndlib.network.Network;
import de.zib.sndlib.network.Node;

/**
 * This class is responsible for generating neighbourhood candidates. In total there are six types of neighbourhood
 * searches. They are designed to provide a good mix of solution modification to ensure a wide range of exploration in
 * the solution space.
 */
public class NeighbourGenerator
{
    private NodeAdjacencies nodeAdjacencies = null;
    private Network         network         = null;
    private List<Link>      networkLinks    = null;
    private List<Node>      networkNodes    = null;
    private Dijkstra        dijkstra        = null;
    private VNSSettings     vnsSettings     = null;
    
    //Random generator
    private static Random rand = new Random();

    private SolutionGenerator solGenerator = null; 
    
    /**
     * NeighbourGenerator constructor that accepts the network model and the adjacency matrix for all nodes in the
     * network.
     */
    public NeighbourGenerator(NodeAdjacencies nodeAdjacencies, Network network, VNSSettings vnsSettings)
    {
        this.nodeAdjacencies = nodeAdjacencies;
        this.network = network;
        this.networkLinks = network.getNetworkStructure().getLinks().getLink();
        this.networkNodes = network.getNetworkStructure().getNodes().getNode();
        this.dijkstra = new Dijkstra(this.networkNodes, this.nodeAdjacencies);
        this.vnsSettings = vnsSettings;
        solGenerator = new SolutionGenerator(network, nodeAdjacencies, vnsSettings);
    }

    /**
     * Edge insertion search attempts to find two spur nodes which are adjacent and can be inserted into an adjacent
     * local ring
     */
    public Solution edgeInsertionSearch(Solution sol)
    {
        Solution clonedSol = sol.clone();

        List<Ring> localRings = clonedSol.getLocalrings();
        List<Spur> spurs = clonedSol.getSpurs();

        if(spurs != null && spurs.size() > 1)
        {
            Spur firstSpur = spurs.get(rand.nextInt(spurs.size()));

            List<AdjNode> spurAdjs = nodeAdjacencies.getAdjList(firstSpur.getSpurNode().getId());
            for(int i = 0; i < spurAdjs.size(); i++)
            {
                AdjNode thisClosest = spurAdjs.get(i);
                Node thisClosestNode = QaRsapUtils.getNodeById(thisClosest.getNodeName(), networkNodes);
                Ring closestRing = QaRsapUtils.getRingByNode(thisClosestNode, localRings);
                if(closestRing != null && closestRing.getSize() < vnsSettings.getMaxLocalRingSize() - 1)
                {
                    int positionOfClosestNode = findNodePosOnRing(closestRing, thisClosestNode);

                    Node leftNode = null;
                    Node rightNode = null;
                    int posLeftNode = 0;
                    int posRightNode = 0;
                    if(positionOfClosestNode == 0)
                    {
                        leftNode = closestRing.getNodes().get((closestRing.getNodes().size()) - 2);
                        rightNode = closestRing.getNodes().get(1);
                        posLeftNode = (closestRing.getNodes().size()) - 2;
                        posRightNode = positionOfClosestNode + 1;
                    }
                    else
                    {
                        leftNode = closestRing.getNodes().get(positionOfClosestNode - 1);
                        rightNode = closestRing.getNodes().get(positionOfClosestNode + 1);
                        posLeftNode = positionOfClosestNode - 1;
                        posRightNode = positionOfClosestNode + 1;
                    }

                    // Try leftSide first
                    boolean operationComplete = false;
                    List<AdjNode> leftNodeAdjs = nodeAdjacencies.getAdjList(leftNode.getId());
                    for(int j = 0; j < leftNodeAdjs.size(); j++)
                    {
                        AdjNode current = leftNodeAdjs.get(j); 
                        Node currentNode = QaRsapUtils.getNodeById(current.getNodeName(), networkNodes);
                        Spur secondSpur = QaRsapUtils.isNodeASpur(current.getNodeName(), spurs);
                        if(secondSpur != null && currentNode != firstSpur.getSpurNode())
                        {
                            if(QaRsapUtils.isAdj(current.getNodeName(), spurAdjs))
                            {
                                closestRing.getNodes().add(posLeftNode + 1, currentNode);
                                closestRing.getNodes().add(posLeftNode + 2, firstSpur.getSpurNode());
                                spurs.remove(firstSpur);
                                spurs.remove(secondSpur);
                                operationComplete = true;
                                break;
                            }
                        }
                        else
                        {
                            continue;
                        }
                    }
                    if(operationComplete)
                    {
                        break;
                    }

                    // Try Right Side next
                    List<AdjNode> rightNodeAdjs = nodeAdjacencies.getAdjList(rightNode.getId());
                    for(int j = 0; j < rightNodeAdjs.size(); j++)
                    {
                        AdjNode current = rightNodeAdjs.get(j); 
                        Node currentNode = QaRsapUtils.getNodeById(current.getNodeName(), networkNodes);
                        Spur secondSpur = QaRsapUtils.isNodeASpur(current.getNodeName(), spurs);
                        if(secondSpur != null && currentNode != firstSpur.getSpurNode())
                        {
                            if(QaRsapUtils.isAdj(current.getNodeName(), spurAdjs))
                            {
                                closestRing.getNodes().add(posRightNode, firstSpur.getSpurNode());
                                closestRing.getNodes().add(posRightNode + 1, currentNode);
                                spurs.remove(firstSpur);
                                spurs.remove(secondSpur);
                                operationComplete = true;
                                break;
                            }
                        }
                        else
                        {
                            continue;
                        }
                    }
                    if(operationComplete)
                        break;
                }
                else
                {
                    continue;
                }
            }
        }

        // Validate soluton
        if(! clonedSol.validate(nodeAdjacencies, networkNodes.size()))
        {
            //validation error detected, returning original solution
            System.out.println("validation error detected on edge inserion, returning original solution");
            clonedSol=sol; 
        }
        return clonedSol;
    }

    /**
     * Split local search attempts to find a local ring that can be split. It iterates through the ring searching for 2 sets of 2 
     * nodes that are are adjacent and are the correct location on the ring for a split to be performed.
     */
    public Solution splitLocalSearch(Solution sol, NodeAdjacencies adjList)
    {
        Solution clonedSol = sol.clone();
        
        Ring spiltCandidate = null;
        Ring clonedSpiltCandidate = null;
        for(Ring ring : clonedSol.getLocalrings())
        {
            if(ring.getSize() >= Constants.MIN_SIZE_FOR_SPLIT)
            {
                spiltCandidate = ring;
                break;
            }
        }
        if(spiltCandidate != null)
        {
            clonedSpiltCandidate = spiltCandidate.clone();
            clonedSpiltCandidate.getNodes().remove(clonedSpiltCandidate.getSize() - 1);

            boolean solutionFound = false;
            Ring firstNewRing = null;

            for(int i = 0; i < clonedSpiltCandidate.getSize() - 3; i++)
            {
                for(int j = i + 2; j < clonedSpiltCandidate.getSize() - 3; j++)
                {
                    Node firstRingNode1 = clonedSpiltCandidate.getSpecificNode(i);
                    Node firstRingNode2 = clonedSpiltCandidate.getSpecificNode(j);

                    Node secondRingNode1 = null;
                    if(i != 0)
                        secondRingNode1 = clonedSpiltCandidate.getSpecificNode(i - 1);
                    else
                        secondRingNode1 = clonedSpiltCandidate.getSpecificNode(clonedSpiltCandidate.getSize() - 1);

                    Node secondRingNode2 = clonedSpiltCandidate.getSpecificNode(j + 1);

                    if(QaRsapUtils.isAdj(firstRingNode1.getId(), adjList.getAdjList(firstRingNode2.getId())))
                    {
                        if(QaRsapUtils.isAdj(secondRingNode1.getId(), adjList.getAdjList(secondRingNode2.getId())))
                        {
                            // Build first new ring
                            firstNewRing = new Ring();
                            for(int k = i; k <= j; k++)
                            {
                                firstNewRing.getNodes().add(clonedSpiltCandidate.getNodes().get(k));
                            }
                            for(int k = 0; k < firstNewRing.getNodes().size(); k++)
                            {
                                clonedSpiltCandidate.removeNode(firstNewRing.getSpecificNode(k));
                            }

                            firstNewRing.getNodes().add(firstNewRing.getNodes().get(0));
                            clonedSol.getLocalrings().add(firstNewRing);

                            // The remainder is the second new ring
                            clonedSpiltCandidate.addNode(clonedSpiltCandidate.getNodes().get(0));
                            clonedSol.getLocalrings().add(clonedSpiltCandidate);

                            // Remove original split candidate ring
                            clonedSol.getLocalrings().remove(spiltCandidate);
                            solutionFound = true;
                            break;
                        }
                    }
                }
                if(solutionFound)
                    break;
            }
            //Ensure newly created rings are still connected to tertiary Ring. If not, connect them.
            if(solutionFound)
            {
                if(clonedSol.getTertiaryRing() == null)
                {
                    List<Node> tempNodeList = new ArrayList<Node>(network.getNetworkStructure().getNodes().getNode());
                    for(Spur spur : clonedSol.getSpurs())
                    {
                        tempNodeList.remove(spur.getSpurNode());
                    }
                   
                    Ring tertiaryRing = solGenerator.generateTertiaryRing(clonedSol.getSpurs(), clonedSol.getLocalrings());
                    if(tertiaryRing != null)
                    {
                        clonedSol.setTertiaryRing(tertiaryRing);
                    }
                    else
                    {
                        return sol; 
                    }
                }
                else
                {
                    Ring tertiaryRing = clonedSol.getTertiaryRing();

                    boolean firstRingConnected = false; // Connected to tertiary
                    boolean clonedSplitCandidateconnected = false; // Connected to tertiary

                    for(Node node : firstNewRing.getNodes())
                    {
                        if(QaRsapUtils.isNodeOnRing(node, tertiaryRing))
                        {
                            firstRingConnected = true;
                            break;
                        }
                    }
                    for(Node node : clonedSpiltCandidate.getNodes())
                    {
                        if(QaRsapUtils.isNodeOnRing(node, tertiaryRing))
                        {
                            clonedSplitCandidateconnected = true;
                            break;
                        }
                    }
                    if(! firstRingConnected || ! clonedSplitCandidateconnected)
                    {
                        // remove final node to aid reconnecting
                        tertiaryRing.getNodes().remove(tertiaryRing.getSize() - 1); 
                    }
                    if(! firstRingConnected)
                    {
                        addLocalRingToTertiary(tertiaryRing, firstNewRing, rand, clonedSol.getSpurs());
                    }
                    else if(! clonedSplitCandidateconnected)
                    {
                        addLocalRingToTertiary(tertiaryRing, clonedSpiltCandidate, rand, clonedSol.getSpurs());
                    }
                }
            }
        }

        // Validate soluton
        if(! clonedSol.validate(nodeAdjacencies, networkNodes.size()))
        {
            //validation error detected, returning original solution
            System.out.println("validation error detected on split local ring, returning original solution");
            clonedSol=sol; 
        }
        return clonedSol;
    }
    
    /**
     * Tertiary ring search modifies the tertiary by disconnection a node attempting to reconnect it. The is randomised VNS 
     * which may result in a better tertiary ring or, alternatively allow local ring node to be modified in later searches.
     */
    public Solution tertiaryRingSearch(Solution sol)
    {
        Solution clonedSol = sol.clone();

        List<Ring> localRings = clonedSol.getLocalrings();
        Ring tertiaryRing = clonedSol.getTertiaryRing();
        List<Spur> spurs = clonedSol.getSpurs();

        // First remove the last node on the tertiary ring
        tertiaryRing.getNodes().remove(tertiaryRing.getSize() - 1);

        // Disconnect selected ring
        Ring initRing = localRings.get(rand.nextInt(localRings.size()));
        for(Node node : initRing.getNodes())
        {

            int posOnRing = findNodePosOnRing(tertiaryRing, node);
            if(posOnRing > - 1)
            {
                tertiaryRing.getNodes().remove(posOnRing);
            }
        }
        
        if(tertiaryRing.getSize() == 1)
        {
            // do nothing
        }
        else
        {
            // ensure each node on the tertiary is adjacent to the next
            // If not call Dijkstra to connect them.
            for(int i = 0; i < tertiaryRing.getSize() - 1; i++)
            {
                Node leftNode = tertiaryRing.getSpecificNode(i);
                Node rightNode = tertiaryRing.getSpecificNode(i + 1);
                if(! QaRsapUtils.isAdj(leftNode.getId(), nodeAdjacencies.getAdjList(rightNode.getId())))
                {
                    List<String> nodesToRemove = QaRsapUtils.nodesToRemove(tertiaryRing, spurs, null,
                                    new String[] {leftNode.getId(), rightNode.getId()});
                    List<DijkstraNode> returnedNodes = dijkstra.runDijkstra(leftNode, rightNode, nodesToRemove);

                    if(returnedNodes != null && returnedNodes.size() == 1)
                    {
                        DijkstraNode returnedNode = returnedNodes.get(0);
                        List<String> pathList = returnedNode.getPathFromRoot();

                        for(int j = 0; j < pathList.size() - 1; j++)
                        {
                            tertiaryRing.getNodes().add((i + 1) + j, QaRsapUtils.getNodeById(pathList.get(j), networkNodes));
                        }
                    }
                    else
                    {
                        // Failed to find path
                        clonedSol.setTertiaryRing(null);
                        tertiaryRing = solGenerator.generateTertiaryRing(spurs, localRings);
                        if(tertiaryRing != null)
                        {
                            clonedSol.setTertiaryRing(tertiaryRing);
                            return clonedSol;
                        }
                        else
                        {
                            //System.out.println("Failed to genrerate new sol, Returning original");
                            return sol; // Cannot complete search, return original unchanged solution to avoid
                                        // corruption.
                        }
                    }
                }
            }
        }
        // Chcek now if at least one node from the removed ring is back on the tertiary ring
        boolean initRingReconnnected = false;
        for(Node node : initRing.getNodes())
        {
            if(QaRsapUtils.isNodeOnRing(node, tertiaryRing))
            {
                initRingReconnnected = true;
                break;
            }
        }
        if(initRingReconnnected) // We know we have at least 2 nodes. we can reform complete ring
        {
            if(tertiaryRing.getSize() == 2)
            {
                tertiaryRing = reconnect2NodeTertiaryRingbyDijkstra(tertiaryRing, spurs);
                if(tertiaryRing == null)
                {
                    clonedSol.setTertiaryRing(null);
                    tertiaryRing = solGenerator.generateTertiaryRing(spurs, localRings);
                    if(tertiaryRing != null)
                    {
                        clonedSol.setTertiaryRing(tertiaryRing);
                        return clonedSol;
                    }
                    else
                    {
                        return sol; // Cannot complete search, return original unchanged solution to avoid corruption.
                    }
                }
            }
            else
            {
                // Complete the ring
                List<String> nodesToRemove = QaRsapUtils.nodesToRemove(
                                tertiaryRing, spurs, null,
                                new String[] {tertiaryRing.getSpecificNodeName(0),
                                        tertiaryRing.getSpecificNodeName(tertiaryRing.getSize() - 1)});
                // call dijsktra
                List<DijkstraNode> returnedNodes = dijkstra.runDijkstra(tertiaryRing.getSpecificNode(tertiaryRing.getSize() - 1),
                                tertiaryRing.getSpecificNode(0), nodesToRemove);

                if(returnedNodes != null && returnedNodes.size() == 1)
                {
                    DijkstraNode returnedNode = returnedNodes.get(0);
                    for(String nodeName : returnedNode.getPathFromRoot())
                    {
                        tertiaryRing.getNodes().add(
                                        QaRsapUtils.getNodeById(nodeName, network.getNetworkStructure().getNodes().getNode()));
                    }
                }
                else
                {
                    clonedSol.setTertiaryRing(null);
                    tertiaryRing = solGenerator.generateTertiaryRing(spurs, localRings);
                    if(tertiaryRing != null)
                    {
                        clonedSol.setTertiaryRing(tertiaryRing);
                        return clonedSol;
                    }
                    else
                    {
                        return sol; // Cannot complete search, return original unchanged solution to avoid corruption.
                    }
                }
            }
        }
        else
        // We need to reinsert the disconnected ring first
        {
            if(tertiaryRing.getSize() == 1)
            {
                clonedSol.setTertiaryRing(null);
                tertiaryRing = solGenerator.generateTertiaryRing(spurs, localRings);
                if(tertiaryRing != null)
                {
                    clonedSol.setTertiaryRing(tertiaryRing);
                    return clonedSol;
                }
                else
                {
                    return sol; // Cannot complete search, return original unchanged solution to avoid corruption.
                }
            }
            else
            {
                // call add to local ring
                boolean success = addLocalRingToTertiary(tertiaryRing, initRing, rand, spurs);
                if(! success)
                {
                    clonedSol.setTertiaryRing(null);
                    tertiaryRing = solGenerator.generateTertiaryRing(spurs, localRings);
                    if(tertiaryRing != null)
                    {
                        clonedSol.setTertiaryRing(tertiaryRing);
                        return clonedSol;
                    }
                    else
                    {
                        return sol; // Cannot complete search, return original unchanged solution to avoid corruption.
                    }
                }
            }
        }
        // Validate soluton
        if(!clonedSol.validate(nodeAdjacencies, networkNodes.size()))
        {
            //validation error detected, returning original solution
            System.out.println("validation error detected on tertiary ring search, returning original solution");
            clonedSol=sol; 
        }

        return clonedSol;
    }
    
    /**
     * Delete small ring search removes a ring of size 3 and attempts to reinsert its nodes nodes into other adjancet rings. If 
     * any nodes fails to be added, the are stored and retried again a the end of the process.A subsequent node getting added may allow 
     * for this unsuccessful node to be added at a later stage.
     */
    public Solution deleteSmallRingSearch(Solution sol)
    {
        Solution clonedSol = sol.clone();
        List<Ring> localRings = clonedSol.getLocalrings();
        Ring ringToDelete = null;
        for(Ring ring : localRings)
        {
            if(ring.getSize() == 4)
            {
                ringToDelete = ring;
                localRings.remove(ring);
                break;
            }
        }
        if(ringToDelete != null)
        {
            //First ensure this ring has no spurs
            if(clonedSol.getSpurs().size() > 0)
            {
                List<Spur> spursToAdd = new ArrayList<Spur>();
                List<Spur> spurs = clonedSol.getSpurs();
                Iterator<Spur> spurIter = spurs.iterator();
                while(spurIter.hasNext())
                {
                    Spur thisSpur = spurIter.next();
                    if(QaRsapUtils.isNodeOnRing(thisSpur.getParentNode(), ringToDelete))
                    {
                        //if our intended ring to delete has spurs we need to relocation the spurs to other rings
                        Spur spur = solGenerator.createSrup(thisSpur.getSpurNode(), network.getNetworkStructure().getNodes().getNode(), localRings);
                        if(spur == null)
                        {
                            // Fail Gracefully by return original sol
                            return sol;
                        }
                        else
                        {
                            spurIter.remove();
                            spursToAdd.add(spur);
                        }
                    }
                }
                for(int i = 0; i < spursToAdd.size(); i++)
                {
                    clonedSol.getSpurs().add(spursToAdd.get(i));
                }
            }
            
            
            List<Node> retries = new ArrayList<Node>();
            for(int i = 0; i < ringToDelete.getSize() - 1; i++)
            {
                Node thisNode = ringToDelete.getNodes().get(i);
                Ring modifiedRing = insert(thisNode, localRings);
                if(modifiedRing == null)
                {
                    retries.add(thisNode);
                }
            }

            // Handle possible retries
            for(int i = 0; i < retries.size(); i++)
            {
                Node thisNode = retries.get(i);
                Ring modifiedRing = insert(thisNode, localRings);
                if(modifiedRing == null)
                {
                    Spur spur = solGenerator.createSrup(thisNode, network.getNetworkStructure().getNodes().getNode(), localRings);
                    if(spur == null)
                    {
                        // Fail Gracefully by return original sol
                        return sol;
                    }
                    else
                    {
                       clonedSol.getSpurs().add(spur);
                    }
                }                
            }
            
            //Final attempt, insert two spurs as an edge
            if(clonedSol.getSpurs().size() >=2)
            {
                clonedSol = edgeInsertionSearch(clonedSol);
            }
        }
        //if Successful and only one ring remains, remove the tertiarty Ring
        if(clonedSol.getLocalrings().size() == 1)
        {
            clonedSol.setTertiaryRing(null);
        }
        
        // Validate soluton
        if(! clonedSol.validate(nodeAdjacencies, networkNodes.size()))
        {
            //validation error detected, returning original solution
            System.out.println("validation error detected on delete small ring search, returning original solution");
            clonedSol=sol; 
        }

        // TODO handle hoe this affect the tertiary ring
        return clonedSol;
    }
    
    /**
     * Delete insert search attempts two things. Firstly is focuses on spur node. It picks a spur node at random and attempts 
     * to insert it into its closest ring. if there are no nodes left, this search attempts to remove a node from one ring a insert
     * it into another ring.
     */
    public Solution deleteInsertSearch(Solution sol)
    {
        Solution clonedSol = sol.clone();

        List<Ring> localRings = clonedSol.getLocalrings();
        List<Spur> spurs = clonedSol.getSpurs();

        boolean spurModified  = false;
        // First try to eliminate spurs
        if(spurs != null && spurs.size() > 0)
        {
            // pick a random spur
            int randomSpur = rand.nextInt(spurs.size());
            Spur pickedSpur = spurs.get(randomSpur);

            Ring modifiedRing = insert(pickedSpur.getSpurNode(), localRings);
            if(modifiedRing != null)
            {
                spurs.remove(pickedSpur);
                spurModified = true;
            }
            else
            {
                //Readd spur to its closest ring
                Spur newSpur = solGenerator.createSrup(pickedSpur.getSpurNode(), networkNodes, localRings);
                if(newSpur!=null && !(newSpur.getParentNode().getId()).equalsIgnoreCase(pickedSpur.getParentNode().getId()))
                {
                    spurs.remove(pickedSpur);
                    spurs.add(newSpur);
                    spurModified = true;
                }            
            }
        }
        //If no modification were made on spurs, try to delete insert a node.
        //Avoids this elements of the VNS getting trapped.
        if (!spurModified)
        {
            if(sol.getLocalrings().size() > 1)
            {
                if(! deleteInsert(clonedSol, rand))
                {
                    // return original to avoid corrupted solution
                    return sol;
                }
            }
        }

        // Validate soluton
        if(!clonedSol.validate(nodeAdjacencies, networkNodes.size()))
        {
            //validation error detected, returning original solution
            System.out.println("validation error detected on delete inset search, returning original solution");
            clonedSol=sol; 
        }

        return clonedSol;
    }
    
    /**
     * Swap node search attempts to swap two nodes on two local ring. Like all other searches, much validation and
     * consistency chceks are preformed to ensure that are node parties are correctly adjacent.
     */
    public Solution swapNodeSearch(Solution sol)
    {
        Solution clonedSol = sol.clone();
        List<Ring> localRings = clonedSol.getLocalrings();

        boolean changeFound = false;
        int maxTries = 0;
        while(! changeFound && maxTries < 5)
        {
            boolean notInitNodeTertiaryRing = false;
            Ring initRing = localRings.get(rand.nextInt(localRings.size()));
            Node initNode = null;
            int initNodeMaxTries = 0;
            while(sol.getTertiaryRing() != null && ! notInitNodeTertiaryRing && initNodeMaxTries < 20)
            {
                initNode = initRing.getNodes().get(rand.nextInt(initRing.getNodes().size()));
                if(! QaRsapUtils.isNodeOnRing(initNode, clonedSol.getTertiaryRing()))
                    notInitNodeTertiaryRing = true;
                initNodeMaxTries++;
            }
            if(initNode == null)
            {
                continue;
            }
            Ring secondRing = null;
            Node secondNode = null;
            boolean differnetRing = false;
            int secondRingMaxTries = 0;
            while(! differnetRing && secondRingMaxTries < 5)
            {
                secondRing = localRings.get(rand.nextInt(localRings.size()));
                secondRingMaxTries++;
                if(secondRing != initRing)
                {
                    boolean notOnSecondNodeTertiaryRing = false;
                    int secondNodeMaxTries = 0;
                    while(! notOnSecondNodeTertiaryRing && secondNodeMaxTries < 20)
                    {
                        secondNode = secondRing.getNodes().get(rand.nextInt(secondRing.getNodes().size()));
                        if(! QaRsapUtils.isNodeOnRing(secondNode, clonedSol.getTertiaryRing()))
                            notOnSecondNodeTertiaryRing = true;

                        secondNodeMaxTries++;
                    }
                    if(! notOnSecondNodeTertiaryRing)
                    {
                        continue;
                    }
                    differnetRing = true;

                }
                else
                {
                    secondRing = null;
                    secondNode = null;
                }
            }
            if(secondNode == null)
            {
                // cannot find a second node to swap
                // Fail graefully
                return sol;
            }

            Node[] initNodeAdj = new Node[2];
            Node[] secondNodeAdj = new Node[2];

            int initNodePos = findNodePosOnRing(initRing, initNode);
            int secondNodePos = findNodePosOnRing(secondRing, secondNode);

            if(initNodePos == 0)
            {
                initNodeAdj[0] = initRing.getNodes().get(initRing.getSize() - 2);
                initNodeAdj[1] = initRing.getNodes().get(1);
            }
            else
            {
                initNodeAdj[0] = initRing.getNodes().get(initNodePos - 1);
                initNodeAdj[1] = initRing.getNodes().get(initNodePos + 1);
            }
            if(secondNodePos == 0)
            {
                secondNodeAdj[0] = secondRing.getNodes().get(secondRing.getSize() - 2);
                secondNodeAdj[1] = secondRing.getNodes().get(1);
            }
            else
            {
                secondNodeAdj[0] = secondRing.getNodes().get(secondNodePos - 1);
                secondNodeAdj[1] = secondRing.getNodes().get(secondNodePos + 1);
            }

            boolean initNodeCheck = false;
            boolean initOneCheck = QaRsapUtils.isAdj(secondNode.getId(), nodeAdjacencies.getAdjList(initNodeAdj[0].getId()));
            boolean initTwoCheck = QaRsapUtils.isAdj(secondNode.getId(), nodeAdjacencies.getAdjList(initNodeAdj[1].getId()));
            if(initOneCheck && initTwoCheck)
                initNodeCheck = true;

            boolean secondNodeCheck = false;
            boolean secondOneCheck = QaRsapUtils.isAdj(initNode.getId(), nodeAdjacencies.getAdjList(secondNodeAdj[0].getId()));
            boolean secondTwoCheck = QaRsapUtils.isAdj(initNode.getId(), nodeAdjacencies.getAdjList(secondNodeAdj[1].getId()));
            if(secondOneCheck && secondTwoCheck)
                secondNodeCheck = true;

            if(initNodeCheck && secondNodeCheck) // we can perform swap
            {
                // preform removes
                if(initNodePos == 0)
                {
                    initRing.getNodes().remove(initRing.getSize() - 1);
                    initRing.getNodes().remove(0);
                }
                else
                {
                    initRing.getNodes().remove(initNodePos);
                }
                if(secondNodePos == 0)
                {
                    secondRing.getNodes().remove(secondRing.getSize() - 1);
                    secondRing.getNodes().remove(0);
                }
                else
                {
                    secondRing.getNodes().remove(secondNodePos);
                }

                // perform adds
                if(secondNodePos == 0)
                {
                    secondRing.getNodes().add(0, initNode);
                    secondRing.getNodes().add(secondRing.getSize(), initNode);
                }
                else
                {
                    secondRing.getNodes().add(secondNodePos, initNode);
                }
                if(initNodePos == 0)
                {
                    initRing.getNodes().add(0, secondNode);
                    initRing.getNodes().add(initRing.getSize(), secondNode);
                }
                else
                {
                    initRing.getNodes().add(initNodePos, secondNode);
                }

                changeFound = true;
            }
            maxTries++;
        }

        // Validate soluton
        if(!clonedSol.validate(nodeAdjacencies, networkNodes.size()))
        {
            //validation error detected, returning original solution
            System.out.println("validation error detected on swap node search, returning original solution");
            clonedSol=sol; 
        }

        return clonedSol;
    }

    /**
     * This method is used by the deleteInsertSearch and attempts to delete a node from one ring and insert it into another ring. 
     * Two local rings are modified in the process. Care is taken to allow node that are already on the tertiarty ring to avoid
     * corrupting the solution.
     */
    public boolean deleteInsert(Solution clonedSol, Random rand)
    {
        boolean success = false;

        List<Ring> localRings = clonedSol.getLocalrings();
        boolean changeFound = false;
        int maxTriesCounter = 0;
        while(! changeFound && maxTriesCounter <= 10)
        {
            maxTriesCounter++;

            Ring selectedRing = null; // Focus on rings the breach max size constraint
            for(Ring ring : localRings)
            {
                if(ring.getSize() > vnsSettings.getMaxLocalRingSize())
                {
                    selectedRing = ring;
                    break;
                }
            }
            if(selectedRing == null)
            {
                List<Ring> clonedListForDel = new ArrayList<Ring>();
                for(Ring ring : localRings)
                {
                    if(ring.getSize() > 4)
                        clonedListForDel.add(ring);
                }
                if(clonedListForDel.size() > 0)
                    selectedRing = clonedListForDel.get(rand.nextInt(clonedListForDel.size()));
                else
                    break; 
            }

            boolean notOnTertiaryRing = false;
            Node selectedNode = null;
            int nodeMaxTries = 0;
            while(clonedSol.getTertiaryRing() != null && ! notOnTertiaryRing && nodeMaxTries < 20)
            {
                nodeMaxTries++;
                selectedNode = selectedRing.getNodes().get(rand.nextInt(selectedRing.getNodes().size()));
                if(! QaRsapUtils.isNodeOnRing(selectedNode, clonedSol.getTertiaryRing()))
                    notOnTertiaryRing = true;
            }
            if(selectedNode == null)
                continue;

            List<Ring> clonedListforIns = new ArrayList<Ring>();
            for(Ring ring : localRings)
            {
                if(ring != selectedRing && ring.getSize() < vnsSettings.getMaxLocalRingSize())
                    clonedListforIns.add(ring);
            }

            Ring modifiedRing = insert(selectedNode, clonedListforIns);
            if(modifiedRing != null)
            {
                if(delete(selectedNode, selectedRing))
                {
                    changeFound = true;
                    success = true;
                }
                else
                {
                    // Delete failed so remove from inserted ring to avoid corrupting solution
                    if(! delete(selectedNode, modifiedRing))
                    {
                        // if remove also fails, end search and return original sol to avoid corruption
                        return false;
                    }
                }
            }
        }
        return success;
    }

    /**
     * This method deletes a node from a selected ring. The node can only be deleted if the nodes either side of it are
     * adjancet. Otherwise the resulting local ring would not be valid.
     */
    public boolean delete(Node deleteNode, Ring deleteRing)
    {
        boolean completedSucessfully = false;

        int positionOnRing = findNodePosOnRing(deleteRing, deleteNode);
        Node leftNode = null;
        Node rightNode = null;
        if(positionOnRing == 0)
        {
            leftNode = deleteRing.getNodes().get(1);
            rightNode = deleteRing.getNodes().get((deleteRing.getNodes().size()) - 2);
        }
        else
        {
            leftNode = deleteRing.getNodes().get(positionOnRing - 1);
            rightNode = deleteRing.getNodes().get(positionOnRing + 1);
        }
        if(QaRsapUtils.isAdj(leftNode.getId(), nodeAdjacencies.getAdjList(rightNode.getId())))
        {
            if(positionOnRing == 0)
            {
                deleteRing.getNodes().remove(0); // Check this
                deleteRing.getNodes().remove(deleteRing.getSize() - 1);
                Node startNode = deleteRing.getNodes().get(0);
                deleteRing.getNodes().add(deleteRing.getSize(), startNode);
            }
            else
            {
                deleteRing.removeNode(deleteNode);
            }
            completedSucessfully = true;
        }
        return completedSucessfully;
    }

    /**
     * This method is used by delete insert search and aims to insert a chosen node into its closest ring.
     * Adjaceny constrains are maintained. In addition, the location of the insert is important so assuming adjacent 
     * on both sides, the side which will results in the smallest ring is chosen.
     */
    public Ring insert(Node insertNode, List<Ring> rings)
    {
        Ring modifiedRing = null;

        List<AdjNode> adjList = nodeAdjacencies.getAdjList(insertNode.getId());
        Iterator<AdjNode> iter = adjList.iterator();

        while(iter.hasNext())
        {
            boolean noSolutionRing = true;
            AdjNode currentAdjNode = iter.next();
            Node closestNode = QaRsapUtils.getNodeById(currentAdjNode.getNodeName(), network.getNetworkStructure().getNodes()
                            .getNode());
            Ring parentRing = null;
            for(Ring ring : rings)
            {
                List<Node> currentRingNodes = ring.getNodes();
                if((currentRingNodes).contains(closestNode) && ! (currentRingNodes).contains(insertNode)
                                && ring.getSize() < vnsSettings.getMaxLocalRingSize())
                {
                    parentRing = ring;
                    noSolutionRing = false;
                    break;
                }
            }

            if(noSolutionRing)
                continue;

            int positionOfClosestNode = findNodePosOnRing(parentRing, closestNode);

            if(positionOfClosestNode == 0)
            {
                Node selecteNode = null;
                Node firstAdj = parentRing.getNodes().get(1);
                Node lastAdj = parentRing.getNodes().get((parentRing.getNodes().size()) - 2);

                // Here we want to insert between the closest nodes and its next nearest node
                double firstAdjCost = QaRsapUtils.isAdjCost(firstAdj.getId(), adjList);
                double lastAdjCost = QaRsapUtils.isAdjCost(lastAdj.getId(), adjList);
                if(firstAdjCost > 0.0 && lastAdjCost > 0.0)
                {
                    selecteNode = firstAdjCost < lastAdjCost ? firstAdj : lastAdj;
                    if(selecteNode == firstAdj)
                        parentRing.getNodes().add(1, insertNode);
                    else
                        parentRing.getNodes().add((parentRing.getNodes().size()) - 1, insertNode);

                    modifiedRing = parentRing;
                    break;
                }
                else if(firstAdjCost > 0.0 || lastAdjCost > 0.0)
                {
                    selecteNode = firstAdjCost > lastAdjCost ? firstAdj : lastAdj;
                    if(selecteNode == firstAdj)
                        parentRing.getNodes().add(1, insertNode);
                    else
                        parentRing.getNodes().add((parentRing.getNodes().size()) - 1, insertNode);

                    modifiedRing = parentRing;
                    break;
                }
                else
                {
                    continue;
                }
            }
            else
            {
                Node selecteNode = null;
                Node leftNode = parentRing.getNodes().get(positionOfClosestNode - 1);
                Node rightNode = parentRing.getNodes().get(positionOfClosestNode + 1);
                double leftCost = QaRsapUtils.isAdjCost(leftNode.getId(), adjList);
                double rightCost = QaRsapUtils.isAdjCost(rightNode.getId(), adjList);
                if(leftCost > 0.0 && rightCost > 0.0)
                {
                    selecteNode = leftCost < rightCost ? leftNode : rightNode;
                    if(selecteNode == leftNode)
                        parentRing.getNodes().add(positionOfClosestNode, insertNode);
                    else
                        parentRing.getNodes().add(positionOfClosestNode + 1, insertNode);

                    modifiedRing = parentRing;
                    break;
                }
                else if(leftCost > 0.0 || rightCost > 0.0)
                {
                    selecteNode = leftCost > rightCost ? leftNode : rightNode;
                    if(selecteNode == leftNode)
                        parentRing.getNodes().add(positionOfClosestNode, insertNode);
                    else
                        parentRing.getNodes().add(positionOfClosestNode + 1, insertNode);

                    modifiedRing = parentRing;
                    break;
                }
                else
                {
                    continue;
                }
            }
        }
        return modifiedRing;
    }

    /**
     *This method is responsible for completing a tertiary which, at this point only has two nodes.
     *A two node ring is invalid so another third node must be chosen before the ring can be completed. 
     */
    private Ring reconnect2NodeTertiaryRingbyDijkstra(Ring tertiaryRing, List<Spur> clonedSolSpurs)
    {
        // Complete the ring
        List<String> nodesToRemove = QaRsapUtils.nodesToRemove(tertiaryRing, clonedSolSpurs, null,
                        new String[] {tertiaryRing.getSpecificNodeName(tertiaryRing.getSize() - 1)});
        // call dijsktra
        List<DijkstraNode> returnedNodes = dijkstra.runDijkstra(
                        tertiaryRing.getSpecificNode(tertiaryRing.getSize() - 1), null, nodesToRemove);

        if(returnedNodes != null)
        {
            double shorestDistance = Double.POSITIVE_INFINITY;
            DijkstraNode bestFirstNode = null;
            DijkstraNode bestSecondNode = null;
            for(int i = 0; i < returnedNodes.size(); i++)
            {
                DijkstraNode firstNode = returnedNodes.get(i);
                Node node = QaRsapUtils.getNodeById(firstNode.getNodeName(), networkNodes);
                if(! QaRsapUtils.isNodeOnRing(node, tertiaryRing))
                {
                    nodesToRemove = QaRsapUtils.nodesToRemove(tertiaryRing, clonedSolSpurs, null,
                                    new String[] {tertiaryRing.getSpecificNodeName(0)});
                    // call dijsktra
                    List<DijkstraNode> returnedNodes2 = dijkstra.runDijkstra(
                                    node, tertiaryRing.getSpecificNode(0), nodesToRemove);
                    
                    if(returnedNodes2 != null && returnedNodes2.size() == 1)
                    {
                        DijkstraNode secondNode = returnedNodes2.get(0);
                        if(shorestDistance > firstNode.getDistanceToRoot() + secondNode.getDistanceToRoot())
                        {
                            shorestDistance = firstNode.getDistanceToRoot() + secondNode.getDistanceToRoot();
                            bestFirstNode = firstNode;
                            bestSecondNode = secondNode;
                        }
                    }
                    else
                    {
                        // cannot find path back. Failed to build tertiray Ring
                        //System.out.println("1) failed to build tertiary ring in perturbe tertiary search");
                        return null;
                    }
                }
            }
            if(bestFirstNode == null || bestSecondNode == null)
            {
                // cannot find path back. Failed to build tertiary Ring
                //System.out.println("1) failed to build tertiary ring in perturbe tertiary search");
                return null;
            }
            // Add paths of both dijkstra nodes to complete the ring
            for(String nodeName : bestFirstNode.getPathFromRoot())
            {
                tertiaryRing.getNodes().add(QaRsapUtils.getNodeById(nodeName, networkNodes));
            }
            for(String nodeName : bestSecondNode.getPathFromRoot())
            {
                tertiaryRing.getNodes().add(QaRsapUtils.getNodeById(nodeName, networkNodes));
            }
        }
        return tertiaryRing;
    }

    /**
     * This method connect a currently disconnected local ring to the tertiary ring. A node on the ring is chosen at random and dijkstra is
     * used to determine where on the tertiary ring this node should be placed.Again left side and right side of possible addition points
     * are check to enusre the closest is selected.
     */
    private boolean addLocalRingToTertiary(Ring tertiaryRing, Ring initRing, Random rand, List<Spur> spurs)
    {
        boolean succeeded = false;
        // TODO should we pick random node or try for best fit, i.e. check all node to tertiary
        Node randNode = initRing.getNodes().get((rand.nextInt(initRing.getSize())));

        List<String> nodesToRemove = QaRsapUtils.nodesToRemove(null, spurs, null, null);
        List<DijkstraNode> returnedNodes1 = dijkstra.runDijkstra(randNode, null, nodesToRemove);
        
        Iterator<DijkstraNode> iter = returnedNodes1.iterator();
        double closest = Double.POSITIVE_INFINITY;
        Node closestNode = null;
        DijkstraNode closestDNode = null;
        DijkstraNode dn = null;
        while(iter.hasNext())
        {
            dn = iter.next();
            Node node = QaRsapUtils.getNodeById(dn.getNodeName(), network.getNetworkStructure().getNodes().getNode());
            if(QaRsapUtils.isNodeOnRing(node, tertiaryRing))
            {
                if(dn.getDistanceToRoot() < closest)
                {
                    closest = dn.getDistanceToRoot();
                    closestNode = node;
                    closestDNode = dn;
                }
            }
        }
        int posOnTRing = findNodePosOnRing(tertiaryRing, closestNode);
        int posLeftSide = 0;
        int posRightSide = 0;
        Node leftside = null;
        Node rightSide = null;

        /*****************************************************
         * Here we need to get left side and right side to determine which is the best part to connect this unconnected
         * ring to the tertiary
         ****************************************************/
        if(posOnTRing == 0)
        {
            leftside = tertiaryRing.getNodes().get(tertiaryRing.getSize() - 1);
            rightSide = tertiaryRing.getNodes().get(posOnTRing + 1);
            posLeftSide = tertiaryRing.getSize() - 1;
            posRightSide = posOnTRing + 1;
        }
        else if(posOnTRing == tertiaryRing.getSize() - 1)
        {
            leftside = tertiaryRing.getNodes().get(posOnTRing - 1);
            rightSide = tertiaryRing.getNodes().get(0);
            posLeftSide = posOnTRing - 1;
            posRightSide = 0;
        }
        else
        {
            leftside = tertiaryRing.getNodes().get(posOnTRing - 1);
            rightSide = tertiaryRing.getNodes().get(posOnTRing + 1);
            posLeftSide = posOnTRing - 1;
            posRightSide = posOnTRing + 1;
        }

        // Call dijkstra for leftside node
        // Exclude all other tertiray and spur nodes
        DijkstraNode sideToInsert = null;
        int sideSelected = - 1; // -1=none selected, 0 = left select, 1 = right selected
        nodesToRemove = QaRsapUtils.nodesToRemove(tertiaryRing, spurs, null, new String[] {leftside.getId()});
        // We need to remove the node that the other search found
        for(int k = 0; k < closestDNode.getPathFromRoot().size() - 1; k++)
        {
            nodesToRemove.add(closestDNode.getPathFromRoot().get(k));
        }
        List<DijkstraNode> returnedNodes2 = dijkstra.runDijkstra(leftside, randNode, nodesToRemove);
        if(returnedNodes2 != null && returnedNodes2.size() == 1)
        {
            sideToInsert = returnedNodes2.get(0);
            sideSelected = 0;
        }

        // Call dijkstra for rightSide node
        // Exclude all other tertiary and spur nodes
        nodesToRemove = QaRsapUtils.nodesToRemove(tertiaryRing, spurs, null, new String[] {rightSide.getId()});
        // We need to remove the node that the other search found
        for(int k = 0; k < closestDNode.getPathFromRoot().size() - 1; k++)
        {
            nodesToRemove.add(closestDNode.getPathFromRoot().get(k));
        }
        List<DijkstraNode> returnedNodes3 = dijkstra.runDijkstra(randNode, rightSide, nodesToRemove);
        if(returnedNodes3 != null && returnedNodes3.size() == 1)
        {
            if(sideToInsert != null)
            {
                if(sideToInsert.getDistanceToRoot() > returnedNodes3.get(0).getDistanceToRoot())
                {
                    sideToInsert = returnedNodes3.get(0);
                    sideSelected = 1;
                }
            }
            else
            {
                sideToInsert = returnedNodes3.get(0);
                sideSelected = 1;
            }
        }

        if(sideToInsert != null && sideSelected != - 1)
        {
            if(sideSelected == 0)// leftside selected
            {
                int offset = 0;
                for(String nodeName : sideToInsert.getPathFromRoot())
                {
                    tertiaryRing.getNodes().add((posLeftSide + 1) + offset,
                                    QaRsapUtils.getNodeById(nodeName, network.getNetworkStructure().getNodes().getNode()));
                    offset++;
                }
                for(int k = 0; k < closestDNode.getPathFromRoot().size() - 1; k++)
                {
                    String thisName = closestDNode.getPathFromRoot().get(k);
                    Node thisNode = QaRsapUtils.getNodeById(thisName, network.getNetworkStructure().getNodes().getNode());
                    tertiaryRing.getNodes().add((posLeftSide + 1) + offset, thisNode);
                    offset++;
                }
            }
            else if(sideSelected == 1) // Rightside selected
            {
                int offset = 0;
                for(int k = closestDNode.getPathFromRoot().size() - 2; k >= 0; k--)
                {
                    String thisName = closestDNode.getPathFromRoot().get(k);
                    tertiaryRing.getNodes().add((posOnTRing + 1) + offset,
                                    QaRsapUtils.getNodeById(thisName, network.getNetworkStructure().getNodes().getNode()));
                    offset++;
                }
                tertiaryRing.getNodes().add((posOnTRing + 1) + offset, randNode);
                offset++;
                for(int k = 0; k < sideToInsert.getPathFromRoot().size() - 1; k++)
                {
                    String thisName = sideToInsert.getPathFromRoot().get(k);
                    tertiaryRing.getNodes().add((posOnTRing + 1) + offset,
                                    QaRsapUtils.getNodeById(thisName, network.getNetworkStructure().getNodes().getNode()));
                    offset++;
                }
            }

            succeeded = true;
            if(tertiaryRing.getSize() == 2)
            {
                tertiaryRing = reconnect2NodeTertiaryRingbyDijkstra(tertiaryRing, spurs);
                if(tertiaryRing == null)
                {
                    succeeded = false;
                }
            }
            else
            {
                // Complete the ring
                nodesToRemove = QaRsapUtils.nodesToRemove(
                                tertiaryRing, spurs, null,
                                new String[] {tertiaryRing.getSpecificNodeName(0),
                                        tertiaryRing.getSpecificNodeName(tertiaryRing.getSize() - 1)});
                // call dijsktra
                List<DijkstraNode> returnedNodes = dijkstra.runDijkstra(tertiaryRing.getSpecificNode(tertiaryRing.getSize() - 1),
                                tertiaryRing.getSpecificNode(0), nodesToRemove);

                if(returnedNodes != null && returnedNodes.size() == 1)
                {
                    DijkstraNode returnedNode = returnedNodes.get(0);
                    for(String nodeName : returnedNode.getPathFromRoot())
                    {
                        tertiaryRing.getNodes().add(
                                        QaRsapUtils.getNodeById(nodeName, network.getNetworkStructure().getNodes().getNode()));
                    }
                }
                else
                {
                    // cannot find path back. Failed to build tertiray Ring
                    succeeded = false;
                }
            }
        }
        else
        {
            succeeded = false;
        }
        return succeeded;
    }
    
    /**
     * This is a utility method which returns the position of a node on its ring.
     */
    private int findNodePosOnRing(Ring srcRing, Node currentNode)
    {
        int positionOfcurrentAdjNode = - 1;
        for(int i = 0; i < srcRing.getNodes().size(); i++)
        {
            Node node = srcRing.getNodes().get(i);
            if(node == currentNode)
            {
                positionOfcurrentAdjNode = i;
                break;
            }
        }
        return positionOfcurrentAdjNode;
    }
}
