/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.slave;

import java.awt.Component;
import java.awt.datatransfer.Transferable;
import java.awt.event.InputEvent;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.TransferHandler;
import modbuspal.slave.ModbusExtendedRegisters.RegisterCopy;

/**
 *
 * @author nnovic
 */
class ModbusExtendedRegistersTransferHandler
extends TransferHandler
{

    List<RegisterCopy> getRegisters(Transferable t)
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
            Logger.getLogger(ModbusExtendedRegistersTransferHandler.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }

        if( data==null )
        {
            return null;
        }

        if( data instanceof List )
        {
            return (List)data;
        }
        return null;
    }


    ModbusExtendedRegisters getRegisters(Component comp)
    {
        if( comp == null )
        {
            return null;
        }

        // First, check that the target component is a JTable
        if( comp instanceof ModbusExtendedRegistersTable )
        {
            // Second, check that the target JTable has a model
            // that is instance of ModbusRegisters
            ModbusExtendedRegistersTable table = (ModbusExtendedRegistersTable)comp;
            return table.getModel();
        }
        return null;
    }

    @Override
    public boolean canImport(TransferSupport support)
    {
        ModbusExtendedRegisters registers = getRegisters(support.getComponent());
        if( registers == null )
        {
            return false;
        }
        return support.isDataFlavorSupported( ModbusTransferableExtendedRegisters.DATA_FLAVOR );
    }

    @Override
    protected Transferable createTransferable(JComponent c)
    {
        if( c instanceof ModbusExtendedRegistersTable )
        {
            ModbusTransferableExtendedRegisters mtr = new ModbusTransferableExtendedRegisters( (ModbusExtendedRegistersTable)c );
            System.out.printf("createTransferable for %d registers\r\n", mtr.getRegistersCount() );
            return mtr;
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
        ModbusExtendedRegistersTable destination = (ModbusExtendedRegistersTable)support.getComponent();
        int addresses[] = destination.getSelectedAddresses();

        // get the target table model
        ModbusExtendedRegisters target = getRegisters(support.getComponent());
        if( target == null )
        {
            return false;
        }

        // get the registers to copy into the target
        List<RegisterCopy> copies = getRegisters(support.getTransferable());
        if( copies == null )
        {
            return false;
        }

        // determine how many "paste" operation to perform
        int opCount = Math.min( copies.size(), addresses.length );

        // paste
        for( int i=0; i<opCount; i++ )
        {
            //int j = i % source.getRowCount();
            target.paste(addresses[i], copies.get(i) );
            //target.replace(source, source.getAddressOf(j), addresses[i]);
        }
        return false;
    }


}
