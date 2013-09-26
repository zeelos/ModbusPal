/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package modbuspal.master;

/**
 *
 * @author JMC15
 */
public class ModbusMasterDelay
extends ModbusMasterRequest
{
    public static ModbusMasterDelay getDelay(int milliseconds)
    {
        ModbusMasterDelay output = new ModbusMasterDelay();
        output.delayMs = milliseconds;
        
        String caption = String.format("Delay (%d milliseconds)", milliseconds);
        output.setUserObject(caption);
        return output;
    }   
        
    private int delayMs = 0;
    
    
    
    public int getDelay() 
    {
        return delayMs;
    }
}
