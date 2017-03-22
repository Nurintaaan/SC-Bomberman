//TO-DO : bikin kalo deket sama powerUP
//TO-DO : bikin bunuh kalo AI playernya mati
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;

public class DiamondAI {
	static class Information{
		private int turn;
		private int cPlayer;
		private ArrayList<Player> players;

		
		public Information(){
			this.turn = 0;
			this.cPlayer = 0;
			this.players = new ArrayList<Player>();
		}

		public int getTurn(){
			return this.turn;
		}

		public void setTurn(int turn){
			this.turn = turn;
		}

		public int cPlayer(){
			return this.cPlayer;
		}
		public void setcPlayer(int cPlayer){
			this.cPlayer = cPlayer;
		}

		public ArrayList<Player> getPlayers(){
			return this.players;
		}

		public void addPlayer(Player player){
			this.players.add(player);
		}
	}

	static class Player implements Obj{
		private int playerId;
		private String playerName;
		private int bombLeft;
		private int maxBomb;
		private int range;
		private String status;
		private long score;
		private int x;
		private int y;

		public Player(int playerId, String playerName,int bombLeft, int maxBomb, int range, String status, long score){
			this.playerId = playerId;
			this.playerName = playerName;
			this.bombLeft = bombLeft;
			this.maxBomb = maxBomb;
			this.status = status;
			this.score = score;
			this.range = range;
			this.x = 0;
			this.y = 0;
		}

		public void setPosition(int x, int y){
			this.x = x;
			this.y = y;
		}

		public int getX(){
			return this.x;
		}

		public int getY(){
			return this.y;
		}

		public String getPlayerName(){
			return this.playerName;
		}

		public String getStatus(){
			return this.status;
		}

		public int getRange(){
			return this.range;
		}

		public String getType(){
			return "player";
		}
	}
	static class Power implements Obj{
		private String type;

		public Power(String type){
			this.type = type;
		}
		public String getType(){
			return this.type;
		}
	}

	static class Bomb implements Obj{
		private int range;
		private int count;
		private int x;
		private int y;

		public Bomb(int range, int count, int x, int y){
			this.range = range;
			this.count = count;
			this.x = x;
			this.y = y;
		}

		public int getX(){
			return this.x;
		}

		public int getY(){
			return this.y;
		}

		public String getType(){
			return "bomb";
		}
	}

	static class Flare implements Obj{
		int life;
		public Flare(int life){
			this.life = life;
		}

		public String getType(){
			return "flare";
		}
	}

	static class Walls implements Obj{

		private String type;
		private String power;
		private int x;
		private int y;

		public Walls(String type, int x, int y){
			this.type = type;
			this.power = "none";
			this.x = x;
			this.y = y;
		}

		public String getType(){
			return this.type;
		}

		public String getPower(){
			return this.power;
		}

		public void setPower(String power){
			this.power = power;
		}

		public int getX(){
			return this.x;
		}

		public int getY(){
			return this.y;
		}
	}

	static class Tiles {
		private ArrayList<Obj> objCol;
		private boolean isHaveWall;

		public Tiles(ArrayList<Obj> objCol){
			this.objCol = objCol;
			isHaveWall = false;
		}

		public void setObject(ArrayList<Obj> objects){
			this.objCol = objects;
		}

		public ArrayList<Obj> getObject(){
			return this.objCol;
		}

		public boolean getIsHaveWall(){
			return this.isHaveWall;
		}

		public void setIsHaveWall(boolean isHaveWall){
			this.isHaveWall = isHaveWall;
		}
	}

	interface Obj {
		public String getType();
	}

	static Tiles[][] maps ;
	static int selfId;
	static int maxRange;
	static ArrayList<Bomb> arrBomb;
	
	public static void main(String[] args) throws IOException{

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		while(true) {
			String input = "";
			Information information = new Information();
			maxRange = 1;
			arrBomb = new ArrayList<Bomb>();

			int berapa = -7;
			input = br.readLine();
			while(!input.equals("END")){
				checkMaps(input, information, berapa);
				berapa++;
				input = br.readLine();
			}

			Random random = new Random();
			random.setSeed(System.currentTimeMillis());

			int targetPlayer = random.nextInt(information.getPlayers().size());
			while(targetPlayer == selfId){
				targetPlayer = random.nextInt(information.getPlayers().size());
			}

			Player self = information.getPlayers().get(selfId);
			Player target = information.getPlayers().get(targetPlayer);

			//so plis taro algoritma dfs yang iterative disini.. AYOK NGODING MALEM INI
			int x = self.getX();
			int y = self.getY();
			System.out.println("Pos x :" + x + "Pos y: " + y);
			if(information.getPlayers().get(targetPlayer).getStatus().equals("Alive")){

				if(!isFarFromBomb(x,y)){
					flee(arrBomb,x,y);
				}else if(isNear(x,y,"+Power") || isNear(x,y,"+Bomb")){
					System.out.println(isNear(x,y,"+Power") + "Near Power");
					System.out.println(isNear(x,y,"+Bomb") + "Near Bomb");
					if(isNear(x,y,"+Power")){
						moveToPower(x,y,"+Power");
					}else{
						moveToPower(x,y,"+Bomb");
					}
					
				}else if(isNearDestructible(x,y) && isFarFromBomb(x,y)){
					System.out.println(">> DROP BOMB");
					arrBomb.add(new Bomb(self.getRange(),8,x,y));
					//terbalik ga ?
					maps[x][y].setIsHaveWall(true);
				}else if(isNear(target.getX(),target.getY(),"player")){
					System.out.println(">> DROP BOMB");
					arrBomb.add(new Bomb(self.getRange(),8,x,y));
					//terbalik ga ?
					maps[x][y].setIsHaveWall(true);
				}else if(isCanMove(x,y-1) && isFarFromBomb(x,y-1)){
					System.out.println(">> MOVE UP");		
					self.setPosition(x,y-1);		
				}else if(isCanMove(x-1,y) && isFarFromBomb(x-1,y)){
					System.out.println(">> MOVE LEFT");
					self.setPosition(x-1,y);
				}else if(isCanMove(x, y+1) && isFarFromBomb(x,y+1)){
					System.out.println(">> MOVE DOWN");
					self.setPosition(x, y+1);
				}else if(isCanMove(x+1,y) && isFarFromBomb(x+1,y)){
					System.out.println(">> MOVE RIGHT");
					self.setPosition(x+1,y);
				}else {
					System.out.println(">> STAY");
				}

				
			
			}else {
				targetPlayer = random.nextInt(information.getPlayers().size());
				while(targetPlayer == selfId){
					targetPlayer = random.nextInt(information.getPlayers().size());
				}
				System.out.println(">> STAY");
			}
		}
	} 
	public static boolean isFarFromBomb(int x, int y){
		for(int d=0; d <= maxRange; d++){
			ArrayList<Obj> collection;
			if (x+d >= 0 && y >= 0 && x+d < maps.length && y < maps[0].length){
				collection = maps[x+d][y].getObject();
				for(int i = 0; i < collection.size(); i++){
					if(collection.get(i).getType().equals("bomb")){
						return false;
					}
				}
			}
			if (x-d >= 0 && y >= 0 && x-d < maps.length && y < maps[0].length){
				collection = maps[x-d][y].getObject();
				for(int i = 0; i < collection.size(); i++){
					if(collection.get(i).getType().equals("bomb")){
						return false;
					}
				}
			}
			if (x >= 0 && y+d >= 0 && x < maps.length && y+d < maps[0].length){
				collection = maps[x][y+d].getObject();
				for(int i = 0; i < collection.size(); i++){
					if(collection.get(i).getType().equals("bomb")){
						return false;
					}
				}
			}
			if (x >= 0 && y-d >= 0 && x < maps.length && y-d < maps[0].length){
				collection = maps[x][y-d].getObject();
				for(int i = 0; i < collection.size(); i++){
					if(collection.get(i).getType().equals("bomb")){
						return false;
					}
				}
			}
		}
		return true;

	}

	public static boolean isBlocked(int x, int y, String command){
		ArrayList<Obj> collection ;
		if(command.equals("RIGHT")){
			//BAHAYA
			if(isNotOutOfBounds(x+1,y)){

				System.out.println(maps[x+1][y].getIsHaveWall()  +""+ (x+1) + "   " + y + "right");
			}
			if(((isNotOutOfBounds(x+1,y) && maps[x+1][y].getIsHaveWall()) || !isNotOutOfBounds(x+1,y) )
				&& ((isNotOutOfBounds(x,y-1) && maps[x][y-1].getIsHaveWall()) || !isNotOutOfBounds(x,y-1))
				&& ((isNotOutOfBounds(x,y+1) && maps[x][y+1].getIsHaveWall()) || !isNotOutOfBounds(x,y+1))
				){

				return true;
			}
		}else if(command.equals("LEFT")){

			//BAHAYA
			if(isNotOutOfBounds(x-1,y)){

				System.out.println(maps[x-1][y].getIsHaveWall()  +""+ (x-1) + "   " + y + "left");
			}

			if(((isNotOutOfBounds(x-1,y) && maps[x-1][y].getIsHaveWall() )|| !isNotOutOfBounds(x-1,y)) 
				&& ((isNotOutOfBounds(x,y-1) && maps[x][y-1].getIsHaveWall() )|| !isNotOutOfBounds(x,y-1)) 
				&& ((isNotOutOfBounds(x,y+1) && maps[x][y+1].getIsHaveWall() )|| !isNotOutOfBounds(x,y+1)) 
				){
				return true;
			}
		}else if(command.equals("UP")){

			//BAHAYA
			if(isNotOutOfBounds(x,y-1)){

				System.out.println(maps[x][y-1].getIsHaveWall()  +""+ (x) + "   " + (y-1) + "up");
			}

			if(((isNotOutOfBounds(x+1,y) && maps[x+1][y].getIsHaveWall() )|| !isNotOutOfBounds(x+1,y)) 
				&& ((isNotOutOfBounds(x-1,y) && maps[x-1][y].getIsHaveWall() )|| !isNotOutOfBounds(x-1,y)) 
				&& ((isNotOutOfBounds(x,y+1) && maps[x][y+1].getIsHaveWall() )|| !isNotOutOfBounds(x,y+1)) 
				){
				return true;
			}
		}else if(command.equals("DOWN")){

			//BAHAYA
			if(isNotOutOfBounds(x,y+1)){

				System.out.println(maps[x][y+1].getIsHaveWall()  +""+ (x) + "   " + (y+1) + "down");
			}

			if(((isNotOutOfBounds(x+1,y) && maps[x+1][y].getIsHaveWall() )|| !isNotOutOfBounds(x+1,y) ) 
				&& ((isNotOutOfBounds(x-1,y) && maps[x-1][y].getIsHaveWall() )|| !isNotOutOfBounds(x-1,y)) 
				&& ((isNotOutOfBounds(x,y-1) && maps[x][y-1].getIsHaveWall() )|| !isNotOutOfBounds(x,y-1)) 
				){
				return true;
			}
		}
		return false;

	}

	public static boolean isNotOutOfBounds(int x, int y){
		if(x>= 0 && y >= 0 && x < maps.length && y < maps[0].length){
			return true;
		}
		return false;
	}

	public static void flee(ArrayList<Bomb> bombs,int x, int y){
		// DLS deep 1 LOL + MD
		int tmpA = 0;
		int tmpKn = 0;
		int tmpKr = 0;
		int tmpB = 0;
		int xP = x;
		int yP = y;
		int xT = 0;
		int yT = 0;
		if(isBlocked(xP-1,yP, "LEFT")){
			tmpKr -= 5;
		}
		if(isBlocked(xP+1,yP, "RIGHT")){
			tmpKn -= 5;
		}
		if(isBlocked(xP,yP+1, "DOWN")){
			tmpB -= 5;
		}
		if(isBlocked(xP,yP-1, "UP")){
			tmpA -= 5;
		}
		for(int i = 0; i < bombs.size(); i++){

			xT = bombs.get(i).getX();
			yT = bombs.get(i).getY();
			String move = "";
			if(isCanMove(xP-1,yP)){
				tmpKr += Math.abs(xP-1-xT) + Math.abs(yP-yT);
				System.out.println(isBlocked(xP-1,yP, "LEFT")+ "AM I LEFT ?");
				

				System.out.println(tmpKr);
				System.out.println(tmpKr + "heuristik kiri");
				//if(move.equals("DOWN")) tmpA -=2;
			}
			if(isCanMove(xP+1,yP)){
				tmpKn += Math.abs(xP+1-xT) + Math.abs(yP-yT);
				System.out.println(isBlocked(xP-1,yP, "RIGHT")+ "AM I RIGHT?");
				
				System.out.println(tmpKn);
				System.out.println(tmpKn + "heuristik kanan");
				//if(move.equals("UP")) tmpB -=2;
			}
			if(isCanMove(xP,yP+1)){
				tmpB += Math.abs(xP-xT) + Math.abs(yP-yT+1);

				System.out.println(tmpB);
				System.out.println(tmpB + "heuristik bawah");
				//if(move.equals("LEFT")) tmpKn -=2;
			}
			if(isCanMove(xP,yP-1)){
				tmpA += Math.abs(xP-xT) + Math.abs(yP-yT-1);
				
				System.out.println(tmpA + "heuristik atas");
				//if(move.equals("RIGHT")) tmpKr -=2;
			}
		}
		int hasil = Math.max(tmpA,Math.max(tmpB,Math.max(tmpKn,tmpKr)));
		if(hasil <= 1 && xP != xT && yP != yT || hasil == 0){
			System.out.println("Masuk Sini coy jadinya dia stay");
			System.out.println(">> STAY");
			return;
		}
		if(tmpA == hasil){
			System.out.println(">> MOVE UP");
			return;
		}
		if(tmpKr == hasil){
			System.out.println(">> MOVE LEFT");
			return;
		}
		if(tmpB == hasil){
			System.out.println(">> MOVE DOWN");
			return;
		}
		if(tmpKn == hasil){
			System.out.println(">> MOVE RIGHT");
			return;
		}

	}
	public static void moveToPower(int x, int y, String type){
		ArrayList<Obj> collection;

		if(x+1 >= 0 && y >= 0 && x+1 < maps.length && y < maps[0].length){
			collection = maps[x+1][y].getObject();
			for(int i = 0; i < collection.size(); i++){
				if(collection.get(i).getType().equals(type)){
					System.out.println(">> MOVE RIGHT");
					return ;
				}
			}
			
		}
		if(x-1 >= 0 && y >= 0 && x-1 < maps.length && y < maps[0].length){
			collection = maps[x-1][y].getObject();
			for(int i = 0; i < collection.size(); i++){
				if(collection.get(i).getType().equals(type)){
					System.out.println(">> MOVE LEFT");
					return ;
				}
			}
		}
		if(x >= 0 && y+1 >= 0 && x < maps.length && y+1 < maps[0].length){
			collection = maps[x][y+1].getObject();
			for(int i = 0; i < collection.size(); i++){
				if(collection.get(i).getType().equals(type)){
					System.out.println(">> MOVE DOWN");
					return ;
				}
			}
		}
		if(x >= 0 && y-1 >= 0 && x < maps.length && y-1 < maps[0].length){
			collection = maps[x][y-1].getObject();
			for(int i = 0; i < collection.size(); i++){
				if(collection.get(i).getType().equals(type)){
					System.out.println(">> MOVE UP");
					return ;
				}
			}
		}
		System.out.println(">> STAY");
		return ;
	}
	public static boolean isNear(int x, int y, String type){

		ArrayList<Obj> collection;

		if(x+1 >= 0 && y >= 0 && x+1 < maps.length && y < maps[0].length){
			collection = maps[x+1][y].getObject();
			for(int i = 0; i < collection.size(); i++){
				if(collection.get(i).getType().equals(type)){
					return true;
				}
			}
			
		}
		if(x-1 >= 0 && y >= 0 && x-1 < maps.length && y < maps[0].length){
			collection = maps[x-1][y].getObject();
			for(int i = 0; i < collection.size(); i++){
				if(collection.get(i).getType().equals(type)){
					return true;
				}
			}
		}
		if(x >= 0 && y+1 >= 0 && x < maps.length && y+1 < maps[0].length){
			collection = maps[x][y+1].getObject();
			for(int i = 0; i < collection.size(); i++){
				if(collection.get(i).getType().equals(type)){
					return true;
				}
			}
		}
		if(x >= 0 && y-1 >= 0 && x < maps.length && y-1 < maps[0].length){
			collection = maps[x][y-1].getObject();
			for(int i = 0; i < collection.size(); i++){
				if(collection.get(i).getType().equals(type)){
					return true;
				}
			}
		}
		return false;
	}

	public static boolean isNearDestructible(int x, int y){

		ArrayList<Obj> collection;

		if(x+1 >= 0 && y >= 0 && x+1 < maps.length && y < maps[0].length){
			collection = maps[x+1][y].getObject();
			for(int i = 0; i < collection.size(); i++){
				if(collection.get(i).getType().equals("destructible")){
					return true;
				}
			}
			
		}
		if(x-1 >= 0 && y >= 0 && x-1 < maps.length && y < maps[0].length){
			collection = maps[x-1][y].getObject();
			for(int i = 0; i < collection.size(); i++){
				if(collection.get(i).getType().equals("destructible")){
					return true;
				}
			}
		}
		if(x >= 0 && y+1 >= 0 && x < maps.length && y+1 < maps[0].length){
			collection = maps[x][y+1].getObject();
			for(int i = 0; i < collection.size(); i++){
				if(collection.get(i).getType().equals("destructible")){
					return true;
				}
			}
		}
		if(x >= 0 && y-1 >= 0 && x < maps.length && y-1 < maps[0].length){
			collection = maps[x][y-1].getObject();
			for(int i = 0; i < collection.size(); i++){
				if(collection.get(i).getType().equals("destructible")){
					return true;
				}
			}
		}
		return false;
	}
	public static boolean isCanMove(int x, int y){
		if (x>= 0 && y >= 0 && x < maps.length && y < maps[0].length){
			ArrayList<Obj> collection = maps[x][y].getObject();
			for(int i = 0; i < collection.size(); i++){
				if(collection.get(i).getType().equals("destructible") || collection.get(i).getType().equals("indestructible") || collection.get(i).getType().equals("flare") ||  collection.get(i).getType().equals("bomb")){
					return false;
				}
			}
			return true;
		}
		return false;
	}

	public static void checkMaps(String str, Information information, int berapa){

		String[] line = str.split(" ");
		int turn = 0;
		int cPlayer = 0;
		int row = 0;
		int column = 0;

		if(line[0].equals("TURN")){
			turn = Integer.parseInt(line[1]);
			information.setTurn(turn);
		}else if (line[0].equals("PLAYER")){
			cPlayer = Integer.parseInt(line[1]);
			information.setcPlayer(cPlayer);
		}else if (line[0].charAt(0) == 'P'){

			String line2[] = line[2].substring(5).split("/");
			int bombLeft = Integer.parseInt(line2[0]);
			int maxBomb = Integer.parseInt(line2[1]);
			int range = Integer.parseInt(line[3].substring(6));
			int score = Integer.parseInt(line[5]);
			int id = Integer.parseInt(line[0].substring(1));

			if (range > maxRange){
				maxRange = range;
			}

			Player player = new Player(id, line[1], bombLeft, maxBomb, range, line[4], score);
			information.addPlayer(player);

		}else if (line[0].equals("BOARD")){
			row = Integer.parseInt(line[1]);
			column = Integer.parseInt(line[2]);
			maps = new Tiles[column][row];
		}else{
			String boardline = str.trim();
			boardline = boardline.replace("[","");
			String[] element = boardline.split("\\]");


			for(int i = 0; i < element.length; i++){
				element[i] = element[i].trim();
				maps[i][berapa] = new Tiles(null);
				ArrayList<Obj> objects = new ArrayList<Obj>();
				if (element[i].equals("")){
					objects.add(new Power ("none"));
				}else if(element[i].equals("###")){
					Walls wall = new Walls("indestructible", i, berapa);
					objects.add(wall);
					maps[i][berapa].setIsHaveWall(true);
				}else if(element[i].equals("XXX")){
					Walls wall = new Walls("destructible", i, berapa);
					objects.add(wall);
					maps[i][berapa].setIsHaveWall(true);
				}else if(element[i].equals("XBX")){
					Walls wall = new Walls("destructible", i, berapa);
					wall.setPower("+Bomb");
					objects.add(wall);
					maps[i][berapa].setIsHaveWall(true);
				}else if(element[i].equals("XPX")){
					Walls wall = new Walls("destructible", i, berapa);
					wall.setPower("+Power");
					objects.add(wall);
					maps[i][berapa].setIsHaveWall(true);
				}else if(element[i].equals("+B")){
					Power power = new Power("+Bomb");
					objects.add(power);
				}else if(element[i].equals("+P")){
					Power power = new Power("+Power");
					objects.add(power);
				}else{
					String[] subElement = element[i].split(";");
					for(int j = 0; j < subElement.length; j++){
						if(element[i].equals("###")){
							Walls wall = new Walls("indestructible", i, berapa);
							objects.add(wall);
							maps[i][berapa].setIsHaveWall(true);
						}else if(subElement[j].equals("XXX")){
							Walls wall = new Walls("destructible", i, berapa);
							wall.setPower("Bomb");
							objects.add(wall);
							maps[i][berapa].setIsHaveWall(true);
						}else if(subElement[j].equals("XBX")){
							Walls wall = new Walls("destructible", i, berapa);
							wall.setPower("+Bomb");
							objects.add(wall);
							maps[i][berapa].setIsHaveWall(true);
						}else if(subElement[j].equals("XPX")){
							Walls wall = new Walls("destructible", i, berapa);
							wall.setPower("+Power");
							objects.add(wall);
							maps[i][berapa].setIsHaveWall(true);
						}else if(subElement[j].equals("+B")){
							Power power = new Power("+Bomb");
							objects.add(power);
						}else if(subElement[j].equals("+P")){
							Power power = new Power("+Power");
							objects.add(power);
						}else if(subElement[j].substring(0,1).equals("F")){
							int life = Integer.parseInt(""+subElement[j].charAt(1)) ;
							Flare flare = new Flare(life);
							objects.add(flare);
						}else if(subElement[j].substring(0,1).equals("B")){
							int bombPower = Integer.parseInt(subElement[j].substring(1,subElement[j].length()-1));
							int bombCount = Integer.parseInt(""+subElement[j].charAt(subElement[j].length()-1));
							Bomb bomb = new Bomb(bombPower, bombCount, i, berapa);
							objects.add(bomb);
							arrBomb.add(bomb);
							maps[i][berapa].setIsHaveWall(true);
						}else if(Character.isDigit(subElement[j].charAt(0))){
							int index = Integer.parseInt(""+subElement[j].charAt(0));
							objects.add(information.getPlayers().get(index));
							if(information.getPlayers().get(index).getPlayerName().equals("DiamondAI")){
								selfId = index;
							}
							information.getPlayers().get(index).setPosition(i,berapa);
						}
					}
				}
				// maps[i][berapa] = new Tiles(objects);
				maps[i][berapa].setObject(objects);
			}
		}
	}

}
