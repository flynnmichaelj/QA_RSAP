/**
 * Created on 4 Aug 2012
 *
 * @author Michael Flynn
 * @version 1.0
 *
 * File: AnnealSettings.java
 * Package ie.ucd.mscba.qa_rsap.settings
 * Project QA_RSAP
 */
package ie.ucd.mscba.qa_rsap.settings;

/**
 * 
 */
public class AnnealSettings
{

    private int numSearches = 0;
    private int trotterSlices = 0;
    private int initQAfluct = 0;
    private double qaFluctSteps = 0;
    private double qaTemp = 0.0;
    private int mcsSteps = 0;
    private int initSATemp = 0;
    private int finalSATemp = 0;
    private double saDeltaTemp = 0.0;
    
    /**
     * @return the numSearches
     */
    public int getNumSearches()
    {
        return numSearches;
    }
    /**
     * @param numSearches the numSearches to set
     */
    public void setNumSearches(int numSearches)
    {
        this.numSearches = numSearches;
    }
    /**
     * @return the trotterSlices
     */
    public int getTrotterSlices()
    {
        return trotterSlices;
    }
    /**
     * @param trotterSlices the trotterSlices to set
     */
    public void setTrotterSlices(int trotterSlices)
    {
        this.trotterSlices = trotterSlices;
    }
    /**
     * @return the initQAfluct
     */
    public int getInitQAfluct()
    {
        return initQAfluct;
    }
    /**
     * @param initQAfluct the initQAfluct to set
     */
    public void setInitQAfluct(int initQAfluct)
    {
        this.initQAfluct = initQAfluct;
    }
    /**
     * @return the qaFluctSteps
     */
    public double getQaFluctSteps()
    {
        return qaFluctSteps;
    }
    /**
     * @param qaFluctSteps the qaFluctSteps to set
     */
    public void setQaFluctSteps(double qaFluctSteps)
    {
        this.qaFluctSteps = qaFluctSteps;
    }
    /**
     * @return the qaTemp
     */
    public double getQaTemp()
    {
        return qaTemp;
    }
    /**
     * @param qaTemp the qaTemp to set
     */
    public void setQaTemp(double qaTemp)
    {
        this.qaTemp = qaTemp;
    }
    /**
     * @return the mcsSteps
     */
    public int getMcsSteps()
    {
        return mcsSteps;
    }
    /**
     * @param mcsSteps the mcsSteps to set
     */
    public void setMcsSteps(int mcsSteps)
    {
        this.mcsSteps = mcsSteps;
    }
    /**
     * @return the initSATemp
     */
    public int getInitSATemp()
    {
        return initSATemp;
    }
    /**
     * @param initSATemp the initSATemp to set
     */
    public void setInitSATemp(int initSATemp)
    {
        this.initSATemp = initSATemp;
    }
    /**
     * @return the finalSATemp
     */
    public int getFinalSATemp()
    {
        return finalSATemp;
    }
    /**
     * @param finalSATemp the finalSATemp to set
     */
    public void setFinalSATemp(int finalSATemp)
    {
        this.finalSATemp = finalSATemp;
    }
    /**
     * @return the saDeltaTemp
     */
    public double getSaDeltaTemp()
    {
        return saDeltaTemp;
    }
    /**
     * @param saDeltaTemp the saDeltaTemp to set
     */
    public void setSaDeltaTemp(double saDeltaTemp)
    {
        this.saDeltaTemp = saDeltaTemp;
    }
}
