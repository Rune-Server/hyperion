package org.hyperion.rs2.net;

import org.apache.mina.core.future.IoFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.hyperion.rs2.Constants;
import org.hyperion.rs2.model.Item;
import org.hyperion.rs2.model.Player;
import org.hyperion.rs2.model.Skills;
import org.hyperion.rs2.model.container.Equipment;
import org.hyperion.rs2.model.container.Inventory;
import org.hyperion.rs2.model.container.impl.EquipmentContainerListener;
import org.hyperion.rs2.model.container.impl.InterfaceContainerListener;
import org.hyperion.rs2.net.Packet.Type;

/**
 * A utility class for sending packets.
 * @author Graham Edgecombe
 *
 */
public class ActionSender {
	
	/**
	 * The player.
	 */
	private Player player;
	
	/**
	 * Creates an action sender for the specified player.
	 * @param player The player to create the action sender for.
	 */
	public ActionSender(Player player) {
		this.player = player;
	}
	
	/**
	 * Sends an inventory interface.
	 * @param interfaceId The interface id.
	 * @param inventoryInterfaceId The inventory interface id.
	 * @return The action sender instance, for chaining.
	 */
	public ActionSender sendInventoryInterface(int interfaceId, int inventoryInterfaceId) {
		player.getInterfaceState().interfaceOpened(interfaceId);
		player.getSession().write(new PacketBuilder(248).putShortA(interfaceId).putShort(inventoryInterfaceId).toPacket());
		return this;
	}
	
	/**
	 * Sends all the login packets.
	 * @return The action sender instance, for chaining.
	 */
	public ActionSender sendLogin() {
		player.setActive(true);
		sendDetails();
		sendMessage("Welcome to RuneScape.");
		sendMapRegion();
		sendSidebarInterfaces();
		sendSkills();
		
		InterfaceContainerListener inventoryListener = new InterfaceContainerListener(player, Inventory.INTERFACE);
		player.getInventory().addListener(inventoryListener);
		
		InterfaceContainerListener equipmentListener = new InterfaceContainerListener(player, Equipment.INTERFACE);
		player.getEquipment().addListener(equipmentListener);
		player.getEquipment().addListener(new EquipmentContainerListener(player));
		
		return this;
	}

	/**
	 * Sends the packet to construct a map region.
	 * @param palette The palette of map regions, int[13][13][2] array.
	 * @return The action sender instance, for chaining.
	 */
	public ActionSender sendConstructMapRegion(int[][][] palette) {
		player.setLastKnownRegion(player.getLocation());

		PacketBuilder bldr = new PacketBuilder(241, Type.VARIABLE_SHORT);
		bldr.putShortA(player.getLocation().getRegionY() + 6);
		bldr.startBitAccess();
		for(int z = 0; z < 4; z++) {
			for(int x = 0; x < 13; x++) {
				for(int y = 0; y < 13; y++) {
					boolean flag = z == player.getLocation().getZ();
					bldr.putBits(1, flag ? 1 : 0);
					if(flag) {
						bldr.putBits(26, palette[x][y][0] << 14 | palette[x][y][1] << 3);
					}
				}
			}
		}
		bldr.finishBitAccess();
		bldr.putShort(player.getLocation().getRegionX() + 6);
		player.getSession().write(bldr.toPacket());
		return this;
	}

	/**
	 * Sends the initial login packet (e.g. members, player id).
	 * @return The action sender instance, for chaining.
	 */
	public ActionSender sendDetails() {
		player.getSession().write(new PacketBuilder(249).putByteA(player.isMembers() ? 1 : 0).putLEShortA(player.getIndex()).toPacket());
		player.getSession().write(new PacketBuilder(107).toPacket());
		return this;
	}
	
	/**
	 * Sends the player's skills.
	 * @return The action sender instance, for chaining.
	 */
	public ActionSender sendSkills() {
		for(int i = 0; i < Skills.SKILL_COUNT; i++) {
			sendSkill(i);
		}
		return this;
	}
	
	/**
	 * Sends a specific skill.
	 * @param skill The skill to send.
	 * @return The action sender instance, for chaining.
	 */
	public ActionSender sendSkill(int skill) {
		PacketBuilder bldr = new PacketBuilder(134);
		bldr.put((byte) skill);
		bldr.putInt1((int) player.getSkills().getExperience(skill));
		bldr.put((byte) player.getSkills().getLevel(skill));
		player.getSession().write(bldr.toPacket());
		return this;
	}

	/**
	 * Sends all the sidebar interfaces.
	 * @return The action sender instance, for chaining.
	 */
	public ActionSender sendSidebarInterfaces() {
		final int[] icons = Constants.SIDEBAR_INTERFACES[0];
		final int[] interfaces = Constants.SIDEBAR_INTERFACES[1];
		for(int i = 0; i < icons.length; i++) {
			sendSidebarInterface(icons[i], interfaces[i]);
		}
		return this;
	}
	
	/**
	 * Sends a specific sidebar interface.
	 * @param icon The sidebar icon.
	 * @param interfaceId The interface id.
	 * @return The action sender instance, for chaining.
	 */
	public ActionSender sendSidebarInterface(int icon, int interfaceId) {
		player.getSession().write(new PacketBuilder(71).putShort(interfaceId).putByteA(icon).toPacket());
		return this;
	}
	
	/**
	 * Sends a message.
	 * @param message The message to send.
	 * @return The action sender instance, for chaining.
	 */
	public ActionSender sendMessage(String message) {
		player.getSession().write(new PacketBuilder(253, Type.VARIABLE).putRS2String(message).toPacket());
		return this;
	}
	
	/**
	 * Sends the map region load command.
	 * @return The action sender instance, for chaining.
	 */
	public ActionSender sendMapRegion() {
		player.setLastKnownRegion(player.getLocation());
		player.getSession().write(new PacketBuilder(73).putShortA(player.getLocation().getRegionX() + 6).putShort(player.getLocation().getRegionY() + 6).toPacket());
		return this;
	}
	
	/**
	 * Sends the logout packet.
	 * @return The action sender instance, for chaining.
	 */
	public ActionSender sendLogout() {
		player.getSession().write(new PacketBuilder(109).toPacket()).addListener(new IoFutureListener<IoFuture>() {
			@Override
			public void operationComplete(IoFuture future) {
				future.getSession().close(false);
			}
		});
		return this;
	}
	
	/**
	 * Sends a packet to update a group of items.
	 * @param interfaceId The interface id.
	 * @param items The items.
	 * @return The action sender instance, for chaining.
	 */
	public ActionSender sendUpdateItems(int interfaceId, Item[] items) {
		PacketBuilder bldr = new PacketBuilder(53, Type.VARIABLE_SHORT);
		bldr.putShort(interfaceId);
		bldr.putShort(items.length);
		for(Item item : items) {
			if(item != null) {
				int count = item.getCount();
				if(count > 254) {
					bldr.put((byte) 255);
					bldr.putInt2(count);
				} else {
					bldr.put((byte) count);
				}
				bldr.putLEShortA(item.getId() + 1);
			} else {
				bldr.put((byte) 0);
				bldr.putLEShortA(0);
			}
		}
		player.getSession().write(bldr.toPacket());
		return this;
	}

	/**
	 * Sends a packet to update a single item.
	 * @param interfaceId The interface id.
	 * @param slot The slot.
	 * @param item The item.
	 * @return The action sender instance, for chaining.
	 */
	public ActionSender sendUpdateItem(int interfaceId, int slot, Item item) {
		PacketBuilder bldr = new PacketBuilder(34, Type.VARIABLE_SHORT);
		bldr.putShort(interfaceId).put((byte) slot);
		if(item != null) {
			bldr.putShort(item.getId() + 1);
			int count = item.getCount();
			if(count > 254) {
				bldr.put((byte) 255);
				bldr.putInt(count);
			} else {
				bldr.put((byte) count);
			}
		} else {
			bldr.putShort(0);
			bldr.put((byte) 0);
		}
		player.getSession().write(bldr.toPacket());
		return this;
	}

}
