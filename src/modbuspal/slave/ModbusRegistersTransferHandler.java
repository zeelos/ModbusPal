/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.slave;

import java.awt.Component;
import java.awt.datatransfer.Transferable;
import java.awt.event.InputEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.TransferHandler;

/**
 *
 * @author nnovic
 */
class ModbusRegistersTransferHandler
extends TransferHandler
{

    ModbusRegisters getRegisters(Transferable t)
    {
        if( t==null )
        {
            return null;
        }

        Object data = null;
        try 
        {
            data = t.getTransferData(ModbusTransferableRegisters.DATA_FLAVOR);
        } 
        catch (Exception ex) 
        {
            Logger.getLogger(ModbusRegistersTransferHandler.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        
        if( data==null )
        {
            return null;
        }

        if( data instanceof ModbusRegisters )
        {
            return (ModbusRegisters)data;
        }
        return null;
    }


    ModbusRegisters getRegisters(Component comp)
    {
        if( comp == null )
        {
            return null;
        }

        // First, check that the target component is a JTable
        if( comp instanceof ModbusRegistersTable )
        {
            // Second, check that the target JTable has a model
            // that is instance of ModbusRegisters
            ModbusRegistersTable table = (ModbusRegistersTable)comp;
            return table.getModel();
        }
        return null;
    }

    @Override
    public boolean canImport(TransferSupport support) 
    {
        ModbusRegisters registers = getRegisters(support.getComponent());
        if( registers == null )
        {
            return false;
        }
        return support.isDataFlavorSupported( ModbusTransferableRegisters.DATA_FLAVOR );
    }

    @Override
    protected Transferable createTransferable(JComponent c)
    {
        if( c instanceof ModbusRegistersTable )
        {
            return new ModbusTransferableRegisters( (ModbusRegistersTable)c );
        }
        else
        {
            return null;
        }
    }

    @Override
    public void exportAsDrag(JComponent comp, InputEvent e, int action) {
        super.exportAsDrag(comp, e, action);
    }

    @Override
    public int getSourceActions(JComponent c)
    {
        return COPY;
    }

    @Override
    public Icon getVisualRepresentation(Transferable t) {
        return super.getVisualRepresentation(t);
    }

    @Override
    public boolean importData(TransferSupport support) 
    {
        if( canImport(support)==false )
        {
            return false;
        }

        // get the target selection
        ModbusRegistersTable destination = (ModbusRegistersTable)support.getComponent();
        int addresses[] = destination.getSelectedAddresses();

        // get the target table model
        ModbusRegisters target = getRegisters(support.getComponent());
        if( target == null )
        {
            return false;
        }

        // get the registers to copy into the target
        ModbusRegisters source = getRegisters(support.getTransferable());
        if( source == null )
        {
            return false;
        }

        // paste
        for( int i=0; i<addresses.length; i++ )
        {
            int j = i % source.getRowCount();
            target.replace(source, source.getAddressAt(j), addresses[i]);
        }
        return false;
    }


}
