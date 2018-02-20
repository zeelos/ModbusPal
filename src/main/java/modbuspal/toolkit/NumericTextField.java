/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.toolkit;

import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

/**
 * a textfield that only accepts numeric values
 * @author nnovic
 */
public class NumericTextField
extends JTextField
{
    /**
     * Creates a new instance of NumericTextField without
     * an initial value
     */
    public NumericTextField()
    {

    }

    /**
     * Creates a new instance of NumericTextField.
     * @param value the initial value
     */
    public NumericTextField(int value)
    {
        setText( String.valueOf(value) );
    }

    /**
     * Creates a new instance of NumericTextField.
     * @param value the initial value
     */
    public NumericTextField(double value)
    {
        setText( String.valueOf(value) );
    }

    /**
     * Gets the value as an Integer.
     * @return the value as an Integer
     */
    public int getInteger()
    {
        return (int)getDouble();
    }

    /**
     * Gets the value as a Long.
     * @return the value as a Long
     */
    public long getLong()
    {
        return (long)getDouble();
    }

    /**
     * Sets the value of the text field
     * @param i the value
     */
    public void setValue(int i)
    {
        setText( String.valueOf(i) );
    }

    /**
     * Sets the value of the text field
     * @param i the value
     */
    public void setValue(long i)
    {
        setText( String.valueOf(i) );
    }

    /**
     * Sets the value of the text field
     * @param d the value
     */
    public void setValue(double d)
    {
        setText( String.valueOf(d) );
    }

    /**
     * Gets the value as a Double.
     * @return the value as a Double
     */
    public double getDouble()
    {
        String txt = getText();
        return Double.parseDouble(txt);
    }

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
