/**
 * Created on 7 Jul 2012
 *
 * @author Michael Flynn
 * @version 1.0
 *
 * File: Spur.java
 * Package ie.ucd.mscba.qa_rsap.valueobjects
 * Project QA_RSAP
 */
package ie.ucd.mscba.qa_rsap.valueobjects;

import java.util.List;

import de.zib.sndlib.network.Link;
import de.zib.sndlib.network.Node;

/**
 * 
 */
public class Spur extends SolutionElement 
{
    private Node spurNode;
    private Node parentNode;
    
    public Spur clone()
    {
        Spur cloned = new Spur();
        cloned.spurNode = spurNode;
        cloned.parentNode = parentNode;
        return cloned;
    }
        
    /**
     * @return the spurNode
     */
    public Node getSpurNode( )
    {
        return spurNode;
    }
    /**
     * @param spurNode the spurNode to set
     */
    public void setSpurNode( Node spurNode )
    {
        this.spurNode = spurNode;
    }
    /**
     * @return the parentNode
     */
    public Node getParentNode( )
    {
        return parentNode;
    }
    /**
     * @param parentNode the parentNode to set
     */
    public void setParentNode( Node parentNode )
    {
        this.parentNode = parentNode;
    }
    

    public double getSpurCost(List<Link> links, double penalty, int capacityCost)
    {
        double spurCost = 0.0;
        Node trgNode = this.getSpurNode( );
        Node srcNode  = this.getParentNode( );
       
        for(int j=0; j<(links.size( )); j++)
        {
            Link link  = links.get( j );
            if((link.getSource( )).equalsIgnoreCase( srcNode.getId( )) &&
                            (link.getTarget( )).equalsIgnoreCase( trgNode.getId( )))
            {
                spurCost = spurCost + link.getAdditionalModules( ).getAddModule( ).get( capacityCost ).getCost( ).doubleValue( );        
            }
            else if((link.getTarget( )).equalsIgnoreCase( srcNode.getId( )) &&
                            (link.getSource( )).equalsIgnoreCase( trgNode.getId( )))
            {
                spurCost = spurCost + link.getAdditionalModules( ).getAddModule( ).get( capacityCost ).getCost( ).doubleValue( );   
            }
        }      
        setSolutionElementCost(spurCost*penalty);
        return spurCost*penalty;
    }
}
