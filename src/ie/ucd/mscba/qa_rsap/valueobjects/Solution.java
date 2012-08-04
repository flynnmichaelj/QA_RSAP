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
import ie.ucd.mscba.qa_rsap.utils.QaRsapUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.zib.sndlib.network.Link;
import de.zib.sndlib.network.Node;

/**
 * 
 */
public class Solution 
{

       private List<Ring> localrings;
       private Ring tertiaryRing;
       private List<Spur> spurs;
       
       private double totalCost;
       private double localRingAndSpursCost;

       public Solution clone()
       {
           Solution cloned = new Solution();
           List<Ring> localRings = getLocalrings();
           List<Spur> spurs = getSpurs();
           Ring tertiaryRing = getTertiaryRing( );
           
           for(int i=0; i<localRings.size(); i++)
           {
               cloned.getLocalrings().add(localRings.get(i).clone());
           }
           for(int i=0; i<spurs.size(); i++)
           {
               cloned.getSpurs().add(spurs.get(i).clone());
           }
           if(tertiaryRing != null)
           {
               cloned.setTertiaryRing(tertiaryRing.clone( ));
           }
           cloned.totalCost = totalCost;
           
           return cloned;
       }
    /**
     * @return the localRingAndSpursCost
     */
    public double getLocalRingAndSpursCost()
    {
        return localRingAndSpursCost;
    }
    /**
     * @param localRingAndSpursCost the localRingAndSpursCost to set
     */
    public void setLocalRingAndSpursCost(double localRingAndSpursCost)
    {
        this.localRingAndSpursCost = localRingAndSpursCost;
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
        double ringsAndSpursCost = 0.0;
        
        //Calculate local Rings Cost
        for(Ring ring : getLocalrings( ))
        {
            totalCost = totalCost + ring.getRingCost(links, Constants.localRingCapcacityModule);
            ringsAndSpursCost = ringsAndSpursCost + ring.getRingCost(links, Constants.localRingCapcacityModule);
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
            ringsAndSpursCost = ringsAndSpursCost + spur.getSpurCost( links, Constants.spurPenaltyCost, Constants.localRingCapcacityModule);
        }
        
        setTotalCost(totalCost);
        setLocalRingAndSpursCost( ringsAndSpursCost );
    }
    
    public void printLocalRing ()
    {
        //Print Local Ring
        System.out.println("******** LOCAL RING *********");
        double totalLocalRingsCost = 0.0;
        for(Ring ring : this.getLocalrings( ))
        {
            for(int i=0; i<ring.getSize( ); i++ )
            {
                System.out.print( ring.getSpecificNodeName(i) + " --> " ); 
            }  
            System.out.println( );
            totalLocalRingsCost = totalLocalRingsCost + ring.getSolutionElementCost( );
        }
        System.out.println("*****Total Local Rings Cost : " + totalLocalRingsCost);
        System.out.println("*****************************");
    }
    
    public void printSpurs()
    {
        System.out.println("******** SPUR *********");
        double totalSpursCost = 0.0;
        for(Spur spur : this.getSpurs( ))
        {  
            System.out.println(spur.getParentNode( ).getId( ) + " --> " + spur.getSpurNode( ).getId( ));
            System.out.println( );
            totalSpursCost = totalSpursCost + spur.getSolutionElementCost( );
        }
        System.out.println("*****Total Spurs Cost : " + totalSpursCost);
        System.out.println("*********************");
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
            System.out.println( );
            System.out.println("*****Tertiary Ring Cost : " + this.getTertiaryRing( ).getSolutionElementCost( ));
        }
        System.out.println("*****************************");
    }
    
    //===========================================
    //Validate Solution
    //============================================
    public boolean validate(NodeAdjacencies nodeAdjs, int numNodesInPlay)
    {
        boolean valid = true;
        //Validate local rings
        for(Ring ring : getLocalrings( ))
        {         
            if(!validateRings(ring, nodeAdjs, "local" ))
            {
                System.out.println("ERROR: A local ring is not valid");
                printBadSolution();
                valid = false;
                break;
            }
                
        }
        
        //Verify that there is not tertiary ring if there is only one local ring
        if(getLocalrings( ).size() == 1) 
        {
            Ring tertiaryRing = getTertiaryRing();
            if(tertiaryRing != null)
            {
                if(tertiaryRing.getNodes().size() > 0)
                {
                    System.out.println("ERROR: A tertiary Ring should not exist if only one local ring exists");
                    printBadSolution();
                    valid = false;
                }
            }
        }
        //Validate tertiary Ring
        if(getTertiaryRing( ) != null)
        {
            if(!validateRings(getTertiaryRing( ), nodeAdjs, "tertiary" ))
            {
                System.out.println("ERROR: Tertiary ring is not valid");
                printBadSolution();
                valid=false;
                
            }
        }
        
        //Ensure a node is not on more than one local ring & spurs
        List<Node> nodesOnRing = new ArrayList<Node>();
        int countOfNodes = 0;
        for(Ring ring : getLocalrings( ))
        {   
            for(int i=0; i<ring.getNodes( ).size( )-1; i++)
            { 
                countOfNodes ++;
                Node node =  ring.getNodes( ).get( i );
                if(!nodesOnRing.contains(node))
                {
                    nodesOnRing.add(node );
                }
                else
                {
                    System.out.println("ERROR: Duplicate node found on more than one local rings:" + node.getId( ));
                    printBadSolution();
                    valid=false; 
                }
            }             
        }
        for(int i=0; i<getSpurs( ).size( ); i++)
        {
            countOfNodes++;
            Spur thisSpur = getSpurs( ).get(i);
            if(!nodesOnRing.contains(thisSpur.getSpurNode( )))
            {
                nodesOnRing.add(thisSpur.getSpurNode( ) );
            }
            else
            {
                System.out.println("ERROR: Duplicate node found ona ring and a spur" + thisSpur.getSpurNode( ).getId( ));
                printBadSolution();
                valid=false; 
            }
        }
        //Validate the correct number of nodes are in play
        if(countOfNodes != numNodesInPlay)
        {
            System.out.println("ERROR: Incorrect number of nodes in play. Found " + countOfNodes +". Should be " + numNodesInPlay);
            printBadSolution();
            valid=false; 
        }
        return valid;
    }
    
    private boolean validateRings(Ring ring, NodeAdjacencies nodeAdjs, String ringType)
    {
        boolean valid = true;
        
        //Check for a minimum length of 4 (3 node + duplicate)
        if(ring.getSize( ) < 4)
        {
            System.out.println("ERROR: Invalid Ring lenght on " +ringType+ ". Lenght is:" + ring.getSize( ));
            printBadSolution();
            valid=false;
        }
        
        //Check is end node and start node the same
        if(ring.getSpecificNode( 0 ) != ring.getSpecificNode(ring.getSize( )-1 ))
        {
            System.out.println("ERROR: End node and start node is not the same");
            printBadSolution();
            valid=false;
        }
        
        //Ensure node is not repeated on a ring & no TempNodes exist
        List<Node> nodesOnRing = new ArrayList<Node>();
        for(int i=0; i<ring.getNodes( ).size( )-1; i++)
        {
            Node node = ring.getNodes( ).get(i);
            if("TempNode".equalsIgnoreCase(node.getId( )))
            {
                System.out.println("ERROR: TempNode Still exists in "+ringType+ " ring");
                printBadSolution();
                valid=false;   
            }
            else
            {
                if(!nodesOnRing.contains(node))
                {
                    nodesOnRing.add(node);
                }
                else
                {
                    System.out.println("ERROR: Duplicate node found in " +ringType+ " ring:" + node.getId( ));
                    printBadSolution();
                    valid=false; 
                }
            }
        }
        
        //Ensure all node on the ring are adjacent
        for(int i=0; i<ring.getNodes( ).size( )-1; i++)
        {
           Node leftNode = ring.getNodes( ).get( i );
           Node rightNode = ring.getNodes( ).get( i+1 );
           List<AdjNode> adjList = nodeAdjs.getAdjList( leftNode.getId( ) );
           if(!QaRsapUtils.isAdj( rightNode.getId( ), adjList ))
           {
               System.out.println("ERROR: Nodes not adjacent:" + leftNode.getId( ) + " & " + rightNode.getId( ));
               printBadSolution();
               valid=false;  
           }
        }
        
        return valid;
    }
    
    private void printBadSolution()
    {
        System.out.println( "============= Validate Solution =============" );
        printLocalRing( );
        printSpurs( );
        printTertiaryRing( );
        System.out.println( "============= End Validate Solution =============" );
        
        System.exit( 1 );
    }
       
}
