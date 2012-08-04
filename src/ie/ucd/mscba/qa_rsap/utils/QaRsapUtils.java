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
import ie.ucd.mscba.qa_rsap.valueobjects.Spur;

import java.util.ArrayList;
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
            if(thisNode.getId( ).equalsIgnoreCase(node.getId( )))
            {
                isOnRing = true;    
                break;
            }
        }
        return isOnRing;
    }
    
    public static Spur isNodeASpur(String nodeName, List<Spur> spurs)
    {
        Spur spurFound = null;
        for(Spur thisSpur : spurs)
        {
            if(thisSpur.getSpurNode( ).getId( ).equalsIgnoreCase(nodeName))
            {
                spurFound = thisSpur;    
                break;
            }
        }
        return spurFound;
    }
    
    public static List<String> nodesToRemove(Ring tertiaryRing, List<Spur> spurs, List<Ring> localRings, String[] keepNodes)
    {
        List<String> nodesToRemove = new ArrayList<String>();
        
        //1) Remove nodes already on Tertiary ring
        if(tertiaryRing != null)
        {            
            for(int i=0; i<tertiaryRing.getSize( ); i++)
            {
                Node thisNode = tertiaryRing.getNodes().get(i);
                if(safeToRemove(keepNodes, thisNode.getId( )))
                {
                    nodesToRemove.add(thisNode.getId() );
                }
            }
        }
        //2) remove nodes that are spurs
        if(spurs != null)
        {
            for(int i=0; i<spurs.size(); i++)
            {
                nodesToRemove.add(spurs.get( i ).getSpurNode( ).getId( ));
            }
        }
        
        //2) remove nodes on localRings
        if(localRings!= null)
        {
            for(int i=0; i<localRings.size( ); i++)
            {
                Ring currentLocalring = localRings.get(i);
                for(int j=0; j<currentLocalring.getNodes( ).size(); j++)
                {
                    Node thisNode = currentLocalring.getNodes().get(j);
                    if(safeToRemove(keepNodes, thisNode.getId( )))
                    {
                        nodesToRemove.add(thisNode.getId( ));
                    }
                }
            }
        }
        
        return nodesToRemove;
    }
    
    //Ensures that the keepNodes are not removed for Dijkstra
    private static boolean safeToRemove(String[] keepNodes, String currentNode)
    {
        boolean safeToRemove = true;
        for(int j=0; j<keepNodes.length; j++)
        {
            String thisKeepNode = keepNodes[j];
            if(currentNode.equalsIgnoreCase(thisKeepNode))
            {
                safeToRemove = false;
                break;
            }
        }
        return safeToRemove;
    }
    
    public static int countRealNode(Ring tertiaryRing)
    {
        int numRealNode = 0;
        for(int i=0; i<tertiaryRing.getSize( ); i++)
        {
            if(!"TempNode".equalsIgnoreCase( tertiaryRing.getSpecificNode( i ).getId( )))
            {
                numRealNode++;
            }
        }
        return numRealNode;
    }
    
    public static double normalizeValue(double input, double minValue, double maxValue)
    {
        double temp2 = (input-minValue)/(maxValue-minValue);
        double temp3 = temp2*(4-1)+1;
        return temp3;    
    }
    
}
