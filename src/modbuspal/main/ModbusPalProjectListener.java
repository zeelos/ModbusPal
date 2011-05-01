/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.main;

/**
 * objects interested in receiving notifications from the modbuspal project
 * must implement this interface
 * @author nnovic
 */
public interface ModbusPalProjectListener
{
    /**
     * this method is triggered by ModbusPalProject when the project
     * is modified
     * @param oldProject reference on the previous project, being replaced
     * @param newProject reference on the new project
     */
    public void modbusPalProjectChanged(ModbusPalProject oldProject, ModbusPalProject newProject);
}
