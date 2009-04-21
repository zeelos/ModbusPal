/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package modbuspal.automation;

import modbuspal.generator.Generator;
import modbuspal.generator.GeneratorFactory;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import modbuspal.generator.GeneratorListener;
import modbuspal.main.XMLTools;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Defines an automation.
 * @author nnovic
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
    private ArrayList<GeneratorListener> generatorListeners = new ArrayList<GeneratorListener>();
    private double currentValue = 0.0;
    private double initialValue = 0.0;

    /**
     * This is the constructor to use when creating the automation from an
     * xml file (i.e: loading a xmpp project). It uses the "loadAttributes"
     * function to parse the "importable" attributes of the "automation" tag,
     * and it also parses the non-importable attributes (like "id").
     * @param attributes xml attributes of the "automation" node
     */
    public Automation(NamedNodeMap attributes)
    {
        loadAttributes(attributes);
    }

    /**
     * This is the constructor that is used to create an new, empty automation.
     * You have to provide a name that is unique in the scope of the current project.
     * The constructor won't check that it is unique; the ModbusPal.addAutomation()
     * may modify it if it isn't. You can also use the ModbusPal.automationExists()
     * method to verify if a name is unique.
     * @param name name of the automation
     */
    public Automation(String name)
    {
        uniqueName = name;
    }

    /**
     * Changes the order of the generators by putting the specified generator
     * one rank down in the list. A "generators swapped" event is then fired.
     * Usually, you don't call this method directly; it is triggered by clicking
     * on the "down" button of a GeneratorRenderer.
     * @param gen the generator to push down
     */
    void down(Generator gen)
    {
        int index = generators.indexOf(gen);
        int max = generators.size();
        if( (index+1) < max )
        {
            swap(index,index+1);
        }
    }

    /**
     * Gets a list of the generators in this automation.
     * @return an array containing the references of all the
     * generators in this automation.
     */
    Generator[] getGenerators()
    {
        Generator[] list = new Generator[0];
        return generators.toArray(list);
    }


    /**
     * Setup the attributes of the automation by extracting
     * the values from an xml node.
     * @param attributes the xml node representing the attributes
     * of the automation
     */
    public void loadAttributes(NamedNodeMap attributes)
    {
        Node stepNode = attributes.getNamedItem("step");
        String stepValue = stepNode.getNodeValue();
        double newStepDelay = Double.parseDouble(stepValue);
        if( stepDelay != newStepDelay )
        {
            stepDelay = newStepDelay;
            fireStepDelayHasChanged(stepDelay);
        }
        
        Node loopNode = attributes.getNamedItem("loop");
        String loopValue = loopNode.getNodeValue();
        boolean newLoop = Boolean.parseBoolean(loopValue);
        if( loop != newLoop )
        {
            loop = newLoop;
            fireLoopEnabled(loop);
        }

        Node nameNode = attributes.getNamedItem("name");
        String nameValue = nameNode.getNodeValue();
        if( uniqueName.compareTo(nameValue) != 0 )
        {
            uniqueName = nameValue;
            fireNameChanged(uniqueName);
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
                fireInitialValueChanged(initialValue);
            }
        }
    }

    /**
     * Returns the duration of the steps, in seconds. A step is the delay
     * between two updates of the automation's current value.
     * @return the duration of the steps, in seconds.
     */
    public double getStepDelay()
    {
        return stepDelay;
    }

    /**
     * Adds a generator at the end of the current list of generators.
     * @param gen the generator to add
     * @return index of the added generator
     */
    public int addGenerator(Generator gen)
    {
        generators.add(gen);
        int index = generators.indexOf(gen);
        fireGeneratorAdded(gen,index);
        return index;
    }

    /**
     * Removes a generator from the current list of generators.
     * @param gen the generator to remove.
     */
    public void removeGenerator(Generator gen)
    {
        generators.remove(gen);
        fireGeneratorRemoved(gen);
    }

    /**
     * Removes all generators from the automation.
     */
    public void removeAllGenerators()
    {
        Generator list[] = new Generator[0];
        list = generators.toArray(list);
        for( int i=0; i<list.length; i++ )
        {
            removeGenerator( list[i] );
        }
    }


    /**
     * Removes all generators from the automation.
     */
    public void removeAllGenerators(String classname)
    {
        Generator list[] = new Generator[0];
        list = generators.toArray(list);
        for( int i=0; i<list.length; i++ )
        {
            if( list[i].getClassName().compareTo(classname)==0 )
            {
                removeGenerator( list[i] );
            }
        }
    }


    void notifyGeneratorHasEnded(Generator gen)
    {
        for(GeneratorListener l:generatorListeners)
        {
            l.generatorHasEnded(gen);
        }
    }

    void notifyGeneratorHasStarted(Generator gen)
    {
        for(GeneratorListener l:generatorListeners)
        {
            l.generatorHasStarted(gen);
        }
    }



    public void addGeneratorListener(GeneratorListener l)
    {
        assert( generatorListeners.contains(l) == false );
        generatorListeners.add(l);
    }

    public void removeGeneratorListener(GeneratorListener l)
    {
        assert( generatorListeners.contains(l) == true );
        generatorListeners.remove(l);
    }



    /**
     * This method is used to add generators into the automation, by
     * using the content of the node list.
     * @param nodes
     * @throws java.lang.InstantiationException
     * @throws java.lang.IllegalAccessException
     */
    public void loadGenerators(NodeList nodes)
    throws InstantiationException, IllegalAccessException
    {
        for(int i=0; i<nodes.getLength(); i++)
        {
            Node node = nodes.item(i);
            if( node.getNodeName().compareTo("generator")==0 )
            {
                String className = XMLTools.getAttribute("class", node);
                Generator gen = GeneratorFactory.newGenerator( className );

                if( gen==null )
                {
                    throw new InstantiationException("Generator "+className+" cannot be instanciated");
                }
                
                gen.load(node);
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

    /**
     * Changes the name of this automation. Caution: this method
     * won't check that it is unique, but it has to be. A "name changed"
     * events is then fired.
     * @param newName the new name  of the automation.
     */
    public void setName(String newName)
    {
        uniqueName = newName;
        fireNameChanged(uniqueName);
    }

    /**
     * Creates and starts a new thread in order to execute this
     * automation. The thread's name is set to the name of the automation.
     * You can only have on running thread at the same time.
     */
    public void start()
    {
        if(thread==null)
        {
            thread = new Thread(this);
            thread.setName(uniqueName);
            suspended = false;
            quit = false;
            thread.start();
        }
    }

    /**
     * Stops the execution of the automation. The method will be blocked
     * until the thread is terminated.
     */
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

    /**
     * Suspends the execution of the automation.
     */
    public void suspend()
    {
        suspended = true;
    }

    /**
     * Resumes the execution of the automation.
     */
    public void resume()
    {
        suspended = false;
        synchronized(this)
        {
            notifyAll();
        }
    }

    /**
     * Check if the "loop" option is enabled for this automation.
     * If so, the execution of the automation continues with the first
     * generator of the list when the last generator is finished.
     * @return true if "loop" is enabled, false otherwise
     */
    public boolean isLoopEnabled()
    {
        return loop;
    }

    /**
     * Defines if "loop" option is enabled or not.
     * @param enabled
     */
    void setLoopEnabled(boolean enabled)
    {
        loop = enabled;
    }

    /**
     * Returns the name of this automation.
     * @return name of the automation.
     */
    public String getName()
    {
        return uniqueName;
    }

    /**
     * Returns the initial value that is configured for this automation.
     * @return initial value of the automation.
     */
    double getInitialValue()
    {
        return initialValue;
    }

    /**
     * Returns se current value of the automation. If the automation is running,
     * this value will return the current value of the current generator.
     * Otherwise, it will return the initial value.
     * @return current value of the automation.
     */
    public double getCurrentValue()
    {
        return currentValue;
    }

    
    @Override
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

        fireAutomationHasStarted();

        while( (currentIndex < genList.length) && (quit==false) )
        {
            
            // prepare to execute generator:
            Generator currentGen = genList[currentIndex];
            currentGen.setInitialValue(currentValue);
            double duration = currentGen.getDuration();
            startTime = currentTime;
            notifyGeneratorHasStarted(currentGen);

            while( (currentTime < startTime + duration) && (quit==false) )
            {
                
                currentValue = currentGen.getValue( currentTime-startTime );
                if( previousValue != currentValue )
                {
                    fireCurrentValueChanged(currentValue);
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
            notifyGeneratorHasEnded(currentGen);

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
        fireCurrentValueChanged(currentValue);

        suspended = false;
        if( quit==true )
        {
            quit = false;
        }
        else
        {
            thread=null;
        }
        fireAutomationHasEnded();
    }

    /**
     * Returns the current "suspended" state of the automation.
     * @return true if the automation is running but suspended. false
     * otherwise.
     */
    public boolean isSuspended()
    {
        return suspended;
    }


    public boolean isRunning()
    {
        return (thread!=null);
    }

    public void disconnect()
    {
        automationStateListeners.clear();
        automationValueListeners.clear();
        generatorListeners.clear();
        generators.clear();
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


    /**
     * Redefines the step delay of the automation.
     * @param delay new step delay in seconds.
     */
    void setStepDelay(double delay)
    {
        stepDelay = delay;
    }

    /**
     * Redefines the initial value of the automation.
     * @param dval
     */
    void setInitialValue(double val)
    {
        initialValue = val;
    }

    /**
     * Changes the order of the generators by putting the specified generator
     * one rank up in the list. A "generators swapped" event is then fired.
     * Usually, you don't call this method directly; it is triggered by clicking
     * on the "up" button of a GeneratorRenderer.
     * @param gen
     */
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

    private void fireAutomationHasEnded()
    {
        for(AutomationStateListener l:automationStateListeners)
        {
            l.automationHasEnded(this);
        }
    }

    private void fireAutomationHasStarted()
    {
        for(AutomationStateListener l:automationStateListeners)
        {
            l.automationHasStarted(this);
        }
    }

    private void fireGeneratorAdded(Generator gen, int index)
    {
        for(AutomationStateListener l:automationStateListeners)
        {
            l.generatorHasBeenAdded(this, gen, index);
        }
    }

    private void fireGeneratorRemoved(Generator gen)
    {
        for(AutomationStateListener l:automationStateListeners)
        {
            l.generatorHasBeenRemoved(this, gen);
        }
    }

    private void fireGeneratorSwap(Generator g1, Generator g2)
    {
        for(AutomationStateListener l:automationStateListeners)
        {
            l.generatorsHaveBeenSwapped(this, g1, g2);
        }
    }

    private void fireInitialValueChanged(double init)
    {
        for(AutomationStateListener l:automationStateListeners)
        {
            l.automationInitialValueChanged(this, init);
        }
    }

    private void fireLoopEnabled(boolean enabled)
    {
        for(AutomationStateListener l:automationStateListeners)
        {
            l.automationLoopEnabled(this, enabled);
        }
    }

    private void fireNameChanged(String newName)
    {
        for(AutomationStateListener l:automationStateListeners)
        {
            l.automationNameHasChanged(this, newName);
        }
    }

    private void fireCurrentValueChanged(double currentValue)
    {
        for(AutomationValueListener l:automationValueListeners)
        {
            l.automationValueHasChanged(this, currentValue);
        }
    }

    private void fireStepDelayHasChanged(double step)
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
        fireGeneratorSwap(g1,g2);
    }
}
