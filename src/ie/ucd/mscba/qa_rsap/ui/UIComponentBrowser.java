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

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.SashForm;

/**
 * 
 */
public class UIComponentBrowser
{

        public Browser buildBrowserComponent(SashForm displaySash)
        {
            Browser browser = null;
            try 
            {
                browser = new Browser(displaySash, SWT.NONE);
            } 
            catch (SWTError e) 
            {
               System.out.println( "Errro crearting browser component" );
               System.out.println( e.getMessage( ));
               e.printStackTrace( );
            }
            if (browser != null) 
            {
                browser.setUrl("about:blank");
            }
            return browser;
        }
}
