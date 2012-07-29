/**
 * Created on 7 Jul 2012
 *
 * @author Michael Flynn
 * @version 1.0
 *
 * File: Ring.java
 * Package ie.ucd.mscba.qa_rsap.valueobjects
 * Project QA_RSAP
 */
package ie.ucd.mscba.qa_rsap.valueobjects;

import java.util.ArrayList;
import java.util.List;

import de.zib.sndlib.network.Link;
import de.zib.sndlib.network.Node;

/**
 * 
 */
public class Ring extends SolutionElement
{
    private List<Node> nodes;

    public Ring clone()
    {
        Ring cloned = new Ring();
        for(Node node : getNodes())
        {
            cloned.getNodes().add(node);
        }   
        return cloned;
    }
    
    /**
     * @return the nodes
     */
    public List<Node> getNodes( )
    {
        if(nodes == null)
            nodes = new ArrayList<Node>();
        return nodes;
    }

    /**
     * @param nodes the nodes to set
     */
    public void setNodes( List<Node> nodes )
    {
        this.nodes = nodes;
    }
    
    public void addNode(Node nodetoAdd)
    {
        getNodes().add(nodetoAdd);
    }
    
    public void removeNode(Node nodetoRemove)
    {
        getNodes().remove(nodetoRemove);
    }
    
    public String getSpecificNodeName(int index)
    {
        return getNodes().get(index).getId();
    }
    
    public Node getSpecificNode(int index)
    {
        return getNodes().get(index);
    }
    
    public int getSize()
    {
        return getNodes().size();
    }
    
    public double getRingCost(List<Link> links, int capacityCost)
    {
        double ringCost = 0.0;
        for(int i=0; i<(this.getNodes( ).size( ))-1; i++)
        {
            Node trgNode = null;
            Node srcNode  = this.getNodes( ).get( i );
            if(i == (this.getNodes( ).size( ))-1)
                trgNode = this.getNodes( ).get(0);
            else
                trgNode = this.getNodes( ).get( i+1 );
           
            for(int j=0; j<(links.size( )); j++)
            {
                Link link  = links.get( j );
                if((link.getSource( )).equalsIgnoreCase( srcNode.getId( )) &&
                                (link.getTarget( )).equalsIgnoreCase( trgNode.getId( )))
                {
                    ringCost = ringCost + link.getAdditionalModules( ).getAddModule( ).get( capacityCost ).getCost( ).doubleValue( );    
                    break;
                }
                else if((link.getTarget( )).equalsIgnoreCase( srcNode.getId( )) &&
                                (link.getSource( )).equalsIgnoreCase( trgNode.getId( )))
                {
                    ringCost = ringCost + link.getAdditionalModules( ).getAddModule( ).get( capacityCost ).getCost( ).doubleValue( );   
                    break;
                }
            }          
        }
        return ringCost;
    }    
}
