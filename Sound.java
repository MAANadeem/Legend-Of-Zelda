/**
 * Sound.java
 * Muhammad Nadeem
 * Quality of life for sounds
 * Allows for quick initialization of sound objects
 */

import java.io.*;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

//had to search up most of this - StackOverflow
class Sound {

    Clip clip;

    //makes a sound by passing in just the file name
    public Sound(String fileName) {
        try {
            File file = new File(fileName);
            AudioInputStream sound = AudioSystem.getAudioInputStream(file);
            clip = AudioSystem.getClip();
            clip.open(sound);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //plays the sound
    public void play() {
        clip.setFramePosition(0);
        clip.start();
    }

    //plays the sound continuously (music)
    public void playMusic() {
        clip.setFramePosition(0);
        clip.loop(Clip.LOOP_CONTINUOUSLY);
    }
    
    //stops the sound
    public void stop() {
        clip.stop();
    }
}
