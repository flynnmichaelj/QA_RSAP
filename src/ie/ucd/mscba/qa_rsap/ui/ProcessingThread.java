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

/**
 * This thread run the actual GA code. A new thread is created for this purpose so that the Event Dispatcher Thread
 * of the UI can continue to process UI events. This allows instant UI updates as the algorithm runs
 */
public class ProcessingThread implements Runnable 
{
    //private GraphPanel graphPanel;
    //private JButton kickoff;
    //private Properties gaProps; 
    //private Properties problemProps;
    private String fileName = null;
    private  QaRsapController controller;

    ProcessingThread(String fileName) 
    {
        this.fileName = fileName;
    }
    
    public void run ( ) 
    {
        controller = new QaRsapController();   
        if(fileName != null)
            controller.runQaRsap(fileName);
        else
            System.out.println("Error: No input file selected");
    }
    
    public void stop( ) 
    {
        //ga.stop();
    }
}