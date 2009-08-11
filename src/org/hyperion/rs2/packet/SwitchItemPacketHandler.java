package org.hyperion.rs2.packet;

import org.hyperion.rs2.model.Player;
import org.hyperion.rs2.model.container.Inventory;
import org.hyperion.rs2.net.Packet;

/**
 * Switch item packet handler.
 * @author Graham
 *
 */
public class SwitchItemPacketHandler implements PacketHandler {

	@Override
	public void handle(Player player, Packet packet) {
		int interfaceId = packet.getLEShortA();
		packet.getByteC();
		int fromSlot = packet.getLEShortA();
		int toSlot = packet.getLEShort();
		
		switch(interfaceId) {
		case Inventory.INTERFACE:
			if(fromSlot >= 0 && fromSlot < Inventory.SIZE && toSlot >= 0 && toSlot < Inventory.SIZE && toSlot != fromSlot) {
				player.getInventory().swap(fromSlot, toSlot);
			}
			break;
		}
	}

}
