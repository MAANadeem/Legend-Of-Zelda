import java.awt.Rectangle;

class Obstacle {
    private Rectangle sRect;

    public Obstacle(Rectangle rect) {
        this.sRect = rect;
         
    }

    public void collide(Player player) {
        if (sRect.intersects(player.getRect())) {
            if (player.getDir() == player.UP) {
                player.setY(player.getY()+player.getDY());
            }
            if (player.getDir() == player.RIGHT) {
                player.setX(player.getX()-player.getDX());
            }
            if (player.getDir() == player.DOWN) {
                player.setY(player.getY()-player.getDY());
            }
            if (player.getDir() == player.LEFT) {
                player.setX(player.getX()+player.getDX());
            }
        }
    }

    public void collide(Enemy enemy) {
        if (sRect.intersects(enemy.getRect())) {
            if (enemy.getDir() == enemy.UP) {
                enemy.setY(enemy.getY()+enemy.getDY());
            }
            if (enemy.getDir() == enemy.RIGHT) {
                enemy.setX(enemy.getX()-enemy.getDX());
            }
            if (enemy.getDir() == enemy.DOWN) {
                enemy.setY(enemy.getY()-enemy.getDY());
            }
            if (enemy.getDir() == enemy.LEFT) {
                enemy.setX(enemy.getX()+enemy.getDX());
            }
        }
    }
}