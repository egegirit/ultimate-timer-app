import javax.sound.sampled.*;
import java.io.File;
import java.util.Objects;

public class AudioPlayer {
    private Clip clip;

    // Volume in dB, -10 means -10 dB lower than the original volume
    public void playNotificationSound(float volume) {
        Thread audioThread = new Thread(() -> {
            try {
                String resourcesFolderPath = System.getProperty("user.dir") + "\\resources\\audio\\notification-sound.wav";
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(resourcesFolderPath));
                clip = AudioSystem.getClip();
                clip.open(audioInputStream);
                setVolume(volume); // Set the desired volume

                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        clip.close();
                    }
                });

                clip.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        audioThread.start();
    }

    private void setVolume(float volume) {
        if (clip != null) {
            try {
                FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                gainControl.setValue(volume);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
    }
}
