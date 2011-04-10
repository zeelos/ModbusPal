/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.main;

/**
 * The tags used by ModbusPal when generating xml files. Incomplete.
 * @author nnovic
 */
public interface ModbusPalXML
{
    /* SLAVE */
    public static final String XML_SLAVE_ID_ATTRIBUTE = "id";

    /* REGISTER */
    public static final String XML_ADDRESS_ATTRIBUTE = "address";

    /* COILS */
    public static final String XML_COILS_TAG = "coils";

    /* FILES */
    public static final String XML_FILE_RELATIVE_PATH_TAG = "rel";
    public static final String XML_FILE_ABSOLUTE_PATH_TAG = "abs";

    /* SCRIPTS */
    public static final String XML_SCRIPT_TYPE_ATTRIBUTE = "type";
    public static final String XML_SCRIPT_TYPE_ONDEMAND = "ondemand";
    public static final String XML_SCRIPT_TYPE_AFTERINIT = "afterinit";
    public static final String XML_SCRIPT_TYPE_BEFOREINIT = "beforeinit";
}
