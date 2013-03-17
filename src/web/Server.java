package web;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Server implements Runnable {

	private ServerSocket server;
	private final String webRoot;
	private ExecutorService threadsPool;

	private final int port;
	private final int threadsLimit;


	public static void main(String[] args) {
		int port = 8080;
		String webRoot = "wwwroot";
		int maxThreads = 10;
		if (args.length == 0 || args[0].equals("-h") || args[0].equals("-help"))
			System.out.println("Usage: java -cp WebServer.jar web.Server <port> <web root> <threads limit>\n");
		else {
			port = Integer.parseInt(args[0]);
			webRoot = args[1];
			maxThreads = Integer.parseInt(args[2]);
		}
		new Thread(new Server(port, webRoot, maxThreads)).start();
	}

	public Server(int port, String webRoot, int maxThreads) {
		this.port = port;
		this.threadsLimit = maxThreads;
		this.webRoot = webRoot;
	}

	@Override
	public void run() {
		try {
			server = new ServerSocket(port);
			threadsPool = Executors.newFixedThreadPool(threadsLimit);
		} catch (IOException e) {
			System.err.println("Cannot listen on port " + port);
			System.exit(1);
		}
		
		System.out.println("Running server on the port " + port + 
				" with web root folder \"" + webRoot + "\" and " + threadsLimit + " threads limit.");

		while (!Thread.interrupted()) {
			try {
				threadsPool.execute(new Thread(new Connection(server.accept(), this)));
			} catch (IOException e) {
				System.err.println("Cannot accept client.");
			}
		}
		close();
	}

	public void close() {
		try {
			server.close();
		} catch (IOException e) {
			System.err.println("Error while closing server socket.");
		}
		threadsPool.shutdown();
		try {
			if (!threadsPool.awaitTermination(10, TimeUnit.SECONDS)) 
				threadsPool.shutdownNow();
		} catch (InterruptedException e) {}
	}

	public String getWebRoot() {
		return webRoot;
	}

}
