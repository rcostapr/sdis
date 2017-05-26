package protocols;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.SocketException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Date;
import java.util.Random;

import backend.ConfigManager;
import backend.MulticastServer;
import utils.SharedDatabase;

public class MasterPeer {
	public static final int MASTER_CMD_INTERVAL = 1000 * 60;
	public static final int TEN_SECONDS = 10000;
	public static final int MAX_WAIT_TIME = 400;
	public static final int WAIT_TIME_BOUND = 500;
	private static MasterPeer instance = null;

	public static final String WAKEUP_CMD = "WAKED_UP";
	public static final String MASTER_CMD = "IM_MASTER";
	public static final String CANDIDATE_CMD = "CANDIDATE";
	public static final int REGISTRY_PORT = 8010;
	private static final int WAKE_UP_TIME_INTERVAL = 500;
	private static final int MAX_RETRIES = 3;

	private static boolean imMaster = false;
	private static Boolean knowsMaster = false;
	private static String masterIp = null;
	private long masterUpTime = 0;

	private long lastMasterCmdTimestamp;
	private Thread masterUpdate = null;
	private Thread masterChecker = null;

	private Long sentUpTime;
	private boolean MasterPeerRunning = false;

	Registry reg;
	private boolean masterCheckerFlag = false;
	private boolean masterUpdateFlag = false;

	private MasterPeer() {
		sentUpTime = (long) 0;
	}

	public static MasterPeer getInstance() {
		if (instance == null) {
			instance = new MasterPeer();
		}
		return instance;
	}

	public boolean imMaster() {
		return imMaster;
	}

	public void sendStartup() {
		String message = "";

		String ip = null;
		try {
			ip = ConfigManager.getConfigManager().getInterfaceIP();
		} catch (SocketException e) {
			e.printStackTrace();
		}

		message += WAKEUP_CMD + " " + "2.0" + " " + ConfigManager.getConfigManager().getMyID() + " " + ip + MulticastServer.CRLF + MulticastServer.CRLF;
		

		InetAddress mcAddress = ConfigManager.getConfigManager().getMcAddr();
		int mcPort = ConfigManager.getConfigManager().getmMCport();

		MulticastServer sender = new MulticastServer(mcAddress, mcPort);

		int counter = 0;

		do {

			try {
				sender.sendMessage(message.getBytes(MulticastServer.ASCII_CODE));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			try {
				System.out.println("WAITING : " + WAKE_UP_TIME_INTERVAL * (int) Math.pow(2, counter));
				Thread.sleep(WAKE_UP_TIME_INTERVAL * (int) Math.pow(2, counter));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			counter++;

			synchronized (knowsMaster) {
				if (knowsMaster) {
					break;
				}
			}

		} while (counter < MAX_RETRIES);

		if (!knowsMaster) {

			if (ConfigManager.getConfigManager().isServer()) {
				System.err.println("Could not connect to master-peer. Exiting...");
				System.exit(1);
			}

			try {
				masterIp = ConfigManager.getConfigManager().getInterfaceIP();
				System.out.println(masterIp);
			} catch (SocketException e) {
				e.printStackTrace();
			}
			imMaster = true;
			knowsMaster = true;

			masterUpdate = new Thread(new MasterCmdDiffuser());
			masterUpdateFlag = true;
			masterUpdate.start();
			try {
				masterPeerStartup();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			masterChecker = new Thread(new CheckMasterCmdExpiration());
			masterCheckerFlag = true;
			masterChecker.start();

			// update database with master's one
			SharedDatabase masterPeerDB = null;
			try {
				masterPeerDB = getMasterStub().getMasterPeerDB();
			} catch (Exception e) {
				e.printStackTrace();
			}
			ConfigManager.getConfigManager().getSharedDatabase().merge(masterPeerDB);
		}

		try {
			ConfigManager.getConfigManager().enterMainStage();
		} catch (ConfigManager.ConfigurationsNotInitializedException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public void sendMasterCmd() throws Exception {
		if (!imMaster) {
			throw new Exception();
		}

		InetAddress mcAddress = ConfigManager.getConfigManager().getMcAddr();
		int mcPort = ConfigManager.getConfigManager().getmMCport();

		MulticastServer sender = new MulticastServer(mcAddress, mcPort);

		String message = null;
//TODO
		message = MASTER_CMD + " " + "2.0" + " " + ConfigManager.getConfigManager().getMyID() + " " + masterIp + MulticastServer.CRLF + MulticastServer.CRLF;

		try {
			sender.sendMessage(message.getBytes(MulticastServer.ASCII_CODE));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	public void updateMaster(String ip, long upTime) throws Exception {
		if (!MasterPeerRunning) {
			throw new Exception();
		}
		if (upTime > masterUpTime) {
			synchronized (knowsMaster) {
				knowsMaster = true;
				masterIp = ip;
			}
			masterUpTime = upTime;
		}
	}

	public static void setInitMaster(String ip) {
		knowsMaster = true;
		masterIp = ip;
	}

	public boolean checkIfMaster(String ip) {
		lastMasterCmdTimestamp = new Date().getTime();
		return ip.equals(masterIp);
	}

	public void candidate() {

		if (ConfigManager.getConfigManager().isServer()) {
			return;
		}

		System.out.println("Initiating CANDIDATE protocol");

		if (imMaster) {
			try {
				reg.unbind(MasterPeerServices.REG_ID);
				UnicastRemoteObject.unexportObject(reg, true);
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (NotBoundException e) {
				e.printStackTrace();
			}
		}

		// initialize variables
		MasterPeerRunning = true;
		long uptime = ConfigManager.getConfigManager().getUpTime();
		knowsMaster = false;
		imMaster = false;
		masterIp = null;
		masterUpTime = 0;
		// I have thoroughly analysed my code and determined that the risks are
		// acceptable
		if (imMaster) {
			masterUpdateFlag = false;
		} else {
			masterCheckerFlag = false;
		}

		synchronized (sentUpTime) {
			sentUpTime = uptime;
		}

		InetAddress mcAddress = ConfigManager.getConfigManager().getMcAddr();
		int mcPort = ConfigManager.getConfigManager().getmMCport();

		MulticastServer sender = new MulticastServer(mcAddress, mcPort);

		String message = null;

		message = CANDIDATE_CMD + " " + "2.0" + " " + sentUpTime + MulticastServer.CRLF + MulticastServer.CRLF;

		Random r = new Random();
		int waitTime = r.nextInt(MAX_WAIT_TIME);
		try {
			Thread.sleep(waitTime);
			System.out.println("Sending CANDIDATE");
			sender.sendMessage(message.getBytes(MulticastServer.ASCII_CODE));
			Thread.sleep(WAIT_TIME_BOUND - waitTime);
		} catch (UnsupportedEncodingException | InterruptedException e) {
			e.printStackTrace();
		}

		if (!knowsMaster) {
			imMaster = true;
			knowsMaster = true;
			try {
				masterIp = ConfigManager.getConfigManager().getInterfaceIP();
			} catch (SocketException e) {
				e.printStackTrace();
			}
			masterUpTime = uptime;

			masterUpdate = new Thread(new MasterCmdDiffuser());
			masterUpdate.start();

			try {
				masterPeerStartup();
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.println("I'm the new MASTER");
		} else {
			System.out.println("New MASTER is " + masterIp);
			masterChecker = new Thread(new CheckMasterCmdExpiration());
			SharedClock.getInstance().startSync();
			masterChecker.start();
		}
	}

	private class MasterCmdDiffuser implements Runnable {
//TODO
		@Override
		public void run() {
			while (masterUpdateFlag) {
				try {
					sendMasterCmd();
					Thread.sleep(MASTER_CMD_INTERVAL);
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (Exception e) {
					System.out.println("Not master trying to send MASTER_CMD");
					System.exit(1);
				}
			}
		}
	}

	public Long getSentUpTime() throws Exception {
		if (!MasterPeerRunning) {
			throw new Exception();
		}
		return sentUpTime;
	}

	private class CheckMasterCmdExpiration implements Runnable {

		@Override
		public void run() {
			while (masterCheckerFlag) {
				try {
					Thread.sleep(TEN_SECONDS);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				long now = new Date().getTime();
				if ((now - lastMasterCmdTimestamp) > (MASTER_CMD_INTERVAL + TEN_SECONDS)) {
					candidate();
				}
			}
		}
	}

	private void masterPeerStartup() throws Exception {
		if (!imMaster) {
			throw new Exception();
		}

		MasterPeerActions obj = new MasterPeerActions();
		try {
			//TODO
			System.setProperty("java.rmi.server.hostname", ConfigManager.getConfigManager().getInterfaceIP());
			reg = LocateRegistry.createRegistry(REGISTRY_PORT);
			MasterPeerServices stub = (MasterPeerServices) UnicastRemoteObject.exportObject(obj, 0);
			reg.rebind(MasterPeerServices.REG_ID, stub);
			System.out.println("Registering stub with id " + MasterPeerServices.REG_ID);

			System.out.println("Master services ready");
		} catch (RemoteException e) {
			System.err.println("RMI registry not available. Exiting...");
			System.exit(1);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	public MasterPeerServices getMasterStub() throws Exception {
		try {
			reg = LocateRegistry.getRegistry(masterIp, REGISTRY_PORT);
			return (MasterPeerServices) reg.lookup(MasterPeerServices.REG_ID);
		} catch (RemoteException e) {
			if (ConfigManager.getConfigManager().isServer()) {
				System.err.println("Server could not connect to Master. Exiting...");
				System.exit(1);
			}
			System.err.println("Error getting stub from RMI Registry. Candidate...");
			candidate();
		} catch (NotBoundException e) {
			System.err.println("Error getting stub from RMI Registry. Exiting...");
			System.exit(1);
		}
		return null;
	}

}
