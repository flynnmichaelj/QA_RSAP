/**
 * Created on 4 Aug 2012
 *
 * @author Michael Flynn
 * @version 1.0
 *
 * File: AppSettings.java
 * Package ie.ucd.mscba.qa_rsap.settings
 * Project QA_RSAP
 */
package ie.ucd.mscba.qa_rsap.settings;

/**
 * 
 */
public class AppSettings
{
    private AnnealSettings   annealSettings     = null;
    private VNSSettings      vnsSettings        = null;

    /**
     * @return the annealSettings
     */
    public AnnealSettings getAnnealSettings()
    {
        return annealSettings;
    }
    /**
     * @param annealSettings the annealSettings to set
     */
    public void setAnnealSettings(AnnealSettings annealSettings)
    {
        this.annealSettings = annealSettings;
    }
    /**
     * @return the vnsSettings
     */
    public VNSSettings getVnsSettings()
    {
        return vnsSettings;
    }
    /**
     * @param vnsSettings the vnsSettings to set
     */
    public void setVnsSettings(VNSSettings vnsSettings)
    {
        this.vnsSettings = vnsSettings;
    }
  
}
