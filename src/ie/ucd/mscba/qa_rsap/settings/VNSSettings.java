/**
 * Created on 4 Aug 2012
 *
 * @author Michael Flynn
 * @version 1.0
 *
 * File: VNSSettings.java
 * Package ie.ucd.mscba.qa_rsap.settings
 * Project QA_RSAP
 */
package ie.ucd.mscba.qa_rsap.settings;

/**
 * 
 */
public class VNSSettings
{
    private int initMaxLRSize = 0;
    private int maxLocalRingSize = 0;
    private int capacityModule = 0;
    private int spurPenalty = 0;
    
    /**
     * @return the initMaxLRSize
     */
    public int getInitMaxLRSize()
    {
        return initMaxLRSize;
    }
    /**
     * @param initMaxLRSize the initMaxLRSize to set
     */
    public void setInitMaxLRSize(int initMaxLRSize)
    {
        this.initMaxLRSize = initMaxLRSize;
    }
    /**
     * @return the maxLocalRingSize
     */
    public int getMaxLocalRingSize()
    {
        return maxLocalRingSize;
    }
    /**
     * @param maxLocalRingSize the maxLocalRingSize to set
     */
    public void setMaxLocalRingSize(int maxLocalRingSize)
    {
        this.maxLocalRingSize = maxLocalRingSize;
    }
    /**
     * @return the capacityModule
     */
    public int getCapacityModule()
    {
        return capacityModule;
    }
    /**
     * @param capacityModule the capacityModule to set
     */
    public void setCapacityModule(int capacityModule)
    {
        this.capacityModule = capacityModule;
    }
    /**
     * @return the spurPenalty
     */
    public int getSpurPenalty()
    {
        return spurPenalty;
    }
    /**
     * @param spurPenalty the spurPenalty to set
     */
    public void setSpurPenalty(int spurPenalty)
    {
        this.spurPenalty = spurPenalty;
    }
}
