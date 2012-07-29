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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * 
 */
public class UIComponentMenuBars 
{
    private String fileName = null;;
    private Label fileValue  = null;
    
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
            this.fileValue.setText(fileName);
        else
            this.fileValue.setText("No input file selected");
    }

    public ExpandBar buildMenu(Composite sashForm, Display display, final Shell shell )
    {
        ExpandBar bar = new ExpandBar (sashForm, SWT.V_SCROLL); 
        //GridData gridData = new GridData(GridData.FILL, GridData.FILL, false, true);
        //bar.setLayoutData(gridData);
        
        // First item
        Composite composite = new Composite (bar, SWT.NONE);
        GridLayout layout = new GridLayout ();
        layout.marginLeft = layout.marginTop = layout.marginRight = layout.marginBottom = 10;
        layout.verticalSpacing = 10;
        composite.setLayout(layout);
        fileValue = new Label (composite, SWT.NONE);
        fileValue.setLayoutData( new GridData(SWT.FILL, SWT.FILL, true, true) );
        if(fileName != null)
            fileValue.setText(fileName);
        else
            fileValue.setText("No input file selected");

        Button button = new Button(composite, SWT.PUSH);
        button.setText("Browse");
        button.addSelectionListener(new SelectionAdapter() 
        {
            public void widgetSelected(SelectionEvent e) 
            {
                FileDialog dialog = new FileDialog(shell, SWT.NONE );
                dialog.setText( "Please pick an input file (XML Format)" );
                dialog.setFilterExtensions( new String[] {"*.xml"} );
                dialog.setFilterPath("C:/Users/Mike/workspace/QA_RSAP/resources");
                String path = dialog.open();
                setFileName( path );
            }
        });

        ExpandItem item0 = new ExpandItem (bar, SWT.NONE, 0);
        item0.setText("Problem properties");
        item0.setHeight(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
        item0.setControl(composite);
        item0.setImage(display.getSystemImage(SWT.ICON_WORKING));
 
        // Second item
        composite = new Composite (bar, SWT.NONE);
        layout = new GridLayout (2, false);
        layout.marginLeft = layout.marginTop = layout.marginRight = layout.marginBottom = 10;
        layout.verticalSpacing = 10;
        composite.setLayout(layout);    
        ExpandItem item1 = new ExpandItem (bar, SWT.NONE, 1);
        item1.setText("VNS Properties");
        item1.setHeight(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
        item1.setControl(composite);
        item1.setImage(display.getSystemImage(SWT.ICON_WORKING));
        
        // Third item
        composite = new Composite (bar, SWT.NONE);
        layout = new GridLayout (2, true);
        layout.marginLeft = layout.marginTop = layout.marginRight = layout.marginBottom = 10;
        layout.verticalSpacing = 10;
        composite.setLayout(layout);
        ExpandItem item2 = new ExpandItem (bar, SWT.NONE, 2);
        item2.setText("QA properties");
        item2.setHeight(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
        item2.setControl(composite);
        item2.setImage(display.getSystemImage(SWT.ICON_WORKING));
        
        item1.setExpanded(true);
        item0.setExpanded(true);
        item2.setExpanded(true);
        
        bar.setSpacing(8);

        return bar;
    }
}
