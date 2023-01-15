/**
 * Projectile.java
 * Muhammad Nadeem
 * Creates free-moving objects that move endlessly, and that come back
 * to the entity that called them after conditions are met
 * While most projectiles can only move in the 4 primary directions,
 * boss shots can move at an angle relative to the player
 */

import java.awt.*;

class Projectile {
    public final int UP = 0, RIGHT = 1, DOWN = 2, LEFT = 3;

    private int shotX, shotY, dx, dy, dir;
    private boolean shotMade;
    private boolean outOfBounds;
    private Image img;

    public Projectile(int x, int y, int dx, int dy, Image img) {
        shotX = x;
        shotY = y;
        this.dx = dx;
        this.dy = dy;
        dir = UP;
        shotMade = false;
        outOfBounds = false;
        this.img = img;
    }

    public Projectile(Player player, Boss boss, int x, int y, int pos, Image img) {
        ///pos is the position of the fireball
        shotX = x;
        shotY = y;
        dir = UP;
        shotMade = false;
        outOfBounds = false;
        this.img = img;

        //calculates the angle at which to shoot the fireball at,
        //if the position of the fireball is the top(1) or bottom(3), their
        //angles are raised and reduced respectively (to act as a wide-shot)
        double angle = Math.atan2(player.getY() - boss.getY(), player.getX() - boss.getX());
        switch(pos) {
            case 1:
            angle += Math.toRadians(20);
            break;
            case 2:
            angle += 0;
            break;
            case 3:
            angle -= Math.toRadians(20);
            break;
        }
        //speed and direction are set
        dx = (int)(Math.cos(angle) * 10);
        dy = (int)(Math.sin(angle) * 10);
    }

    public int getX() {return shotX;}
    public int getY() {return shotY;}
    public int getDX() {return dx;}
    public int getDY() {return dy;}
    public int getDir() {return dir;}
    public boolean getShotMade() {return shotMade;}
    public boolean getOutOfBounds() {return outOfBounds;}
    public Rectangle getRect() {return new Rectangle(shotX, shotY, img.getWidth(null), img.getHeight(null));}

    public void setX(int newShotX) {shotX = newShotX;}
    public void setY(int newShotY) {shotY = newShotY;}
    public void setDir(int newDir) {dir = newDir;}
    public void setShotMade(boolean isShot) {shotMade = isShot;}
    public void setImage(Image newImg) {img = newImg;}
    public void setDX(int newDX) {dx = newDX;}
    public void setDY(int newDY) {dy = newDY;}

    public void draw(Graphics g) {
        g.drawImage(img, shotX, shotY, null);
    }

    public void move(int x, int y) { //player.getX() | boss.getX() | enemy.getX()
        //regular projectiles shoot in one of the 4 directions
        switch(dir) {
            case UP:
            shotY -= dy;
            if (shotY < 150) {
                outOfBounds = true;
            }
            break;
            case RIGHT:
            shotX += dx;
            if (shotX > GPanel.WIDTH) {
                outOfBounds = true;
            }
            break;
            case DOWN:
            shotY += dy;
            if (shotY > GPanel.HEIGHT) {
                outOfBounds = true;
            }
            break;
            case LEFT:
            shotX -= dx;
            if (shotX < 0) {
                outOfBounds = true;
            }
            break;
        }

        if (!outOfBounds) {
            setShotMade(false);
        }

        //if the projectile is shot, its position is set to the x and y value passed in,
        // which should be the x and y values of the entity shooting it
        if (shotMade) {
            shotX = x;
            shotY = y;
            outOfBounds = false;
        }

    }
    
    public void move(Boss boss) {
        //exclusive to boss projectiles, just moves the bullets
        //and lets the boss class take care of reseting shots
        shotX += dx;
        shotY += dy;
    }

    //sees if the player's sword projectile hits a rectangle (an entity)
    public boolean hit(Rectangle rect) {
        return getRect().intersects(rect);
    }

    //sees if a projectile from an entity hits the player
    public boolean hit(Player player) {
        return getRect().intersects(player.getRect());
    }

    public void sendOut() {
        //if a projectile hits something, it is sent off screen depending on direction
        switch(dir) {
            case UP:
            shotY = -1000;
            break;
            case RIGHT:
            shotX = GPanel.WIDTH;
            break;
            case DOWN:
            shotY = GPanel.HEIGHT;
            break;
            case LEFT:
            shotX = -50;
            break;
        }
    }
    
}
