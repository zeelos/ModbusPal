/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.binding;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Vector;
import modbuspal.automation.Automation;
import modbuspal.automation.AutomationValueListener;
import modbuspal.slave.ModbusRegisters;

/**
 *
 * @author nnovic
 */
public abstract class Binding
implements AutomationValueListener
{



    public static Class getClass(String className)
    {
        Class[] list = getClassList();
        for(int i=0; i<list.length; i++)
        {
            if( list[i].getSimpleName().compareTo(className)==0 )
            {
                return list[i];
            }
        }
        return null;
    }
    
    public static Class[] getClassList()
    {
        Vector<Class> list = new Vector<Class>();
        list.add( Binding_SINT32.class );
        list.add( Binding_FLOAT32.class );
        Class[] array = new Class[0];
        return list.toArray(array);
    }

    protected Automation automation;
    protected int order;
    private ModbusRegisters registers;
    private int registerAddress;

    public Binding()
    {
    }

    public void attach(ModbusRegisters l, int address)
    {
        registers = l;
        registerAddress = address;
        automation.addAutomationValueListener(this);
    }

    public void detach()
    {
        registers=null;
        automation.removeAutomationValueListener(this);
    }

    @Override
    public void automationValueHasChanged(Automation source, double value)
    {
        registers.notifyRegisterChanged(registerAddress);
    }


    public void save(OutputStream out)
    throws IOException
    {
        StringBuffer tag = new StringBuffer("<binding");
        tag.append(" automation=\""+ automation.getName() +"\"");
        tag.append(" class=\""+ getClass().getSimpleName() +"\"");
        tag.append(" order=\""+ String.valueOf(order) +"\"");
        tag.append("/>\r\n");
        out.write( tag.toString().getBytes() );
    }

    public void setup(Automation source, int order)
    {
        automation = source;
        this.order = order;
        if( this.order < 0 )
        {
            throw new IllegalArgumentException();
        }
    }

    public abstract int getRegister();

    public abstract boolean getCoil();

    @Override
    public String toString()
    {
        return automation.getName() + " (" +getClass().getSimpleName() + ":" + String.valueOf(order) + ")";
    }

    public String getAutomationName()
    {
        return automation.getName();
    }
}
