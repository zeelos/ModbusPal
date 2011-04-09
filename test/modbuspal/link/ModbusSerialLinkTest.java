/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.link;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author nnovic
 */
public class ModbusSerialLinkTest
{
    public ModbusSerialLinkTest()
    {
    }

    @BeforeClass
    public static void setUpClass()
    throws Exception
    {
    }

    @AfterClass
    public static void tearDownClass()
    throws Exception
    {
    }

    @Before
    public void setUp()
    {
    }

    @After
    public void tearDown()
    {
    }

    @Test
    public void test_computeCRC()
    {
        byte buffer[] = new byte[6];
        buffer[0] = (byte)0xF7;
        buffer[1] = (byte)0x03;
        buffer[2] = (byte)0x00;
        buffer[3] = (byte)0x02;
        buffer[4] = (byte)0x00;
        buffer[5] = (byte)0x10;
        int result = ModbusSerialLink.computeCRC(buffer,0,6);
    }

}