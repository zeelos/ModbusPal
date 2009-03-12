/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.automation;

import java.io.IOException;
import java.io.InvalidClassException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * History:
 * - generator listeners have been moved into "Generator" class
 * - generators are not removed when the user does an import
 *
 * @author avincon
 */
public class Automation
implements Runnable
{
    public final static String DEFAULT_NAME = "no name";
    private ArrayList<Generator> generators = new ArrayList<Generator>();
    private double stepDelay = 1.0;
    private Thread thread = null;
    private boolean loop = true;
    private String uniqueName="";
    private boolean suspended = false;
    private boolean quit = false;
    private ArrayList<AutomationStateListener> automationStateListeners = new ArrayList<AutomationStateListener>();
    private ArrayList<AutomationValueListener> automationValueListeners = new ArrayList<AutomationValueListener>();
    private double currentValue = 0.0;
    private double initialValue = 0.0;

    /**
     * this is the constructor to use when creating the automation from an
     * xml file (i.e: loading a xmpp project). It uses the "loadAttributes"
     * function to parse the "importable" attributes of the "automation" tag,
     * and it also parses the non-importable attributes (like "id").
     * @param attributes
     */
    public Automation(NamedNodeMap attributes)
    {
        loadAttributes(attributes);
    }

    public Automation(String name)
    {
        uniqueName = name;
    }

    void down(Generator gen)
    {
        int index = generators.indexOf(gen);
        int max = generators.size();
        if( (index+1) < max )
        {
            swap(index,index+1);
        }
    }

    Generator[] getGenerators()
    {
        Generator[] list = new Generator[0];
        return generators.toArray(list);
    }


    public void loadAttributes(NamedNodeMap attributes)
    {
        Node stepNode = attributes.getNamedItem("step");
        String stepValue = stepNode.getNodeValue();
        double newStepDelay = Double.parseDouble(stepValue);
        if( stepDelay != newStepDelay )
        {
            stepDelay = newStepDelay;
            notifyStepDelayHasChanged(stepDelay);
        }
        
        Node loopNode = attributes.getNamedItem("loop");
        String loopValue = loopNode.getNodeValue();
        boolean newLoop = Boolean.parseBoolean(loopValue);
        if( loop != newLoop )
        {
            loop = newLoop;
            notifyLoopEnabled(loop);
        }

        Node nameNode = attributes.getNamedItem("name");
        String nameValue = nameNode.getNodeValue();
        if( uniqueName.compareTo(nameValue) != 0 )
        {
            uniqueName = nameValue;
            notifyNameChanged(uniqueName);
        }

        // Extract initial value of the whole automation
        Node initNode = attributes.getNamedItem("init");
        if( initNode!= null )
        {
            String initValue = initNode.getNodeValue();
            double newInit = Double.parseDouble(initValue);
            if( initialValue != newInit )
            {
                initialValue = newInit;
                notifyInitialValueChanged(initialValue);
            }
        }
    }

    public double getStepDelay()
    {
        return stepDelay;
    }

    public int addGenerator(Generator gen)
    {
        generators.add(gen);
        int index = generators.indexOf(gen);
        notifyGeneratorAdded(gen,index);
        return index;
    }

    public void removeGenerator(Generator gen)
    {
        generators.remove(gen);
        gen.removeAllGeneratorListeners();
        notifyGeneratorRemoved(gen);
    }

    public void removeAllGenerators()
    {
        Generator list[] = new Generator[0];
        list = generators.toArray(list);
        for( int i=0; i<list.length; i++ )
        {
            removeGenerator( list[i] );
        }
    }

    public void loadGenerators(NodeList nodes)
    throws InvalidClassException
    {
        for(int i=0; i<nodes.getLength(); i++)
        {
            Node node = nodes.item(i);
            if( node.getNodeName().compareTo("generator")==0 )
            {
                Generator gen = Generator.create(node);
                gen.load(node.getChildNodes());
                addGenerator(gen);
            }
        }
    }




    public void save(OutputStream out)
    throws IOException
    {
        String openTag = createOpenTag();
        out.write( openTag.getBytes() );

        for(Generator g:generators)
        {
            g.save(out);
        }

        String closeTag = "</automation>\r\n";
        out.write( closeTag.getBytes() );
    }

    public void setName(String text)
    {
        uniqueName = text;
        notifyNameChanged(uniqueName);
    }

    public void start()
    {
        if(thread==null)
        {
            thread = new Thread(this);
            suspended = false;
            quit = false;
            thread.start();
        }
    }

    public void stop()
    {
        if( thread != null )
        {
            quit = true;
            synchronized(this)
            {
                notifyAll();
            }
            try
            {
                thread.join( (long)(stepDelay*2000.0) );
            }
            catch (InterruptedException ex)
            {
                Logger.getLogger(Automation.class.getName()).log(Level.SEVERE, null, ex);
            }
            thread=null;
        }
    }

    public void suspend()
    {
        suspended = true;
    }

    public void resume()
    {
        suspended = false;
        synchronized(this)
        {
            notifyAll();
        }
    }

    public boolean isLoopEnabled()
    {
        return loop;
    }

    public void setLoopEnabled(boolean enabled)
    {
        loop = enabled;
    }

    public String getName()
    {
        return uniqueName;
    }

    public double getInitialValue()
    {
        return initialValue;
    }

    public double getCurrentValue()
    {
        return currentValue;
    }

    public void run()
    {
        System.out.println("start automation thread");
        
        // Get generators
        Generator genList[] = new Generator[generators.size()];
        genList = generators.toArray(genList);

        // init:
        int currentIndex = 0;
        currentValue = initialValue;
        double previousValue = 0.0;
        double currentTime = 0.0;
        double startTime = 0.0;

        notifyAutomationHasStarted();

        while( (currentIndex < genList.length) && (quit==false) )
        {
            
            // prepare to execute generator:
            Generator currentGen = genList[currentIndex];
            currentGen.setInitialValue(currentValue);
            double duration = currentGen.getDuration();
            startTime = currentTime;
            currentGen.notifyGeneratorHasStarted();

            while( (currentTime < startTime + duration) && (quit==false) )
            {
                
                currentValue = currentGen.getValue( currentTime-startTime );
                if( previousValue != currentValue )
                {
                    notifyCurrentValueChanged(currentValue);
                }

                try
                {
                    while( (suspended == true) && (quit == false) )
                    {
                        System.out.println("suspended");
                        synchronized(this)
                        {
                            wait();
                        }
                    }
                    suspended = false;
                    if( quit == false )
                    {
                        Thread.sleep( (long)(stepDelay*1000.0) );
                    }
                }
                catch (InterruptedException ex)
                {
                    Logger.getLogger(Automation.class.getName()).log(Level.SEVERE, null, ex);
                }
                currentTime += stepDelay;
                previousValue = currentValue;
            }

            // finish the execution of the generator
            previousValue = currentValue;
            currentValue = currentGen.getValue(duration);
            currentGen.notifyGeneratorHasEnded();

            currentIndex++;
            if( currentIndex >= genList.length )
            {
                if( loop == true )
                {
                    currentIndex = 0;
                }
            }
        }
        
        System.out.println("end of automation thread");
        currentValue = 0.0;
        suspended = false;
        if( quit==true )
        {
            quit = false;
        }
        else
        {
            thread=null;
        }
        notifyAutomationHasEnded();
    }

    public boolean isSuspended()
    {
        return suspended;
    }

    public void removeAllListeners()
    {
        automationStateListeners.clear();
        automationValueListeners.clear();
    }

    public void addAutomationStateListener(AutomationStateListener l)
    {
        assert( automationStateListeners.contains(l) == false );
        automationStateListeners.add(l);
    }

    public void removeAutomationStateListener(AutomationStateListener l)
    {
        automationStateListeners.remove(l);
    }

    public void addAutomationValueListener(AutomationValueListener l)
    {
        assert( automationValueListeners.contains(l) == false );
        automationValueListeners.add(l);
    }

    public void removeAutomationValueListener(AutomationValueListener l)
    {
        automationValueListeners.remove(l);
    }



    public void setStepDelay(double val)
    {
        stepDelay = val;
    }

    void setInitialValue(double dval)
    {
        initialValue = dval;
    }

    void up(Generator gen)
    {
        int index = generators.indexOf(gen);
        if( index >= 1 )
        {
            swap(index,index-1);
        }
    }

    private String createOpenTag()
    {
        StringBuffer tag = new StringBuffer("<automation");
        tag.append(" name=\""+ uniqueName +"\"");
        tag.append(" step=\""+ String.valueOf(stepDelay) +"\"");
        tag.append(" loop=\""+ Boolean.toString(loop) +"\"");
        tag.append(" init=\""+ Double.toString(initialValue) +"\"");
        tag.append(">\r\n");
        return tag.toString();
    }

    private void notifyAutomationHasEnded()
    {
        for(AutomationStateListener l:automationStateListeners)
        {
            l.automationHasEnded(this);
        }
    }

    private void notifyAutomationHasStarted()
    {
        for(AutomationStateListener l:automationStateListeners)
        {
            l.automationHasStarted(this);
        }
    }

    private void notifyGeneratorAdded(Generator gen, int index)
    {
        for(AutomationStateListener l:automationStateListeners)
        {
            l.generatorHasBeenAdded(this, gen, index);
        }
    }

    private void notifyGeneratorRemoved(Generator gen)
    {
        for(AutomationStateListener l:automationStateListeners)
        {
            l.generatorHasBeenRemoved(this, gen);
        }
    }

    private void notifyGeneratorSwap(Generator g1, Generator g2)
    {
        for(AutomationStateListener l:automationStateListeners)
        {
            l.generatorsHaveBeenSwapped(this, g1, g2);
        }
    }

    private void notifyInitialValueChanged(double init)
    {
        for(AutomationStateListener l:automationStateListeners)
        {
            l.automationInitialValueChanged(this, init);
        }
    }

    private void notifyLoopEnabled(boolean enabled)
    {
        for(AutomationStateListener l:automationStateListeners)
        {
            l.automationLoopEnabled(this, enabled);
        }
    }

    private void notifyNameChanged(String newName)
    {
        for(AutomationStateListener l:automationStateListeners)
        {
            l.automationNameHasChanged(this, newName);
        }
    }

    private void notifyCurrentValueChanged(double currentValue)
    {
        for(AutomationValueListener l:automationValueListeners)
        {
            l.automationValueHasChanged(this, currentValue);
        }
    }

    private void notifyStepDelayHasChanged(double step)
    {
        for(AutomationStateListener l:automationStateListeners)
        {
            l.automationStepHasChanged(this, step);
        }
    }

    private void swap(int i1, int i2)
    {
        Generator g1 = generators.get(i1);
        Generator g2 = generators.get(i2);
        generators.set(i1, g2);
        generators.set(i2, g1);
        System.out.println("generator "+g2.getGeneratorName()+" is now at position "+i1);
        System.out.println("generator "+g1.getGeneratorName()+" is now at position "+i2);
        notifyGeneratorSwap(g1,g2);
    }
}
