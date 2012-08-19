/**
 * Created on 28 Jul 2012
 *
 * @author Michael Flynn
 * @version 1.0
 *
 * File: Bro.java
 * Package ie.ucd.mscba.qa_rsap.ui
 * Project QA_RSAP
 */

package ie.ucd.mscba.qa_rsap.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;
import org.openstreetmap.gui.jmapviewer.OsmMercator;
import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.MapQuestOsmTileSource;

import de.zib.sndlib.network.Node;
import de.zib.sndlib.network.Nodes;

/**
 * 
 */
public class UIComponentMap
{
    private JMapViewer map          = null;
    private boolean  mapMaximized = false;
    private JLabel helpLabel  = null;
    
    /**
     * @return the map
     */
    public JMapViewer getMap()
    {
        return map;
    }

    public void buildMapComponent(final SashForm displaySash, final SashForm menuSash, final Display display)
    {
        Composite composite = new Composite(displaySash, SWT.EMBEDDED | SWT.NO_BACKGROUND);
        Frame frame = SWT_AWT.new_Frame(composite);
        map = new JMapViewer();
        map.setTileSource((TileSource) new MapQuestOsmTileSource());

        frame.setLayout(new BorderLayout());

        JPanel helpPanel = new JPanel();
        helpPanel.setLayout(new BorderLayout());
        frame.add(helpPanel, BorderLayout.SOUTH);

        JButton butt_SetDisplay = new JButton("Fit Markers");
        butt_SetDisplay.addActionListener(new ActionListener()
        {

            public void actionPerformed(ActionEvent e)
            {
                map.setDisplayToFitMapMarkers();
            }
        });
        helpPanel.add(butt_SetDisplay, BorderLayout.WEST);

        helpLabel = new JLabel("Use right mouse button to move,\n " + "left double click or mouse wheel to zoom.");
        helpLabel.setHorizontalAlignment(SwingConstants.CENTER);
        helpPanel.add(helpLabel, BorderLayout.CENTER);

        final JButton butt_fullScreen = new JButton("Maxamize Map");
        butt_fullScreen.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                display.syncExec(new Runnable()
                {
                    public void run()
                    {
                        if(! displaySash.isDisposed() && ! menuSash.isDisposed())
                        {
                            if(! mapMaximized)
                            {
                                menuSash.setWeights(new int[] {0, 1});
                                displaySash.setWeights(new int[] {1, 0});
                                butt_fullScreen.setText("Reduce Map");
                                mapMaximized = true;
                            }
                            else
                            {
                                menuSash.setWeights(new int[] {1, 2});
                                displaySash.setWeights(new int[] {2, 1});
                                butt_fullScreen.setText("Maxamize Map");
                                mapMaximized = false;
                            }
                        }
                    }
                });
            }
        });
        helpPanel.add(butt_fullScreen, BorderLayout.EAST);

        frame.add(map);
    }
    
    public void setMapLabel(String msg, Color color)
    {
        helpLabel.setText(msg);
        helpLabel.setForeground(color);
    }
    
    public void loadNodesOnMap(Nodes networkNodes)
    {
        map.removeAllMapMarkers();
        map.removeAllMapPolygons();
        if(!"Pixel".equalsIgnoreCase(networkNodes.getCoordinatesType()))
        {
            List<Node> allNetworkNodes = networkNodes.getNode();
            for(int i=0; i<allNetworkNodes.size(); i++)
            {
                Node currentNode = allNetworkNodes.get( i );
                double yCoord = currentNode.getCoordinates().getY().doubleValue();
                double xCoord = currentNode.getCoordinates().getX().doubleValue();
                map.addMapMarker(new MapMarkerDot(yCoord, xCoord));
            }
        }
    }
}
