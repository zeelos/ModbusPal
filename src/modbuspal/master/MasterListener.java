/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modbuspal.master;

import modbuspal.main.ModbusRequest;

/**
 *
 * @author avincon
 */
public interface MasterListener
{

    public void masterHasEnded(ModbusMaster master);

    public void masterHasStarted(ModbusMaster master);

    public void masterLinkHasBeenSetup(ModbusMaster aThis);

    public void masterLinkIsLost(ModbusMaster aThis);

    public void replyReceived(ModbusRequest request);

    public void requestTransmitted(ModbusRequest request);

}
