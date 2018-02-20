/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.main;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager2;
import java.util.ArrayList;

/**
 * the swing layout for displaying lists of Components
 * @author nnovic
 */
public class ListLayout
implements LayoutManager2
{
    private Component[] indexedList = new Component[256];
    private ArrayList<Component> componentList = new ArrayList<Component>();
    private static final int borderThickness = 5;

    public void addLayoutComponent(Component comp, Object constraints)
    {
        int index;

        if( constraints == null )
        {
            index = getFreeIndex();
        }
        else
        {
            index = ( (Integer)constraints ).intValue();
        }

        // check if resize is necessary
        if( index >= indexedList.length )
        {
            resizeTo(index);
        }

        // check if object already exists at this index
        Component old = indexedList[index];
        if( old != null )
        {
            componentList.remove(old);
        }

        indexedList[index] = comp;
        componentList.add(comp);
    }

    public Dimension maximumLayoutSize(Container target)
    {
        return preferredLayoutSize(target);
    }

    public float getLayoutAlignmentX(Container target)
    {
        return 0;
    }

    public float getLayoutAlignmentY(Container target)
    {
        return 0;
    }

    public void invalidateLayout(Container target)
    {
        return;
    }

    public void addLayoutComponent(String name, Component comp)
    {
        int index = getFreeIndex();
        indexedList[index] = comp;
        componentList.add(comp);
    }

    public void removeLayoutComponent(Component comp)
    {
        int index = indexOf(comp);
        indexedList[index] = null;
        componentList.remove(comp);
    }

    public Dimension preferredLayoutSize(Container parent)
    {
        double maxWidth = 0.0;
        double totalHeight = 0.0;

        for(Component comp:componentList)
        {
            Dimension dim = comp.getPreferredSize();
            if( dim.getWidth() > maxWidth )
            {
                maxWidth = dim.getWidth();
            }
            totalHeight += dim.getHeight();
        }

        maxWidth += 2*borderThickness;
        totalHeight += (2*borderThickness)*componentList.size();

        return new Dimension( (int)maxWidth, (int)totalHeight );
    }

    public Dimension minimumLayoutSize(Container parent)
    {
        return preferredLayoutSize(parent);
    }

    public void layoutContainer(Container parent)
    {
        double maxWidth = 0.0;
        double currentHeight = 0.0;

        for(Component comp:componentList)
        {
            Dimension dim = comp.getPreferredSize();
            if( dim.getWidth() > maxWidth )
            {
                maxWidth = dim.getWidth();
            }
        }

        //for(Component comp:componentList)
        for( int i=0; i<indexedList.length; i++)
        {
            Component comp = indexedList[i];
            if( comp != null )
            {
                Dimension dim = comp.getPreferredSize();
                currentHeight += borderThickness ;
                comp.setLocation(borderThickness, (int)currentHeight);
                comp.setSize( (int)maxWidth, (int)dim.getHeight() );
                currentHeight += comp.getHeight();
                currentHeight += borderThickness;
            }
        }

        //parent.setSize( (int)maxWidth+2*borderThickness, (int)currentHeight);
    }

    /**
     * Swaps two components in the list, identified by their indexes.
     * @param i1 the index of the first component involved in the swap
     * @param i2 the index of the second component involved in the swap
     */
    public void swapComponents(int i1, int i2)
    {
        Component comp1 = indexedList[i1];
        Component comp2 = indexedList[i2];
        indexedList[i2] = comp1;
        indexedList[i1] = comp2;
    }

    /**
     * Swaps two components in the list
     * @param r1 the first component involved in the swap
     * @param r2 the second component involved in the swap
     */
    public void swapComponents(Component r1, Component r2)
    {
        int i1 = indexOf(r1);
        int i2 = indexOf(r2);
        swapComponents(i1,i2);
    }

    private int getFreeIndex()
    {
        int index = -1;
        for(int i=0; i<indexedList.length; i++ )
        {
            if( indexedList[i] == null )
            {
                index = i;
                break;
            }
        }

        if( index >= 0 )
        {
            return index;
        }

        int currentSize = indexedList.length;
        resizeTo( currentSize + 20 );
        return currentSize;
    }

    private int indexOf(Component comp)
    {
        for(int index=0; index<indexedList.length; index++)
        {
            if( indexedList[index] == comp )
            {
                return index;
            }
        }
        return(-1);
    }

    /**
     * Gets the component in the layout that has the specified index
     * in the list
     * @param index index of the component to return
     * @return the component at the specified index
     */
    public Component getComponent(int index)
    {
        return indexedList[index];
    }

    private void resizeTo(int index)
    {
        Component[] newList = new Component[index];
        int max = Math.min(newList.length, indexedList.length);
        for(int i=0; i<max; i++ )
        {
            newList[i] = indexedList[i];
        }
        indexedList = newList;
    }

}
