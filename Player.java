/**
 * Player.java
 * Muhammad Nadeem
 * Creates a player that can be moved using WASD and can attack using J
 * Can hit and be hit by entities and projectiles, and the room the player
 * currently is in dictates the room drawn (can be switched when the player walks past
 * a threshold)
 * Holds a projectile that becomes available for use after a power up is collected (see Item class)
 */

import java.awt.*;
import javax.swing.ImageIcon;

class Player{

    public final int UP = 0;
    public final int RIGHT = 1;
    public final int DOWN = 2;
    public final int LEFT = 3;

    //to be shown for certain frames when player takes damage
    public final Image noPic = new ImageIcon("Resources/Enemies/noPic.png").getImage();
    //image sets for animations while walking and attacking
    public final Image[][] imageSetWalking = {{new ImageIcon("Resources/Link/Walking/Walk0.png").getImage(),new ImageIcon("Resources/Link/Walking/Walk1.png").getImage()},
                                              {new ImageIcon("Resources/Link/Walking/Walk2.png").getImage(),new ImageIcon("Resources/Link/Walking/Walk3.png").getImage()},
                                              {new ImageIcon("Resources/Link/Walking/Walk4.png").getImage(),new ImageIcon("Resources/Link/Walking/Walk5.png").getImage()},
                                              {new ImageIcon("Resources/Link/Walking/Walk6.png").getImage(),new ImageIcon("Resources/Link/Walking/Walk7.png").getImage()}};

    public final Image[][] imageSetStriking = {{new ImageIcon("Resources/Link/Striking/Strike0.png").getImage(),new ImageIcon("Resources/Link/Striking/Strike1.png").getImage(),new ImageIcon("Resources/Link/Striking/Strike2.png").getImage(),new ImageIcon("Resources/Link/Striking/Strike3.png").getImage()},
                                               {new ImageIcon("Resources/Link/Striking/Strike4.png").getImage(),new ImageIcon("Resources/Link/Striking/Strike5.png").getImage(),new ImageIcon("Resources/Link/Striking/Strike6.png").getImage(),new ImageIcon("Resources/Link/Striking/Strike7.png").getImage()},
                                               {new ImageIcon("Resources/Link/Striking/Strike8.png").getImage(),new ImageIcon("Resources/Link/Striking/Strike9.png").getImage(),new ImageIcon("Resources/Link/Striking/Strike10.png").getImage(),new ImageIcon("Resources/Link/Striking/Strike11.png").getImage()},
                                               {new ImageIcon("Resources/Link/Striking/Strike12.png").getImage(),new ImageIcon("Resources/Link/Striking/Strike13.png").getImage(),new ImageIcon("Resources/Link/Striking/Strike14.png").getImage(),new ImageIcon("Resources/Link/Striking/Strike15.png").getImage()}};
    //the images for the sword projectile (depending on direction)
    public final Image[] imageSetLaser = {new ImageIcon("Resources/Link/SwordShot/UpShot.png").getImage(), new ImageIcon("Resources/Link/SwordShot/RightShot.png").getImage(), new ImageIcon("Resources/Link/SwordShot/DownShot.png").getImage(), new ImageIcon("Resources/Link/SwordShot/LeftShot.png").getImage()};
    
    private int health;
    private Sound damaged;      //sound to be played when hit
    
    private int playerX, playerY, dx, dy;
    private int[] playerKeys;   //keys the player can use
    private int dir;
    private int offset = 5;     //some slack is given for the player rectangle through this offset to not be strict when colliding

    private Image[] walkAnimate;    //frames of animation (walking)
    private int walkFrame;          //current frame
    private int wait;               //animation delay counter (decrements every frame, when 0, animation progresses to next frame)
    private int startWait;          //animation delay timer ("wait" resets to this every time the frame progresses)    

    private Image[] attackAnimate;  //frames of animation (striking)
    private int attackFrame;        //current frame
    private boolean isAttacking;
    private Rectangle attackRect;   //the rectangle that encloses just the sword when the player attacks
    private int iframes = 0;        
    private Sound swordSwing;       //sound to be played when sword is swung

    private Projectile swordLaser;  //sword projectile that shoots after the player picks up the powerup
    private Sound swordLaserSwing;  //sound to be played when sword laser is shot

    private Dungeon dungeon;        
    private OverWorld overworld;
    private Room currentRoom;
    
    private String state;           //sees if the player is in the overworld or dungeon

    private boolean canShoot;       //checks if the player can shoot a sword projectile, false until powerup is picked up

    public Player(int x, int y, int[] keys) {
        health = 7;
        playerX = x;
        playerY = y;
        dx = 5;
        dy = 5;
        playerKeys = keys;
        dir = RIGHT;

        walkAnimate = new Image[2];
        walkAnimate[0] = imageSetWalking[2][0];
        walkAnimate[1] = imageSetWalking[2][1];
        walkFrame = 0;
        startWait = 3;
        wait = startWait;

        attackAnimate = new Image[4];
        attackFrame = 0;
        isAttacking = false;
        attackRect = new Rectangle();
        canShoot = false;

        state = "overworld";
        dungeon = new Dungeon(this);
        overworld = new OverWorld(this);
        //player spawns in the top middle region of the overworld
        currentRoom = overworld.getArea().get(1);

        //sounds to be used
        damaged = new Sound("Resources/Sound/LOZ_Link_Hurt.wav");
        swordSwing = new Sound("Resources/Sound/LOZ_Sword_Slash.wav");
        swordLaserSwing = new Sound("Resources/Sound/LOZ_Sword_Combined.wav");

        //makes the projectile to be shot
        swordLaser = new Projectile(playerX+24, 0, 15, 15, imageSetLaser[0]);
    }

    public int getX() {return playerX;}
    public int getY() {return playerY;}
    public int getDX() {return dx;}
    public int getDY() {return dy;}
    public int getWidth() {return walkAnimate[walkFrame].getWidth(null);}
    public int getHeight() {return walkAnimate[walkFrame].getHeight(null);}
    public int getDir() {return dir;}
    public int getHealth() {return health;}
    public Rectangle getRect() {return new Rectangle(playerX+offset, playerY+offset, getWidth()-2*offset, getHeight()-2*offset);}
    public Rectangle getAttackRect() {return attackRect;}
    public Room getRoom() {return currentRoom;}
    public OverWorld getOverWorld() {return overworld;}
    public Dungeon getDungeon() {return dungeon;}
    public String getState() {return state;}
    public Projectile getLaser() {return swordLaser;}
    public boolean getCanShoot() {return canShoot;}

    public void setX(int x) {playerX = x;}
    public void setY(int y) {playerY = y;}
    public void setDX(int dx) {this.dx = dx;}
    public void setDY(int dy) {this.dy = dy;}
    public void setHealth(int newHealth) {health = newHealth;}
    public void setRoom(Room room) {currentRoom = room;}
    public void setState(String newState) {state = newState;}
    public void setCanAttack(boolean newCanAttack) {isAttacking = newCanAttack;}
    public void setCanShoot(boolean newCanShoot) {canShoot = newCanShoot;}
    public void setIFrames(int newIFrames) {iframes = newIFrames;}
    

    public void draw(Graphics g) {
        //chooses animation frames depending on direction
        for (int i = 0; i < imageSetWalking[0].length; i++) {
            walkAnimate[i] = imageSetWalking[dir][i];
        }
        for (int i = 0; i < imageSetStriking[0].length; i++) {
            attackAnimate[i] = imageSetStriking[dir][i];
        }

        //as the attack images are larger than the walk images, the offset is the difference between them, which
        // solve the problem of incorrectly drawing the attack images
        //also change the attack rectangle
        int imageOffsetX = attackAnimate[attackFrame].getWidth(null) - walkAnimate[walkFrame].getWidth(null);
        int imageOffsetY = attackAnimate[attackFrame].getHeight(null) - walkAnimate[walkFrame].getHeight(null);

        //draws the attack frames and sets attackRect with respect to direction
        if (isAttacking) {
            if (dir == UP) { 
                g.drawImage(attackAnimate[attackFrame],playerX-imageOffsetX,playerY-imageOffsetY,null);
                attackRect.setRect(playerX-imageOffsetX,playerY-imageOffsetY, attackAnimate[attackFrame].getWidth(null), imageOffsetY);
            }
            else if (dir == LEFT) {
                g.drawImage(attackAnimate[attackFrame],playerX-imageOffsetX,playerY-imageOffsetY,null);
                attackRect.setRect(playerX-imageOffsetX,playerY-imageOffsetY, imageOffsetX, attackAnimate[attackFrame].getHeight(null));
            }
            else if (dir == RIGHT){
                g.drawImage(attackAnimate[attackFrame],playerX,playerY,null);
                attackRect.setRect(playerX+walkAnimate[walkFrame].getWidth(null),playerY, imageOffsetX, attackAnimate[attackFrame].getHeight(null));
            }
            else if (dir == DOWN) {
                g.drawImage(attackAnimate[attackFrame],playerX,playerY,null);
                attackRect.setRect(playerX,playerY+walkAnimate[walkFrame].getHeight(null), attackAnimate[attackFrame].getWidth(null), imageOffsetY);
            }
        }
        ////while the player is going through their invincibility frames, the 1st and 2nd frames will be a blank image
        // and the rest will show the enemy; emulates a flickering effect to show damage has been dealt
        else {
            if (iframes%5 == 1 || iframes%5 == 2) {
                g.drawImage(noPic, playerX, playerY, null);
            }
            else {
                g.drawImage(walkAnimate[walkFrame],playerX,playerY,null);
            }
            //if the player is not attacking, the attack rectangle is set to an arbitrary position and size
            attackRect.setRect(0,0,0,0);
        }

        if (canShoot) {
            swordLaser.draw(g);            
        }
    }

    public void move(boolean[] keys) {
        //moves the player while they aren't attacking depending on the button pressed
        //meant for one direction at a time
        boolean isMoving = false;
        if (!isAttacking) {
            if (keys[playerKeys[UP]] ) {
                isMoving = true;
                dir = UP;
                playerY -= dy;
            }
            else if (keys[playerKeys[RIGHT]]){
                isMoving = true;
                dir = RIGHT;
                playerX += dx;
            }
            else if (keys[playerKeys[DOWN]]){
                isMoving = true;
                dir = DOWN;
                playerY += dy;
            }
            else if (keys[playerKeys[LEFT]]){
                isMoving = true;
                dir = LEFT;
                playerX -= dx;
            }
        }
        
        //animates the player with the previously mentioned delay
        if (isMoving){
            if (wait == 0) {
                walkFrame = (walkFrame +1) % walkAnimate.length;
                wait = startWait;
            }
            else {
                wait--;
            }
        }
        if (isAttacking) {
            attack();
        }
        shootSword();

        //allows the player to traverse rooms of the area they are in
        if (state.equals("dungeon")) {
            dungeon.switchRooms();
        }
        else if (state.equals("overworld")) {
            overworld.switchRooms();
        }
        stopMoving();
    }

    public void stopMoving() {
        //stops the player from moving further if they collide with a border in the dungeon
        //or blocks/terrain
        if (state.equals("dungeon")) {
            for (Obstacle barrier : currentRoom.getBorders()) {
                barrier.collide(this);
            }
        }
        for (Obstacle block : currentRoom.getCollideSquares()) {
            block.collide(this);
        }
    }

    public void attack() {
        //attack animation is played and stops once one cycle is made and the frame goes back to 0
        attackFrame = (attackFrame +1) % attackAnimate.length;
        swordSwing.play();
        if (attackFrame == 0) {
            isAttacking = false;
        }
    }

    //shifts the player a bit when they are hit
    public void knockBack() {
        if (dir == UP) {
            playerY += 20;
        }
        if (dir == RIGHT) {
            playerX -= 20;
        }
        if (dir == DOWN) {
            playerY -= 20;
        }
        if (dir == LEFT) {
            playerX += 20;
        }
    }

    public void checkHitByEntity(Rectangle rect) {
        //sees if the player is colliding with a certain living thing
        //their health is reduced by one and they become invincible for some time
        if (rect.intersects(getRect()) && iframes == 0) {
            damaged.play();
            knockBack();
            health--;
            iframes = 120; //2 second invincibility
        }
        //iframes reduce every frame, so after a bit, they can be hit again
        else if (iframes > 0) {
            iframes--;
        }
    }
    
    public void checkHitByShot(Projectile shot) {
        //same as above but with projectiles instead of entities
        if (shot.hit(this) && iframes == 0) {
            shot.sendOut();
            damaged.play();
            knockBack();
            health--;
            iframes = 120; //2 second invincibility
        }
        else if (iframes > 0) {
            iframes--;
        }
    }

    public void shootSword() {
        //moves the sword projectile, and if the sword is being shot
        //their direction and respective image is set according to the player's direction
        if (canShoot) {
            swordLaser.move(playerX, playerY);
            if (swordLaser.getShotMade()) {
                swordLaserSwing.play();
                switch(dir) {
                    case UP:
                    swordLaser.setDir(UP);
                    swordLaser.setImage(imageSetLaser[0]);
                    break;
                    case RIGHT:
                    swordLaser.setDir(RIGHT);
                    swordLaser.setImage(imageSetLaser[1]);
                    break;
                    case DOWN:
                    swordLaser.setDir(DOWN);
                    swordLaser.setImage(imageSetLaser[2]);
                    break;
                    case LEFT:
                    swordLaser.setDir(LEFT);
                    swordLaser.setImage(imageSetLaser[3]);
                    break;
                }
            }
        }
    }
}