/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.toolkit;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.TransferHandler;

/**
 *
 * @author nnovic
 */
public class FileTransferHandler
extends TransferHandler
implements DropTargetListener
{
    public interface FileTransferTarget
    {
        public boolean importFiles(Component target, List<File> files);
    }
    

    private FileTransferTarget target;
    private boolean allowMultipleFiles = true;

    public FileTransferHandler(FileTransferTarget obj)
    {
        target = obj;
    }

    public void allowMultipleFiles(boolean enable)
    {
        allowMultipleFiles = enable;
    }



    private List<File> getFiles(TransferSupport support)
    {
        return getFiles(support.getTransferable());
    }

    private List<File> getFiles(Transferable transferable)
    {
        try
        {
            List<File> files = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
            return files;
        }

        catch (UnsupportedFlavorException ex)
        {
            Logger.getLogger(FileTransferHandler.class.getName()).log(Level.SEVERE, null, ex);
        }        catch (IOException ex)
        {
            Logger.getLogger(FileTransferHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }


    private boolean canImport(List<File> files)
    {
        if( files==null )
        {
            return false;
        }
        if( (files.size()>1) && (allowMultipleFiles==false) )
        {
            return false;
        }
        return true;
    }

    @Override
    public boolean canImport(TransferSupport support)
    {
        List<File> files = getFiles(support);
        return canImport(files);
    }

    private boolean canImport(Transferable transferable)
    {
        List<File> files = getFiles(transferable);
        return canImport(files);
    }


    @Override
    public boolean importData(TransferSupport support)
    {
        List<File> files = getFiles(support);
        if( files==null)
        {
            return false;
        }
        Component comp = support.getComponent();
        return target.importFiles(comp,files);
    }

    public void dragEnter(DropTargetDragEvent dtde)
    {
        // can import data ?
        if( canImport( dtde.getTransferable() ) )
        {
            dtde.acceptDrag( DnDConstants.ACTION_LINK );
        }
        else
        {
            dtde.rejectDrag();
        }
    }

    public void dragOver(DropTargetDragEvent dtde)
    {
        // can import data ?
        if( canImport( dtde.getTransferable() ) )
        {
            dtde.acceptDrag( DnDConstants.ACTION_LINK );
        }
        else
        {
            dtde.rejectDrag();
        }
    }

    public void dropActionChanged(DropTargetDragEvent dtde)
    {
    }

    public void dragExit(DropTargetEvent dte)
    {
    }

    public void drop(DropTargetDropEvent dtde)
    {
        dtde.acceptDrop( DnDConstants.ACTION_COPY );
        TransferSupport support = new TransferSupport( dtde.getDropTargetContext().getComponent(),  dtde.getTransferable() );
        dtde.dropComplete( importData(support) );
    }


}
