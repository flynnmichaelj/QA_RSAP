/**
 * Created on 7 Jul 2012
 *
 * @author Michael Flynn
 * @version 1.0
 *
 * File: GraphView.java
 * Package ie.ucd.mscba.qa_rsap.ui
 * Project QA_RSAP
 */
package ie.ucd.mscba.qa_rsap.ui;

import ie.ucd.mscba.qa_rsap.valueobjects.Ring;
import ie.ucd.mscba.qa_rsap.valueobjects.Solution;
import ie.ucd.mscba.qa_rsap.valueobjects.Spur;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.math.BigDecimal;
import java.util.List;

import javax.swing.JApplet;
import javax.swing.JFrame;

import org.jgraph.JGraph;
import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.DefaultEdge;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.GraphConstants;
import org.jgrapht.DirectedGraph;
import org.jgrapht.ListenableGraph;
import org.jgrapht.ext.JGraphModelAdapter;
import org.jgrapht.graph.DefaultListenableGraph;
import org.jgrapht.graph.DirectedMultigraph;
import org.jgrapht.graph.ListenableUndirectedGraph;

import de.zib.sndlib.network.Node;


/**
 * 
 */
public class GraphView extends JApplet
{
    private JGraphModelAdapter<String, DefaultEdge> jgAdapter;
    
    private static final long serialVersionUID = 3256444702936019250L;
    private static final Color DEFAULT_BG_COLOR = Color.decode("#FAFBFF");
    private static final Dimension DEFAULT_SIZE = new Dimension(580, 520);
    
    private Solution sol; 
    

    public void drawGraph(Solution sol)
    {
        GraphView applet = new GraphView();
        applet.init(sol);

        JFrame frame = new JFrame();
        frame.getContentPane().add(applet);
        frame.setTitle("QA RSAP Graph");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
    
    public void init(Solution sol)
    {
        // create a JGraphT graph
        ListenableGraph<String, DefaultEdge> g =
            new ListenableUndirectedGraph<String, DefaultEdge>(
                DefaultEdge.class);

        // create a visualization using JGraph, via an adapter
        jgAdapter = new JGraphModelAdapter<String, DefaultEdge>(g);

        JGraph jgraph = new JGraph(jgAdapter);
        //jgraph.
        adjustDisplaySettings(jgraph);
        getContentPane().add(jgraph);
        resize(DEFAULT_SIZE);

        List<Ring> rings = sol.getLocalrings( );

        for(int i=0; i<rings.size( ); i++)
        {
            Ring currentRing = rings.get(i);
            List<Node> nodes = currentRing.getNodes();
            for(int j=0; j<nodes.size( ); j++)
            {   
                Node currentNode = nodes.get( j );
                g.addVertex(currentNode.getId());
                positionVertexAt(currentNode.getId(), normalizeX(currentNode.getCoordinates( ).getX( )), 
                                                      normalizeY(currentNode.getCoordinates( ).getY( )));
            }
            
            int counter = 0;
            while(counter < nodes.size( )-1)
            {
                String src = nodes.get(counter).getId();
                String target = nodes.get(counter+1).getId();
                g.addEdge(src, target);
                counter++;
            }
            
        }
        
        //Handle spurs
        List<Spur> spurs = sol.getSpurs( );
        for(int i=0; i<spurs.size( ); i++)
        {
            Spur currentSpur = spurs.get(i);
            g.addVertex(currentSpur.getSpurNode( ).getId( ));
            
            positionVertexAt(currentSpur.getSpurNode( ).getId( ), normalizeX(currentSpur.getSpurNode( ).getCoordinates( ).getX( )), 
                                                                  normalizeY(currentSpur.getSpurNode( ).getCoordinates( ).getY( )));
            g.addEdge(currentSpur.getParentNode( ).getId( ), 
                      currentSpur.getSpurNode( ).getId( ));

        }


        // position vertices nicely within JGraph component
        //positionVertexAt(v1, 130, 40);
        //positionVertexAt(v2, 60, 200);
        //positionVertexAt(v3, 310, 230);
        //positionVertexAt(v4, 380, 70);

        // that's all there is to it!...
    }
    
    private int normalizeX(BigDecimal input)
    {
        int output = 0;
        double temp = input.doubleValue( );
        double temp2 = (temp-6.57)/(13.18-6.57);
        double temp3 = temp2*((100-1)+1);
        output = (int)(temp3*4);
        return output;
        
    }
    
    private int normalizeY(BigDecimal input)
    {
        int output = 0;
        double temp = input.doubleValue( );
        double temp2 = (temp-48.08)/(53.34-48.08);
        double temp3 = temp2*((100-1)+1);
        output = (int)(temp3*4);
        return output;
        
    }
    
    private void adjustDisplaySettings(JGraph jg)
    {
        jg.setPreferredSize(DEFAULT_SIZE);

        Color c = DEFAULT_BG_COLOR;
        String colorStr = null;

        try {
            colorStr = getParameter("bgcolor");
        } catch (Exception e) {
        }

        if (colorStr != null) {
            c = Color.decode(colorStr);
        }

        jg.setBackground(c);
    }
    
    @SuppressWarnings("unchecked") // FIXME hb 28-nov-05: See FIXME below
    private void positionVertexAt(Object vertex, int x, int y)
    {
        DefaultGraphCell cell = jgAdapter.getVertexCell(vertex);
        AttributeMap attr = cell.getAttributes();
        Rectangle2D bounds = GraphConstants.getBounds(attr);

        Rectangle2D newBounds =
            new Rectangle2D.Double(
                x,
                y,
                bounds.getWidth(),
                bounds.getHeight());

        GraphConstants.setBounds(attr, newBounds);

        // TODO: Clean up generics once JGraph goes generic
        AttributeMap cellAttr = new AttributeMap();
        cellAttr.put(cell, attr);
        jgAdapter.edit(cellAttr, null, null, null);
    }
    
  //~ Inner Classes ----------------------------------------------------------

    /**
     * a listenable directed multigraph that allows loops and parallel edges.
     */
    private static class ListenableDirectedMultigraph<V, E>
        extends DefaultListenableGraph<V, E>
        implements DirectedGraph<V, E>
    {
        private static final long serialVersionUID = 1L;

        ListenableDirectedMultigraph(Class<E> edgeClass)
        {
            super(new DirectedMultigraph<V, E>(edgeClass));
        }
    }

}
