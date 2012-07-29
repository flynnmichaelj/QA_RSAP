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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import de.zib.sndlib.network.Node;

/**
 * 
 */
public class Dijkstra
{

    HashMap<String, DijkstraNode> dijkstraNodeMap = null;
    TreeMap<String, DijkstraNode> responseMap = null;
    
    public TreeMap<String, DijkstraNode> runDijkstra(Node root, Node targetNode,  List<Node> allNodes, NodeAdjacencies nodeadj)
    {
        dijkstraNodeMap = new HashMap<String, DijkstraNode>();
        responseMap = new TreeMap<String, DijkstraNode>();

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
            responseMap.put( dn.getNodeName( ), dn );
            dn.setVisited( true );
            List<AdjNode> adjList = dn.getAdjList( );
            for (int j=0; j<adjList.size( ); j++)
            {
                AdjNode thisAdjNode = adjList.get(j);
                DijkstraNode thisDn = dijkstraNodeMap.get(thisAdjNode.getNodeName( ));
                if(thisDn == null)
                {
                    thisDn = responseMap.get(thisAdjNode.getNodeName( ));
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
        
        TreeMap<String, DijkstraNode> returnedMap = new TreeMap<String, DijkstraNode>();
        if(targetNode != null)
        {
            DijkstraNode foundNode = null;
            Set<String> mapSet = responseMap.keySet( );
            Iterator<String> iter =  mapSet.iterator( );
            while(iter.hasNext( ) )
            {
                DijkstraNode thisNode = responseMap.get(iter.next( ));
                if(thisNode.getNodeName( ).equalsIgnoreCase( targetNode.getId( )))
                {
                    foundNode = thisNode;
                    break;
                }
            }
            returnedMap.put( foundNode.getNodeName( ), foundNode );
        }
        else
        {
            returnedMap = responseMap;
        }
        return returnedMap;
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
        dijkstraNodeMap.remove( returned.getNodeName( ) );
        return returned;
    }
}
