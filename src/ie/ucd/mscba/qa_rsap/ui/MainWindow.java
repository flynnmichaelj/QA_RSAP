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

import ie.ucd.mscba.qa_rsap.settings.AppSettings;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class MainWindow
{    
    private Button butt_start = null;
    private Button butt_stop = null;
    private ProgressBar progressBarO = null;
    private ProgressBar progressBarS = null;
    private Label lab_timer = null;
    private UIComponentMenuBars menuBars = null;
    private Text resultText = null;
    private ProcessingThread processThread = null;
    private UIComponentMap mapComponent = null;

    private long stratTime = 0;
    
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
        GridLayout shellLayout = new GridLayout(1, false);
        shell.setLayout(shellLayout);
        
        Composite buttonHolder = new Composite( shell, SWT.BORDER );
        GridLayout buttHolderLayout = new GridLayout(6, false);
        buttHolderLayout.marginLeft = buttHolderLayout.marginTop = buttHolderLayout.marginRight = buttHolderLayout.marginBottom = 0;
        buttHolderLayout.verticalSpacing = 10;
        buttHolderLayout.horizontalSpacing = 10;
        buttonHolder.setLayout(buttHolderLayout);
        buttonHolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        
        
        Label lab_contorolBar  = new Label (buttonHolder, SWT.NONE);
        lab_contorolBar.setText("");
        lab_contorolBar.setFont(new Font(display, "Ariel", 10, 1));
        lab_contorolBar.setLayoutData(new GridData(80, 25));
        butt_start = new Button(buttonHolder, SWT.PUSH);
        butt_start.setText("Start");
        butt_start.setEnabled(false);
        butt_start.setLayoutData(new GridData(100, 25));
        butt_stop = new Button(buttonHolder, SWT.PUSH);
        butt_stop.setText("Stop");
        butt_stop.setEnabled(false);
        butt_stop.setLayoutData(new GridData(100, 25));
        
        Label separator = new Label(buttonHolder, SWT.VERTICAL | SWT.SEPARATOR);
        separator.setLayoutData(new GridData(3, 25));
        
        Composite pcomposite = new Composite (buttonHolder, SWT.NONE);
        GridLayout pLayout = new GridLayout (2, true);
        pLayout.marginLeft = pLayout.marginTop = pLayout.marginRight = pLayout.marginBottom = 0;
        pLayout.verticalSpacing = 5;
        pLayout.horizontalSpacing = 5;
        pcomposite.setLayout(pLayout);
        
        Label lab_progressBarO  = new Label (pcomposite, SWT.NONE);
        lab_progressBarO.setText("Overall Progress");
        
        progressBarO = new ProgressBar(pcomposite, SWT.SMOOTH);
        progressBarO.setState(SWT.NORMAL);
        progressBarO.setMinimum(0);
        progressBarO.setMaximum(100);
        progressBarO.setSelection(0);
        GridData lb_progressOgd = new GridData(100, 15);
        progressBarO.setLayoutData(lb_progressOgd);
        
        Label lab_progressBarS  = new Label (pcomposite, SWT.NONE);
        lab_progressBarS.setText("Search Progress");
        
        progressBarS = new ProgressBar(pcomposite, SWT.SMOOTH);
        progressBarS.setState(SWT.NORMAL);
        progressBarS.setMinimum(0);
        progressBarS.setMaximum(100);
        progressBarS.setSelection(0);
        GridData lb_progressSgd = new GridData(100, 15);
        progressBarS.setLayoutData(lb_progressSgd);
        
        lab_timer  = new Label (buttonHolder, SWT.NONE);
        lab_timer.setText("Total runtime : ...");
        lab_timer.setFont(new Font(display, "Ariel", 10, 1));
        lab_timer.setLayoutData(new GridData(200, 25));
        
        SashForm menuSash = new SashForm(shell, SWT.HORIZONTAL);
        GridLayout menuSashLayout = new GridLayout();
        menuSash.setLayout(menuSashLayout);
        menuSash.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, true ));
                
        //Add the menu bar component
        menuBars = new UIComponentMenuBars();
        ExpandBar menubar =  menuBars.buildMenu(menuSash, display, shell, butt_start);
        
        // Create the SashForm with vertical (Contains browser and results components)
        SashForm displaySash = new SashForm(menuSash, SWT.VERTICAL);
        GridLayout displaySashLayout = new GridLayout();
        displaySash.setLayout(menuSashLayout);
        displaySash.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, true ));
        
        //Add Map Component
        mapComponent = new UIComponentMap( );
        mapComponent.buildMapComponent( displaySash, menuSash, display );
        menuBars.setMapComponent(mapComponent);
        
 
        
        //Add resultsPanelcomponent
        UIComponentResultsPanel resultsPanel = new UIComponentResultsPanel( );
        resultText = resultsPanel.buildResultsPanel( displaySash );
        
        //Initilize appropiate sizes for display components.
        menuSash.setWeights( new int[] {1,2} );
        displaySash.setWeights( new int[] {2,1});
        
        butt_start.addSelectionListener(new SelectionAdapter() 
        {
            public void widgetSelected(SelectionEvent e) 
            {
                progressBarO.setSelection(0);
                progressBarS.setSelection(0);
                butt_stop.setEnabled(true);
                butt_start.setEnabled(false);
                mapComponent.getMap().removeAllMapPolygons();
                lab_timer.setText("Total runtime : ... ");
                stratTime = System.currentTimeMillis();
                AppSettings appSettings= menuBars.collectSettings();
                processThread = new ProcessingThread(menuBars.getNetwork(), appSettings);
                processThread.setUIComponents(butt_start, butt_stop, resultText, display, progressBarO, progressBarS, stratTime, lab_timer, mapComponent.getMap());
                //new Thread( processThread ).start( );  
                //processThread.allDone = true;
                processThread.start();         
            }
        });
        butt_stop.addSelectionListener(new SelectionAdapter() 
        {
            public void widgetSelected(SelectionEvent e) 
            {
                processThread.stopProcess();
                butt_stop.setEnabled(false);
                butt_start.setEnabled(true);
                progressBarO.setSelection(100);
                progressBarS.setSelection(100);
                resultText.append("\r\n Process Stoped by user");         
            }
        });
        return shell;
    }
    
  }
