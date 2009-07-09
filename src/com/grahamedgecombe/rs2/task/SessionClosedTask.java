package com.grahamedgecombe.rs2.task;

import java.util.logging.Logger;

import org.apache.mina.core.session.IoSession;

import com.grahamedgecombe.rs2.GameEngine;
import com.grahamedgecombe.rs2.model.Player;
import com.grahamedgecombe.rs2.model.World;

public class SessionClosedTask implements Task {

	private static final Logger logger = Logger.getLogger(SessionClosedTask.class.getName());
	private IoSession session;
	
	public SessionClosedTask(IoSession session) {
		this.session = session;
	}

	@Override
	public void execute(GameEngine context) {
		logger.info("Session closed : " + session.getRemoteAddress());
		if(session.containsAttribute("player")) {
			Player p = (Player) session.getAttribute("player");
			World.getWorld().unregister(p);
		}
	}

}
