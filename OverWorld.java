/**
 * OverWorld.java
 * Muhammad Nadeem
 * Makes the OverWorld of the game (the sandy, tree-and-mountain-filled area)
 * 7 regions are there for the player to traverse, with one leading to the dungeon
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.awt.Rectangle;

class OverWorld {
    private Player player;
    private ArrayList<Room> mapArea;

    //sets up all the rooms and puts them into an arraylist to form a grid
    public OverWorld(Player player) {
        this.player = player;
        Room room0 = new Room(player, "Space0", 0, "None");
        Room room1 = new Room(player, "Space1", 1, "Stalfos");
        Room room2 = new Room(player, "Space2", 2, "Rope");
        Room room3 = new Room(player, "Space3", 4, "Stalfos");
        Room room4 = new Room(player, "Space4", 1, "Wizzrobe");
        Room room5 = new Room(player, "Space5", 2, "Stalfos");
        Room room6 = new Room(player, "Space6", 5, "Stalfos");
        Room room7 = new Room(player, "Space7", 4, "Rope");

        mapArea = new ArrayList<Room> 
        (Arrays.asList( null, room0, room1,
                       room7, room5, room2,
                       room6, room4, room3));
    }

    public ArrayList<Room> getArea() {return mapArea;}
    
    public void switchRooms() {
        //switches the player's current room depending on if they hit the edge of the screen,
        // with respect to the direction they come from, by traversing the arraylist
        int tileWidth = 48;
        int tileHeight = 48;
        int roomOffset = 150;

        Rectangle entrance = new Rectangle(tileWidth*4, tileHeight+roomOffset, tileWidth,tileHeight-35);

        if (player.getY() < 150) {
            player.setRoom(mapArea.get(mapArea.indexOf(player.getRoom())-3));
            player.setY(550);
        }
        if (player.getX()+player.getWidth() > 768) {
            player.setRoom(mapArea.get(mapArea.indexOf(player.getRoom())+1));
            player.setX(50);
        }
        if (player.getY()+player.getHeight() > 672) {
            player.setRoom(mapArea.get(mapArea.indexOf(player.getRoom())+3));
            player.setY(250);
        }
        if (player.getX() < 0) {
            player.setRoom(mapArea.get(mapArea.indexOf(player.getRoom())-1));
            player.setX(650);
        }

        //if the entrance is collided with, the player's state is changed to being in the dungeon
        if (player.getRect().intersects(entrance) && player.getRoom() == mapArea.get(7)) {
            player.setRoom(player.getDungeon().getArea().get(21)); //dungeon's starting room
            player.setState("dungeon");
            player.setX(tileWidth*7+24);
            player.setY(tileHeight*8+roomOffset);
        }
    }
}