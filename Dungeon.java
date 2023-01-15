/**
 * Dungeon.java
 * Muhammad Nadeem
 * Makes the Dungeon of the game (the concrete structure)
 * Holds 16 rooms to traverse, with one being the boss room
 * another being the room for the power up, and a 3rd to hold
 * the triforce, which upon collection completes the game
 */

import java.util.ArrayList;
import java.util.Arrays;

class Dungeon {

    private Player player;
    private ArrayList<Room> fullArea;

    //since these 3 objects are room-exclusive, I put them here for convenience
    private Boss aqua;
    private Item laser;
    private Item triforce;
        
    //sets up all the rooms and puts them into an arraylist to form a grid
    public Dungeon(Player player) {
        this.player = player;
        Room start = new Room(player, "startLayout","Door0","Door1","Door2","Door3", 0, "None");
        Room room1 = new Room(player, "Layout0","Wall0","Door1","Wall2","Wall3", 2, "Wizzrobe");
        Room room2 = new Room(player, "Layout1","Wall0","Wall1","Wall2","Door3", 2, "Rope");
        Room room3 = new Room(player, "Layout5","Door0","Wall1","Door2","Wall3", 5, "Stalfos");
        Room room4 = new Room(player, "Layout6","Wall0","Door1","Door2","Door3", 3, "Rope");
        Room room5 = new Room(player, "Layout5","Door0","Door1","Wall2","Wall3", 3, "Wizzrobe");
        Room room6 = new Room(player, "Layout7","Door0","Wall1","Wall2","Door3", 3, "Stalfos");
        Room room7 = new Room(player, "Layout0","Door0","Door1","Door2","Wall3", 5, "Rope");
        Room room8 = new Room(player, "Layout8","Door0","Door1","Wall2","Door3", 3, "Wizzrobe");
        Room room9 = new Room(player, "Layout6","Door0","Door1","Door2","Door3", 5, "Rope");
        Room room10 = new Room(player, "Layout2","Wall0","Wall1","Wall2","Door3", 5, "Stalfos");
        Room room11 = new Room(player, "Layout12","Wall0","Wall1","Door2","Wall3", 0, "None");
        Room room12 = new Room(player, "Layout3","Door0","Wall1","Door2","Wall3", 4, "Wizzrobe");
        Room room13 = new Room(player, "Layout4","Wall0","Door1","Door2","Wall3", 0, "None");
        Room room14 = new Room(player, "Layout9","Wall0","Wall1","Wall2","Door3", 0, "None");
        Room room15 = new Room(player, "Layout11","Wall0","Door1","Wall2","Wall3", 1, "Stalfos");
        Room room16 = new Room(player, "Layout10","Wall0","Wall1","Door2","Door3", 4, "Wizzrobe");

        fullArea = new ArrayList<Room> 
        (Arrays.asList(room15, room16,   null,   null,
                       room11, room12, room13, room14,
                       room7,   room8,  room9, room10,
                       room5,   room4,  room6,   null,
                       null,    room3,   null,   null,
                       room1,   start,  room2,   null));

        aqua = new Boss(player, 600, 330);
        laser = new Item(374, 387, "laser");
        triforce = new Item(370, 408, "triforce");
    }
    
    public ArrayList<Room> getArea() {return fullArea;}
    public Boss getBoss() {return aqua;}
    public Item getPlayerLaser() {return laser;}
    public Item getTriforce() {return triforce;}
    
    public void switchRooms() {
        //switches the player's current room depending on if they pass a certain point,
        // with respect to the direction they come from, by traversing the arraylist
        if (player.getY() < 200) {
            player.setRoom(fullArea.get(fullArea.indexOf(player.getRoom())-4));
            player.setY(550);
        }
        if (player.getX() > 670) {
            player.setRoom(fullArea.get(fullArea.indexOf(player.getRoom())+1));
            player.setX(50);
        }
        if (player.getY() > 580) {
            //if the starting room's back door is collided with, the player's state is changed to overworld
            if (player.getRoom() == fullArea.get(21)) {
                player.setRoom(player.getOverWorld().getArea().get(7)); //room with the entrance
                player.setState("overworld");
                player.setX(4*48);
                player.setY(2*48+150);
            }
            else {
                player.setRoom(fullArea.get(fullArea.indexOf(player.getRoom())+4));
            }
            player.setY(250);
        }
        if (player.getX() < 50) {
            player.setRoom(fullArea.get(fullArea.indexOf(player.getRoom())-1));
            player.setX(650);
        }
    }
}