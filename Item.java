/**
 * Item.java
 * Muhammad Nadeem
 * Makes 3 types of items that the player can
 * interact with, each having their own collide function
 * - Heart
 * - Sword laser pickup
 * - Triforce
 */

import java.awt.*;
import javax.swing.ImageIcon;

class Item {
  private int itemX, itemY;
  private String type;        //the type of item
  private Image itemPic;      //the picture of the item
  
  public Item(int x, int y, String type) {
    itemX = x;
    itemY = y;
    this.type = type;

    //chooses an image for the item depending on type
    if (type.equals("heart")) {
      itemPic = new ImageIcon("Resources/Link/itemHeart.png").getImage();
    }
    else if (type.equals("laser")) {
      itemPic = new ImageIcon("Resources/Link/SwordShot/UpShot.png").getImage();
    }
    else if (type.equals("triforce")) {
      itemPic = new ImageIcon("Resources/Link/triforce.png").getImage();
    }
  }
  
  public Rectangle getRect() {return new Rectangle(itemX, itemY, itemPic.getWidth(null), itemPic.getHeight(null));}

  public void draw(Graphics g) {
    g.drawImage(itemPic, itemX, itemY, null);
  }
  
  //checks for collision for each possible item type
  public boolean collideLaser(Player player) {
    return type.equals("laser") && getRect().intersects(player.getRect());
  }
  public boolean collideHeart(Player player) {
    return type.equals("heart") && getRect().intersects(player.getRect());
  }
  public boolean collideTriforce(Player player) {
    return type.equals("triforce") && getRect().intersects(player.getRect());
  }
}