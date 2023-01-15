/**
 * Enemy.java
 * Muhammad Nadeem
 * Creates enemies of 3 types, each with exclusive attributes
 * like sprites, method of moving, and damage medium
 */

import java.awt.Image;
import java.awt.Graphics;
import java.util.Random;
import javax.swing.ImageIcon;
import java.awt.Rectangle;

class Enemy {
    //randomizer for hearts
    private Random rand = new Random();

    //directions
    public final int UP = 0, RIGHT = 1, DOWN = 2, LEFT = 3;

    //picture to show for certain frames when hit
    public final Image noPic = new ImageIcon("Resources/Enemies/noPic.png").getImage();
    //image sets for each enemy type
    public final Image[] imageSetStalfos = {new ImageIcon("Resources/Enemies/Stalfos0.png").getImage(),new ImageIcon("Resources/Enemies/Stalfos1.png").getImage()};
    
    public final Image[][] imageSetRope = {{new ImageIcon("Resources/Enemies/Rope0.png").getImage(),new ImageIcon("Resources/Enemies/Rope1.png").getImage()},
                                           {new ImageIcon("Resources/Enemies/Rope2.png").getImage(),new ImageIcon("Resources/Enemies/Rope3.png").getImage()}};

    public final Image[][] imageSetWizzrobe = {{new ImageIcon("Resources/Enemies/Wizzrobe0.png").getImage(),new ImageIcon("Resources/Enemies/Wizzrobe1.png").getImage()},
                                               {new ImageIcon("Resources/Enemies/Wizzrobe2.png").getImage(),new ImageIcon("Resources/Enemies/Wizzrobe3.png").getImage()}};

    private int enemyX, enemyY, dx, dy, dir;
    private int offset;             //sets an offset for the rectangles (to make it easier for player to not get hit)
    private int moveDir;            //sets the direction for the enemy to move in (random)
    private String type;            //sets type of enemy

    private Image[] animate;        //frames of animation
    private int frame;              //current frame
    private int wait;               //animation delay counter (decrements every frame, when 0, animation progresses to next frame)
    private int startWait;          //animation delay timer ("wait" resets to this every time the frame progresses)    

    private int health;
    private int iframes;            //invincibility frames, makes sure enemies can't be hit quickly in succession

    private Player player;          
    private Room currentRoom;

    private Projectile fireball;    //exclusive to one type of enemy and its main form of attack

    private Sound damaged = new Sound ("Resources/Sound/LOZ_Enemy_Hit.wav");        //plays whenever enemy is hit

    public Enemy(Player player, String type, int x, int y, Room room) {
        this.player = player;
        this.type = type;
        enemyX = x;
        enemyY = y;
        offset = 10;
        //chooses random direction (0-9; since directions are 0-3, theres a 40% chance of moving and a 60% chance of staying in the same spot)
        moveDir = rand.nextInt(10);
        dir = RIGHT;            //starting direction is right
        dx = 5;
        dy = 5;
        iframes = 0;

        currentRoom = room;

        animate = new Image[2];
        //sets health and frames of animation for each type of enemy, with wizzrobes having a fireball
        switch (type) {
            case "Stalfos":
            animate[0] = imageSetStalfos[0];
            animate[1] = imageSetStalfos[1];
            health = 2;
            break;
            case "Rope":
            animate[0] = imageSetRope[0][0];
            animate[1] = imageSetRope[0][1];
            health = 2;
            break;
            case "Wizzrobe":
            animate[0] = imageSetWizzrobe[0][0];
            animate[1] = imageSetWizzrobe[0][1];
            health = 3;
            fireball = new Projectile(enemyX, enemyY, 10, 10, new ImageIcon("Resources/Enemies/FireBall.png").getImage());
        }
        frame = 0;
        startWait = 12;
        wait = startWait;
    }

    public int getX() {return enemyX;}
    public int getY() {return enemyY;}
    public int getWidth() {return animate[frame].getWidth(null);}
    public int getHeight() {return animate[frame].getHeight(null);}
    public int getDX() {return dx;}
    public int getDY() {return dy;}
    public int getDir() {return dir;}
    public int getHealth() {return health;}
    public Room getRoom() {return currentRoom;}
    public Rectangle getRect() {return new Rectangle(enemyX+offset, enemyY+offset, getWidth()-2*offset, getHeight()-2*offset);}
    public Projectile getFireball() {return fireball;}

    public void setX(int newX) {enemyX = newX;}
    public void setY(int newY) {enemyY = newY;}
    public void setHealth(int newHealth) {health = newHealth;}
    public void setIFrames(int newIFrames) {iframes = newIFrames;}

    public void draw(Graphics g) {
        //draws enemies depending on direction and current frame
        switch (type) {
            case "Stalfos":
            for (int i = 0; i < imageSetStalfos.length; i++) {
                animate[i] = imageSetStalfos[i];
            }
            break;
            case "Rope":
            for (int i = 0; i < imageSetRope[0].length; i++) {
                if (dir == RIGHT) {
                    animate[i] = imageSetRope[0][i];
                }
                else if (dir == LEFT) {
                    animate[i] = imageSetRope[1][i];
                }
            }
            break;
            case "Wizzrobe":
            for (int i = 0; i < imageSetWizzrobe[0].length; i++) {
                if (dir == RIGHT) {
                    animate[i] = imageSetWizzrobe[0][i];
                }
                else if (dir == LEFT) {
                    animate[i] = imageSetWizzrobe[1][i];
                }
            }
            //fireball drawn for wizzrobes
            fireball.draw(g);
            break;
            default : System.out.println("Error!");
            break;
        }
        
        //while an enemy is going through their invincibility frames, the 1st and 2nd frames will be a blank image
        // and the rest will show the enemy; emulates a flickering effect to show damage has been dealt
        if (iframes%5 == 1 || iframes%5 == 2) {
            g.drawImage(noPic, enemyX, enemyY, null);
        }
        else {
            g.drawImage(animate[frame], enemyX, enemyY, null);
        }
    }

    public void move(){
        switch (type) {
            //moves randomly
            case "Stalfos": 
            randomMovement();
            break;
            //moves randomly and charges at player
            case "Rope":
            randomMovement();
            charge();
            break;
            case "Wizzrobe": 
            //moves randomly and shoots fireballs in a clockwise fashion
            randomMovement();
            if (fireball.getOutOfBounds()) {
                fireball.setShotMade(true);
                fireball.setDir((fireball.getDir()+1)%4); //direction goes 0,1,2,3 then repeats
            }
            fireball.move(enemyX, enemyY);
            break;
            default: System.out.println("Error!");
            break;
        }
    }

    public void randomMovement () {
        //checks collision with walls and blocks
        stopMoving();
        //animates enemies with the previously mentioned delay
        //the move direction is re-randomized, so the enemy either
        //switches directions, keeps moving in the same direction
        //or stops for a bit
        if (wait == 0) {
            moveDir = rand.nextInt(10);
            frame = (frame + 1) % animate.length;
            wait = startWait;
        }
        else {
            wait--;
        }

        //moves enemy depending on direction
        if (moveDir == UP) {
            enemyY -= dy;
            dir = UP;
        }
        if (moveDir == RIGHT) {
            enemyX += dx;
            dir = RIGHT;
        }
        
        if (moveDir == DOWN) {
            enemyY += dy;
            dir = DOWN;
        }
        
        if (moveDir == LEFT) {
            enemyX -= dx;
            dir = LEFT;
        }
    }

    public void stopMoving() {
        //stops the enemy from moving any further if borders or blocks are hit
        if (player.getState().equals("overworld")) {
            for(Obstacle barrier : currentRoom.getEOBorders()) {
                barrier.collide(this);
            }        
        }
        else if (player.getState().equals("dungeon")) {
            for(Obstacle barrier : currentRoom.getEDBorders()) {
                barrier.collide(this);
            }        
        }
        for(Obstacle block : currentRoom.getCollideSquares()) {
            block.collide(this);
        }
    }

    public void hitByPlayer() {
        //checks if the player has hit the enemy, then reduces their health and makes them invincible for some tme
        if (this.getRect().intersects(player.getAttackRect()) && iframes == 0) {
            if (health > 1) { 
                damaged.play();
            }
            iframes = 30;
            health--;
        }
        else if (iframes > 0) {
            iframes--;
        }
    }

    public void hitByShot() {
        //does the same thing as above but when the player's projectile hits them
        if (player.getLaser().hit(this.getRect())) {
            player.getLaser().sendOut();
            if (health > 1) { 
                damaged.play();
            }
            iframes = 30;
            health--;
        }
        else if (iframes > 0) {
            iframes--;
        }
    }

    public void charge() {
        //if the player is within line of sight of the enemy (ie. their center lies between the top and bottom of enemy if facing right or left,
        // or between the right and left is enemy is facing up or down), and the enemy is facing in the player's direction, then the enemy is given
        // a boost in speed
        switch(dir) {
            case UP:
            if (enemyX < player.getX()+player.getWidth()/2 && player.getX()+player.getWidth()/2 < enemyX+getWidth() && player.getY() < enemyY){
                enemyY -= 5;
            }
            break;
            case RIGHT:
            if (enemyY < player.getY()+player.getHeight()/2 && player.getY()+player.getHeight()/2 < enemyY+getHeight() && player.getX() > enemyX){
                enemyX += 5;
            }
            break;
            case DOWN:
            if (enemyX < player.getX()+player.getWidth()/2 && player.getX()+player.getWidth()/2 < enemyX+getWidth() && player.getY() > enemyY){
                enemyY += 5;
            }
            break;
            case LEFT:
            if (enemyY < player.getY()+player.getHeight()/2 && player.getY()+player.getHeight()/2 < enemyY+getHeight() && player.getX() < enemyX){
                enemyX -= 5;
            }
            break;
        }
    }
}