/**
 * Room.java
 * Muhammad Nadeem
 * Makes the room that all action occurs on
 * Most of the main logic (including collision and entity states) occurs here
 * Rooms are drawn based on a tile map that is linked to a text file, which
 * allows for easy design and modifications
 * They can be made for the dungeon, or for the overworld, each having
 * specific attributes
 * Rooms are given borders and collidable objects so that movement for entities
 * is restricted
 */

import java.io.*;
import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Random;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class Room {
	private Random rand;							//randomizer

	private Player player;
	public static int roomOffset = 150;				//rectangle above the room is 150 pixels high
	public static int obstacleOffset = 10;			//to give some slack when colliding with blocks

	private BufferedImage pixelMap, back;
	private int tileWidth, tileHeight;
	private HashMap<Integer, Image> tilePics;
	private ArrayList<Obstacle> collideSquares;		//squares that aren't walkable tiles

	//Regarding enemies
	private int amount;								//number of enemies
	private String type;							//type of enemy
	private ArrayList<Enemy> squadStalfos;			//stores 1st type of enemy if chosen
	private ArrayList<Enemy> squadRope;				//stores 2nd type of enemy if chosen
	private ArrayList<Enemy> squadWizzrobe;			//stores 3rd type of enemy if chosen
	private Sound enemyDeath = new Sound("Resources/Sound/LOZ_Enemy_Die.wav"); 

	//OverWorld-exclusive
	private HashMap<String, String> plains;
	private ArrayList<Obstacle> enemyBordersO;		//enemy borders are different than player borders (overworld)

	//Dungeon-exclusive
	private HashMap<String, String> floors; 		//maps certain pixel layouts to floor layouts of tiles
	private HashMap<String, Image> wallCentres;		//maps a word (Door or Wall) to an image to show
	private ArrayList<Obstacle> borders;			//walls of the room's interior
	private ArrayList<Obstacle> enemyBordersD;		//enemy borders are different than player borders (dungeon)
	private Item heart;								//makes a heart object
	private int heartChance;						//50-50 chance of that heart actually spawning
	private boolean pickedUp;						//checks if the player has picked up the heart
	private Sound itemPickUp = new Sound("Resources/Sound/LOZ_Get_Heart.wav");

	private int scaleFactor;						//scales all tiles and the room itself by some factor

	//these are fixed tuples for enemies to spawn in within each area
    private int[][] dungeonStartSpots = {{240, 342}, {480, 288}, {98, 390}, {624, 390}, {336, 342}};
	private int[][] overworldStartSpots = {{432, 438}, {288,534}, {96,438}, {384,294}, {672,534}};
	
	public Image loadImage(String name){
		return new ImageIcon(name).getImage();
	}
	
	public BufferedImage loadBuffImage(String name){
		try {
    		return ImageIO.read(new File(name));
		} 
		catch (IOException e) {
			System.out.println(e);
		}
		return null;
	}
	
	//bare-bones method taken from Mr. McKenzie
	/* Level file format
	 * ------------------
	 * tile width
	 * tile height
	 * background pic
	 * # of tile types
	 * ---- colour #
	 * ---- tile picture	
	 */

	//method exlusive to dungeon
    public void dungeonLoadHeader(String name) {
    	try{
			//maps the word "Layout", and a number with it, to a text file with the same number to be used to load in the room's floor
			for (int i = 0; i<14; i++) {
				floors.put("Layout"+i, "Resources/Dungeon/RoomTextFiles/Room"+i+".txt");
			}
			floors.put("startLayout", "Resources/Dungeon/RoomTextFiles/startRoom.txt");

    		Scanner inFile = new Scanner(new File(floors.get(name)));
    		tileWidth = Integer.parseInt(inFile.nextLine());
    		tileHeight = Integer.parseInt(inFile.nextLine());

			//scales a buffered image (help from internet)
    		Image tempBack = loadBuffImage(inFile.nextLine()).getScaledInstance(256*scaleFactor,176*scaleFactor,Image.SCALE_SMOOTH);
			back = new BufferedImage(256*scaleFactor,176*scaleFactor,BufferedImage.TYPE_INT_RGB);
			back.getGraphics().drawImage(tempBack, 0,0, null);
            
    		pixelMap = loadBuffImage(inFile.nextLine()); // read pixels
    		int numTile = Integer.parseInt(inFile.nextLine());
    		
    		for(int i=0; i<numTile; i++){            // The 16 is saying it's base 16
    			int col = Integer.parseInt(inFile.nextLine(), 16);
    			tilePics.put(col, loadBuffImage(inFile.nextLine()).getScaledInstance(48,48,Image.SCALE_SMOOTH));
    		}
			//maps the words "Wall" and "Door", along with some number denoting direction, to the image file of similar name
			for (int i = 0; i < 4; i++) {
				wallCentres.put("Wall"+i, loadBuffImage("Resources/Dungeon/DoorPics/dWall"+i+".png").getScaledInstance(96,96,Image.SCALE_SMOOTH));
				wallCentres.put("Door"+i, loadBuffImage("Resources/Dungeon/DoorPics/dDoor"+i+".png").getScaledInstance(96,96,Image.SCALE_SMOOTH));
			}

    	}
    	catch(IOException ex){
    		System.out.println(ex);
    	}	
    }

	//method exclusive to overworld
	public void regionLoadHeader(String name) {
    	try{
			//acts similarly to "floors" above, but instead with the word "Space"
			for (int i = 0; i<8; i++) {
				plains.put("Space"+i, "Resources/OverWorld/OverWorldTextFiles/Plain"+i+".txt");
			}

    		Scanner inFile = new Scanner(new File(plains.get(name)));
    		tileWidth = Integer.parseInt(inFile.nextLine());
    		tileHeight = Integer.parseInt(inFile.nextLine());

    		Image tempBack = loadBuffImage(inFile.nextLine()).getScaledInstance(256*scaleFactor,176*scaleFactor,Image.SCALE_SMOOTH);
			back = new BufferedImage(256*scaleFactor,176*scaleFactor,BufferedImage.TYPE_INT_RGB);
			back.getGraphics().drawImage(tempBack, 0,0, null);
            
    		pixelMap = loadBuffImage(inFile.nextLine()); // read pixels
    		int numTile = Integer.parseInt(inFile.nextLine());
    		
    		for(int i=0; i<numTile; i++){            // The 16 is saying it's base 16
    			int col = Integer.parseInt(inFile.nextLine(), 16);
    			tilePics.put(col, loadBuffImage(inFile.nextLine()).getScaledInstance(48,48,Image.SCALE_SMOOTH));
    		}

    	}
    	catch(IOException ex){
    		System.out.println(ex);
    	}	
    }
    
	//bare-bones method taken from Mr. McKenzie
	//for rooms within dungeons
	//passes in the doors to be used
    public void dungeonMakeFull(String door1, String door2, String door3, String door4){
    	Graphics buffG = back.getGraphics();
    	int wid = pixelMap.getWidth();
    	int height = pixelMap.getHeight();
		
    	// Go to each pixel of the map picture, if the colour is in out
    	// HashMap then draw the image to our background.
    	for(int x=0; x<wid; x++){
	    	for(int y=0; y<height; y++){
    			int col = pixelMap.getRGB(x,y); 
    			col = col & 0xffffff; // This gets rid of the 2 bytes for the alpha
    			if(tilePics.containsKey(col)){
    				Image tile = tilePics.get(col);
    				int offset = tileHeight - tile.getHeight(null); // so objects are on the ground
    				buffG.drawImage(tile, 32*scaleFactor+x*tileWidth, 32*scaleFactor+y*tileHeight+offset,null);
    			}
				//if pixel used is not blue or black (denoting walkable tiles), then it is collidable object
				// and is added to an arraylist of blocks
				if (col != 0x0000ff && col != 0x000000) {
					collideSquares.add(new Obstacle(new Rectangle(32*scaleFactor+x*tileWidth+obstacleOffset,32*scaleFactor+y*tileHeight+roomOffset+obstacleOffset-5,
																  tileWidth-2*obstacleOffset, tileHeight-2*obstacleOffset)));
				}
	    	}
    	}
		//the floor layout is 2 tileWidths right and 2 tileHeights down from the top-left point of the whole room
		//draws the 4 doors in specific areas
		buffG.drawImage(wallCentres.get(door1),tileWidth*7,0,null);
		buffG.drawImage(wallCentres.get(door2),tileWidth*14,(int)(tileHeight*4.5),null);
		buffG.drawImage(wallCentres.get(door3),tileWidth*7,tileHeight*9,null);
		buffG.drawImage(wallCentres.get(door4),0,(int)(tileHeight*4.5),null);

		setBorders(buffG, door1,door2,door3,door4);

		//below are the borders affecting the player, they keep the player from walking on the room's walls and from
		//switching rooms when not supposed to (as in dungoen rooms, after a certain point is reached, the player switches rooms)

		//top left - right
		borders.add(new Obstacle(new Rectangle(tileWidth+35,tileHeight+20+roomOffset,tileWidth*5+20,1)));
		//bottom left - right
		borders.add(new Obstacle(new Rectangle(tileWidth+35,tileHeight*9+20+roomOffset,tileWidth*5+20,1)));
		//top middle - right
		borders.add(new Obstacle(new Rectangle(tileWidth*9,tileHeight+20+roomOffset,tileWidth*5+25,1)));
		//bottom middle - right
		borders.add(new Obstacle(new Rectangle(tileWidth*9-10,tileHeight*9+20+roomOffset,tileWidth*5+25,1)));
		//top left - down
		borders.add(new Obstacle(new Rectangle(tileWidth+35,tileHeight+20+roomOffset, 1, tileHeight*3+15)));
		//top right - down
		borders.add(new Obstacle(new Rectangle(tileWidth*14+15,tileHeight+20+roomOffset, 1, tileHeight*3+15)));
		//center left - down
		borders.add(new Obstacle(new Rectangle(tileWidth+35, tileHeight*6+roomOffset, 1, tileHeight*3+20)));
		//center right - down
		borders.add(new Obstacle(new Rectangle(tileWidth*14+15,tileHeight*6+roomOffset, 1, tileHeight*3+20)));

		//enemy borders are similar to player borders, but without any gaps

		//top
		enemyBordersD.add(new Obstacle(new Rectangle(tileWidth+20,tileHeight+20+roomOffset,tileWidth*12+40,1)));
		//right
		enemyBordersD.add(new Obstacle(new Rectangle(tileWidth*14+15,tileHeight+20+roomOffset, 1, tileHeight*7+45)));
		//bottom
		enemyBordersD.add(new Obstacle(new Rectangle(tileWidth+20,tileHeight*9+20+roomOffset,tileWidth*12+45,1)));
		//left
		enemyBordersD.add(new Obstacle(new Rectangle(tileWidth+15,tileHeight+20+roomOffset, 1, tileHeight*7+50)));
    }
	
	//for regions, or "rooms", in the overworld
	public void regionMakeFull() {
		Graphics buffG = back.getGraphics();
    	int wid = pixelMap.getWidth();
    	int height = pixelMap.getHeight();
		
    	// Go to each pixel of the map picture, if the colour is in out
    	// HashMap then draw the image to our background.
    	for(int x=0; x<wid; x++){
	    	for(int y=0; y<height; y++){
    			int col = pixelMap.getRGB(x,y); 
    			col = col & 0xffffff; // This gets rid of the 2 bytes for the alpha
    			if(tilePics.containsKey(col)){
    				Image tile = tilePics.get(col);
    				int offset = tileHeight - tile.getHeight(null); // so objects are on the ground
    				buffG.drawImage(tile, x*tileWidth, y*tileHeight+offset,null);
    			}
				//if pixel used is not blue or black (denoting walkable tiles), then it is collidable object
				// and is added to an arraylist of blocks
				if (col != 0x0000ff && col != 0x000000) {
					collideSquares.add(new Obstacle(new Rectangle(x*tileWidth+obstacleOffset,+y*tileHeight+roomOffset+obstacleOffset,
													tileWidth-2*obstacleOffset, tileHeight-2*obstacleOffset)));
				}
	    	}
		}

		//enemies cannot go past the edges of the screen in the overworld like the player can, so borders are set up
		
		//top
		enemyBordersO.add(new Obstacle(new Rectangle(0, 150, GPanel.WIDTH, 1)));
		//right
		enemyBordersO.add(new Obstacle(new Rectangle(GPanel.WIDTH, 150, 1, GPanel.HEIGHT-150)));
		//bottom
		enemyBordersO.add(new Obstacle(new Rectangle(0, GPanel.HEIGHT-1, GPanel.WIDTH, 1)));
		//left
		enemyBordersO.add(new Obstacle(new Rectangle(0, 150, 1, GPanel.HEIGHT-150)));
	}
	
	public void setBorders (Graphics g, String door1, String door2, String door3, String door4) {
		//with borders spanning only certain portions of the walls of the dungeon rooms, some gaps are left open,
		// if said gaps are in front of actual doors, they are left there, but if there are walls being placed there,
		// those gaps are filled with additional borders
		if (door1.contains("Wall")) {
			borders.add(new Obstacle(new Rectangle(tileWidth*7, tileHeight+20+roomOffset, tileWidth*2, 1)));
		}
		if (door2.contains("Wall")) {
			borders.add(new Obstacle(new Rectangle(tileWidth*14+15, tileHeight*5+roomOffset, 1, tileHeight)));
		}
		if (door3.contains("Wall")) {
			borders.add(new Obstacle(new Rectangle(tileWidth*7, tileHeight*9+20+roomOffset, tileWidth*2, 1)));
		}
		if (door4.contains("Wall")) {
			borders.add(new Obstacle(new Rectangle(tileWidth+35, tileHeight*5+roomOffset, 1, tileHeight)));
		}
	}

    public Image getBackground(){return back;}

	//above code is to load in and render the rooms themselves
	//below is the logic code for the rooms
    
    public Room(Player player, String name, String door1, String door2, String door3, String door4, int amount, String type) {
    	rand = new Random();
		this.player = player;
		squadStalfos = new ArrayList<Enemy>();
		squadRope = new ArrayList<Enemy>();
		squadWizzrobe = new ArrayList<Enemy>();

		this.amount = amount;
		this.type = type;
		heart = new Item(370, 262,"heart");			//shows up at the top middle of the room
		heartChance = rand.nextInt(2);				//either 0 or 1 (coin flip)
		pickedUp = false;

		tilePics = new HashMap<Integer, Image>();
		floors = new HashMap<String, String>();
		wallCentres = new HashMap<String, Image>();
		collideSquares = new ArrayList<Obstacle>();
		borders = new ArrayList<Obstacle>();
		enemyBordersD = new ArrayList<Obstacle>();

		scaleFactor = 3;
    	dungeonLoadHeader(name);
    	dungeonMakeFull(door1,door2,door3,door4);
    }

	public Room(Player player, String name, int amount, String type) {
    	this.player = player;
		squadStalfos = new ArrayList<Enemy>();
		squadRope = new ArrayList<Enemy>();
		squadWizzrobe = new ArrayList<Enemy>();

		this.amount = amount;
		this.type = type;

		tilePics = new HashMap<Integer, Image>();
		collideSquares = new ArrayList<Obstacle>();
		plains = new HashMap<String, String>();
		enemyBordersO = new ArrayList<Obstacle>();

		scaleFactor = 3;
    	regionLoadHeader(name);
		regionMakeFull();
    }

	public void initDungeonEnemies() {
		for (int i = 0; i < getAmount(); i++) {
			if (type.equals("Stalfos")) {
				squadStalfos.add(new Enemy(player, "Stalfos", dungeonStartSpots[i][0], dungeonStartSpots[i][1], this));
			}
			if (type.equals("Rope")) {
				squadRope.add(new Enemy(player, "Rope", dungeonStartSpots[i][0], dungeonStartSpots[i][1], this));
			}
			if (type.equals("Wizzrobe")) {
				squadWizzrobe.add(new Enemy(player, "Wizzrobe", dungeonStartSpots[i][0], dungeonStartSpots[i][1], this));
			}
		}
	}

	public void initOverWorldEnemies() {
		//puts enemies in the room
		for (int i = 0; i < getAmount(); i++) {
			if (type.equals("Stalfos")) {
				squadStalfos.add(new Enemy(player, "Stalfos", overworldStartSpots[i][0], overworldStartSpots[i][1], this));
			}
			if (type.equals("Rope")) {
				squadRope.add(new Enemy(player, "Rope", overworldStartSpots[i][0], overworldStartSpots[i][1], this));
			}
			if (type.equals("Wizzrobe")) {
				squadWizzrobe.add(new Enemy(player, "Wizzrobe", overworldStartSpots[i][0], overworldStartSpots[i][1], this));
			}
		}
	}

	public void drawEnemies(Graphics g) {
		//as long as there are enemies in the arraylist used by the room, enemies are drawn
		for (Enemy stalfos : squadStalfos) {
			if (squadStalfos.size() == 0) {return;} 
			else {
				stalfos.draw(g);
			}
		}
		for (Enemy rope : squadRope) {
			if (squadRope.size() == 0) {return;} 
			else {
				rope.draw(g);
			}
		}
		for (Enemy wizzrobe : squadWizzrobe) {
			if (squadWizzrobe.size() == 0) {return;} 
			else {
				wizzrobe.draw(g);
			}
		}
	}

	public void moveEnemies() {
		//as long as there are enemies in the arraylist used by the room, enemies move and can damage the player if collided with
		for (Enemy stalfos : squadStalfos) {
			if (squadStalfos.size() == 0) {return;} 
			else {
				stalfos.move();
				player.checkHitByEntity(stalfos.getRect());
			}
		}
		for (Enemy rope : squadRope) {
			if (squadRope.size() == 0) {return;} 
			else {
				rope.move();
				player.checkHitByEntity(rope.getRect());
			}
		}
		for (Enemy wizzrobe : squadWizzrobe) {
			if (squadWizzrobe.size() == 0) {return;} 
			else {
				wizzrobe.move();
				//wizzrobes cannot hurt the player by their collision, only their fireballs can
				player.checkHitByShot(wizzrobe.getFireball());
			}
		}
	}

	public void killEnemies() {
		//damages enemies depending on how they were hit, and if their health runs out, they are removed
		// from their arraylist and both the player's and enemy's iframes are forcibly set to 0
		for (int i = 0; i < squadStalfos.size(); i++) {		//used direct iterator to avoid concurrent modification exception
			if (squadStalfos.size() == 0) {return;}
			else {
				squadStalfos.get(i).hitByPlayer();
				squadStalfos.get(i).hitByShot();
			}
			if (squadStalfos.get(i).getHealth() == 0) {
				squadStalfos.get(i).setIFrames(0);
				player.setIFrames(0);
				squadStalfos.remove(squadStalfos.get(i));
				enemyDeath.play();
			}
		}
		for (int i = 0; i < squadRope.size(); i++) {
			if (squadRope.size() == 0) {return;}
			else {
				squadRope.get(i).hitByPlayer();
				squadRope.get(i).hitByShot(); 
			}
			if (squadRope.get(i).getHealth() == 0) {
				squadRope.get(i).setIFrames(0);
				player.setIFrames(0);
				squadRope.remove(squadRope.get(i));
				enemyDeath.play();
			}
		}
		for (int i = 0; i < squadWizzrobe.size(); i++) {
			if (squadWizzrobe.size() == 0) {return;}
			else {
				squadWizzrobe.get(i).hitByPlayer();
				squadWizzrobe.get(i).hitByShot();
			}
			if (squadWizzrobe.get(i).getHealth() == 0) {
				squadWizzrobe.get(i ).setIFrames(0);
				player.setIFrames(0);
				squadWizzrobe.remove(squadWizzrobe.get(i));
				enemyDeath.play();
			}
		}
	}

	public void drawHeart(Graphics g) {
		//draws the heart if
		// - the coin flip shows heads (if tails == 0 and heads == 1)
		// - all the enemies in the room are cleared (or no enemies exist)
		// - the heart has not been picked up yet
		//thus theres a chance that even after clearing the room, no heart spawns
		if (heartChance == 1 && !pickedUp && 
			(squadStalfos.size() == 0 && squadRope.size() == 0 && squadWizzrobe.size() == 0)) {
			heart.draw(g);
		}
	}

	public void updateHeart() {
		//updates the heart with the same conditions as the draw function,
		// but if the player collides with it, it heals them for 1 health and changes
		// the hearts condition to "picked up"
		if (heartChance == 1 && !pickedUp &&
			(squadStalfos.size() == 0 && squadRope.size() == 0 && squadWizzrobe.size() == 0)) {
			if (heart.collideHeart(player)) {
				pickedUp = true;
				itemPickUp.play();
				player.setHealth(player.getHealth()+1);
			}
		}
	}

	public ArrayList<Obstacle> getBorders() {return borders;}
	public ArrayList<Obstacle> getCollideSquares() {return collideSquares;}
	public ArrayList<Obstacle> getEDBorders() {return enemyBordersD;}
	public ArrayList<Obstacle> getEOBorders() {return enemyBordersO;}
	public int getAmount() {return amount;}
}