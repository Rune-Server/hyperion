package com.grahamedgecombe.rs2.net;

import org.apache.mina.core.session.IoSession;

import com.grahamedgecombe.rs2.model.Player;
import com.grahamedgecombe.rs2.packet.*;

public class PacketManager {
	
	private static PacketManager manager = new PacketManager();
	
	public static PacketManager getPacketManager() {
		return manager;
	}
	
	private PacketHandler[] packetHandlers = new PacketHandler[256];
	
	public PacketManager() {
		/*
		 * Set handlers.
		 */
		// keep alive
		packetHandlers[0] = new QuietPacketHandler();
		// region load
		packetHandlers[121] = new QuietPacketHandler();
		// click
		packetHandlers[241] = new QuietPacketHandler();
		// action button
		packetHandlers[185] = new ActionButtonPacketHandler();
		
		/*
		 * Set default handlers.
		 */
		final PacketHandler defaultHandler = new DefaultPacketHandler();
		for(int i = 0; i < packetHandlers.length; i++) {
			if(packetHandlers[i] == null) {
				packetHandlers[i] = defaultHandler;
			}
		}
	}

	public void handle(IoSession session, Packet packet) {
		packetHandlers[packet.getOpcode()].handle((Player) session.getAttribute("player"), packet);
	}

}
