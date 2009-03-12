/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.main;

import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

/**
 *
 * @author avincon
 */
public class NumericTextField
extends JTextField
{


    @Override
    protected Document createDefaultModel()
    {
        return new NumericDocument();
    }

    static class NumericDocument
    extends PlainDocument
    {
        @Override
        public void insertString(int offs, String str, AttributeSet a)
        throws BadLocationException
        {
            if (str == null)
            {
                return;
            }
            for (int i=0; i<str.length(); i++)
            {
                char c = str.charAt(i);
                switch(c)
                {
                    case '0':
                    case '1':
                    case '2':
                    case '3':
                    case '4':
                    case '5':
                    case '6':
                    case '7':
                    case '8':
                    case '9':
                    case '.':
                        super.insertString(offs++, str.substring(i,i+1), a);
                        break;
                    case '-':
                        if( offs==0 )
                        {
                            super.insertString(offs++, str.substring(i,i+1), a);
                        }
                        break;
                }
            }
        }
    }
}
