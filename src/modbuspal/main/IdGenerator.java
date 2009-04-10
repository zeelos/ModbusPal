/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.main;

import java.io.IOException;
import java.io.OutputStream;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author nnovic
 */
class IdGenerator
{
    long idCreator = 0;

    long createID()
    {
        long newID = 0;
        synchronized(this)
        {
            newID = (++idCreator);
        }
        return newID;
    }

    void reset()
    {
        synchronized(this)
        {
            idCreator=0;
        }
    }

    void save(OutputStream out) throws IOException
    {
        StringBuffer idGenTag = new StringBuffer("<idgen ");
        synchronized(this)
        {
            idGenTag.append(" value=\""+ String.valueOf(idCreator) +"\"");
        }
        idGenTag.append("/>\r\n");
        out.write( idGenTag.toString().getBytes() );
    }

    void load(Document doc)
    {
        // get all tags with the name "idgen"
        NodeList idGenList = doc.getElementsByTagName("idgen");

        // scan the list, and get only the highest value. the project file
        // is supposed to contain only when "idgen" openTag, anyway:
        long maxID=0;
        for(int i=0; i<idGenList.getLength(); i++)
        {
            Node idGenNode = idGenList.item(i);

            // get the attributes of the node:
            NamedNodeMap attributes = idGenNode.getAttributes();

            // scan attributes
            for(int j=0; j<attributes.getLength(); j++)
            {
                Node attr = attributes.item(j);
                if( attr.getNodeName().compareTo("value")==0 )
                {
                    long idGenValue = Long.parseLong( attr.getNodeValue() );
                    if( idGenValue > maxID )
                    {
                        maxID = idGenValue;
                    }
                }
            }
        }

        // load the max value into the id creator:
        synchronized(this)
        {
            idCreator = maxID;
        }
    }

}
