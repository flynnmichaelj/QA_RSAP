/**
 * Created on 27 Jul 2012
 *
 * @author Michael Flynn
 * @version 1.0
 *
 * File: MainWindow.java
 * Package ie.ucd.mscba.qa_rsap.ui
 * Project QA_RSAP
 */
package ie.ucd.mscba.qa_rsap.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class MainWindow
{
    
    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = new MainWindow().createShell(display);
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
    }
    
    public Shell createShell(final Display display) 
    {
        //Setup Allpication Shell
        final Shell shell = new Shell(display);
        shell.setText("Quantum Annealing Ring Spur Assignment");
        GridLayout shellLayout = new GridLayout();
        shell.setLayout(shellLayout);
        
        SashForm menuSash = new SashForm(shell, SWT.HORIZONTAL);
        GridLayout menuSashLayout = new GridLayout();
        menuSash.setLayout(menuSashLayout);
        menuSash.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, true ));
                
        //Add the menu bar component
        final UIComponentMenuBars menuBars = new UIComponentMenuBars();
        ExpandBar menubar =  menuBars.buildMenu(menuSash, display, shell);
        
        // Create the SashForm with vertical (Contains browser and results components)
        SashForm displaySash = new SashForm(menuSash, SWT.VERTICAL);
        GridLayout displaySashLayout = new GridLayout();
        displaySash.setLayout(menuSashLayout);
        displaySash.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, true ));
        
        //Add Browser Component
        UIComponentBrowser browserComponent = new UIComponentBrowser( );
        Browser browser = browserComponent.buildBrowserComponent( displaySash );
        
        //Add resultsPanelcomponent
        UIComponentResultsPanel resultsPanel = new UIComponentResultsPanel( );
        Text resultTest = resultsPanel.buildResultsPanel( displaySash );
        
        //Initilize appropiate sizes for display components.
        menuSash.setWeights( new int[] {1,2} );
        displaySash.setWeights( new int[] {2,1});
        
        Composite buttonHolder = new Composite( shell, SWT.NONE );
        FillLayout fillLayout = new FillLayout();
        fillLayout.type = SWT.HORIZONTAL;
        buttonHolder.setLayout(fillLayout);
        Button start_Butt = new Button(buttonHolder, SWT.PUSH);
        start_Butt.setText("Start");
        start_Butt.addSelectionListener(new SelectionAdapter() 
        {
            public void widgetSelected(SelectionEvent e) 
            {
                ProcessingThread t = new ProcessingThread(menuBars.getFileName( ));
                new Thread( t ).start( );            
            }
        });
        
        Button stop_Butt =  new Button(buttonHolder, SWT.PUSH);
        stop_Butt.setText("Stop");
        Button pause_butt = new Button(buttonHolder, SWT.PUSH);
        pause_butt.setText("Pause");
        
        return shell;
    }
}
