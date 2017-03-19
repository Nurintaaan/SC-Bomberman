package Server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class Main
{
	// Default values, for my testing in Eclipse project directory.
	public static int numberOfPlayer = 2;
	public static String mapFile = "defaultmap.txt";
	public static String classPath = "bin/";
	public static String[] classNames = {"ContohAI1", "ContohAI2"};
	public static String boardMap = "0....\n" + "B#X#P\n" + ".....\n";
	
	public static void main(String[] args) {		
		try {
			// NOTE: COMMENT EVERYTHING INSIDE THIS TRY-BLOCK IF YOU ARE USING ECLIPSE.
			// `args` will be in form of: {<map>, <classPath>, <playerClassName> ...}
			// (minimum length of `args[]` should be at least 3)
			if (args.length < 3) {
				throw new Exception("Missing required parameters.");
			}
			// Read map-file-source and class-path
			mapFile = args[0];
			classPath = args[1];
			// Read map file and build `boardMap`
			BufferedReader bufferedReader = new BufferedReader(new FileReader(mapFile));
			String line = bufferedReader.readLine();
			boardMap = "";
			while (line != null) {
				boardMap += line + "\n";
				line = bufferedReader.readLine();
			}
			// Get player class names
			numberOfPlayer = args.length - 2;
			classNames = new String[numberOfPlayer];
			for (int argsIndex = 2; argsIndex < args.length; argsIndex++) {
				int playerIndex = argsIndex - 2;
				classNames[playerIndex] = args[argsIndex];
				// Check if file exists
				String pathToFile = classPath + args[argsIndex] + ".class";
				File file = new File(pathToFile);
				if (!file.exists()) {
					throw new Exception(pathToFile + " does not exists.");
				}
			}
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
			System.out.println();
			System.out.println("Usage:");
			System.out.println("  java Server/Main <map> <classPath> <player1> <player2> ...");
			System.out.println();
			System.out.println("Example:");
			System.out.println("  java Server/Main defaultmap.txt AI/ ContohAI1 ContohAI2");
			System.out.println("  (Compile `ContohAI1.java` and `ContohAI2.java` first!)");
			System.out.println();
			System.out.println("Where:");
			System.out.println("  * <map> is a text file that contains the map.");
			System.out.println("  * <classPath> is the location of AI .class files relative to this directory.");
			System.out.println("  * <playerN> is the AI-program class-name for player-N.");
			System.exit(0);
		}
		
		// Initiate game machine
		GameMachine.initiate(numberOfPlayer, classNames, boardMap);
				
		// Prepare player processes (also run in the background)
		PlayerProcess[] processes = new PlayerProcess[numberOfPlayer];
		for (int i = 0; i < numberOfPlayer; i++) {
			processes[i] = new PlayerProcess(classPath, classNames[i]);
		}
				
		// Create threads that run `PlayerProcessBridge`
		// (Responsible for connecting `playerProcess` and `GameMachine` I/O).
		Thread[] processThreads = new Thread[numberOfPlayer];
		for (int i = 0; i < numberOfPlayer; i++) {
			int threadId = i;
			processThreads[i] = new Thread() {				
				public void run() {
					String playerName = classNames[threadId];
					PlayerProcess playerProcess = processes[threadId];
					PlayerProcessBridge processBridge = new PlayerProcessBridge(playerName, playerProcess);
					processBridge.run();
				}
			};
		}
		
		// Run the game and run each threads
		Thread machineThread = new Thread() {
			public void run() {
				GameMachine.run();
			}
		};
		machineThread.start();
		for (int i = 0; i < numberOfPlayer; i++) {
			Thread iterProcessThread = processThreads[i];
			iterProcessThread.start();
		}
		
	}
}
