/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.binding;

import java.io.IOException;
import java.io.OutputStream;
import modbuspal.automation.Automation;
import modbuspal.automation.AutomationExecutionListener;
import modbuspal.instanciator.Instantiable;
import modbuspal.slave.ModbusRegisters;

/**
 *
 * @author nnovic
 */
public abstract class Binding
implements AutomationExecutionListener, Cloneable, Instantiable<Binding>
{

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    private Automation automation;
    private int order;
    private ModbusRegisters registers;
    private int registerAddress;

    public Binding()
    {
    }

    @Override
    public void init()
    {
    }

    @Override
    public void reset()
    {
    }

    public void attach(ModbusRegisters l, int address)
    {
        registers = l;
        registerAddress = address;
        automation.addAutomationExecutionListener(this);
    }

    public void detach()
    {
        registers=null;
        automation.removeAutomationExecutionListener(this);
    }

    @Override
    public void automationValueHasChanged(Automation source, double time, double value)
    {
        registers.notifyRegisterChanged(registerAddress);
    }

    @Override
    public void automationHasStarted(Automation source) {
    }

    @Override
    public void automationHasEnded(Automation source) {
    }

    @Override
    public void automationReloaded(Automation source) {
    }

    /**
     *
     * @return size in bits
     */
    public abstract int getSize();

    
    public void save(OutputStream out)
    throws IOException
    {
        StringBuffer tag = new StringBuffer("<binding");
        tag.append(" automation=\""+ automation.getName() +"\"");
        tag.append(" class=\""+ getClassName() +"\"");
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


    public final int getRegister()
    {
        return getRegister(order, automation.getCurrentValue());
    }


    public abstract int getRegister(int rank, double value);


    public final boolean getCoil()
    {
        return getCoil(order, automation.getCurrentValue());
    }


    protected boolean getCoil(int rank, double value)
    {
        int rankWord = rank / 16;
        int rankBit = rank % 16;
        int reg = getRegister(rankWord,value);
        int mask = 1 << rankBit;
        return (reg&mask)!=0;
    }

    @Override
    public String toString()
    {
        return automation.getName() + " (" + getClassName() + ":" + String.valueOf(order) + ")";
    }

    public String getAutomationName()
    {
        return automation.getName();
    }

    @Override
    public String getClassName()
    {
        return getClass().getSimpleName();
    }

    @Override
    public Binding newInstance()
    throws InstantiationException, IllegalAccessException
    {
        return getClass().newInstance();
    }


}
