/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.link;

import java.io.IOException;
import modbuspal.main.ModbusRequest;

/**
 *
 * @author nnovic
 */
public interface ModbusLink
{
    public void start(ModbusLinkListener l) throws IOException;

    public void stop();

    public void execute(ModbusRequest req);
}
