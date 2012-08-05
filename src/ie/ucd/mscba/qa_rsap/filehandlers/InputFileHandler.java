/**
 * Created on 7 Jul 2012
 *
 * @author Michael Flynn
 * @version 1.0
 *
 * File: InputFileHandler.java
 * Package ie.ucd.mscba.qa_rsap.filehandlers
 * Project QA_RSAP
 */
package ie.ucd.mscba.qa_rsap.filehandlers;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import de.zib.sndlib.network.Network;

/**
 * 
 */
public class InputFileHandler
{
    public Network loadInput(String fileName)
    {
        JAXBContext jc = null;
        Unmarshaller unmarshaller = null;
        Network network = null;
        
        try
        {
            jc = JAXBContext.newInstance("de.zib.sndlib.network");
            unmarshaller = jc.createUnmarshaller();
            JAXBElement element = (JAXBElement)unmarshaller.unmarshal(new File(fileName));
            network = (Network)element.getValue( );
        }
        catch ( JAXBException e )
        {
            System.out.println("Invalid input file: " + fileName);
            System.out.println(e.getMessage());
            return null;
        } 
        catch ( NullPointerException npe )
        {
            System.out.println("Invalid input file: " + fileName);
            System.out.println(npe.getMessage());
            return null;
        }
                
        return network;
    }
}
