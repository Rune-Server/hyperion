package org.hyperion.rs2.action;

import org.hyperion.rs2.event.Event;
import org.hyperion.rs2.model.Player;

/**
 * An <code>Event</code> used for handling game actions.
 * @author blakeman8192
 * @author Graham Edgecombe
 * 
 */
public abstract class Action extends Event {
	
	/**
	 * A queue policy determines when the clients should queue up actions.
	 * @author Graham Edgecombe
	 *
	 */
	public enum QueuePolicy {
		
		/**
		 * This indicates actions will always be queued.
		 */
		ALWAYS,
		
		/**
		 * This indicates actions will never be queued.
		 */
		NEVER,
		
	}

	/**
	 * The <code>Player</code> associated with this ActionEvent.
	 */
	private Player player;

	/**
	 * Creates a new ActionEvent.
	 * @param player The player.
	 * @param delay The initial delay.
	 */
	public Action(Player player, long delay) {
		super(delay);
		this.player = player;
	}

	/**
	 * Gets the player.
	 * @return The player.
	 */
	public Player getPlayer() {
		return player;
	}
	
	/**
	 * Gets the queue policy of this action.
	 * @return The queue policy of this action.
	 */
	public abstract QueuePolicy getQueuePolicy();
	
	@Override
	public void stop() {
		super.stop();
		player.getActionQueue().processNextAction();
	}

}
