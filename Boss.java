/**
 * Boss.java
 * Muhammad Nadeem
 * Creates a boss that shoots fireballs in a wide-shot of 3
 * The final enemy to face before beating the game
 */

import java.awt.*;
import javax.swing.ImageIcon;
import java.util.ArrayList;

class Boss {
    private Player player;
    private int health;
    private int iframes = 0;
    
    private final Image noPic = new ImageIcon("Resources/Boss/noBossPic.png").getImage();
    private final Image[] imageSetBoss = {new ImageIcon("Resources/Boss/Aquamentus0.png").getImage(), new ImageIcon("Resources/Boss/Aquamentus1.png").getImage(),
                                          new ImageIcon("Resources/Boss/Aquamentus2.png").getImage(), new ImageIcon("Resources/Boss/Aquamentus3.png").getImage()};
  
    //image of the projectile
    private final Image firePic = new ImageIcon("Resources/Enemies/FireBall.png").getImage();
    
    private int bossX, bossY, dx;   //boss only moves left and right, so no need for dy
    private int offset = 30;        //for accuracy to the game, since the collision rectangle doesn't enclose the whole boss
    private int frame;
    private Image[] animate;
    private int wait;
    private int startWait;
    
    private ArrayList<Projectile> fireballs;    //holds 3 fireballs to be shot at once depending on the location of the player
    private boolean isAttacking;
    
    private Sound scream;

    public Boss (Player player, int x, int y) {
        this.player = player;
        health = 10;
        bossX = x;
        bossY = y;
        dx = 1;

        frame = 0;
        startWait = 10;
        wait = startWait;

        animate = new Image[2];
        animate[0] = imageSetBoss[2];
        animate[1] = imageSetBoss[3];

        isAttacking = true;
        fireballs = new ArrayList<Projectile>();
        //makes 3 projectiles and puts them in the aforementioned arraylist
        for (int i = 0; i < 3; i++) {
            fireballs.add(new Projectile(player, this, bossX, bossY, i+1, firePic));
        }

        scream = new Sound("Resources/Sound/LOZ_Boss_Scream1.wav");
    }

    public int getX() {return bossX;}
    public int getY() {return bossY;}
    public int getHealth() {return health;}
    public Rectangle getRect() {return new Rectangle(bossX, bossY+offset, animate[frame].getWidth(null),animate[frame].getHeight(null)-2*offset);}

    public void draw(Graphics g) {
        //draws all fireballs
        for (Projectile shot : fireballs) {
            shot.draw(g);
        }
        //draws boss with iframes same as with player and enemy
        if (iframes%5 == 1 || iframes%5 == 2) {
            g.drawImage(noPic, bossX, bossY, null);
        }
        else {
            g.drawImage(animate[frame], bossX, bossY, null);
        }
        if (wait == 0) {
            frame = (frame + 1) % animate.length;
            wait = startWait;
        }
        else {
            wait--;
        }
    }

    public void update() {
        //moves the boss one way, until a certain point is reached and it moves the other way
        bossX -= dx;
        if (bossX < 500 || bossX > 620) {
            dx *= -1;
        }
        shoot();
        resetFire();
        //sees if the boss is hit by the player's sword or projectile, and subsequently reduces health and sets iframes
        if ((player.getAttackRect().intersects(getRect()) || player.getLaser().hit(getRect()) && iframes == 0)) {
            player.getLaser().sendOut();
            health--;
            iframes = 30;   //0.5 second invincibility
        }
        else if (iframes > 0) {
            iframes--;
        }
        //sees if the player is hit by the body of the boss
        player.checkHitByEntity(getRect());
    }

    public void shoot() {
        for (Projectile shot : fireballs) {
            //moves each shot
            shot.move(this);
            //sees if any fireball hits the player
            player.checkHitByShot(shot);
            //if the shots go off screen, the boss stops attacking, but if those same projectiles reach
            // a certain point off screen, the boss starts a new attack
            if (shot.getX() < 0) {
                isAttacking = false;
            }
            if (shot.getX() < -400 || shot.getY() > 1000) {
                shot.setShotMade(true);
                isAttacking = true;
            }
        }
        //if the boss is attacking, its mouth is open, otherwise, its mouth is closed
        if (isAttacking) {
            animate[0] = imageSetBoss[0];
            animate[1] = imageSetBoss[1];
        }
        else {
            animate[0] = imageSetBoss[2];
            animate[1] = imageSetBoss[3];
        }
    }

    public void resetFire() {
        boolean reset = false;
        for (Projectile shot : fireballs) {
            if (shot.getShotMade()) {
                //if any of the shots have the potential to be shot again, a reset can be done
                reset = true;
            }
        }
        if (reset) {
            //all the current fireballs are removed from the arraylist, and a new set of 3 are added
            //to the arraylist (with respect to the player's location)
            fireballs.removeAll(fireballs);
            for (int i = 0; i < 3; i++) {
                fireballs.add(new Projectile(player, this, bossX, bossY, i+1, firePic));
            }
            scream.play();
            isAttacking = true;
        }
    }
}