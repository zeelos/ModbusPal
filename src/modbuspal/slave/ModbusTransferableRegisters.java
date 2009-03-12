/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.slave;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import javax.swing.table.TableModel;

/**
 *
 * @author avincon
 */
public class ModbusTransferableRegisters
implements Transferable
{

    public static final DataFlavor DATA_FLAVOR = new DataFlavor(ModbusRegisters.class,"registers");
    public static final DataFlavor SUPPORTED_DATA_FLAVORS[] = { DATA_FLAVOR };
    private ModbusRegisters registers = new ModbusRegisters();


    public ModbusTransferableRegisters(ModbusRegistersTable table)
    {
        TableModel model = table.getModel();
        if( model instanceof ModbusRegisters )
        {
            ModbusRegisters source = (ModbusRegisters)model;
            registers.setOffset( source.getOffset() );

            int addresses[] = table.getSelectedAddresses();
            for(int i=0; i<addresses.length; i++)
            {
//                System.out.printf("Copying address=%d, value=%d; name=%s\r\n",
//                    addresses[i],
//                    source.getRegister(addresses[i]),
//                    source.getName(addresses[i]) );
                registers.add( source, addresses[i] );
            }
        }
    }

    public DataFlavor[] getTransferDataFlavors()
    {
        return SUPPORTED_DATA_FLAVORS;
    }

    public boolean isDataFlavorSupported(DataFlavor flavor)
    {
        return flavor.equals(DATA_FLAVOR);
    }

    public Object getTransferData(DataFlavor flavor)
    throws UnsupportedFlavorException, IOException
    {
        if( flavor.equals(DATA_FLAVOR)==false )
        {
            throw new UnsupportedFlavorException(flavor);
        }
        return registers;
    }

}
