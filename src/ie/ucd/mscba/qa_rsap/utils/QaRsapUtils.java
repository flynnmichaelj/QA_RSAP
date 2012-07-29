/**
 * Created on 7 Jul 2012
 *
 * @author Michael Flynn
 * @version 1.0
 *
 * File: QaRsapUtils.java
 * Package ie.ucd.mscba.qa_rsap.utils
 * Project QA_RSAP
 */
package ie.ucd.mscba.qa_rsap.utils;

import ie.ucd.mscba.qa_rsap.valueobjects.AdjNode;
import ie.ucd.mscba.qa_rsap.valueobjects.Ring;

import java.util.List;

import de.zib.sndlib.network.Node;

/**
 * 
 */
public class QaRsapUtils
{
    //Retrieves a Nose object based on name.
    public static Node getNodeById(String nodeName, List<Node> nodeList)
    {
        Node returnNode = null;
        for(Node node : nodeList)
        {
            if(nodeName.equalsIgnoreCase(node.getId()))
            {
                returnNode = node;  
                break;
            }
        }
        return returnNode;
    }
    
    public static Ring getRingByNode(Node node, List<Ring> rings)
    {
        Ring returnRing = null;
        for(Ring ring : rings)
        {
            if(ring.getNodes( ).contains(node))
            {
                returnRing = ring;  
                break;
            }
        }
        return returnRing;
    }
    
//    public static GraphNode getGraphNodeByName(String nodeName, List<GraphNode> graphNodes)
//    {
//        GraphNode graphNode = null;
//        for(GraphNode currrentGraphNode : graphNodes)
//        {
//            if(nodeName.equalsIgnoreCase(currrentGraphNode.getVal()))
//            {
//                graphNode = currrentGraphNode;  
//                break;
//            }
//        }
//        return graphNode;
//    }
    
    public static boolean isAdj(String nodeName, List<AdjNode> adjList)
    {
        boolean adj = false;
        for (int i=0; i<adjList.size( ); i++)
        {
            if(nodeName.equalsIgnoreCase(adjList.get(i).getNodeName()))
            {
                adj = true;
                break;
            }
        }  
        return adj;
    }
    
    public static double isAdjCost(String nodeName, List<AdjNode> adjList)
    {
        double adjCost = 0.0;
        for (int i=0; i<adjList.size( ); i++)
        {
            if(nodeName.equalsIgnoreCase(adjList.get(i).getNodeName()))
            {
                adjCost=adjList.get(i).getCost( );
                break;
            }
        }  
        return adjCost;
    }
    
    public static boolean isNodeOnRing(Node node, Ring ring)
    {
        boolean isOnRing = false;
        for(Node thisNode : ring.getNodes( ))
        {
            if(thisNode == node)
                isOnRing = true;         
        }
        return isOnRing;
    }

}
