package game2D;

import java.io.*;
import javax.sound.sampled.*;
import javax.sound.midi.*;

/**
 * The Sound class is responsible for playing audio files (both wav and midi sounds).
 * It supports volume control, global mute, and other sound filters.
 */
public class Sound extends Thread {
    private String filename;
    private boolean finished; // Indicates if the sound has finished playing
    private float volume = 1.0f; // Volume level 
    private boolean isMidi = false; // checks if the file is a MIDI file
    private SoundFilter filter; 
    private static boolean globalMute = false; // Global mute state
    
    private Clip clip;   // Clip object to control playback
    private boolean paused = false; // Track if the sound is paused

    /**
     * Constructor for creating a Sound object with a file name.
     *
     * @param fname The file name of the sound to play.
     */
    public Sound(String fname) {
        filename = fname;
        finished = false;
        isMidi = filename.toLowerCase().endsWith(".mid");
    }

    /**
     * Constructor for creating a Sound object with a file name and a filter.
     *
     * @param fname  The file name of the sound to play.
     * @param filter The sound filter to apply to the sound.
     */
    public Sound(String fname, SoundFilter filter) {
        this(fname);
        this.filter = filter;
    }

    /**
     * Sets the volume of the sound.
     *
     * @param vol The volume level (from 0.0 to 1.0).
     */
    public void setVolume(float vol) {
        if (vol < 0.0f) vol = 0.0f;
        if (vol > 1.0f) vol = 1.0f;
        this.volume = vol;
    }

    /**
     * Sets the global mute state for all sounds.
     *
     * @param mute True to mute all sounds, false to unmute.
     */
    public static void setGlobalMute(boolean mute) {
        globalMute = mute;
    }

    /**
     * The main run method for the thread. Plays the sound file.
     */
    @Override
    public void run() {
        try {
            if (!isMidi) {
                playWavFile();
            } else {
                playMidiFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        finished = true;
    }

    /**
     * Plays a wav file with sound filtering and volume control.
     *
     * @throws Exception If an error occurs while playing the file.
     */
    private void playWavFile() throws Exception {
        File file = new File(filename);
        AudioInputStream stream = AudioSystem.getAudioInputStream(file);
        AudioFormat audio_format = stream.getFormat();

        // Apply filter if one was specified
        if (filter != null) {
            InputStream filtered_Stream = new FilterInputStream(stream) {
                @Override
                public int read(byte[] b, int off, int len) throws IOException {
                    return filter.read(b, off, len);
                }
            };
            stream = new AudioInputStream(filtered_Stream, audio_format, stream.getFrameLength());
        }

        DataLine.Info info = new DataLine.Info(Clip.class, audio_format);
        Clip clip = (Clip) AudioSystem.getLine(info);
        clip.open(stream);

        if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float sound_decibels = globalMute ? -80.0f : (float) (Math.log(volume) / Math.log(10.0) * 20.0);
            volumeControl.setValue(sound_decibels);
        }

        clip.start();
        Thread.sleep(100);
        while (clip.isRunning()) {
            if (globalMute && clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                volumeControl.setValue(-80.0f); // Mute dynamically
            }
            Thread.sleep(100);
        }
        clip.close();
    }

    /**
     * Plays a MIDI file with looping to it.
     *
     * @throws Exception If an error occurs while playing the file.
     */
    private void playMidiFile() throws Exception {
        File midiFile = new File(filename);
        Sequence sequence = MidiSystem.getSequence(midiFile);
        Sequencer sequencer = MidiSystem.getSequencer();
        sequencer.open();
        sequencer.setSequence(sequence);

        sequencer.setLoopCount(Sequencer.LOOP_CONTINUOUSLY); // Loop MIDI continuously
        sequencer.start();
        while (sequencer.isRunning()) {
            Thread.sleep(100);
        }
        sequencer.close();

        /* 
        // the below code is commented out because it is not used in the current implementation. 
        // it was tried to use to dynamically adjust the volume of the MIDI channels.
        // However, it wasnt working really well.
        
        Thread volumeAdjuster = new Thread(() -> {
            try {
                while (sequencer.isRunning()) {
                    if (sequencer instanceof Synthesizer) {
                        Synthesizer synthesizer = (Synthesizer) sequencer;
                        MidiChannel[] channels = synthesizer.getChannels();
                        for (MidiChannel channel : channels) {
                            if (channel != null) {
                                channel.controlChange(7, globalMute ? 0 : (int) (volume * 127));
                            }
                        }
                    }
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        volumeAdjuster.start();
        */       
    }

     /**
     * Pause the sound playback.
     */
    public void pause() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
            paused = true;
        }
    }

    /**
     * Resume the sound playback.
     */
    public void resumeSound() {
        if (clip != null && paused) {
            clip.start();
            paused = false;
        }
    }
}