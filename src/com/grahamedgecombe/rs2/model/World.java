package com.grahamedgecombe.rs2.model;

import java.util.logging.Logger;

import org.apache.mina.core.future.IoFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.future.WriteFuture;

import com.grahamedgecombe.rs2.GameEngine;
import com.grahamedgecombe.rs2.GenericWorldLoader;
import com.grahamedgecombe.rs2.WorldLoader;
import com.grahamedgecombe.rs2.WorldLoader.LoadResult;
import com.grahamedgecombe.rs2.net.PacketBuilder;
import com.grahamedgecombe.rs2.task.SessionLoginTask;

public class World {
	
	private static final Logger logger = Logger.getLogger(World.class.getName());
	private static final World world = new World();
	
	public static World getWorld() {
		return world;
	}

	private GameEngine engine;
	private WorldLoader loader = new GenericWorldLoader();
	
	public void init(GameEngine engine) {
		if(this.engine != null) {
			throw new IllegalStateException("The world has already been initialized.");
		} else {
			this.engine = engine;
		}
	}
	
	public WorldLoader getWorldLoader() {
		return loader;
	}
	
	public GameEngine getEngine() {
		return engine;
	}

	public void load(final PlayerDetails pd) {
		engine.getWorkService().submit(new Runnable() {
			public void run() {
				LoadResult lr = loader.loadPlayer(pd);
				PacketBuilder bldr = new PacketBuilder();
				bldr.put((byte) lr.getReturnCode());
				if(lr.getReturnCode() == 2) {
					bldr.put((byte) lr.getPlayer().getRights().toInteger());
				} else {
					bldr.put((byte) 0);
				}
				bldr.put((byte) 0);
				WriteFuture wf = pd.getSession().write(bldr.toPacket());
				if(lr.getReturnCode() != 2) {
					wf.addListener(new IoFutureListener<IoFuture>() {
						@Override
						public void operationComplete(IoFuture future) {
							future.getSession().close(false);
						}
					});
				} else {
					lr.getPlayer().getSession().setAttribute("player", lr.getPlayer());
					engine.pushTask(new SessionLoginTask(lr.getPlayer()));
				}
			}
		});
	}

	public void register(Player player) {
		logger.info("Registered player : " + player);
	}
	
	public void unregister(Player player) {
		player.getSession().close(false);
		logger.info("Unregistered player : " + player);
	}
	
}
