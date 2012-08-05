/**
 * Created on 28 Jul 2012
 *
 * @author Michael Flynn
 * @version 1.0
 *
 * File: QAMenuBars.java
 * Package ie.ucd.mscba.qa_rsap.ui
 * Project QA_RSAP
 */
package ie.ucd.mscba.qa_rsap.ui;

import ie.ucd.mscba.qa_rsap.filehandlers.InputFileHandler;
import ie.ucd.mscba.qa_rsap.settings.AnnealSettings;
import ie.ucd.mscba.qa_rsap.settings.AppSettings;
import ie.ucd.mscba.qa_rsap.settings.VNSSettings;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;

import de.zib.sndlib.network.AdditionalModules;
import de.zib.sndlib.network.Network;

/**
 * 
 */
public class UIComponentMenuBars 
{
    //UI Components
    private CLabel lab_fileValue            = null;
    private Combo com_initMaxLRS            = null;
    private Combo com_maxLRS                = null;
    private Combo com_capMod                = null;
    private Combo com_numSearches           = null;
    private Combo com_trotSlices            = null;
    private Combo com_QAFluctSteps          = null;
    private Spinner spin_spurPen            = null;
    private Spinner spin_initQAFluctSpin    = null;
    private Spinner spin_qaTemp             = null;
    private Spinner spin_mcsSteps           = null;
    private Combo com_saInitTemp            = null;
    private Combo com_saFinalTemp           = null;
    private Spinner spin_saTempDelta        = null;
    private Image img_tick                  = null;
    private Image img_error                 = null;
    private Image img_vnssearch             = null;
    private Image img_network               = null;
    private Image img_anneal                = null;
    
    //Error location
    private Label lab_initMaxLRS_Valid      = null;
    private Label lab_maxLRS_Valid          = null;
    private Label lab_capMod_Valid          = null;
    private Label lab_spurPen_Valid         = null;
    private Label lab_numSearches_Valid     = null;
    private Label lab_mcsSteps_Valid        = null;
    private Label lab_trotSlices_Valid      = null;
    private Label lab_initQAFluctSpin_Valid = null;
    private Label lab_QAFluctSteps_Valid    = null;
    private Label lab_qaTemp_Valid          = null;
    private Label lab_saInitTemp_Valid      = null;
    private Label lab_saFinalTemp_Valid     = null;
    private Label lab_saTempDelta_Valid     = null;
    
    //Hold values of settings as they change
    private String fileName = null;
    private Network network = null;
    
    
    /*private int initMaxLRSize = 0;
    private int maxLocalRingSize = 0;
    private int capacityModule = 0;
    private int spurPenalty = 0;
    private int numSearches = 0;
    private int trotterSlices = 0;
    private int initQAfluct = 0;
    private double qaFluctSteps = 0;
    private double qaTemp = 0.0;
    private double mcsSteps = 0;
    private int initSATemp = 0;
    private int finalSATemp = 0;
    private double saDeltaTemp = 0.0;*/

    /**
     * @return the network
     */
    public Network getNetwork()
    {
        return network;
    }

    /**
     * @param network the network to set
     */
    public void setNetwork(Network network)
    {
        this.network = network;
    }

    public ExpandBar buildMenu(Composite sashForm, final Display display, 
                                final Shell shell, Network network, final Button butt_start )
    {
        ExpandBar bar = new ExpandBar (sashForm, SWT.V_SCROLL); 
        //================================================
        //Define images
        //================================================
        
       InputStream is = null; 
       try{
            is = this.getClass().getResourceAsStream("/images/tick.png");
            img_tick = new Image(display, is);
            is = this.getClass().getResourceAsStream("/images/error.png");
            img_error = new Image(display, is);
            is = this.getClass().getResourceAsStream("/images/anneal.png");
            img_anneal = new Image(display, is);
            is = this.getClass().getResourceAsStream("/images/VNSSearch.png");
            img_vnssearch = new Image(display, is);
            is = this.getClass().getResourceAsStream("/images/network.png");
            img_network = new Image(display, is);
        } 
        finally 
        {
                try 
                {
                    is.close();
                } 
                catch (IOException ioe){
                }
        }

       
        
        GridData imgData = new GridData(SWT.FILL, SWT.FILL, true, true);
        imgData.minimumHeight = 34;
        imgData.horizontalAlignment = GridData.FILL;
        
        //==============================================================
        //Problem UI Elements
        //==============================================================
        Composite composite = new Composite (bar, SWT.NONE);
        GridLayout layout = new GridLayout (2, false);
        layout.marginLeft = layout.marginTop = layout.marginRight = layout.marginBottom = 10;
        layout.verticalSpacing = 10;
        layout.horizontalSpacing = 20;
        composite.setLayout(layout);
        
        lab_fileValue = new CLabel (composite, SWT.NONE);
        GridData fileValueData = new GridData(SWT.FILL, SWT.FILL, true, true);
        fileValueData.minimumHeight = 34;
        fileValueData.horizontalSpan = 2;
        fileValueData.horizontalAlignment = GridData.FILL;
        lab_fileValue.setLayoutData(fileValueData);
        if(fileName != null)
            lab_fileValue.setText(fileName);
        else
            lab_fileValue.setText("No input file selected");
  
        Button butt_browse = new Button(composite, SWT.PUSH);
        butt_browse.setText("Browse");
        //GridData browseData = new GridData(GridData.VERTICAL_ALIGN_END);
        //browseData.horizontalSpan = 2;
       // browseData.horizontalAlignment = GridData.FILL;
        //butt_browse.setLayoutData(browseData);
        
        butt_browse.addSelectionListener(new SelectionAdapter() 
        {
            public void widgetSelected(SelectionEvent e) 
            {
                disableUIComponents(butt_start);
                FileDialog fd_dialog = new FileDialog(shell, SWT.NONE );
                fd_dialog.setText( "Please pick an input file (XML Format)" );
                fd_dialog.setFilterExtensions( new String[] {"*.xml"} );
                String path = fd_dialog.open();
                if(path!=null)
                {
                    if(loadandParseInputFile(path))
                    {
                        lab_fileValue.setForeground(display.getSystemColor (SWT.COLOR_DARK_GREEN));
                        setFileName( "Selected file \"" + path.substring(path.lastIndexOf("\\")+1) + "\" is valid");
                        lab_fileValue.setImage(img_tick);
                        enableUIComponents(butt_start); 
                    }
                    else
                    {
                        setFileName( "Selected file \"" + path.substring(path.lastIndexOf("\\")+1) + " \" is invalid");
                        lab_fileValue.setForeground(display.getSystemColor (SWT.COLOR_RED));
                        lab_fileValue.setImage(img_error);
                    }
                }
            }
        });

        ExpandItem item0 = new ExpandItem (bar, SWT.NONE, 0);
        item0.setText("Network properties");
        item0.setHeight(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
        item0.setControl(composite);
        item0.setImage(img_network);
 
        //==============================================================
        //VNS Properties UI Elements
        //==============================================================
        composite = new Composite (bar, SWT.NONE);
        layout = new GridLayout (3, false);
        layout.marginLeft = layout.marginTop = layout.marginRight = layout.marginBottom = 10;
        layout.verticalSpacing = 10;
        layout.horizontalSpacing = 10;
        composite.setLayout(layout);  
        
        Label lab_initMaxLRS  = new Label (composite, SWT.NONE);
        lab_initMaxLRS.setText("Initial max Local ring size");
        com_initMaxLRS = new Combo (composite, SWT.READ_ONLY);
        com_initMaxLRS.setLayoutData(new GridData(70, 25));
        com_initMaxLRS.setItems (new String [] {"3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15"});
        com_initMaxLRS.select(4);
        com_initMaxLRS.setEnabled(false);
        com_initMaxLRS.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                if(Integer.parseInt(com_maxLRS.getText()) <= Integer.parseInt(com_initMaxLRS.getText()))
                { 
                    lab_initMaxLRS_Valid.setImage(img_error);
                    lab_initMaxLRS_Valid.setToolTipText("Max local ring size must be greater than initial local ring size");
                    butt_start.setEnabled(false);
                }
                else
                {
                    lab_initMaxLRS_Valid.setImage(null);
                    lab_initMaxLRS_Valid.setToolTipText("");
                    lab_maxLRS_Valid.setImage(null);
                    lab_maxLRS_Valid.setToolTipText("");
                    butt_start.setEnabled(true);
                }
            }
          });
        lab_initMaxLRS_Valid  = new Label (composite, SWT.NONE);
        lab_initMaxLRS_Valid.setLayoutData(imgData);
        
        Label lab_maxLRS  = new Label (composite, SWT.NONE);
        lab_maxLRS.setText("Max local ring size");
        com_maxLRS = new Combo (composite, SWT.READ_ONLY);
        com_maxLRS.setLayoutData(new GridData(70, 25));
        com_maxLRS.setItems (new String [] {"3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17"});
        com_maxLRS.select(5);
        com_maxLRS.setEnabled(false);
        com_maxLRS.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
             if(Integer.parseInt(com_maxLRS.getText()) <= Integer.parseInt(com_initMaxLRS.getText()))
             { 
                 lab_maxLRS_Valid.setImage(img_error);
                 lab_maxLRS_Valid.setToolTipText("Max local ring size must be greater than initial local ring size");
                 butt_start.setEnabled(false);
             }
             else
             {
                 lab_maxLRS_Valid.setImage(null);
                 lab_maxLRS_Valid.setToolTipText("");
                 lab_initMaxLRS_Valid.setImage(null);
                 lab_initMaxLRS_Valid.setToolTipText("");
                 butt_start.setEnabled(true);
             }
            }
          });
        lab_maxLRS_Valid  = new Label (composite, SWT.NONE);
        lab_maxLRS_Valid.setLayoutData(imgData);
        
        Label lab_capMod  = new Label (composite, SWT.NONE); //TODO build Cap module
        lab_capMod.setText("Capacity module");
        com_capMod = new Combo (composite, SWT.READ_ONLY);
        com_capMod.setLayoutData(new GridData(70, 25));
        com_capMod.select(0);
        com_capMod.setEnabled(false);
        com_capMod.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
              //setCapacityModule(Integer.parseInt(com_capMod.getText()));          
            }
          });
        lab_capMod_Valid  = new Label (composite, SWT.NONE);
        lab_capMod_Valid.setLayoutData(imgData);
        
        Label lab_spurPen  = new Label (composite, SWT.NONE);
        lab_spurPen.setText("Spur penalty");
        spin_spurPen = new Spinner (composite, SWT.BORDER | SWT.READ_ONLY);
        spin_spurPen.setLayoutData(new GridData(70, 20));
        spin_spurPen.setMinimum(0);
        spin_spurPen.setMaximum(100);
        spin_spurPen.setSelection(7);
        spin_spurPen.setIncrement(1);
        spin_spurPen.setEnabled(false);
        com_capMod.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
              //setSpurPenalty(spin_spurPen.getSelection());          
            }
          });
        lab_spurPen_Valid  = new Label (composite, SWT.NONE);
        lab_spurPen_Valid.setLayoutData(imgData);
        
        ExpandItem item1 = new ExpandItem (bar, SWT.NONE, 1);
        item1.setText("VNS Properties");
        item1.setHeight(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
        item1.setControl(composite);
        //item1.setImage(display.getSystemImage(SWT.ICON_WORKING));
        item1.setImage(img_vnssearch);
        
        //==============================================================
        //Annealing Properties UI Elements
        //==============================================================
        composite = new Composite (bar, SWT.NONE);
        layout = new GridLayout (3, true);
        layout.marginLeft = layout.marginTop = layout.marginRight = layout.marginBottom = 10;
        layout.verticalSpacing = 10;
        layout.horizontalSpacing = 22;
        composite.setLayout(layout);
        
        Label lab_numSearches  = new Label (composite, SWT.NONE);
        lab_numSearches.setText("Number of searches");
        com_numSearches = new Combo (composite, SWT.READ_ONLY);
        com_numSearches.setLayoutData(new GridData(70, 25));
        com_numSearches.setItems (new String [] {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20"});
        com_numSearches.select(0);
        com_numSearches.setEnabled(false);
        com_numSearches.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
              //setNumSearches(Integer.parseInt(com_numSearches.getText()));          
            }
          });
        lab_numSearches_Valid  = new Label (composite, SWT.NONE);
        lab_numSearches_Valid.setLayoutData(imgData);
        
        Label lab_mcsSteps  = new Label (composite, SWT.NONE);
        lab_mcsSteps.setText("MCS Steps");
        spin_mcsSteps = new Spinner (composite, SWT.BORDER | SWT.READ_ONLY);
        spin_mcsSteps.setLayoutData(new GridData(70, 20));
        spin_mcsSteps.setMinimum(1);
        spin_mcsSteps.setMaximum(100);
        spin_mcsSteps.setSelection(25);
        spin_mcsSteps.setIncrement(1);
        spin_mcsSteps.setEnabled(false);
        spin_mcsSteps.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
              //setMcsSteps(spin_mcsSteps.getSelection());          
            }
          });
        lab_mcsSteps_Valid  = new Label (composite, SWT.NONE);
        lab_mcsSteps_Valid.setLayoutData(imgData);
        
        Label lab_qaDivider  = new Label(composite, SWT.NONE);
        lab_qaDivider.setText("Quantum Annealing Parameters");
        lab_qaDivider.setFont(new Font(display, "Ariel", 8, 1));
        GridData qaGridData = new GridData(GridData.VERTICAL_ALIGN_END);
        qaGridData.horizontalSpan = 3;
        qaGridData.horizontalAlignment = GridData.FILL;
        lab_qaDivider.setLayoutData(qaGridData);
        
        Label lab_trotSlices  = new Label (composite, SWT.NONE);
        lab_trotSlices.setText("Trotter slices");
        com_trotSlices = new Combo (composite, SWT.READ_ONLY);
        com_trotSlices.setLayoutData(new GridData(70, 25));
        com_trotSlices.setItems (new String [] {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"});
        com_trotSlices.select(5);
        com_trotSlices.setEnabled(false);
        com_trotSlices.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                if(Integer.parseInt(com_trotSlices.getText()) > 1)
                {         
                    com_QAFluctSteps.setEnabled(true);     
                    spin_initQAFluctSpin.setEnabled(true);      
                    spin_qaTemp.setEnabled(true);  
                    spin_saTempDelta.setEnabled(false);
                    com_saInitTemp.setEnabled(false);      
                    com_saFinalTemp.setEnabled(false);   
                }
                else
                {             
                    com_QAFluctSteps.setEnabled(false);     
                    spin_initQAFluctSpin.setEnabled(false);      
                    spin_qaTemp.setEnabled(false);  
                    spin_saTempDelta.setEnabled(true);
                    com_saInitTemp.setEnabled(true);      
                    com_saFinalTemp.setEnabled(true);      
                }            
            }
          });
        lab_trotSlices_Valid  = new Label (composite, SWT.NONE);
        lab_trotSlices_Valid.setLayoutData(imgData);
        
        Label lab_initQAFluct  = new Label (composite, SWT.NONE);
        lab_initQAFluct.setText("Initial QA Fluctations");
        spin_initQAFluctSpin = new Spinner (composite, SWT.BORDER | SWT.READ_ONLY);
        spin_initQAFluctSpin.setLayoutData(new GridData(70, 20));
        spin_initQAFluctSpin.setMinimum(1);
        spin_initQAFluctSpin.setMaximum(10);
        spin_initQAFluctSpin.setSelection(5);
        spin_initQAFluctSpin.setIncrement(1);
        spin_initQAFluctSpin.setEnabled(false);
        spin_initQAFluctSpin.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
              //setInitQAfluct(spin_initQAFluctSpin.getSelection());          
            }
          });
        lab_initQAFluctSpin_Valid  = new Label (composite, SWT.NONE);
        lab_initQAFluctSpin_Valid.setLayoutData(imgData);
        
        Label lab_QAFluctSteps  = new Label (composite, SWT.NONE);
        lab_QAFluctSteps.setText("QA Fluctations Steps");
        com_QAFluctSteps = new Combo (composite, SWT.READ_ONLY);
        com_QAFluctSteps.setLayoutData(new GridData(70, 25));
        com_QAFluctSteps.setItems (new String [] {"0.1", "0.2", "0.3", "0.4", "0.5", "0.6", "0.7", "0.8", "0.9", "1"});
        com_QAFluctSteps.select(1);
        com_QAFluctSteps.setEnabled(false);
        com_QAFluctSteps.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
              //setQaFluctSteps(Double.parseDouble(com_QAFluctSteps.getText()));          
            }
          });
        lab_QAFluctSteps_Valid  = new Label (composite, SWT.NONE);
        lab_QAFluctSteps_Valid.setLayoutData(imgData);
        
        Label lab_qaTemp  = new Label (composite, SWT.NONE);
        lab_qaTemp.setText("QA Temperature");
        spin_qaTemp = new Spinner(composite, SWT.BORDER | SWT.READ_ONLY);
        spin_qaTemp.setLayoutData(new GridData(70, 20));
        spin_qaTemp.setDigits(2);
        spin_qaTemp.setMinimum(1);
        spin_qaTemp.setMaximum(100);
        spin_qaTemp.setIncrement(1);
        spin_qaTemp.setSelection(042);
        spin_qaTemp.setEnabled(false);
        spin_qaTemp.addSelectionListener(new SelectionAdapter() {
          public void widgetSelected(SelectionEvent e) {
            //int selection = spin_qaTemp.getSelection();
            //int digits = spin_qaTemp.getDigits();
            //setQaTemp((spin_qaTemp.getSelection() / Math.pow(10, spin_qaTemp.getDigits())));
          }
        });
        lab_qaTemp_Valid  = new Label (composite, SWT.NONE);
        lab_qaTemp_Valid.setLayoutData(imgData);
        
        Label lab_saDivider  = new Label(composite, SWT.NONE);
        lab_saDivider.setText("Simulated Annealing Parameters");
        lab_saDivider.setFont(new Font(display, "Ariel", 8, 1));
        GridData saGridData = new GridData(GridData.VERTICAL_ALIGN_END);
        saGridData.horizontalSpan = 3;
        saGridData.horizontalAlignment = GridData.FILL;
        lab_saDivider.setLayoutData(qaGridData);
        
        Label lab_saInitTemp  = new Label (composite, SWT.NONE);;
        lab_saInitTemp.setText("SA Initital Temperature");
        com_saInitTemp = new Combo (composite, SWT.READ_ONLY);
        com_saInitTemp.setLayoutData(new GridData(70, 25));
        com_saInitTemp.setItems (new String [] {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"});
        com_saInitTemp.select(3);
        com_saInitTemp.setEnabled(false);
        com_saInitTemp.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                if(Integer.parseInt(com_saFinalTemp.getText()) >= Integer.parseInt(com_saInitTemp.getText()))
                { 
                    lab_saInitTemp_Valid.setImage(img_error);
                    lab_saInitTemp_Valid.setToolTipText("Initial temperature must be greater than final temperature");
                    butt_start.setEnabled(false);
                }
                else
                {
                    lab_saFinalTemp_Valid.setImage(null);
                    lab_saFinalTemp_Valid.setToolTipText("");
                    lab_saInitTemp_Valid.setImage(null);
                    lab_saInitTemp_Valid.setToolTipText("");
                    butt_start.setEnabled(true);
                }         
            }
          });
        lab_saInitTemp_Valid  = new Label (composite, SWT.NONE);
        lab_saInitTemp_Valid.setLayoutData(imgData);
        
        Label lab_saFinalTemp  = new Label (composite, SWT.NONE);;
        lab_saFinalTemp.setText("SA Final Temperature");
        com_saFinalTemp = new Combo (composite, SWT.READ_ONLY);
        com_saFinalTemp.setLayoutData(new GridData(70, 25));
        com_saFinalTemp.setItems (new String [] {"1", "2", "3", "4", "5"});
        com_saFinalTemp.select(1);
        com_saFinalTemp.setEnabled(false);
        com_saFinalTemp.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {     
                if(Integer.parseInt(com_saFinalTemp.getText()) >= Integer.parseInt(com_saInitTemp.getText()))
                { 
                    lab_saFinalTemp_Valid.setImage(img_error);
                    lab_saFinalTemp_Valid.setToolTipText("Initial temperature must be greater than final temperature");
                    butt_start.setEnabled(false);
                }
                else
                {
                    lab_saFinalTemp_Valid.setImage(null);
                    lab_saFinalTemp_Valid.setToolTipText("");
                    lab_saInitTemp_Valid.setImage(null);
                    lab_saInitTemp_Valid.setToolTipText("");
                    butt_start.setEnabled(true);
                }
            }
          });
        lab_saFinalTemp_Valid  = new Label (composite, SWT.NONE);
        lab_saFinalTemp_Valid.setLayoutData(imgData);
        
        Label lab_saTempDelta  = new Label (composite, SWT.NONE);
        lab_saTempDelta.setText("SA Delta Temperature");
        spin_saTempDelta = new Spinner(composite, SWT.BORDER | SWT.READ_ONLY);
        spin_saTempDelta.setLayoutData(new GridData(70, 20));
        spin_saTempDelta.setDigits(1);
        spin_saTempDelta.setMinimum(1);
        spin_saTempDelta.setMaximum(10);
        spin_saTempDelta.setIncrement(1);
        spin_saTempDelta.setSelection(01);
        spin_saTempDelta.setEnabled(false);
        spin_saTempDelta.addSelectionListener(new SelectionAdapter() {
          public void widgetSelected(SelectionEvent e) {
            //int selection = spin_saTempDelta.getSelection();
            //int digits = spin_saTempDelta.getDigits();
            //setSaDeltaTemp((spin_saTempDelta.getSelection() / Math.pow(10, spin_saTempDelta.getDigits())));
          }
        });
        lab_saTempDelta_Valid  = new Label (composite, SWT.NONE);
        lab_saTempDelta_Valid.setLayoutData(imgData);
        
        ExpandItem item2 = new ExpandItem (bar, SWT.NONE, 2);
        item2.setText("Annealing properties");
        item2.setHeight(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
        item2.setControl(composite);
        item2.setImage(img_anneal);
        
        item0.setExpanded(true);
        item1.setExpanded(false);
        item2.setExpanded(false);
        
        bar.setSpacing(8);

        return bar;
    }
    
    public AppSettings collectSettings()
    {
        AppSettings appSetting = new AppSettings();
               
        //Get VNS Setting
        VNSSettings vnsSettings = new VNSSettings();
        vnsSettings.setCapacityModule(com_capMod.getSelectionIndex());
        vnsSettings.setInitMaxLRSize(Integer.parseInt(com_initMaxLRS.getText())+1);
        vnsSettings.setMaxLocalRingSize(Integer.parseInt(com_maxLRS.getText())+1);
        vnsSettings.setSpurPenalty(spin_spurPen.getSelection());
        appSetting.setVnsSettings(vnsSettings);
        
        //Get Annealing Setting
        AnnealSettings annealSettings = new AnnealSettings();
        
        annealSettings.setMcsSteps(spin_mcsSteps.getSelection());
        annealSettings.setNumSearches(Integer.parseInt(com_numSearches.getText()));
        annealSettings.setTrotterSlices(Integer.parseInt(com_trotSlices.getText()));
        
        annealSettings.setInitQAfluct(spin_initQAFluctSpin.getSelection());
        annealSettings.setQaFluctSteps(Double.parseDouble(com_QAFluctSteps.getText()));
        annealSettings.setQaTemp((spin_qaTemp.getSelection() / Math.pow(10, spin_qaTemp.getDigits())));

        annealSettings.setInitSATemp(Integer.parseInt(com_saInitTemp.getText()));
        annealSettings.setFinalSATemp(Integer.parseInt(com_saFinalTemp.getText()));
        annealSettings.setSaDeltaTemp((spin_saTempDelta.getSelection() / Math.pow(10, spin_saTempDelta.getDigits())));
        
        appSetting.setAnnealSettings(annealSettings);
            
        return appSetting;
    }
    
    private void enableUIComponents(Button butt_Start)
    {
        butt_Start.setEnabled(true);
        com_initMaxLRS.setEnabled(true);    
        com_maxLRS.setEnabled(true);                   
        com_capMod.setEnabled(true);  
        spin_spurPen.setEnabled(true); 
        com_numSearches.setEnabled(true);    
        spin_mcsSteps.setEnabled(true);    
        com_trotSlices.setEnabled(true); 
        if(Integer.parseInt(com_trotSlices.getText()) > 1)
        {
                 
            com_QAFluctSteps.setEnabled(true);     
            spin_initQAFluctSpin.setEnabled(true);      
            spin_qaTemp.setEnabled(true);         
        }
        else
        {             
            spin_saTempDelta.setEnabled(true);
            com_saInitTemp.setEnabled(true);      
            com_saFinalTemp.setEnabled(true);     
        }     
    }
    
    private void disableUIComponents(Button butt_Start)
    {
        butt_Start.setEnabled(false);
        com_initMaxLRS.setEnabled(false);    
        com_maxLRS.setEnabled(false);                   
        com_capMod.setEnabled(false);  
        spin_spurPen.setEnabled(false); 
        com_numSearches.setEnabled(false);    
        spin_mcsSteps.setEnabled(false);    
        com_trotSlices.setEnabled(false);      
        com_QAFluctSteps.setEnabled(false);     
        spin_initQAFluctSpin.setEnabled(false);      
        spin_qaTemp.setEnabled(false);                   
        spin_saTempDelta.setEnabled(false);
        com_saInitTemp.setEnabled(false);      
        com_saFinalTemp.setEnabled(false);       
    }
    
    private boolean loadandParseInputFile(String fileName)
    {      
        boolean success = false;
        com_capMod.removeAll();
        InputFileHandler inFileHandler = new InputFileHandler();
        network = inFileHandler.loadInput(fileName);
        if(network!=null)
        {
            AdditionalModules additionalModules = network.getNetworkStructure().getLinks()
                            .getLink().get(0).getAdditionalModules();
            for(int i=0; i<additionalModules.getAddModule().size(); i++ )
            {
                BigDecimal moduleCapacity = additionalModules.getAddModule().get(i).getCapacity();
                com_capMod.add(String.valueOf(moduleCapacity.doubleValue()), i);
            }
            com_capMod.select(0);
            success = true; 
        }
        return success;
    }

    /**
     * @return the fileName
     */
    public String getFileName()
    {
        return fileName;
    }

    /**
     * @param fileName the fileName to set
     */
    public void setFileName(String fileName)
    {
        this.fileName = fileName;
        if(fileName != null)
            this.lab_fileValue.setText(fileName.substring(fileName.lastIndexOf("\\")+1));
        else
            this.lab_fileValue.setText("No input file selected");
    }
}
