/**
 * Created on 20 Feb 2011
 *
 * @author Michael Flynn
 * @version 1.0
 *
 * File: ProcessingThread.java
 * Package ui
 * Project nsm_Flynn
 */
package ie.ucd.mscba.qa_rsap.ui;

import ie.ucd.mscba.qa_rsap.QaRsapController;
import ie.ucd.mscba.qa_rsap.settings.AppSettings;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Text;
import org.openstreetmap.gui.jmapviewer.JMapViewer;

import de.zib.sndlib.network.Network;

/**
 * This thread run the actual GA code. A new thread is created for this purpose so that the Event Dispatcher Thread
 * of the UI can continue to process UI events. This allows instant UI updates as the algorithm runs
 */
public class ProcessingThread  extends Thread //implements Runnable
{
    private Network network                 = null;
    private QaRsapController controller     = null;
    private AppSettings appSettings         = null;
    private Text outputArea                 = null;
    private Display display                 = null;
    private ProgressBar progressBarO        = null;
    private ProgressBar progressBarS        = null;
    private Label lab_Timer                 = null;
    private JMapViewer map                  = null;
    
    private Button start = null; 
    private Button stop  = null;

    private boolean allDone  = false;
    private long startTime = 0;
    
    /**
     * @return the allDone
     */
    public void  stopProcess()
    {
        controller.stop(true);
    }
    public void setUIComponents(Button start, Button stop, Text outputArea, Display display, 
                                ProgressBar progressBarO,  ProgressBar progressBarS, Long startTime, Label lab_Timer, JMapViewer map)
    {
        this.start = start;
        this.stop = stop;
        this.outputArea = outputArea;
        this.display = display;
        this.progressBarO = progressBarO;
        this.progressBarS = progressBarS;
        this.startTime = startTime;
        this.lab_Timer = lab_Timer;
        this.map = map;
        
    }

    ProcessingThread(Network network, AppSettings appSettings) 
    {
        this.network = network;
        this.appSettings = appSettings;
    }
    
    public void run () 
    {
        while (true) 
        {
            if (allDone) {
                return;
            }
            
            controller = new QaRsapController();   
            if(network != null)
            {
                controller.setUIComponents(outputArea, display, progressBarO, progressBarS, map);
                controller.runQaRsap(network, appSettings);
            }
            else
                System.out.println("Error: Invalid network");
            
            
            display.asyncExec (new Runnable () {
                public void run () {
                   if (!start.isDisposed() && !stop.isDisposed())
                   {
                       start.setEnabled(true);
                       stop.setEnabled(false);
                       long runtime = System.currentTimeMillis() - startTime;
                       
                       int seconds = (int) (runtime / 1000) % 60 ;
                       int minutes = (int) ((runtime / (1000*60)) % 60);
                       String sSeconds = null;
                       String sMinutes = null;
                       if(seconds == 0)
                           sSeconds = "00";
                       else if(seconds < 9)
                           sSeconds = "0"+String.valueOf(seconds);
                       else
                           sSeconds = String.valueOf(seconds);
                       if(minutes == 0)
                           sMinutes = "00";
                       else if(minutes < 9)
                           sMinutes = "0"+String.valueOf(minutes);
                       else
                           sMinutes = String.valueOf(minutes);
                       lab_Timer.setText("Total runtime : " + sMinutes +":" + sSeconds);
                   }
                }
             });
            allDone = true;
           
        }
        
    }
}