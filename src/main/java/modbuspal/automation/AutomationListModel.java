/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.automation;

import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.ComboBoxModel;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import modbuspal.main.ModbusPalProject;

/**
 * Defines a list of automations
 * @author nnovic
 */
public class AutomationListModel
implements ListModel, ComboBoxModel
{    
    private ArrayList<Automation> automations = new ArrayList<Automation>();
    private String selectedAutomation = null;
    private final ArrayList<ListDataListener> listeners = new ArrayList<ListDataListener>();

    /**
     * Creates a new instance of AutomationListModel.
     * @param list the list of automation for this model
     */
    public AutomationListModel( Automation[] list )
    {
        changeModel(list);
    }

    /**
     * Creates a new instance of AutomationListModel.
     * @param mpp the project from which the list of automation for this model must be obtained
     */
    public AutomationListModel( ModbusPalProject mpp )
    {
        changeModel(mpp);
    }

    
    /**
     * Creates a new instance of AutomationListModel.
     * The list is empty.
     */
    public AutomationListModel()
    {
    }


    /**
     * Modifies the list of automations for this model
     * @param list the new list of automations
     */
    public final void changeModel( Automation[] list )
    {
        automations.clear();
        automations.addAll(Arrays.asList(list));
        /*        for(int i=0; i<list.length; i++)
        {
            automations.add(list[i]);
        }*/
        selectedAutomation = null;
        notifyModelChanged();
    }



    
    /**
     * Modifies the list of automations for this model by getting the list
     * of automations defined in the specified ModbusPalProject
     * @param mpp the project from which the list of automations must be obtained
     */
    public final void changeModel( ModbusPalProject mpp )
    {
        changeModel(mpp.getAutomations());
    }

    /**
     * Returns the automation that is defined at the specified index in the 
     * list.
     * @param index index of the automation to return
     * @return the automation at the specified index in the lit
     */
    public Automation getAutomation(int index)
    {
        if( index==0 )
        {
            return NullAutomation.getInstance();
        }
        else
        {
            return automations.get(index-1);
        }
    }

    @Override
    public int getSize() {
        return 1+automations.size();
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
            return automations.get(index-1).getName();
        }
    }

    @Override
    public void addListDataListener(ListDataListener l) {
        if(listeners.contains(l)==false){
            listeners.add(l);
        }
    }

    @Override
    public void removeListDataListener(ListDataListener l) {
        if( listeners.contains(l)==true ){
            listeners.remove(l);
        }
    }

    @Override
    public void setSelectedItem(Object anItem)
    {
        String s = (String)anItem;

        if( s.compareTo(NullAutomation.NAME)==0 )
        {
            selectedAutomation = s;
            return;
        }

        // check that the object to select actually belongs to the list:
        for(Automation a:automations )
        {
            if( a.getName().compareTo(s)==0 )
            {
                selectedAutomation = s;
            }
        }
    }

    @Override
    public String getSelectedItem() {
        return selectedAutomation;
    }

    private void notifyModelChanged() {
        ListDataEvent listDataEvent = new ListDataEvent(this,ListDataEvent.CONTENTS_CHANGED,0,automations.size());
        for(ListDataListener l:listeners)
        {
            l.contentsChanged(listDataEvent);
        }
    }

    private void notifyInsertion(int index)
    {
        ListDataEvent listDataEvent = new ListDataEvent(this,ListDataEvent.INTERVAL_ADDED,index,1);
        for(ListDataListener l:listeners)
        {
            l.intervalAdded(listDataEvent);
        }        
    }

    private void notifyRemoval(int index)
    {
        ListDataEvent listDataEvent = new ListDataEvent(this,ListDataEvent.INTERVAL_REMOVED,index,1);
        for(ListDataListener l:listeners)
        {
            l.intervalAdded(listDataEvent);
        }
    }

    /**
     * Adds the specified automation to the model
     * @param a the automation to add to the model
     */
    public void add(Automation a)
    {
        automations.add(a);
        int index = automations.indexOf(a);
        notifyInsertion(index);
    }

    /**
     * Removes the specified automation from the model
     * @param a the automation to remove from the model
     */
    public void remove(Automation a)
    {
        int index = automations.indexOf(a);
        automations.remove(a);
        notifyRemoval(index);
    }
}
