/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.dlm.providers;

import org.jasig.portal.layout.dlm.Evaluator;
import org.jasig.portal.layout.dlm.EvaluatorFactory;
import org.jasig.portal.utils.XML;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Implementation of the Evaluator Factory interface that creates evaluators
 * of string attributes in implementations of IPerson to determine if a user
 * gets a layout fragment.
 *   
 * @author mboyd@sungardsct.com
 * @version $Revision$ $Date$
 * @since uPortal 2.5
 */
public class PersonEvaluatorFactory
    implements EvaluatorFactory
{
    public static final String RCS_ID = "@(#) $Header$";

    private static final int OR = 0;
    private static final int AND = 1;
    private static final int NOT = 2;

    public Evaluator getEvaluator( Node audience ) 
    {
        return getGroupEvaluator( OR, audience );
    }

    private Evaluator getGroupEvaluator( int type, Node node ) 
    {
        NodeList nodes = node.getChildNodes();
        Evaluator container = null;

        if ( nodes == null || 
             nodes.getLength() == 0 || 
             ( container = createGroupEvaluator( type, nodes ) ) == null )
        {
            throw new RuntimeException( "Invalid content. Expected one to many " +
                                 "<paren>, <NOT>, or <attribute> in '" + 
                                 XML.serializeNode(node) + "'" );
        }
        return container;
    }

    private Evaluator createGroupEvaluator( int type, NodeList nodes )
    {
        // if only one child skip wrapping in container for AND and OR
        if ( nodes.getLength() == 1 &&
             ( type == OR || type == AND ) )
                 return createEvaluator( nodes.item(0) );

        Paren container = null;

        if ( type == NOT )
            container = new Paren( Paren.NOT );
        else if ( type == OR )
            container = new Paren( Paren.OR );
        else if ( type == AND )
            container = new Paren( Paren.AND );

        boolean validContentAdded = false;
  
        for ( int i=0; i<nodes.getLength(); i++ )
        {
            if ( nodes.item(i).getNodeType() == Node.ELEMENT_NODE )
            {
                Evaluator e = createEvaluator( nodes.item(i) );
                
                if ( e != null )
                {
                    validContentAdded = true;
                    container.addEvaluator( e );
                }
            }
        }
        if ( validContentAdded )
            return container;
        return null;
    }

    private Evaluator createEvaluator( Node node )
    {
        String nodeName = node.getNodeName();

        if ( nodeName.equals( "paren" ) )
            return createParen( node );
        else if ( nodeName.equals( "attribute" ) )
            return createAttributeEvaluator( node );
        throw new RuntimeException( "Unrecognized element '" + nodeName + "' in '" +
                XML.serializeNode(node) + "'" );
    }

    private Evaluator createParen( Node n )
    {
        NamedNodeMap attribs = n.getAttributes();
        Node opNode = attribs.getNamedItem( "mode" );

        
        if ( opNode == null )
            throw new RuntimeException( "Invalid mode. Expected 'AND','OR', or 'NOT'"
                                 + " in '" +
                                 XML.serializeNode(n) + "'" );
        else if ( opNode.getNodeValue().equals( "OR" ))
            return getGroupEvaluator( OR, n );
        else if ( opNode.getNodeValue().equals( "NOT" ))
            return getGroupEvaluator( NOT, n );
        else if ( opNode.getNodeValue().equals( "AND" ) )
            return getGroupEvaluator( AND, n );
        else
            throw new RuntimeException( "Invalid mode. Expected 'AND','OR', or 'NOT'"
                                 + " in '" +
                                 XML.serializeNode(n) + "'" );
    }

    private Evaluator createAttributeEvaluator( Node n )
    {
        NamedNodeMap attribs =  n.getAttributes();
        Node attribNode = attribs.getNamedItem( "name" );

        if ( attribNode == null ||
             attribNode.getNodeValue().equals( "" ) )
            throw new RuntimeException( "Missing or empty name attribute in '" +
                    XML.serializeNode(n) + "'" );
        String name = attribNode.getNodeValue();
        String value = null;
        attribNode = attribs.getNamedItem( "value" );

        if ( attribNode != null )
            value = attribNode.getNodeValue();

        attribNode = attribs.getNamedItem( "mode" );

        if ( attribNode == null ||
             attribNode.getNodeValue().equals( "" ) )
            throw new RuntimeException( "Missing or empty mode attribute in '" +
                    XML.serializeNode(n) + "'" );
        String mode = attribNode.getNodeValue();
        Evaluator eval = null;
        
        try
        {
            eval = getAttributeEvaluator(name, mode, value);
        }
        catch( Exception e )
        {
            throw new RuntimeException( e.getMessage() + " in '" + XML.serializeNode(n));
        }
        return eval;
    }

    /**
     * returns an Evaluator unique to the type of attribute being
     * evaluated.  subclasses can override this method to return the
     * Evaluator that's appropriate to their implementation.
     *
     * @param name  the attribute's name.
     * @param mode  the attribute's mode. (i.e. 'equals')
     * @param value the attribute's value.
     *
     * @return an Evaluator for evaluating attributes
     */
    public Evaluator getAttributeEvaluator(String name,
                                           String mode,
                                           String value)
        throws Exception
    {
        return new AttributeEvaluator( name, mode, value );
    }
}
