/**
 * Created on 7 Jul 2012
 *
 * @author Michael Flynn
 * @version 1.0
 *
 * File: AdjNode.java
 * Package ie.ucd.mscba.qa_rsap.valueobjects
 * Project QA_RSAP
 */
package ie.ucd.mscba.qa_rsap.valueobjects;

/**
 * 
 */
public class AdjNode implements Comparable<AdjNode> 
{
    String nodeName = null;
    double cost = 0.0;
    /**
     * @return the nodeName
     */
    public String getNodeName( )
    {
        return nodeName;
    }
    /**
     * @param nodeName the nodeName to set
     */
    public void setNodeName( String nodeName )
    {
        this.nodeName = nodeName;
    }
    /**
     * @return the cost
     */
    public double getCost( )
    {
        return cost;
    }
    /**
     * @param cost the cost to set
     */
    public void setCost( double cost )
    {
        this.cost = cost;
    }
    
    public int compareTo(AdjNode o) 
    {
        int returnVal = 0; 
        if (this.cost < o.cost) 
        {
            returnVal = -1;
        } 
        else if (this.cost > o.cost) 
        {
            returnVal = 1;
        } 
        return returnVal;
      }
    
}
