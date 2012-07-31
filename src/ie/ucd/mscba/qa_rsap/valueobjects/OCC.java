/**
 * Created on 30 Jul 2012
 *
 * @author Michael Flynn
 * @version 1.0
 *
 * File: OCC.java
 * Package ie.ucd.mscba.qa_rsap.valueobjects
 * Project QA_RSAP
 */
package ie.ucd.mscba.qa_rsap.valueobjects;

import ie.ucd.mscba.qa_rsap.Constants;

import java.util.ArrayList;
import java.util.List;

import de.zib.sndlib.network.Node;

/**
 * 
 */
public class OCC
{
    List<int[][]> trotterSlices = null;
    int[] diffOcc = null;

    /**
     * @return the diffOcc
     */
    public int[] getDiffOcc()
    {
        return diffOcc;
    }

    /**
     * @param diffOcc the diffOcc to set
     */
    public void setDiffOcc(int[] diffOcc)
    {
        this.diffOcc = diffOcc;
    }

    public void initDiffOcc(int numTrotterSlices)
    {
        diffOcc = new int[numTrotterSlices-1];
    }
    
    /**
     * @return the trotterSlices
     */
    public List<int[][]> getTrotterSlices()
    {
        if(trotterSlices == null)
            trotterSlices = new ArrayList<int[][]>( );
        return trotterSlices;
    }

    /**
     * @param trotterSlices the trotterSlices to set
     */
    public void setTrotterSlices(List<int[][]> trotterSlices)
    {
        this.trotterSlices = trotterSlices;
    }
    
    public void updateOCC(Solution currentSol, int numNodes, int k)
    {
        //Re-initilize this slice to 0;
        int[][] thisSlice = getTrotterSlices().get( k );
        for( int index1 = 0 ; index1 < numNodes; index1++)
        {
            for( int index2 = 0 ; index2 < numNodes; index2++)
            {
                thisSlice[index1][index2] = 0;
            }
        }
        
        List<Ring> localRings = currentSol.getLocalrings( );
        List<Spur> spurs = currentSol.getSpurs();
        Ring tertiaryRing = currentSol.getTertiaryRing( );
        
        //Handle local ring
        for(int i = 0; i<localRings.size(); i++)
        {
            Ring currentRing =  localRings.get(i);
            List<Node> ringNodes = currentRing.getNodes( );
            for(int j = 0; j<ringNodes.size()-1; j++)
            {
                int index1 = ringNodes.get(j).getNodeNumber( );
                int index2 = ringNodes.get(j+1).getNodeNumber( );
                thisSlice[index1][index2] = thisSlice[index1][index2] +1;
            }     
        }
        //Handle spurs
        for(int i = 0; i<spurs.size( ); i++)
        {
            Spur thisSpur = spurs.get( i );
            int index1 = thisSpur.getParentNode( ).getNodeNumber( );
            int index2 = thisSpur.getSpurNode( ).getNodeNumber( );
            thisSlice[index1][index2] = thisSlice[index1][index2] +1;
        }
        //Handle tertiary Ring
        if(tertiaryRing != null)
        {
            List<Node> ringNodes = tertiaryRing.getNodes( );
            for(int j = 0; j<ringNodes.size()-1; j++)
            {
                int index1 = ringNodes.get(j).getNodeNumber( );
                int index2 = ringNodes.get(j+1).getNodeNumber( );
                thisSlice[index1][index2] = thisSlice[index1][index2] +1;
            }    
        }
    }
    
    public void getOCC(Solution currentSol, int numNodes)
    {
        for(int i =0; i< Constants.TROTTER_NUMBER; i++)
        {
            int[][] slice = new int[numNodes][numNodes];
            getTrotterSlices( ).add(slice);
        }
        initDiffOcc(Constants.TROTTER_NUMBER);
        
        List<Ring> localRings = currentSol.getLocalrings( );
        List<Spur> spurs = currentSol.getSpurs();
        Ring tertiaryRing = currentSol.getTertiaryRing( );
        
        for( int r = 0 ; r<Constants.TROTTER_NUMBER; r++)
        {
            int[][] thisSlice = getTrotterSlices().get( r );
            //Handle local ring
            for(int i = 0; i<localRings.size(); i++)
            {
                Ring currentRing =  localRings.get(i);
                List<Node> ringNodes = currentRing.getNodes( );
                for(int j = 0; j<ringNodes.size()-1; j++)
                {
                    int index1 = ringNodes.get(j).getNodeNumber( );
                    int index2 = ringNodes.get(j+1).getNodeNumber( );
                    thisSlice[index1][index2] = thisSlice[index1][index2] +1;
                }     
            }
            //Handle spurs
            for(int i = 0; i<spurs.size( ); i++)
            {
                Spur thisSpur = spurs.get( i );
                int index1 = thisSpur.getParentNode( ).getNodeNumber( );
                int index2 = thisSpur.getSpurNode( ).getNodeNumber( );
                thisSlice[index1][index2] = thisSlice[index1][index2] +1;
            }
            //Handle tertiary Ring
            if(tertiaryRing!= null)
            {
                List<Node> ringNodes = tertiaryRing.getNodes( );
                for(int j = 0; j<ringNodes.size()-1; j++)
                {
                    int index1 = ringNodes.get(j).getNodeNumber( );
                    int index2 = ringNodes.get(j+1).getNodeNumber( );
                    thisSlice[index1][index2] = thisSlice[index1][index2] +1;
                }
            }
        }
        for( int r = 0 ; r<Constants.TROTTER_NUMBER-1; r++)
        {
            int numDiffCounter = 0;
            int[][] trotterSliceR = getTrotterSlices( ).get( r );
            int[][] trotterSliceRNext = getTrotterSlices( ).get( r+1 );
            for( int index1 = 0 ; index1 < numNodes; index1++)
            {
                for( int index2 = 0 ; index2 < numNodes; index2++)
                {
                    if((trotterSliceR[index1][index2])!=(trotterSliceRNext[index1][index2]))
                        numDiffCounter++;
                }
            }      
            getDiffOcc()[r] = (int)numDiffCounter/2;  //TODO Check why divide by 2
        }
    }
    
    public OCCChangeHolder occChange(int k, Solution beforeVNS, Solution afterVNS)
    {
        List<Ring> localRingsBefore = beforeVNS.getLocalrings( );
        List<Spur> spursBefore = beforeVNS.getSpurs();
        Ring tertiaryRingBefore = beforeVNS.getTertiaryRing( );
        
        List<Ring> localRingsAfter = afterVNS.getLocalrings( );
        List<Spur> spursAfter = afterVNS.getSpurs();
        Ring tertiaryRingAfter = afterVNS.getTertiaryRing( );
        
        int changem = 0;
        int changep = 0;
        
        //Change of number of different occ between K and K-1
        if (k!=0) 
        {
            int[][] thisSlice = getTrotterSlices().get(k-1);
            //Handle local ring
            for(int i = 0; i<localRingsBefore.size(); i++)
            {
                Ring currentRing =  localRingsBefore.get(i);
                List<Node> ringNodes = currentRing.getNodes( );
                for(int j = 0; j<ringNodes.size()-1; j++)
                {
                    int index1 = ringNodes.get(j).getNodeNumber( );
                    int index2 = ringNodes.get(j+1).getNodeNumber( );
                    if(thisSlice[index1][index2] == 0) 
                    {
                        changem = changem - 1;
                    }
                    else
                    {
                        changem = changem + thisSlice[index1] [index2]; 
                    }
                }     
            }
            //Handle spurs
            for(int i = 0; i<spursBefore.size( ); i++)
            {
                Spur thisSpur = spursBefore.get( i );
                int index1 = thisSpur.getParentNode( ).getNodeNumber( );
                int index2 = thisSpur.getSpurNode( ).getNodeNumber( );
                if(thisSlice[index1][index2] == 0) 
                {
                    changem = changem - 1;
                }
                else
                {
                    changem = changem + thisSlice[index1] [index2]; 
                }
                
            }
            //Handle tertiary Ring
            if(tertiaryRingBefore != null)
            {
                List<Node> ringNodes = tertiaryRingBefore.getNodes( );
                for(int j = 0; j<ringNodes.size()-1; j++)
                {
                    int index1 = ringNodes.get(j).getNodeNumber( );
                    int index2 = ringNodes.get(j+1).getNodeNumber( );
                    if(thisSlice[index1][index2] == 0) 
                    {
                        changem = changem - 1;
                    }
                    else
                    {
                        changem = changem + thisSlice[index1] [index2]; 
                    }
                }  
            }
            
            //Stage 2
            for(int i = 0; i<localRingsAfter.size(); i++)
            {
                Ring currentRing =  localRingsAfter.get(i);
                List<Node> ringNodes = currentRing.getNodes( );
                for(int j = 0; j<ringNodes.size()-1; j++)
                {
                    int index1 = ringNodes.get(j).getNodeNumber( );
                    int index2 = ringNodes.get(j+1).getNodeNumber( );
                    if(thisSlice[index1][index2] > 0) 
                    {
                        changem = changem - thisSlice[index1] [index2]; 
                    }
                    else
                    {
                        changem = changem + 1;
                    }
                }     
            }
            //Handle spurs
            for(int i = 0; i<spursAfter.size( ); i++)
            {
                Spur thisSpur = spursAfter.get( i );
                int index1 = thisSpur.getParentNode( ).getNodeNumber( );
                int index2 = thisSpur.getSpurNode( ).getNodeNumber( );
                if(thisSlice[index1][index2] > 0) 
                {
                    changem = changem - thisSlice[index1] [index2]; 
                }
                else
                {
                    changem = changem + 1;
                }
                
            }
            //Handle tertiary Ring
            if(tertiaryRingAfter != null)
            {
                List<Node> ringNodes = tertiaryRingAfter.getNodes( );
                for(int j = 0; j<ringNodes.size()-1; j++)
                {
                    int index1 = ringNodes.get(j).getNodeNumber( );
                    int index2 = ringNodes.get(j+1).getNodeNumber( );
                    if(thisSlice[index1][index2] > 0) 
                    {
                        changem = changem - thisSlice[index1] [index2]; 
                    }
                    else
                    {
                        changem = changem + 1;
                    }
                }  
            }
        }
        
        // change of the number of different occ between k and k+1
        if (k!=Constants.TROTTER_NUMBER-1) 
        {
            int[][] thisSlice = getTrotterSlices().get(k+1);
            //Handle local ring
            for(int i = 0; i<localRingsBefore.size(); i++)
            {
                Ring currentRing =  localRingsBefore.get(i);
                List<Node> ringNodes = currentRing.getNodes( );
                for(int j = 0; j<ringNodes.size()-1; j++)
                {
                    int index1 = ringNodes.get(j).getNodeNumber( );
                    int index2 = ringNodes.get(j+1).getNodeNumber( );
                    if(thisSlice[index1][index2] == 0) 
                    {
                        changep = changep - 1;
                    }
                    else
                    {
                        changep = changep + thisSlice[index1] [index2]; 
                    }
                }     
            }
            //Handle spurs
            for(int i = 0; i<spursBefore.size( ); i++)
            {
                Spur thisSpur = spursBefore.get( i );
                int index1 = thisSpur.getParentNode( ).getNodeNumber( );
                int index2 = thisSpur.getSpurNode( ).getNodeNumber( );
                if(thisSlice[index1][index2] == 0) 
                {
                    changep = changep - 1;
                }
                else
                {
                    changep = changep + thisSlice[index1] [index2]; 
                }
                
            }
            //Handle tertiary Ring
            if(tertiaryRingBefore != null)
            {
                List<Node> ringNodes = tertiaryRingBefore.getNodes( );
                for(int j = 0; j<ringNodes.size()-1; j++)
                {
                    int index1 = ringNodes.get(j).getNodeNumber( );
                    int index2 = ringNodes.get(j+1).getNodeNumber( );
                    if(thisSlice[index1][index2] == 0) 
                    {
                        changep = changep - 1;
                    }
                    else
                    {
                        changep = changep + thisSlice[index1] [index2]; 
                    }
                } 
            }
            
            //Stage 2
            for(int i = 0; i<localRingsAfter.size(); i++)
            {
                Ring currentRing =  localRingsAfter.get(i);
                List<Node> ringNodes = currentRing.getNodes( );
                for(int j = 0; j<ringNodes.size()-1; j++)
                {
                    int index1 = ringNodes.get(j).getNodeNumber( );
                    int index2 = ringNodes.get(j+1).getNodeNumber( );
                    if(thisSlice[index1][index2] > 0) 
                    {
                        changep = changep - thisSlice[index1] [index2]; 
                    }
                    else
                    {
                        changep = changep + 1;
                    }
                }     
            }
            //Handle spurs
            for(int i = 0; i<spursAfter.size( ); i++)
            {
                Spur thisSpur = spursAfter.get( i );
                int index1 = thisSpur.getParentNode( ).getNodeNumber( );
                int index2 = thisSpur.getSpurNode( ).getNodeNumber( );
                if(thisSlice[index1][index2] > 0) 
                {
                    changep = changep - thisSlice[index1] [index2]; 
                }
                else
                {
                    changep = changep + 1;
                }
                
            }
            //Handle tertiary Ring
            if(tertiaryRingAfter != null)
            {
                List<Node> ringNodes = tertiaryRingAfter.getNodes( );
                for(int j = 0; j<ringNodes.size()-1; j++)
                {
                    int index1 = ringNodes.get(j).getNodeNumber( );
                    int index2 = ringNodes.get(j+1).getNodeNumber( );
                    if(thisSlice[index1][index2] > 0) 
                    {
                        changep = changep - thisSlice[index1] [index2]; 
                    }
                    else
                    {
                        changep = changep + 1;
                    }
                } 
            }
        }
        OCCChangeHolder changeHolder = new OCCChangeHolder( );
        changeHolder.setChangem( changem );
        changeHolder.setChangep( changep );
        return changeHolder;
    }
}
