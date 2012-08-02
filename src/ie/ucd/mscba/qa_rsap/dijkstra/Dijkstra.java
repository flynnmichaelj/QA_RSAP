/**
 * Created on 22 Jul 2012
 *
 * @author Michael Flynn
 * @version 1.0
 *
 * File: dijkstra.java
 * Package ie.ucd.mscba.qa_rsap.dijsktra
 * Project QA_RSAP
 */
package ie.ucd.mscba.qa_rsap.dijkstra;

import ie.ucd.mscba.qa_rsap.valueobjects.AdjNode;
import ie.ucd.mscba.qa_rsap.valueobjects.NodeAdjacencies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import de.zib.sndlib.network.Node;

/**
 * 
 */
public class Dijkstra
{

    HashMap<String, DijkstraNode> dijkstraNodeMap = null;
    List<DijkstraNode> responseList = null;
    
    public List<DijkstraNode> runDijkstra(Node root, Node targetNode,  List<Node> allNodes, NodeAdjacencies nodeadj)
    {
        dijkstraNodeMap = new HashMap<String, DijkstraNode>();
        responseList = new ArrayList<DijkstraNode>();

        //init
        for (int i=0; i<allNodes.size( ); i++)
        {
            Node thisNode = allNodes.get( i );
            DijkstraNode dn = new DijkstraNode();
            dn.setNodeName(thisNode.getId( ));
            dn.setAdjList( nodeadj.getAdjList( thisNode.getId( )));
            dn.setVisited( false );
            if((thisNode.getId( )).equals(root.getId()))
            {
                dn.setDistanceToRoot(0);
            }
            else
            {            
                dn.setDistanceToRoot(Double.POSITIVE_INFINITY); 
            }
            dijkstraNodeMap.put( dn.getNodeName( ), dn );
        }
        
        while(!dijkstraNodeMap.isEmpty( ))
        {
            DijkstraNode dn = findMin( dijkstraNodeMap );
            if(dn != null)
            {
                responseList.add(dn);
                dn.setVisited( true );
                List<AdjNode> adjList = dn.getAdjList( );
                for (int j=0; j<adjList.size( ); j++)
                {
                    AdjNode thisAdjNode = adjList.get(j);
                    DijkstraNode thisDn = dijkstraNodeMap.get(thisAdjNode.getNodeName( ));
                    if(thisDn == null)
                    {
                        thisDn = findDNodeinRespList(responseList, thisAdjNode.getNodeName( ));
                    }
                    if(thisDn.getDistanceToRoot( ) > dn.getDistanceToRoot( ) + thisAdjNode.getCost( ))
                    {
                        thisDn.setDistanceToRoot( dn.getDistanceToRoot( ) + thisAdjNode.getCost( ) );
                        List<String> newPathList = new ArrayList<String>();
                        List<String> pathFromRoot = dn.getPathFromRoot( );
                        for(String thisString : pathFromRoot)
                        {
                            newPathList.add(thisString);
                        }
                        newPathList.add(thisDn.getNodeName( ));
                        thisDn.setPathFromRoot(newPathList);
                    }
                }
            }
            else
            {
                break;
            }
        }
        
        //Collections.sort( responseList );
        List<DijkstraNode> returnedList = new ArrayList<DijkstraNode>();
        if(targetNode != null)
        {
            DijkstraNode foundNode = findDNodeinRespList( responseList, targetNode.getId( ) );
            if(foundNode != null)
                returnedList.add(foundNode);
            else
                return null;
        }
        else
        {
            returnedList = responseList;
            Collections.sort(returnedList);
        }
        return returnedList;
    }
    
    private DijkstraNode findDNodeinRespList(List<DijkstraNode> respList, String nodeToFind)
    {
        DijkstraNode returnNode = null;
        for(DijkstraNode dn : respList)
        {
            if(nodeToFind.equalsIgnoreCase( dn.getNodeName( ) ))
            {
                returnNode = dn;
            }
        }
        return returnNode;            
    }
    
    public DijkstraNode findMin(HashMap<String, DijkstraNode> dijkstraNodeMap)
    {
        DijkstraNode returned = null;
        double minVal = Double.POSITIVE_INFINITY;
        Set<String> mapSet = dijkstraNodeMap.keySet( );
        Iterator<String> iter =  mapSet.iterator( );
        while(iter.hasNext( ) )
        {
            DijkstraNode thisNode = dijkstraNodeMap.get(iter.next( ));
            if(minVal > thisNode.getDistanceToRoot( ))
            {
                minVal = thisNode.getDistanceToRoot( );
                returned = thisNode;
            }
        }
        if(returned != null)
            dijkstraNodeMap.remove( returned.getNodeName( ) );
        return returned;
    }
    
//    public DijkstraNode findClosestNodeFromDijkstra(TreeMap<String, DijkstraNode> returnedNodes, List<Node> allNodes, boolean useTertiaryRing, Ring tertiaryRing)
//    {
//        //Find closest node
//        Iterator<String> iter = returnedNodes.keySet( ).iterator( );
//        double closest = Double.POSITIVE_INFINITY;
//        DijkstraNode closestNode = null;
//        while(iter.hasNext( ))
//        {
//            String thisKey = iter.next( );
//            DijkstraNode dn = returnedNodes.get(thisKey);
//            Node node = QaRsapUtils.getNodeById( dn.getNodeName( ), allNodes);
//            if(useTertiaryRing && QaRsapUtils.isNodeOnRing( node, tertiaryRing ))
//            {
//                if(dn.getDistanceToRoot( ) < closest)
//                {
//                    closest = dn.getDistanceToRoot( );
//                    closestNode = dn;
//                } 
//            } 
//            else
//            {
//                if(dn.getDistanceToRoot( ) < closest)
//                {
//                    closest = dn.getDistanceToRoot( );
//                    closestNode = dn;
//                }          
//            }
//        }
//        return closestNode;     
//    }
}
