/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.toolkit;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.prefs.Preferences;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * a file chooser that keeps track of the last visited directory
 * @author nnovic
 */
public class XFileChooser
extends JFileChooser
implements ActionListener
{
    public static final String MODBUSPAL_REG_PATH = "modbuspal";
    public static final int PROJECT_FILE = 0;
    public static final int SLAVE_FILE = 1;
    public static final int AUTOMATION_FILE = 2;
    public static final int RECORDER_FILE = 3;

    public static final String PROJECT_EXT = "xmpp";
    public static final String SLAVE_EXT = "xmps";
    public static final String AUTOMATION_EXT = "xmpa";
    public static final String RECORDER_EXT = "xmpr";

    private static final String REG_KEYS[] =   {"project_file", "slave_file",        "automation_file",        "recorder_file"};
    private static final String PROF_TYPES[] = {"Project file", "Slave export file", "Automation export file", "Recorder file"};
    private static final String PROF_EXT[] =   {PROJECT_EXT,    SLAVE_EXT,           AUTOMATION_EXT,           RECORDER_EXT};

    private Preferences preferences;
    private int selectedProfile;

    public XFileChooser(int profile)
    {
        selectedProfile = profile;

        // create file extension filter:
        FileNameExtensionFilter filter = new FileNameExtensionFilter(PROF_TYPES[selectedProfile], PROF_EXT[selectedProfile]);
        setFileFilter(filter);

        // retrieve directory from preferences
        Preferences prefs = Preferences.userRoot();
        preferences = prefs.node(MODBUSPAL_REG_PATH);
        String dir = preferences.get(REG_KEYS[selectedProfile], null);

        // setup current directory if available
        if( dir != null )
        {
            File cwd = new File(dir);
            if( (cwd.isDirectory()==true) && (cwd.exists()==true) )
            {
                setCurrentDirectory(cwd);
            }
        }

        addActionListener(this);
    }


    private File forceFileExtension(File file)
    {
        if( file!=null )
        {
            String path = file.getPath();
            if( path.endsWith(PROF_EXT[selectedProfile])==false )
            {
                return new File( path+"."+PROF_EXT[selectedProfile]);
            }
        }
        return file;
    }

    @Override
    public File getSelectedFile()
    {
        File selected = super.getSelectedFile();
        return forceFileExtension(selected);
    }

    @Override
    public File[] getSelectedFiles() {
        return super.getSelectedFiles();
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        if( e.getActionCommand().compareTo(JFileChooser.APPROVE_SELECTION)==0 )
        {
            String dir = this.getCurrentDirectory().getAbsolutePath();
            preferences.put(REG_KEYS[selectedProfile], dir);
        }
    }
}
