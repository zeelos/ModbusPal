/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.main;

import java.util.ArrayList;
import java.util.Collection;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author nnovic
 */
public class XMLTools
{

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
