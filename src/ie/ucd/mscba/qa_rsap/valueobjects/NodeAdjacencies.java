/**
 * Created on 7 Jul 2012
 *
 * @author Michael Flynn
 * @version 1.0
 *
 * File: NodeAdjacencies.java
 * Package ie.ucd.mscba.qa_rsap.valueobjects
 * Project QA_RSAP
 */
package ie.ucd.mscba.qa_rsap.valueobjects;

import ie.ucd.mscba.qa_rsap.utils.QaRsapUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import de.zib.sndlib.network.AddModule;
import de.zib.sndlib.network.AdditionalModules;
import de.zib.sndlib.network.Link;
import de.zib.sndlib.network.Links;
import de.zib.sndlib.network.Network;

/**
 * 
 */
public class NodeAdjacencies
{
       private Hashtable<String,List<AdjNode>> adjacencies  = null;
       private double scaleRatio  = 0.0;
       
        /**
         * @return the scaleRatio
         */
        public double getScaleRatio()
        {
            return scaleRatio;
        }

        public NodeAdjacencies()
       {    
       }
       
        public NodeAdjacencies(List<Link> linkList)
        {      
            
            double minValue = Double.POSITIVE_INFINITY;
            double maxValue = Double.NEGATIVE_INFINITY;
            
            adjacencies = new Hashtable<String,List<AdjNode>>();
 
           for (Link link : linkList)
           {
               String source = link.getSource( );
               String target = link.getTarget( );
               AdditionalModules additionalModules = link.getAdditionalModules( );
               List<AddModule> moduleList  = additionalModules.getAddModule( );
               
               double cost = moduleList.get(0).getCost( ).doubleValue();
               if(cost < minValue)
                   minValue = cost;
               if(cost > maxValue)
                   maxValue = cost;
               
               List<AdjNode> sourceAdj = adjacencies.get(source);
               List<AdjNode> targetAdj = adjacencies.get(target);
               
               AdjNode srcAdjNode = new AdjNode();
               
               //Record sources adj
               if(sourceAdj == null)
               { 
                   sourceAdj = new ArrayList<AdjNode>();
                   srcAdjNode.setNodeName(target);
                   srcAdjNode.setCost(cost);
                   sourceAdj.add(srcAdjNode);
                   adjacencies.put(source, sourceAdj );                  
               }
               else
               {
                   srcAdjNode.setNodeName(target);
                   srcAdjNode.setCost(cost);
                   sourceAdj.add(srcAdjNode);                   
               }
               
               AdjNode targAdjNode = new AdjNode();
               //Record target adj
               if(targetAdj == null)
               { 
                   
                   targetAdj = new ArrayList<AdjNode>();
                   targAdjNode.setNodeName(source);
                   targAdjNode.setCost(cost);
                   targetAdj.add(targAdjNode);
                   adjacencies.put(target, targetAdj );                  
               }
               else
               {
                   targAdjNode.setNodeName(source);
                   targAdjNode.setCost(cost);
                   targetAdj.add(targAdjNode);
                   
               }
           }
           
           Enumeration enumList =  adjacencies.keys();
           while (enumList.hasMoreElements( ))
           {
               String thisKey = (String)enumList.nextElement( );
               List<AdjNode> thisList = adjacencies.get( thisKey ) ;
               Collections.sort( thisList );
           }
           
           //Perform Scaling on min -> Max value for Quantum Annealing
           double averageValue = (minValue+maxValue)/2;
           double scaledValue = QaRsapUtils.normalizeValue(averageValue, minValue, maxValue);
           scaleRatio = scaledValue/averageValue;


        }
        
        public NodeAdjacencies reducedClone(List<String> nodeToRemove)
        {
            NodeAdjacencies cloned = new NodeAdjacencies();
            Hashtable<String,List<AdjNode>> cloneAdjacencies = new Hashtable<String,List<AdjNode>>();
            
            Enumeration enumList =  adjacencies.keys();
            while (enumList.hasMoreElements( ))
            {
                String thisKey = (String)enumList.nextElement( );
                boolean proceed = true;
                for(int j=0; j<nodeToRemove.size( ); j++ )
                {
                    if((nodeToRemove.get(j)).equalsIgnoreCase(thisKey))
                    {
                        proceed = false;
                        break;
                    }
                }
                if(proceed)
                {
                    List<AdjNode> origList = adjacencies.get( thisKey ) ;
                    List<AdjNode> newList = new ArrayList<AdjNode>( );
                    for(int i=0; i<origList.size( ); i++ )
                    {
                        AdjNode thisNode = origList.get( i );
                        if(safeToAdd(nodeToRemove, thisNode.getNodeName( )))
                        {
                            newList.add(thisNode);
                        }               
                    }
                    cloneAdjacencies.put( thisKey, newList );
                }
            }
            cloned.adjacencies = cloneAdjacencies;
            
            return cloned;
        }
       
        private boolean safeToAdd(List<String> nodeToRemove, String currenNodeName)
        {
           boolean proceedToAdd = true;
           for(int j=0; j<nodeToRemove.size( ); j++ )
           {
               if((currenNodeName).equalsIgnoreCase(nodeToRemove.get(j)))
               {
                   proceedToAdd = false;
                   break;
               }
           }
           return proceedToAdd;
        }
        
        public List<AdjNode> getAdjList(String nodeName)
        {
           return adjacencies.get( nodeName );
        }
        
       
}
