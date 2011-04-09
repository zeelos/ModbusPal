/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.slave;

/**
 *
 * @author nnovic
 */
class ModbusCoilsPanel
extends ModbusRegistersPanel
{
    /** Creates new form ModbusCoilsPanel */
    public ModbusCoilsPanel(ModbusSlaveDialog parent, ModbusCoils coils)
    {
        super(parent,coils);
    }

    @Override
    protected String getCaption_register()
    {
        return "coil";
    }

    @Override
    protected String getCaption_registers()
    {
        return "coils";
    }
}
