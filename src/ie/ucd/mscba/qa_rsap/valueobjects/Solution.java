/**
 * Created on 7 Jul 2012
 *
 * @author Michael Flynn
 * @version 1.0
 *
 * File: Solution.java
 * Package ie.ucd.mscba.qa_rsap.valueobjects
 * Project QA_RSAP
 */
package ie.ucd.mscba.qa_rsap.valueobjects;

import ie.ucd.mscba.qa_rsap.Constants;

import java.util.ArrayList;
import java.util.List;

import de.zib.sndlib.network.Link;

/**
 * 
 */
public class Solution 
{

       private List<Ring> localrings;
       private Ring tertiaryRing;
       private List<Spur> spurs;
       
       private double totalCost;

       public Solution clone()
       {
           Solution cloned = new Solution();
           for(Ring ring : getLocalrings())
           {
               cloned.getLocalrings().add(ring.clone());
           }
           for(Spur spur : getSpurs())
           {
               cloned.getSpurs().add(spur.clone());
           }
           if(getTertiaryRing( ) != null)
           {
               cloned.setTertiaryRing(getTertiaryRing( ).clone( ));
           }
           cloned.totalCost = totalCost;
           
           return cloned;
       }
    /**
     * @return the localrings
     */
    public List<Ring> getLocalrings( )
    {
        if(localrings == null)
            localrings = new ArrayList<Ring>();
        return localrings;
    }

    /**
     * @param localrings the localrings to set
     */
    public void setLocalrings( List<Ring> localrings )
    {
        this.localrings = localrings;
    }

    /**
     * @return the tertiaryRing
     */
    public Ring getTertiaryRing( )
    {
        return tertiaryRing;
    }

    /**
     * @param tertiaryRing the tertiaryRing to set
     */
    public void setTertiaryRing( Ring tertiaryRing )
    {
        this.tertiaryRing = tertiaryRing;
    }

    /**
     * @return the spurs
     */
    public List<Spur> getSpurs( )
    {
        if(spurs == null)
            spurs = new ArrayList<Spur>();
        return spurs;
    }

    /**
     * @param spurs the spurs to set
     */
    public void setSpurs(List<Spur> spurs )
    {
        this.spurs = spurs;
    }

    /**
     * @return the totalCost
     */
    public double getTotalCost( )
    {
        return totalCost;
    }

    /**
     * @param totalCost the totalCost to set
     */
    public void setTotalCost( double totalCost )
    {
        this.totalCost = totalCost;
    } 
    
    public void calculateTotalCost(List<Link> links)
    {
        double totalCost = 0.0;
        
        //Calculate local Rings Cost
        for(Ring ring : getLocalrings( ))
        {
            totalCost = totalCost + ring.getRingCost(links, Constants.localRingCapcacityModule);
        }
        
        //Calculate tertiary ring cost
        if(getTertiaryRing() != null)
        {
            totalCost = totalCost + getTertiaryRing( ).getRingCost( links, Constants.tertiaryRingCapcacityModule);
        }
        
        //Calculate spurs cost
        for(Spur spur : getSpurs( ))
        {
            totalCost = totalCost + spur.getSpurCost( links, Constants.spurPenaltyCost, Constants.localRingCapcacityModule);
        }
        
        setTotalCost(totalCost);
    }
    
    public void printLocalRing ()
    {
        for(Ring ring : this.getLocalrings( ))
        {
            //Print Local Ring
            System.out.println("******** LOCAL RING *********");
            for(int i=0; i<ring.getSize( ); i++ )
            {
                System.out.print( ring.getSpecificNodeName(i) + " --> " );
            }
            System.out.println("");
            System.out.println("*****************************");
        }
        
    }
    
    public void printSpurs()
    {

        for(Spur spur : this.getSpurs( ))
        {
            System.out.println("******** SPUR *********");
            System.out.print(spur.getParentNode( ).getId( ) + " --> " + spur.getSpurNode( ).getId( ));
            System.out.println("");
            System.out.println("*********************");
        }
    }
    
    public void printTertiaryRing()
    {
        //Print Tertiary Ring
        System.out.println("******** TERTIARY RING *********");
        if(getTertiaryRing( ) != null)
        {
            for(int i=0; i<this.getTertiaryRing( ).getSize( ); i++ )
            {
                System.out.print(this.getTertiaryRing( ).getSpecificNodeName(i) + " --> " );
            }
        }
        System.out.println("");
        System.out.println("*****************************");
    }
       
}
