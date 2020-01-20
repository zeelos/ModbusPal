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
 * various tools for the DOM structure
 * @author nnovic
 */
public class XMLTools
{

    /**
     * Creates a DOM document from the specified XMl formatted file.
     * @param source the file to parse
     * @return the resulting DOM document
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     * @throws InstantiationException
     * @throws IllegalAccessException 
     */
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


    /**
     * Gets the attribute (specified by its name) of the provided Node.
     * @param attr the name of the attribute to get
     * @param node the node that is expected to contain this attribute
     * @return the value of the attribute, or null
     */
    public static String getAttribute(String attr, Node node)
    {
    	if( node == null )
    	{
    		return null;
    	}
        NamedNodeMap attributes = node.getAttributes();
        Node attribute = attributes.getNamedItem(attr);
        if( attribute==null )
        {
            return null;
        }
        return attribute.getNodeValue();
    }

    /**
     * Gets the node with the specified node name from the list of nodes
     * @param nodes the list of nodes
     * @param nodeName the node name of the node to return
     * @return the specified node, or null
     */
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

    /**
     * Gets all the nodes with the specified node name from the list of nodes
     * @param nodes the list of nodes
     * @param nodeName the node name of the nodes to return
     * @return the specified nodes. the list might be empty, but is never null
     */
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
    
    
    /**
     * Gets the node with the specified node name from the children of the
     * provided node
     * @param root the node to search into
     * @param nodeName the node name of the child node to return
     * @return the specified node, or null
     */
    public static Node findChild(Node root,String nodeName)
    {
        return getNode( root.getChildNodes(), nodeName );
    }


    /**
     * Recursively get the parents of the specified node until
     * one the parent matches the specified node name
     * @param child the node
     * @param nodeName the requested node name
     * @return the parent node with the specified node name, or null
     */
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

    /**
     * Recursively find the children of the specified node that math the
     * provided node name
     * @param root the initial node
     * @param nodeName the node name to match
     * @return collection of children matching the node name. the collection
     * might be empty, but is never null
     */
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
