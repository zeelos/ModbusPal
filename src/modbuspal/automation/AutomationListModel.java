/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.automation;

import javax.swing.ListModel;
import javax.swing.event.ListDataListener;
import modbuspal.main.ModbusPalProject;

/**
 *
 * @author avincon
 */
public class AutomationListModel
implements ListModel
{    
    private Automation[] automations;

    public AutomationListModel( Automation[] list )
    {
        automations = list;
    }

    public AutomationListModel( ModbusPalProject mpp )
    {
        this( mpp.getAutomations() );
    }

    public Automation getAutomation(int index)
    {
        if( index==0 )
        {
            return NullAutomation.getInstance();
        }
        else
        {
            return automations[index-1];
        }
    }

    @Override
    public int getSize() {
        return 1+automations.length;
    }

    @Override
    public Object getElementAt(int index)
    {
        if( index==0 )
        {
            return NullAutomation.NAME;
        }
        else
        {
            return automations[index-1].getName();
        }
    }

    @Override
    public void addListDataListener(ListDataListener l) {
        return;
    }

    @Override
    public void removeListDataListener(ListDataListener l) {
        return;
    }

}
