/**
 * LegendOfZelda.java
 * Muhammad Nadeem
 * Main game file; runs, draws, and updates the game loop to show a
 * runnable, playable game
 * Once the player reaches the triforce, or dies in the process, the game ends
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class LegendOfZelda extends JFrame{
    private static final long serialVersionUID = 1L;

    GPanel game;

    public LegendOfZelda() {
        super("Legend of Zelda: NES");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        game = new GPanel();
        add(game);
        pack();
        setVisible(true);
        setResizable(false);
    }

    public static void main(String[] args) {
        LegendOfZelda frame = new LegendOfZelda();
    }
}

class GPanel extends JPanel implements ActionListener, KeyListener{
    private static final long serialVersionUID = 1L;

    public static final int WIDTH = 768, HEIGHT = 672;
    private Timer time;
    private Font retroType;         //an arcade-like font
    private boolean[] keys;         //keyboard keys
    
    private String screen;          //the screen being shown on the frame
    private Image introScreen;
    private Image gameOverScreen;
    private Image winScreen;

    private Player link;
    //the keys the player can use
    private int[] playerKeys = {KeyEvent.VK_W, KeyEvent.VK_D, KeyEvent.VK_S, KeyEvent.VK_A, KeyEvent.VK_J};
    private Image heartPic;

    //the sound played when the player picks up the sword upgrade
    private Sound itemPickUp = new Sound("Resources/Sound/LOZ_Get_Heart.wav");
    //the background music
    private Sound overWorldMusic = new Sound("Resources/Sound/LOZ_Overworld_Music.wav");

    public GPanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        addKeyListener(this);

        screen = "intro";
        link = new Player(140,400, playerKeys);
        keys = new boolean[KeyEvent.KEY_LAST+1];
        heartPic = new ImageIcon("Resources/Link/healthHeart.png").getImage();

        introScreen = new ImageIcon("Resources/Screens/introScreen.png").getImage();
        winScreen = new ImageIcon("Resources/Screens/winScreen.png").getImage();
        gameOverScreen = new ImageIcon("Resources/Screens/gameOverScreen.png").getImage();

        //initializes all the enemies of all the rooms of the dungeon
        for (Room room : link.getDungeon().getArea()) {
            if (room == null) {}
            else {room.initDungeonEnemies();}
        }
        //initializes all the enemies of all the rooms of the overworld
        for (Room room : link.getOverWorld().getArea()) {
            if (room == null) {}
            else {room.initOverWorldEnemies();}
        }

        //loads in the arcade-like font (help from internet)
        try {
            retroType = Font.createFont(Font.TRUETYPE_FONT, new File("Resources/Fonts/PressStart2P-Regular.ttf")).deriveFont(25f);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(retroType);
        } catch (IOException | FontFormatException e) {e.printStackTrace();}

        overWorldMusic.playMusic();

        time = new Timer(20, this);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        setFocusable(true);
        requestFocus();
    }

    @Override
    public void paint(Graphics g) {
        //below 3 ifs are to show specific images depending on the screen/game state
        if (screen.equals("intro")) {
            g.drawImage(introScreen,0,0,null);
        }
        if (screen.equals("gameover")) {
            g.drawImage(gameOverScreen, 0,0, null);
        }
        if (screen.equals("win")) {
            g.setColor(Color.BLACK);
            g.fillRect(0,0,WIDTH,HEIGHT);
            g.drawImage(winScreen, 290, 300, null);
            g.setColor(Color.GREEN);
            g.setFont(retroType);
            g.drawString("YOU WIN!", 270, 150);
        }
        if (screen.equals("game")) {
            //draws the room the player is in
            g.drawImage(link.getRoom().getBackground(),0,150,null);
            //draws player
            link.draw(g);
            //draws the enemies of the room the player is in
            link.getRoom().drawEnemies(g);
            //draws the room's heart (if the room has one)
            link.getRoom().drawHeart(g);
            //draws the boss in the boss room as long as the boss is alive
            if (link.getRoom() == link.getDungeon().getArea().get(6) && link.getDungeon().getBoss().getHealth() > 0) { // change to 6
                link.getDungeon().getBoss().draw(g);
            }
            //draws the sword laser power up in the all-black room if the player hasn't picked the power up yet
            if (link.getRoom() == link.getDungeon().getArea().get(4) && !link.getCanShoot()) {
                link.getDungeon().getPlayerLaser().draw(g);
            }
            //draws the triforce in the final room (room after boss)
            if (link.getRoom() == link.getDungeon().getArea().get(7)) {
                link.getDungeon().getTriforce().draw(g);
            }

            //draws the black box on top of screen
            g.setColor(Color.BLACK);
            g.fillRect(0,0,WIDTH, 150);

            //draws hearts on the top of the screen depending on the health of the player
            g.setColor(Color.RED);
            g.setFont(retroType);
            g.drawString("-LIVES-",0,40);
            for (int i = 0; i < link.getHealth(); i++) {
                g.drawImage(heartPic, 50*i, 70, null);
            }
        }
    }
    
    public void update() {
        if (screen.equals("game")) {
            //moves player
            link.move(keys);
            //moves enemies in the player's current room
            link.getRoom().moveEnemies();
            //kills enemies if conditions are met in the player's current room
            link.getRoom().killEnemies();
            //updates the heart of the player's current room
            link.getRoom().updateHeart();
            //allows boss to function in the boss room
            if (link.getRoom() == link.getDungeon().getArea().get(6) && link.getDungeon().getBoss().getHealth() > 0) { // change to 6
                link.getDungeon().getBoss().update();
            }
            //allows player to pick up the sword power up and plays a sound when they do
            if (link.getRoom() == link.getDungeon().getArea().get(4)) {
                if (link.getDungeon().getPlayerLaser().collideLaser(link) && link.getCanShoot() == false) {
                    itemPickUp.play();
                    link.setCanShoot(true);
                }
            }
            //allows player to pick up the triforce and ends the game on a win if they do
            //music stops and a sound is played if picked up
            if (link.getRoom() == link.getDungeon().getArea().get(7)) {
                if (link.getDungeon().getTriforce().collideTriforce(link)) {
                    screen = "win";
                    overWorldMusic.stop();
                    Sound triforcePickUp = new Sound("Resources/Sound/LOZ_Win_Game.wav");
                    triforcePickUp.play();
                }
            }
            //ends the game on a game over if player health runs out
            //music stops and plays a sound if health drops to 0
            if (link.getHealth() == 0) {
                screen = "gameover";
                overWorldMusic.stop();
                Sound death = new Sound("Resources/Sound/LOZ_Link_Die.wav");
                death.play();
            }
        }
    }
    //main game loop
    public void actionPerformed(ActionEvent e) {
        if (screen.equals("game")) {
            update();
        }
        repaint();
    }

    //starts the game after any key is pressed
    //makes sure the attack event happens only once even when key is held down
    public void keyPressed(KeyEvent e) {
        time.start();
        if (!(screen.equals("gameover") || screen.equals("win"))) {
            screen = "game";
        }
        if (e.getKeyCode() == KeyEvent.VK_J && !keys[KeyEvent.VK_J]) {
            link.setCanAttack(true);
            link.getLaser().setShotMade(true);
        }
        keys[e.getKeyCode()] = true; 
    }
    public void keyReleased(KeyEvent e) {
        keys[e.getKeyCode()] = false;
    }
    public void keyTyped(KeyEvent e) {}   
}