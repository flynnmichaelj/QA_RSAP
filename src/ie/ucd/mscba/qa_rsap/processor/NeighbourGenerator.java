/**
 * Created on 11 Jul 2012
 *
 * @author Michael Flynn
 * @version 1.0
 *
 * File: NeighbourGenerator.java
 * Package ie.ucd.mscba.qa_rsap.processor
 * Project QA_RSAP
 */
package ie.ucd.mscba.qa_rsap.processor;

import ie.ucd.mscba.qa_rsap.Constants;
import ie.ucd.mscba.qa_rsap.dijkstra.Dijkstra;
import ie.ucd.mscba.qa_rsap.dijkstra.DijkstraNode;
import ie.ucd.mscba.qa_rsap.utils.QaRsapUtils;
import ie.ucd.mscba.qa_rsap.valueobjects.AdjNode;
import ie.ucd.mscba.qa_rsap.valueobjects.NodeAdjacencies;
import ie.ucd.mscba.qa_rsap.valueobjects.Ring;
import ie.ucd.mscba.qa_rsap.valueobjects.Solution;
import ie.ucd.mscba.qa_rsap.valueobjects.Spur;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;

import de.zib.sndlib.network.Network;
import de.zib.sndlib.network.Node;

/**
 * 
 */
public class NeighbourGenerator
{
    NodeAdjacencies nodeAdjacencies;
    Network         network;

    public NeighbourGenerator( NodeAdjacencies nodeAdjacencies, Network network )
    {
        this.nodeAdjacencies = nodeAdjacencies;
        this.network = network;
    }

    public void deleteInsert(Solution clonedSol , Random rand)
    {
        List<Ring> localRings = clonedSol.getLocalrings( );
        boolean changeFound = false;
        int  maxTriesCounter = 0;
        while(!changeFound && maxTriesCounter <= 5)
        {
            Ring selectedRing = null; //Focus on rings the breach max size constraint
            for(Ring ring : localRings)
            {
                if(ring.getSize() > Constants.maxLocalRingSize)
                    selectedRing = ring;
                break;
            }
            if(selectedRing == null)
            {
                List<Ring> clonedListForDel = new ArrayList<Ring>();
                for(Ring ring : localRings)
                {
                    if(ring.getSize() > 3)
                        clonedListForDel.add(ring);
                }
                if(clonedListForDel.size() > 0)
                    selectedRing = clonedListForDel.get(rand.nextInt(clonedListForDel.size())); 
                else
                    break; //TODO consider catering for many small rings by deleting one ring
            }
        
            boolean notOnTertiaryRing = false;
            Node selectedNode = null;
            while(clonedSol.getTertiaryRing( )!=null && !notOnTertiaryRing)
            {
                selectedNode = selectedRing.getNodes( ).get(rand.nextInt(selectedRing.getNodes().size()));
                if(!QaRsapUtils.isNodeOnRing( selectedNode, clonedSol.getTertiaryRing( )))
                    notOnTertiaryRing = true; 
            }
            
            List<Ring> clonedListforIns = new ArrayList<Ring>();
            for(Ring ring : localRings)
            {
                if(ring != selectedRing && ring.getSize() < Constants.maxLocalRingSize)
                    clonedListforIns.add(ring);
            }
            
            Ring modifiedRing = insert(selectedNode, clonedListforIns);
            if(modifiedRing != null)
            {
                delete( selectedNode, selectedRing ); 
                changeFound = true;
            }
            maxTriesCounter ++;
        }
    }
    
    public boolean delete(Node deleteNode, Ring deleteRing)
    {
        boolean completedSucessfully = false; 
        
        int positionOnRing = findNodePosOnRing(deleteRing, deleteNode);
        Node leftNode = null;
        Node rightNode = null;
        if(positionOnRing == 0)
        {
            leftNode = deleteRing.getNodes( ).get(1);
            rightNode = deleteRing.getNodes( ).get((deleteRing.getNodes( ).size( ))-2);
        }
        else
        {
             leftNode = deleteRing.getNodes( ).get(positionOnRing-1);
             rightNode = deleteRing.getNodes( ).get(positionOnRing+1);
        }
        if(QaRsapUtils.isAdj(leftNode.getId(), nodeAdjacencies.getAdjList(rightNode.getId())))
        {
            if(positionOnRing == 0)
            {
                deleteRing.getNodes( ).remove(0);  //Check this
                deleteRing.getNodes( ).remove(deleteRing.getSize( )-1);  
                Node startNode = deleteRing.getNodes().get(0);
                deleteRing.getNodes( ).add(deleteRing.getSize(), startNode);
            }
            else
            {
                deleteRing.removeNode(deleteNode); 
            }   
            completedSucessfully = true;
        }
        return completedSucessfully;
    }


    public Ring insert(Node insertNode, List<Ring> rings) 
    {
        Ring modifiedRing = null;
        
        List<AdjNode> adjList = nodeAdjacencies.getAdjList(insertNode.getId( ));
        Iterator<AdjNode> iter = adjList.iterator();
       
        while(iter.hasNext())
        {
            boolean noSolutionRing = true;
            AdjNode currentAdjNode = iter.next();
            Node closestNode = QaRsapUtils.getNodeById(currentAdjNode.getNodeName(), network.getNetworkStructure().getNodes().getNode());
            Ring parentRing = null;
            for(Ring ring : rings)
            {
                List<Node> currentRingNodes = ring.getNodes();
                if((currentRingNodes).contains(closestNode) && 
                                !(currentRingNodes).contains(insertNode) &&
                                ring.getSize( ) < Constants.maxLocalRingSize)
                {
                    parentRing = ring;
                    noSolutionRing = false;
                    break;
                }      
            }

            if(noSolutionRing)
                continue;
            
            int positionOfClosestNode = findNodePosOnRing(parentRing, closestNode);
        
            if(positionOfClosestNode == 0)
            {
                Node selecteNode = null;
                Node firstAdj = parentRing.getNodes( ).get(1);
                Node lastAdj = parentRing.getNodes( ).get((parentRing.getNodes( ).size( ))-2);
                
                //Here we want to insert between the closest nodes and its next nearest node
                double firstAdjCost = QaRsapUtils.isAdjCost(firstAdj.getId( ), adjList);
                double lastAdjCost = QaRsapUtils.isAdjCost(lastAdj.getId( ), adjList);
                if(firstAdjCost > 0.0 && lastAdjCost > 0.0)
                {        
                    selecteNode = firstAdjCost<lastAdjCost ? firstAdj : lastAdj;
                    if(selecteNode == firstAdj)
                        parentRing.getNodes( ).add( 1, insertNode);
                    else
                        parentRing.getNodes( ).add( (parentRing.getNodes( ).size( ))-1, insertNode);
                    
                    modifiedRing = parentRing;
                    break;
                }
                else if(firstAdjCost > 0.0 || lastAdjCost > 0.0)
                {
                    selecteNode = firstAdjCost>lastAdjCost ? firstAdj : lastAdj;
                    if(selecteNode == firstAdj)
                        parentRing.getNodes( ).add( 1, insertNode);
                    else
                        parentRing.getNodes( ).add( (parentRing.getNodes( ).size( ))-1, insertNode);
                    
                    modifiedRing = parentRing;
                    break;
                }
                else
                {
                    continue;
                }                   
            }
            else
            {
                Node selecteNode = null;
                Node leftNode = parentRing.getNodes( ).get(positionOfClosestNode-1);
                Node rightNode = parentRing.getNodes( ).get(positionOfClosestNode+1);
                double leftCost = QaRsapUtils.isAdjCost( leftNode.getId( ), adjList );
                double rightCost = QaRsapUtils.isAdjCost( rightNode.getId( ), adjList );
                if(leftCost > 0.0 && rightCost > 0.0)
                {        
                    selecteNode = leftCost<rightCost ? leftNode : rightNode;
                    if(selecteNode == leftNode)
                        parentRing.getNodes( ).add( positionOfClosestNode,  insertNode);
                    else
                        parentRing.getNodes( ).add( positionOfClosestNode+1, insertNode);
                    
                    modifiedRing = parentRing;
                    break;
                }
                else if(leftCost > 0.0 || rightCost > 0.0)
                {
                    selecteNode = leftCost>rightCost ? leftNode : rightNode;
                    if(selecteNode == leftNode)
                        parentRing.getNodes( ).add( positionOfClosestNode,  insertNode);
                    else
                        parentRing.getNodes( ).add( positionOfClosestNode+1,  insertNode);
                  
                    modifiedRing = parentRing;
                    break;
                }
                else
                {
                    continue;
                }    
            }   
        }
        return modifiedRing;
    }
    
    public Solution deleteInsertSearch(Solution sol)
    {
        Solution clonedSol = sol.clone();
        Random rand  = new Random();
        
        List<Ring> localRings = clonedSol.getLocalrings( );
        List<Spur> spurs = clonedSol.getSpurs();
        
        //First try to eliminate spurs
        if(spurs != null && spurs.size( ) > 0)
        {
            //pick a random spur
            int randomSpur = rand.nextInt(spurs.size( ));
            Spur pickedSpur = spurs.get(randomSpur);
            
            Ring modifiedRing = insert( pickedSpur.getSpurNode( ), localRings);
            if(modifiedRing != null)
            {
                spurs.remove(pickedSpur);
            }    
        }        
        else
        {
            if(sol.getLocalrings( ).size( ) > 1)
            {
                deleteInsert(clonedSol, rand);
            }
        }
        return clonedSol;
    }

    public Solution swapNodeSearch(Solution sol)
    {
          Solution clonedSol = sol.clone();
          List<Ring> localRings = clonedSol.getLocalrings();
          
          Random rand = new Random();
          boolean changeFound = false;
          int maxTries = 0;
          while(!changeFound && maxTries<5) 
          {
              boolean notInitNodeTertiaryRing = false;
              Ring initRing = localRings.get(rand.nextInt(localRings.size()));
              Node initNode = null;
              int initNodeMaxTries = 0;
              while(sol.getTertiaryRing( )!= null && !notInitNodeTertiaryRing && initNodeMaxTries < 20)
              {
                  initNode = initRing.getNodes( ).get(rand.nextInt(initRing.getNodes().size()));
                  if(!QaRsapUtils.isNodeOnRing( initNode, clonedSol.getTertiaryRing( )))
                      notInitNodeTertiaryRing = true; 
                  initNodeMaxTries ++;
              }
              if(initNode == null)
              {
                  continue;
              }
              Ring secondRing = null;
              Node secondNode = null;
              boolean differnetRing = false;
              int secondRingMaxTries = 0;
              while(!differnetRing && secondRingMaxTries<5)
              {
                  secondRing = localRings.get(rand.nextInt(localRings.size()));
                  secondRingMaxTries++;
                  if(secondRing != initRing)
                  {
                      boolean notOnSecondNodeTertiaryRing = false;
                      int secondNodeMaxTries = 0;
                      while(!notOnSecondNodeTertiaryRing && secondNodeMaxTries < 20)
                      {
                          secondNode = secondRing.getNodes( ).get(rand.nextInt(secondRing.getNodes().size()));
                          if(!QaRsapUtils.isNodeOnRing( secondNode, clonedSol.getTertiaryRing( )))
                              notOnSecondNodeTertiaryRing = true; 
                          
                          secondNodeMaxTries++;
                      }
                      if(!notOnSecondNodeTertiaryRing)
                      {
                          continue;
                      }
                      differnetRing = true;
                      
                  }
                  else
                  {
                      secondRing = null;
                      secondNode = null;
                  }
              }
              if(secondNode == null)
              {
                  //cannot find a second node to swap 
                  //Fail graefully
                  return sol;
              }
          
              Node[] initNodeAdj = new Node[2];
              Node[] secondNodeAdj = new Node[2];
          
              int initNodePos = findNodePosOnRing( initRing, initNode );
              int secondNodePos = findNodePosOnRing( secondRing, secondNode );
                
              if(initNodePos == 0)
              {
                  initNodeAdj[0] = initRing.getNodes( ).get(initRing.getSize( )-2);
                  initNodeAdj[1] = initRing.getNodes( ).get(1);        
              }
              else
              {
                  initNodeAdj[0] = initRing.getNodes( ).get(initNodePos-1);
                  initNodeAdj[1] = initRing.getNodes( ).get(initNodePos+1);
              }
              if(secondNodePos == 0)
              {
                  secondNodeAdj[0] = secondRing.getNodes( ).get(secondRing.getSize( )-2);
                  secondNodeAdj[1] = secondRing.getNodes( ).get(1);  
              }
              else
              {
                  secondNodeAdj[0] = secondRing.getNodes( ).get(secondNodePos-1);
                  secondNodeAdj[1] = secondRing.getNodes( ).get(secondNodePos+1);    
              }
       
              boolean initNodeCheck = false;
              boolean initOneCheck = QaRsapUtils.isAdj(secondNode.getId(), nodeAdjacencies.getAdjList(initNodeAdj[0].getId()));
              boolean initTwoCheck = QaRsapUtils.isAdj(secondNode.getId(), nodeAdjacencies.getAdjList(initNodeAdj[1].getId()));
              if(initOneCheck && initOneCheck)
                  initNodeCheck = true;
              
              boolean secondNodeCheck = false;
              boolean secondOneCheck = QaRsapUtils.isAdj(initNode.getId(), nodeAdjacencies.getAdjList(secondNodeAdj[0].getId()));
              boolean secondTwoCheck = QaRsapUtils.isAdj(initNode.getId(), nodeAdjacencies.getAdjList(secondNodeAdj[1].getId()));
              if(secondOneCheck && secondTwoCheck)
                  secondNodeCheck = true;
                     
              if(initNodeCheck && secondNodeCheck) //we can perform swap
              {   
                  //preform removes
                  if(initNodePos == 0)
                  {
                      initRing.getNodes( ).remove(0);
                      initRing.getNodes( ).remove(initRing.getSize( )-1);
                  }
                  else
                  {
                      initRing.getNodes( ).remove(initNodePos);
                  }
                  if(secondNodePos == 0)
                  {
                      secondRing.getNodes( ).remove(0);
                      secondRing.getNodes( ).remove(initRing.getSize( )-1);
                  }
                  else
                  {
                      secondRing.getNodes( ).remove(secondNodePos);
                  }
                  
                  
                  //perform adds
                  if(secondNodePos == 0)
                  {
                      secondRing.getNodes( ).add(0, initNode );
                      secondRing.getNodes( ).add(initRing.getSize( )-1, initNode );
                  }
                  else
                  {                  
                      secondRing.getNodes( ).add(secondNodePos, initNode );
                  }  
                  if(initNodePos == 0)
                  {
                      initRing.getNodes( ).add(0, secondNode );
                      initRing.getNodes( ).add(initRing.getSize( ), secondNode );
                  }
                  else
                  {                  
                      initRing.getNodes( ).add(initNodePos, secondNode );
                  } 
                  
                  changeFound =true;
              }
              maxTries++;
          }
          return clonedSol;
    }
    
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // Delete small ring searches
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    public Solution deleteSmallRingSearch(Solution sol)
    {
        Solution clonedSol = sol.clone();
        List<Ring> localRings = clonedSol.getLocalrings( );
        Ring ringToDelete = null;
        for(Ring ring : localRings)
        {
            if(ring.getSize( ) == 4)
            {
                ringToDelete = ring;
                localRings.remove(ring);
                break;
            }     
        }
        if(ringToDelete != null)
        {
               List<Node> retries = new ArrayList<Node>();
               
               Random rand = new Random();
               SolutionGenerator solGenerator = new SolutionGenerator( network, nodeAdjacencies );
               for(int i=0; i<ringToDelete.getSize( )-1; i++) 
               {
                   Node thisNode = ringToDelete.getNodes( ).get( i );
                   Spur spur = solGenerator.createSrup( thisNode, network.getNetworkStructure( ).getNodes( ).getNode( ), localRings );
                   if(spur == null)
                   {
                       retries.add(thisNode);
                   }
                   else
                   {
                       Ring modifiedRing = insert( spur.getSpurNode( ), localRings);
                       if(modifiedRing != null)
                       {
                           clonedSol.getSpurs( ).add(spur);
                       } 
                       else
                       {
                           retries.add(thisNode);
                       }
                       //TODO Fix this
                   }
               }
               
               //Handle possible retries
               for(int i=0; i<retries.size(); i++) 
               {
                   Node thisNode = retries.get( i );
                   Spur spur = solGenerator.createSrup( thisNode, network.getNetworkStructure( ).getNodes( ).getNode( ), localRings );
                   if(spur == null)
                   {
                       //Fail Gracefully by return original sol 
                       return sol;
                   }
                   else
                   {
                       Ring modifiedRing = insert( spur.getSpurNode( ), localRings);
                       if(modifiedRing != null)
                       {
                           clonedSol.getSpurs( ).add(spur);
                       }
                       else
                       {
                           //Fail Gracefully by return original sol 
                           return sol;
                       }
                   }
               }
        } 
        
        //TODO handle hoe this affect the tertiary ring
        return clonedSol;
    }

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // Tertiary Ring searches
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    public Solution tertiaryRingSearch(Solution sol)
    {
        Solution clonedSol = sol.clone();
        List<Ring> localRings = clonedSol.getLocalrings();
        Ring tertiaryRing = clonedSol.getTertiaryRing();
        
        Random rand = new Random();
        Ring initRing = localRings.get(rand.nextInt(localRings.size()));
        for(Node node : initRing.getNodes( ))
        {
            //Disconnect selected ring 
            int posOnRing = findNodePosOnRing( tertiaryRing, node );
            if(posOnRing > -1)
            {
                Node dummyNode = new Node();
                dummyNode.setId("TempNode");
                //Remove
                if(posOnRing == 0)
                {
                    tertiaryRing.getNodes( ).remove(0);
                    tertiaryRing.getNodes( ).remove(tertiaryRing.getSize( )-1);
                }
                else
                {
                    tertiaryRing.getNodes( ).remove(posOnRing);
                }
                //Add
                if(posOnRing == 0)
                {
                    tertiaryRing.getNodes( ).add(0, dummyNode );
                    tertiaryRing.getNodes( ).add(tertiaryRing.getSize( ), dummyNode );
                }
                else
                {                  
                    tertiaryRing.getNodes( ).add(posOnRing, dummyNode );
                }   
            }   
        }
       //Reform tertiary ring by removing dummy nodes.
        for(int i=0; i<tertiaryRing.getSize( ); i++)
        {
            if(QaRsapUtils.countRealNode( tertiaryRing ) <=1)
            {
                //Fail gracefully
                clonedSol.setTertiaryRing( null );
                SolutionGenerator solGenerator = new SolutionGenerator( network, nodeAdjacencies );
                List<Node> tempNodeList = new ArrayList<Node>(network.getNetworkStructure( ).getNodes( ).getNode());
                List<Spur> spurs = clonedSol.getSpurs();
                for(Spur spur : spurs)
                {
                    tempNodeList.remove(spur.getSpurNode());
                }
                tertiaryRing = solGenerator.generateTertiaryRing( clonedSol.getSpurs( ), clonedSol.getLocalrings( ), tempNodeList );
                if(tertiaryRing!=null)
                {
                     clonedSol.setTertiaryRing(tertiaryRing);
                }
                else
                {
                    return sol; //Cannot complete search, return original unchanged sol.
                }      
            }
            
            Node thisNode = tertiaryRing.getNodes( ).get( i );
            Node leftSide = null;
            Node rightSide = null;
            if("TempNode".equals(thisNode.getId( )))
            {
                if(i==0)
                {
                    leftSide = tertiaryRing.getNodes( ).get( tertiaryRing.getSize( )-2 );
                    rightSide = tertiaryRing.getNodes( ).get( 1 );    
                }
                else if(i == tertiaryRing.getSize( )-1)
                {
                    leftSide = tertiaryRing.getNodes( ).get( i-1);
                    rightSide = tertiaryRing.getNodes( ).get( 0 );                 
                }
                else
                {
                    leftSide = tertiaryRing.getNodes( ).get( i-1 );
                    rightSide = tertiaryRing.getNodes( ).get( i+1 ); 
                }
                if("TempNode".equals(rightSide.getId( )))
                {
                    tertiaryRing.removeNode(rightSide);
                    i--;
                    continue;
                }
                else if("TempNode".equals(leftSide.getId( )))
                {
                    tertiaryRing.removeNode(leftSide);
                    i--;
                    continue;
                }
                else
                {
                    tertiaryRing.getNodes( ).remove(i);
                    if(i==0)
                    {
                        tertiaryRing.getNodes().remove(tertiaryRing.getSize( )-1);
                    }
                    
                    if(tertiaryRing.getSize( ) < 3)
                    {
                        Node lastNode = tertiaryRing.getNodes( ).get( tertiaryRing.getSize( )-1);
                        List<AdjNode> adjList = nodeAdjacencies.getAdjList( lastNode.getId( ) );
                        boolean notOnTertiaryRing = false;
                        int counter = 0;
                        while(!notOnTertiaryRing)
                        {
                            Node nodeToInsert = QaRsapUtils.getNodeById(adjList.get(counter).getNodeName( ), network.getNetworkStructure( ).getNodes( ).getNode( ) );
                            if(!QaRsapUtils.isNodeOnRing( thisNode, tertiaryRing ))
                            {
                                tertiaryRing.getNodes( ).add( nodeToInsert );
                                notOnTertiaryRing = true;
    
                                leftSide = nodeToInsert;
                                rightSide = tertiaryRing.getSpecificNode( 0 );
             
                            }
                            counter++;
                        }    
                    }
                    
                    List<String> nodesToRemove = QaRsapUtils.nodesToRemove(tertiaryRing, clonedSol.getSpurs( ), leftSide.getId( ), rightSide.getId( ));
                    List<Node> reducedAllNodes = new ArrayList<Node>(network.getNetworkStructure( ).getNodes( ).getNode( ));
                    List<Node> origAllNode = network.getNetworkStructure( ).getNodes( ).getNode( );
                    for(int j=0; j<nodesToRemove.size( ); j++)
                    {
                        Node currentNode = QaRsapUtils.getNodeById( nodesToRemove.get(j), origAllNode);
                        reducedAllNodes.remove( currentNode );
                    }
                        
                    Dijkstra dijkstra = new Dijkstra();
                    TreeMap<String, DijkstraNode> returnedNodes =  dijkstra.runDijkstra( leftSide, rightSide, 
                                            reducedAllNodes, 
                                            nodeAdjacencies.reducedClone(nodesToRemove));
                    
                    if(returnedNodes != null)
                    {
                        String key = returnedNodes.firstKey( );
                        DijkstraNode returnedNode = returnedNodes.get( key );
                        
                        List<String> pathList = returnedNode.getPathFromRoot( );
                        for(int j=0; j<pathList.size( )-1; j++)
                        {
                            tertiaryRing.getNodes( ).add( i+j, QaRsapUtils.getNodeById( pathList.get( j ), network.getNetworkStructure( ).getNodes( ).getNode( )));
                        }
                    }
                    else
                    {
                        //Failed to find path
                        clonedSol.setTertiaryRing( null );
                        SolutionGenerator solGenerator = new SolutionGenerator( network, nodeAdjacencies );
                        List<Node> tempNodeList = new ArrayList<Node>(network.getNetworkStructure( ).getNodes( ).getNode());
                        List<Spur> spurs = clonedSol.getSpurs();
                        for(Spur spur : spurs)
                        {
                            tempNodeList.remove(spur.getSpurNode());
                        }
                        tertiaryRing = solGenerator.generateTertiaryRing( clonedSol.getSpurs( ), clonedSol.getLocalrings( ), tempNodeList );
                        if(tertiaryRing!=null)
                        {
                             clonedSol.setTertiaryRing(tertiaryRing);
                        }
                        else
                        {
                            return sol; //Cannot complete search, return original unchanged sol.
                        }
                    }
                }               
            }
        } 
        boolean solutionFound = false;
        for(Node node : initRing.getNodes( ))
        {
            if(QaRsapUtils.isNodeOnRing( node, tertiaryRing ))
            {
                solutionFound = true;
                break;
            }
        }
        if(!solutionFound)
        {
            boolean succeeded = addLocalRingToTertiary(tertiaryRing, initRing, rand, clonedSol.getSpurs( ));  
            if(!succeeded)
            {
                clonedSol.setTertiaryRing( null );
                SolutionGenerator solGenerator = new SolutionGenerator( network, nodeAdjacencies );
                List<Node> tempNodeList = new ArrayList<Node>(network.getNetworkStructure( ).getNodes( ).getNode());
                List<Spur> spurs = clonedSol.getSpurs();
                for(Spur spur : spurs)
                {
                    tempNodeList.remove(spur.getSpurNode());
                }
                tertiaryRing = solGenerator.generateTertiaryRing( clonedSol.getSpurs( ), clonedSol.getLocalrings( ), tempNodeList );
                if(tertiaryRing!=null)
                {
                     clonedSol.setTertiaryRing(tertiaryRing);
                }
                else
                {
                    return sol; //Cannot complete search, return original unchanged sol.
                }
            }
        }
        return clonedSol;
    }
    

    private boolean addLocalRingToTertiary(Ring tertiaryRing, Ring initRing, Random rand, List<Spur> spurs)
    {
        boolean succeeded = false;
        //TODO should we pick random node or try for best fit, i.e. check all node to tertiary
        Node randNode = initRing.getNodes( ).get((rand.nextInt(initRing.getSize( ))));
        
        List<String> nodesToRemove = QaRsapUtils.nodesToRemove(null, spurs, null, null );
        List<Node> reducedAllNodes = new ArrayList<Node>(network.getNetworkStructure( ).getNodes( ).getNode( ));
        List<Node> origAllNode = network.getNetworkStructure( ).getNodes( ).getNode( );
        for(int j=0; j<nodesToRemove.size( ); j++)
        {
            Node currentNode = QaRsapUtils.getNodeById( nodesToRemove.get(j), origAllNode);
            reducedAllNodes.remove( currentNode );
        }
        
        Dijkstra dijkstra = new Dijkstra();
        TreeMap<String, DijkstraNode> returnedNodes1 =  dijkstra.runDijkstra( randNode, null, 
                                network.getNetworkStructure( ).getNodes( ).getNode( ), 
                                nodeAdjacencies );
       Iterator<String> iter = returnedNodes1.keySet( ).iterator( );
       double closest = Double.POSITIVE_INFINITY;
       Node closestNode = null;
       DijkstraNode dn = null;
       while(iter.hasNext( ))
       {
           String thisKey = iter.next( );
           dn = returnedNodes1.get( thisKey );
           Node node = QaRsapUtils.getNodeById( dn.getNodeName( ), network.getNetworkStructure( ).getNodes( ).getNode( ) );
           if(QaRsapUtils.isNodeOnRing( node, tertiaryRing ))
           {
               if(dn.getDistanceToRoot( ) < closest)
               {
                   closest = dn.getDistanceToRoot( );
                   closestNode = node;
               } 
           }    
       }
       int posOnTRing = findNodePosOnRing(tertiaryRing, closestNode);
       int posLeftSide = 0;
       int posRightSide = 0;
       Node leftside = null;
       Node rightSide = null;
      
       /*****************************************************
        * Here we need to get left side and right side to determine which
        * is the best part to connect this unconnected ring to the tertiary
        ****************************************************/
       if(posOnTRing == 0)
       {
           leftside = tertiaryRing.getNodes( ).get( tertiaryRing.getSize( )-2);
           rightSide = tertiaryRing.getNodes( ).get(posOnTRing +1);
           posLeftSide = tertiaryRing.getSize( )-2;
           posRightSide = posOnTRing +1;
       }
       else
       {
           leftside = tertiaryRing.getNodes( ).get( posOnTRing-1 );
           rightSide = tertiaryRing.getNodes( ).get(posOnTRing +1);
           posLeftSide = posOnTRing-1;
           posRightSide = posOnTRing +1;
       }

       //Call dijkstra for leftside node
       //Exclude all other tertiray and spur nodes
       DijkstraNode sideToInsert = null;
       int sideSelected = -1; //-1=none selected, 0 = left select, 1 = right selected
       nodesToRemove = QaRsapUtils.nodesToRemove(tertiaryRing, spurs, leftside.getId(), null);
       reducedAllNodes = new ArrayList<Node>(network.getNetworkStructure( ).getNodes( ).getNode( ));
       for(int j=0; j<nodesToRemove.size( ); j++)
       {
           Node currentNode = QaRsapUtils.getNodeById( nodesToRemove.get(j), origAllNode);
           reducedAllNodes.remove( currentNode );
       }
       TreeMap<String, DijkstraNode> returnedNodes2 =  dijkstra.runDijkstra( leftside, randNode, 
                               reducedAllNodes, 
                               nodeAdjacencies.reducedClone(nodesToRemove) );
       if(returnedNodes2!= null)
       {
           String key = returnedNodes2.firstKey( );
           sideToInsert = returnedNodes2.get( key );
           sideSelected = 0;
       }
       
       //Call dijkstra for rightSide node
       //Exclude all other tertiary and spur nodes
       nodesToRemove = QaRsapUtils.nodesToRemove(tertiaryRing, spurs, rightSide.getId(), null);
       reducedAllNodes = new ArrayList<Node>(network.getNetworkStructure( ).getNodes( ).getNode( ));
       for(int j=0; j<nodesToRemove.size( ); j++)
       {
           Node currentNode = QaRsapUtils.getNodeById( nodesToRemove.get(j), origAllNode);
           reducedAllNodes.remove( currentNode );
       }
       TreeMap<String, DijkstraNode> returnedNodes3 =  dijkstra.runDijkstra( randNode, rightSide, 
                               reducedAllNodes, 
                               nodeAdjacencies.reducedClone(nodesToRemove) );
       if(returnedNodes3!= null)
       {
           String key = returnedNodes3.firstKey( );
           if(sideToInsert != null)
           {
               if(sideToInsert.getDistanceToRoot( ) > returnedNodes3.get( key ).getDistanceToRoot( ))
               {
                   sideToInsert = returnedNodes3.get( key );
                   sideSelected = 1;
               }
           }
           else if(returnedNodes3.get( key ) != null)
           {
               sideToInsert = returnedNodes3.get( key );
               sideSelected = 1;
           }
       }
 
       if(sideToInsert !=null && sideSelected != -1)
       {
           if(sideSelected == 0)//leftside selected
           {
               int offset=0;
               for(String nodeName : sideToInsert.getPathFromRoot( ))
               {
                   tertiaryRing.getNodes( ).add((posLeftSide+1)+offset, QaRsapUtils.getNodeById( nodeName, network.getNetworkStructure( ).getNodes( ).getNode( ) ));
                   offset++;
               }
               for(int k=0; k<dn.getPathFromRoot( ).size( )-1; k++)
               {
                   String thisName = dn.getPathFromRoot( ).get( k );
                   tertiaryRing.getNodes( ).add((posLeftSide+1)+offset, QaRsapUtils.getNodeById( thisName, network.getNetworkStructure( ).getNodes( ).getNode( ) ));
                   offset++;
               }
               if(tertiaryRing.getSpecificNode(0) != tertiaryRing.getSpecificNode(tertiaryRing.getSize( )-1))
               {
                   tertiaryRing.getNodes().add(tertiaryRing.getSize( ),tertiaryRing.getSpecificNode(0) );
               }  
           }
           else if(sideSelected == 1) //Rightside selected
           {
               int offset=0;
               for(int k=dn.getPathFromRoot( ).size( )-2; k>=0; k--)
               {
                   String thisName = dn.getPathFromRoot( ).get( k );
                   tertiaryRing.getNodes( ).add((posOnTRing+1)+offset, QaRsapUtils.getNodeById( thisName, network.getNetworkStructure( ).getNodes( ).getNode( ) ));
                   offset++;
               }
               tertiaryRing.getNodes( ).add((posOnTRing+1)+offset, randNode);
               offset++;
               for(int k=0; k<sideToInsert.getPathFromRoot( ).size( )-1; k++)
               {
                   String thisName = dn.getPathFromRoot( ).get( k );
                   tertiaryRing.getNodes( ).add((posOnTRing+1)+offset, QaRsapUtils.getNodeById( thisName, network.getNetworkStructure( ).getNodes( ).getNode( ) ));
                   offset++;
               }
               if(tertiaryRing.getSpecificNode(0) != tertiaryRing.getSpecificNode(tertiaryRing.getSize( )-1))
               {
                   tertiaryRing.getNodes().add(tertiaryRing.getSize( ),tertiaryRing.getSpecificNode(0) );
               }  
           }
           succeeded = true;
       }
       else
       {
           succeeded = false;
       }
       return succeeded;
    }
    
    private int findNodePosOnRing(Ring srcRing, Node currentNode)
    {
        int positionOfcurrentAdjNode = -1;
        for ( int i = 0; i < srcRing.getNodes( ).size( ); i++ )
        {
            Node node = srcRing.getNodes( ).get( i );
            if ( node == currentNode )
            {
                positionOfcurrentAdjNode = i;
                break;
            }
        }
        return positionOfcurrentAdjNode;
    }
    
    public Solution splitLocalSearch(Solution sol, NodeAdjacencies adjList)
    {
        Solution clonedSol = sol.clone( );
        Ring spiltCandidate = null;
        Ring clonedSpiltCandidate = null;
        for(Ring ring : clonedSol.getLocalrings( ))
        {
            if(ring.getSize( ) >= Constants.MIN_SIZE_FOR_SPLIT )
            {
                spiltCandidate = ring;
                break;
            }
        }
        if(spiltCandidate != null)
        {
            clonedSpiltCandidate = spiltCandidate.clone();
            clonedSpiltCandidate.getNodes( ).remove(clonedSpiltCandidate.getSize( )-1);
            
            boolean solutionFound = false;
            Ring firstNewRing = null;
            
            for(int i=0; i<clonedSpiltCandidate.getSize( )-3; i++)
            {
                for(int j=i+2; j<clonedSpiltCandidate.getSize( )-3; j++)
                {
                    Node firstRingNode1 = clonedSpiltCandidate.getSpecificNode(i);
                    Node firstRingNode2 = clonedSpiltCandidate.getSpecificNode(j);
                    
                    Node secondRingNode1 = null;
                    if(i!=0)
                        secondRingNode1 = clonedSpiltCandidate.getSpecificNode(i-1);
                    else
                        secondRingNode1 = clonedSpiltCandidate.getSpecificNode(clonedSpiltCandidate.getSize()-1);
                    
                    Node secondRingNode2 = clonedSpiltCandidate.getSpecificNode(j+1);
                    
                    if(QaRsapUtils.isAdj( firstRingNode1.getId( ), adjList.getAdjList(firstRingNode2.getId( ))))
                    {
                        if(QaRsapUtils.isAdj( secondRingNode1.getId( ), adjList.getAdjList(secondRingNode2.getId( ))))
                        {
                            //Build first new ring
                            firstNewRing = new Ring();
                            for(int k=i; k<=j; k++)
                            {
                                firstNewRing.getNodes( ).add(clonedSpiltCandidate.getNodes( ).get( k ));  
                            }
                            for(int k=0; k<firstNewRing.getNodes( ).size( ); k++)
                            {
                                clonedSpiltCandidate.removeNode(firstNewRing.getSpecificNode(k));
                            }
                            
                            firstNewRing.getNodes( ).add(firstNewRing.getNodes( ).get(i));
                            clonedSol.getLocalrings( ).add(firstNewRing);
                            
                            //The remainder is the second new ring
                            clonedSpiltCandidate.addNode( clonedSpiltCandidate.getNodes().get(0));
                            clonedSol.getLocalrings( ).add(clonedSpiltCandidate);
                            
                            //Remove original split candidate ring
                            clonedSol.getLocalrings( ).remove(spiltCandidate);
                            solutionFound = true;
                            break;
                        }
                    }
                }
                if(solutionFound)
                    break;
            }  
            if(solutionFound)
            {
                if(clonedSol.getTertiaryRing( ) == null)
                {
                    List<Node> tempNodeList = new ArrayList<Node>(network.getNetworkStructure( ).getNodes( ).getNode());
                    List<Spur> spurs = sol.getSpurs();
                    for(Spur spur : spurs)
                    {
                        tempNodeList.remove(spur.getSpurNode());
                    }
                    SolutionGenerator solGenerator = new SolutionGenerator(network, nodeAdjacencies);
                    Ring tertiaryRing = solGenerator.generateTertiaryRing(clonedSol.getSpurs( ), clonedSol.getLocalrings(), tempNodeList);
                    if(tertiaryRing!=null)
                    {
                         clonedSol.setTertiaryRing(tertiaryRing);
                    }
                    else
                    {
                        System.out.println( "Failed to find tertiary ring on local ring split search" );
                        return sol; //No change in solution
                    }    
                }
                else
                {
                    boolean firstRingConnected = false; //Connected to tertiary
                    boolean clonedSplitCandidateconnected = false; //Connected to tertiary
                    
                    for(Node node : firstNewRing.getNodes( ))
                    {
                        if(QaRsapUtils.isNodeOnRing( node, clonedSol.getTertiaryRing( )))
                        {
                            firstRingConnected = true;
                            break;
                        }
                    }
                    for(Node node : clonedSpiltCandidate.getNodes( ))
                    {
                        if(QaRsapUtils.isNodeOnRing( node, clonedSol.getTertiaryRing( )))
                        {
                            clonedSplitCandidateconnected = true;
                            break;
                        }
                    }
                    if(!firstRingConnected) 
                    {
                        addLocalRingToTertiary(clonedSol.getTertiaryRing( ), firstNewRing, new Random(), clonedSol.getSpurs( ));  
                    }
                    
                    if(!clonedSplitCandidateconnected) 
                    {
                        addLocalRingToTertiary(clonedSol.getTertiaryRing( ), clonedSpiltCandidate, new Random(), clonedSol.getSpurs( ));  
                    }
                }
            }
        }
      
        return clonedSol;
    }

}
