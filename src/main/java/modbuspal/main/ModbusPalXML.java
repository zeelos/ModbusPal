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
    public static final String XML_SLAVE_ID2_ATTRIBUTE = "id2";
    public static final String XML_SLAVE_ENABLED_ATTRIBUTE = "enabled";
    public static final String XML_SLAVE_NAME_ATTRIBUTE = "name";
    public static final String XML_SLAVE_IMPLEMENTATION_ATTRIBUTE = "implementation";
    public static final String XML_SLAVE_IMPLEMENTATION_MODBUS_VALUE = "modbus";
    public static final String XML_SLAVE_IMPLEMENTATION_JBUS_VALUE = "j-bus";

    /* REGISTER */
    public static final String XML_HOLDING_REGISTERS_TAG = "holding_registers";
    public static final String XML_EXTENDED_REGISTERS_TAG = "extended_registers";
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

    /* FUNCTIONS */
    public static final String XML_FUNCTIONS_TAG = "functions";
    public static final String XML_FUNCTION_INSTANCE_TAG = "instance";
    public static final String XML_FUNCTION_TAG = "function";
    public static final String XML_FUNCTION_CODE_ATTRIBUTE = "code";
    public static final String XML_FUNCTION_SETTINGS_TAG = "settings";

    /* TUNING */
    public static final String XML_TUNING_TAG = "tuning";
    public static final String XML_REPLYDELAY_TAG = "reply_delay";
    public static final String XML_REPLYDELAY_MIN_ATTRIBUTE = "min";
    public static final String XML_REPLYDELAY_MAX_ATTRIBUTE = "max";
    public static final String XML_ERRORRATES_TAG = "error_rates";
    public static final String XML_ERRORRATES_NOREPLY_ATTRIBUTE = "no_reply";
}
