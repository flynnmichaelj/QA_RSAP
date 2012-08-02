/**
 * Created on 1 Aug 2012
 *
 * @author Michael Flynn
 * @version 1.0
 *
 * File: BestConnectors.java
 * Package ie.ucd.mscba.qa_rsap.valueobjects
 * Project QA_RSAP
 */
package ie.ucd.mscba.qa_rsap.valueobjects;

import de.zib.sndlib.network.Node;

/**
 * 
 */
public class BestConnectors
{
    Node bestLeftConnector;
    Node bestRightConnector;
    /**
     * @return the bestLeftConnector
     */
    public Node getBestLeftConnector()
    {
        return bestLeftConnector;
    }
    /**
     * @param bestLeftConnector the bestLeftConnector to set
     */
    public void setBestLeftConnector(Node bestLeftConnector)
    {
        this.bestLeftConnector = bestLeftConnector;
    }
    /**
     * @return the bestRightConnector
     */
    public Node getBestRightConnector()
    {
        return bestRightConnector;
    }
    /**
     * @param bestRightConnector the bestRightConnector to set
     */
    public void setBestRightConnector(Node bestRightConnector)
    {
        this.bestRightConnector = bestRightConnector;
    }
    
    
}
