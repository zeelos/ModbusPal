/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.toolkit;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import modbuspal.main.ModbusPalPane;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author nnovic
 */
public class XMLTools
{

    public static Document ParseXML(File source)
    throws ParserConfigurationException, SAXException, IOException, InstantiationException, IllegalAccessException
    {
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();

        docBuilder.setEntityResolver( new EntityResolver()
        {
            public InputSource resolveEntity(String publicId, String systemId)
            throws SAXException, IOException
            {
                if( systemId.endsWith("modbuspal.dtd") )
                {
                    return new InputSource( ModbusPalPane.class.getResourceAsStream("modbuspal.dtd") );
                }
                return null;
            }
        });

        // the parse will fail if xml doc doesn't match the dtd.
        return docBuilder.parse(source);
    }


    public static String getAttribute(String attr, Node node)
    {
        NamedNodeMap attributes = node.getAttributes();
        Node attribute = attributes.getNamedItem(attr);
        if( attribute==null )
        {
            return null;
        }
        return attribute.getNodeValue();
    }

    public static Node getNode(NodeList nodes,String nodeName)
    {
        for(int i=0; i<nodes.getLength(); i++ )
        {
            Node node = nodes.item(i);
            if( node.getNodeName().compareTo(nodeName) == 0 )
            {
                return node;
            }
        }
        return null;
    }

    public static List<Node> getNodes(NodeList nodes,String nodeName)
    {
        ArrayList<Node> list = new ArrayList<Node>();

        for(int i=0; i<nodes.getLength(); i++ )
        {
            Node node = nodes.item(i);
            if( node.getNodeName().compareToIgnoreCase(nodeName) == 0 )
            {
                list.add(node);
            }
        }
        return list;
    }
    public static Node findChild(Node root,String nodeName)
    {
        return getNode( root.getChildNodes(), nodeName );
    }


    public static Node findParent(Node child, String nodeName)
    {
        Node current = child.getParentNode();

        while(current != null)
        {
            if( current.getNodeName().compareTo(nodeName)==0 )
            {
                return current;
            }

            current = current.getParentNode();
        }

        return null;
    }

    public static Collection<Node> findChildren(Node root, String nodeName)
    {
        ArrayList<Node> list = new ArrayList<Node>();

        NodeList nodes = root.getChildNodes();
        for( int i=0; i<nodes.getLength(); i++ )
        {
            Node node = nodes.item(i);
            if( node.getNodeName().compareTo(nodeName)==0 )
            {
                list.add(node);
            }
            else
            {
                list.addAll( findChildren(node,nodeName) );
            }
        }


        return list;

    }

}
