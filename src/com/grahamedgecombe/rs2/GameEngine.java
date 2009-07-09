package com.grahamedgecombe.rs2;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import com.grahamedgecombe.rs2.task.Task;
import com.grahamedgecombe.util.BlockingExecutorService;

public class GameEngine implements Runnable {
	
	private final BlockingQueue<Task> tasks = new LinkedBlockingQueue<Task>();
	private final BlockingExecutorService taskService = new BlockingExecutorService(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()));
	private final ExecutorService workService = Executors.newSingleThreadExecutor();
	private boolean running = false;
	private Thread thread;
	
	public ExecutorService getWorkService() {
		return workService;
	}
	
	public BlockingExecutorService getTaskService() {
		return taskService;
	}
	
	public void pushTask(Task task) {
		tasks.offer(task);
	}

	public boolean isRunning() {
		return running;
	}
	
	public void start() {
		if(running) {
			throw new IllegalStateException("The engine is already running.");
		}
		running = true;
		thread = new Thread(this);
		thread.start();
	}
	
	public void stop() {
		if(!running) {
			throw new IllegalStateException("The engine is already stopped.");
		}
		running = false;
		thread.interrupt();
	}
	
	public void run() {
		while(running) {
			try {
				tasks.take().execute(this);
			} catch (InterruptedException e) {
				continue;
			}
		}
	}

}
