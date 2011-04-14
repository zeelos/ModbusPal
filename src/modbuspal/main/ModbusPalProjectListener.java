/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.main;

/**
 *
 * @author avincon
 */
public interface ModbusPalProjectListener
{
    public void modbusPalProjectChanged(ModbusPalProject oldProject, ModbusPalProject newProject);
}
