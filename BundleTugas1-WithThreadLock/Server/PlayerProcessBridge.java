package Server;

public class PlayerProcessBridge
{
	public static final boolean DEBUG_OUTPUT_ENABLED = false;
	public static final int TURN_TIME_LIMIT_MS = 1200;
	public static final int DELAY_DURATION_MS = 10;
	
	public String playerName = null;
	public PlayerProcess playerProcess = null;
	public int lastTurnHandled = 0;
	
	public PlayerProcessBridge(String playerName, PlayerProcess process) {
		this.playerName = playerName;
		this.playerProcess = process;
		this.lastTurnHandled = -1;
	}
	
	/**
	 * Pause program/thread execution for some duration
	 * @param msDuration - length of sleep in ms.
	 */
	public void sleep(int msDuration) {
		try {
			Thread.sleep(msDuration);
		}
		catch (InterruptedException e) {}     
	}
	
	/**
	 * Handle input/output from player process
	 */
	public void run() {   
		while (true) {
			int currentTurn = GameMachine.turn;
			boolean isProcessTimeout = false;
			
			if (currentTurn > lastTurnHandled) {
				try {					
					// Give input (board state)
					GameMachine.acquireLock();
					String boardStateString = GameMachine.getBoardStateString();
					playerProcess.sendLine(boardStateString);
					playerProcess.sendLine("END");
					
					// Declare thread that fetches player's output.
					Thread fetchOutputThread = new Thread(new Runnable() {
						public void run() {
							// Try to fetch player move (starts with ">> ")
							// If a move is detected, report it to GameMachine.
							boolean isplayerMoveObtained = false;
							while (!isplayerMoveObtained) {
								String playerMove = "";
								if (playerProcess.hasNextLine()) {
									playerMove = playerProcess.getNextLine();
									if (DEBUG_OUTPUT_ENABLED) {
										System.out.print("[" + playerName + "]: ");
										System.out.println(playerMove);
									}
									if (playerMove.startsWith(">> ")) {
										// Report player move
										String parsedPlayerMove = playerMove.substring(3);
										GameMachine.reportMove(playerName, parsedPlayerMove);
										lastTurnHandled = currentTurn;
										isplayerMoveObtained = true;
									}
								}
							}
						}
					});
					
					// Ensure the thread finishes before the deadline.
					long ouputDeadlineMS = System.currentTimeMillis() + TURN_TIME_LIMIT_MS;
					fetchOutputThread.start();
					while (fetchOutputThread.isAlive()) {
						if (System.currentTimeMillis() > ouputDeadlineMS) {
							// Report player timeout
							fetchOutputThread.interrupt();
							GameMachine.reportMove(playerName, "TIMEOUT");
							lastTurnHandled = currentTurn;
							isProcessTimeout = true;
							break;
						}
						try {
							Thread.sleep(DELAY_DURATION_MS);
						}
						catch (InterruptedException t) {}
					}
				}
				finally {
					GameMachine.releaseLock();
				}
			}
			
			// Sleep to prevent race condition (?)
			// Also, exit this thread if timeout has been detected
			sleep(DELAY_DURATION_MS);
			if (isProcessTimeout) {
				return;
			}
		}
	}
	
}
