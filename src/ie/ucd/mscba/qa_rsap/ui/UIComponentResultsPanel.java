/**
 * Created on 28 Jul 2012
 *
 * @author Michael Flynn
 * @version 1.0
 *
 * File: ResultsPanelcomponent.java
 * Package ie.ucd.mscba.qa_rsap.ui
 * Project QA_RSAP
 */
package ie.ucd.mscba.qa_rsap.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Text;

/**
 * 
 */
public class UIComponentResultsPanel
{
    public Text buildResultsPanel(SashForm displaySash)
    {
        final Text outputArea = new Text(displaySash, SWT.BORDER | SWT.MULTI |  SWT.V_SCROLL | SWT.WRAP);       
        return outputArea;
    }   
}
