package com.codcodes.icebreaker.auxilary;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.codcodes.icebreaker.model.User;
import com.codcodes.icebreaker.screens.MainActivity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.logging.Handler;

//import be.tarsos.dsp.AudioDispatcher;
//import be.tarsos.dsp.AudioEvent;
//import be.tarsos.dsp.io.TarsosDSPAudioFormat;
//import be.tarsos.dsp.io.android.AndroidAudioPlayer;
//import be.tarsos.dsp.io.android.AndroidFFMPEGLocator;
//import be.tarsos.dsp.io.android.AudioDispatcherFactory;
//import be.tarsos.dsp.io.jvm.AudioPlayer;
//import be.tarsos.dsp.io.jvm.JVMAudioInputStream;
//import be.tarsos.dsp.pitch.PitchDetectionHandler;
//import be.tarsos.dsp.pitch.PitchDetectionResult;
//import be.tarsos.dsp.pitch.PitchProcessor;
import be.tarsos.dsp.*;
import be.tarsos.dsp.io.TarsosDSPAudioFormat;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;

/**
 * Created by Casper on 2016/10/30.
 */
public class AudioMgr// implements Runnable
{
    AudioRecord mRecordInstance = null;

    private final int FREQUENCY = 11025;
    private final int CHANNEL = AudioFormat.CHANNEL_IN_MONO;
    private final int ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    private Thread recThread;
    private volatile boolean isRecording = false;
    private short audioData[];
    private short[] buffer;

    private static MediaRecorder recorder=null;
    private String fileName;
    private ByteArrayOutputStream outBytes;

    private static String TAG = "IB/AudioMgr";
    private Context context;
    private int minBufferSize, bufferSize;
    private final int SAMPLE_RATE=11025;//44100;
    private long last_pitch_update=0;
    private float hz=0.0f;
    private AudioDispatcher adp;

    public AudioMgr(Context context)
    {
        this.context=context;
    }

    /*@Override
    public void run()
    {
        this.isRunning = true;
        try
        {
            // create the audio buffer
            // get the minimum buffer size
            /*int minBufferSize = AudioRecord.getMinBufferSize(FREQUENCY, CHANNEL, ENCODING);

            // and the actual buffer size for the audio to record
            // frequency * seconds to record.
            bufferSize = Math.max(minBufferSize, this.FREQUENCY * this.secondsToRecord);

            audioData = new short[bufferSize];

            // start recorder
            mRecordInstance = new AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    FREQUENCY, CHANNEL,
                    ENCODING, minBufferSize);

            mRecordInstance.startRecording();
            boolean firstRun = true;*
            //do
            {
                try
                {
                    fileName = MainActivity.rootDir+"/Icebreak/sound.wav";

                    recorder = new MediaRecorder();
                    recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                    recorder.setOutputFile(fileName);
                    recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

                    try {
                        recorder.prepare();
                    } catch (Exception e) {
                        Log.e("In Recording", "prepare() failed");
                    }

                    recorder.start();
                    /*long time = System.currentTimeMillis();
                    // fill audio buffer with mic data.
                    int samplesIn = 0;
                    //ffmpeg_audio encoding
                    while (samplesIn < bufferSize)
                    {
                        samplesIn += mRecordInstance.read(audioData, samplesIn, bufferSize - samplesIn);
                        //Buffer bffr = ShortBuffer.wrap(audioData);

                        if(mRecordInstance.getRecordingState() == AudioRecord.RECORDSTATE_STOPPED)
                            break;
                        Log.d(TAG,"Listening.");
                    }

                    Log.d(TAG, "Audio recorded: " + (System.currentTimeMillis() - time) + " ms");

                    int sampleRate=44100;
                    int bufferSize = AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
                    AudioTrack mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize, AudioTrack.MODE_STREAM);
                    mAudioTrack.play();

                    //Convert short[] to byte[]
                    final byte[] byteBuffer = new byte[audioData.length * 2];
                    int bufferIndex = 0;
                    for (int i = 0; i < byteBuffer.length; i++) {
                        final int x = (int) (audioData[bufferIndex++] * 32767.0);
                        byteBuffer[i] = (byte) x;
                        i++;
                        byteBuffer[i] = (byte) (x >>> 8);
                    }
                    //Write to disk
                    File out = new File(MainActivity.rootDir+"/Icebreak/output.wav");
                    FileOutputStream fos = new FileOutputStream(out);
                    DataOutputStream dos = new DataOutputStream(fos);
                    dos.write(byteBuffer);
                    dos.flush();
                    dos.close();*/
                    //fos.write(byteBuffer);
                    //fos.flush();
                    //fos.close();
                    /*boolean bigEndian = false;
                    boolean signed = true;
                    int bits = 16;
                    int channels = 1;
                    int sampleRate=44100;
                    javax.sound.sampled.
                    AudioFormat format;
                    format = new AudioFormat(sampleRate, bits, channels, signed, bigEndian);
                    ByteArrayInputStream bais = new ByteArrayInputStream(byteBuffer);
                    AudioInputStream audioInputStream;
                    audioInputStream = new AudioInputStream(bais, format,buffer.length);
                    AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, out);
                    audioInputStream.close();*

                    Log.d(TAG,"Wrote to disk.");

                    // see if the process was stopped.
                    //if(mRecordInstance.getRecordingState() == AudioRecord.RECORDSTATE_STOPPED || (!firstRun && !this.continuous))
                    //    break;
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                    Log.e(TAG, e.getLocalizedMessage());
                }
            }
            //while (this.continuous);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.e(TAG, e.getLocalizedMessage());
        }

        /*if(mRecordInstance != null)
        {
            mRecordInstance.stop();
            mRecordInstance.release();
            mRecordInstance = null;
        }
        this.isRunning = false;*
    }*/

    public void listen(final int secondsToRecord, final String fileName) throws IOException
    {
        //outBytes = new ByteArrayOutputStream();
        this.fileName=fileName;

        int minBufferSize = AudioRecord.getMinBufferSize(FREQUENCY, CHANNEL, ENCODING);
        bufferSize = Math.max(minBufferSize, this.FREQUENCY * secondsToRecord);

        // start recorder
        mRecordInstance = new AudioRecord(
                MediaRecorder.AudioSource.MIC,
                FREQUENCY, CHANNEL,
                ENCODING, minBufferSize);

        mRecordInstance.startRecording();
        isRecording=true;
        buffer = new short[bufferSize];
        audioData = new short[bufferSize];

        final TarsosDSPAudioFormat tarsosFormat=new be.tarsos.dsp.io.TarsosDSPAudioFormat((float)SAMPLE_RATE,16,1,true,false);
        Thread listeningThread=new Thread(new Runnable(){
            @Override public void run()
            {
                while (isRecording)
                {
                    int bufferReadResult=mRecordInstance.read(audioData,0,audioData.length);

                    //Wave wave = new Wave(SAMPLE_RATE, (short) 1, audioData, 0, audioData.length-1);
                    //String fileName = MainActivity.rootDir+"/Icebreak/media/"+System.currentTimeMillis()+".wav";
                    //wave.writeToFile(fileName);

                    if(bufferReadResult>0)
                    {
                        int cursor = 0;
                        if(buffer.length>0)
                        {
                            //backup buffer
                            short[] tmp = new short[buffer.length];
                            cursor = tmp.length;
                            System.arraycopy(buffer, 0, tmp, 0, buffer.length - 1);
                            buffer = new short[tmp.length + bufferReadResult];
                            //restore buffer
                            System.arraycopy(tmp, 0, buffer, 0, buffer.length - tmp.length);
                        }else
                        {
                            buffer = new short[bufferReadResult+1];
                            cursor=0;
                        }
                        //add new recorded audio to buffer
                        System.arraycopy(audioData, 0, buffer, cursor, bufferReadResult);

                        Log.d(TAG, "Recorded " + secondsToRecord + "s [" + bufferReadResult + " samples; audioData.capacity=" + audioData.length + "], and added to buffer [~"+buffer.length*2+"bytes].");
                    }
                    //AudioEvent audioEvent=new AudioEvent(tarsosFormat,bufferReadResult);
                    //audioEvent.setFloatBufferWithByteBuffer(buffer);
                    //mPitchProcessor.process(audioEvent);
                }
                //mRecordInstance.stop();
                //mRecordInstance.release();
            }
        }
        );
        listeningThread.start();
    }

    public void pitch(final int buffer_size, final int  buffer_overlap)
    {
        final ArrayList<Float> samples = new ArrayList<>();
        last_pitch_update = System.currentTimeMillis();
        if(!isRecording)
        {
            if(mRecordInstance!=null)
                if (mRecordInstance.getState() == AudioRecord.RECORDSTATE_RECORDING)
                    return;
            //Else proceed as normal
            final long start_time = System.currentTimeMillis();
            PitchDetectionHandler handler = new PitchDetectionHandler()
            {
                @Override
                public void handlePitch(PitchDetectionResult pitchDetectionResult, AudioEvent audioEvent)
                {
                    //System.out.println(">>>>>>>>>"+audioEvent.getTimeStamp() + " " + pitchDetectionResult.getPitch());
                    System.out.println(pitchDetectionResult.getPitch() + "Hz");
                    //Collect samples
                    hz = pitchDetectionResult.getPitch();
                    samples.add(hz);

                    long ms_elapsed = (System.currentTimeMillis() - start_time);
                    Log.v(TAG, "Recording pitch samples [" + hz + "Hz]. MS elapsed = " + ms_elapsed);
                    if(ms_elapsed > INTERVALS.PITCH_MS.getValue())
                    {
                        if (adp != null && samples != null)
                        {
                            //Stop pitch recorder
                            adp.stop();
                            adp = null;
                            //adp.removeAudioProcessor(pitchProcessor);
                            //Compute average pitch
                            double avg_pitch = 0.0;
                            double total_pitch = 0.0;
                            for (float pitch : samples)
                                total_pitch += pitch;
                            avg_pitch = total_pitch / samples.size();
                            //Upload average pitch
                            Log.d(TAG,"Average pitch: " + avg_pitch + "Hz");
                            try
                            {
                                //User user = RemoteComms.getUser(context, SharedPreference.getUsername(context));
                                User u = new User();
                                u.setUsername(SharedPreference.getUsername(context));
                                u.setPitch(avg_pitch);
                                if (u != null)
                                {
                                    //Update user pitch on server
                                    String resp = RemoteComms.postData("userUpdate/" + u.getUsername(), u.toString());
                                    if (resp.contains("200"))
                                    {
                                        Log.d(TAG, "Updated user pitch.");
                                    }
                                } else Log.d(TAG, "User object is null.");
                            } catch (IOException e)
                            {
                                LocalComms.logException(e);
                            }
                            //clear samples
                            samples.clear();
                        } else Log.d(TAG, "AudioDispatcher is null.");
                    }
                    //long ms_since_last_pitch_upd = System.currentTimeMillis()-last_pitch_update;
                }
            };

            //AudioDispatcher adp = AudioDispatcherFactory.fromDefaultMicrophone(44100,5000,2500);
            adp = AudioDispatcherFactory.fromDefaultMicrophone(44100,buffer_size,buffer_overlap);
            final PitchProcessor pitchProcessor = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.YIN, 44100, 2048, handler);
            adp.addAudioProcessor(pitchProcessor);
            adp.run();
        }
    }

    /**
     * stops the listening process if there's one in progress
     */
    public void stop()
    {
        /*this.continuous = false;
        if(mRecordInstance != null)
            mRecordInstance.stop();*/
        if(mRecordInstance != null)
        {
            mRecordInstance.stop();
            mRecordInstance.release();
        }
        isRecording = false;
        try
        {
            String ev_id = WritersAndReaders.readAttributeFromConfig(Config.EVENT_ID.getValue());
            if(ev_id!=null)
            {
                if(!ev_id.isEmpty())
                {
                    Wave wave = new Wave(SAMPLE_RATE, (short) 1, buffer, 0, buffer.length - 1);
                    String path = "media/"+ev_id+"/" + fileName + ".wav";
                    //WritersAndReaders.createDirectories(path);
                    wave.writeToFile(context, path);

                    if(Long.parseLong(ev_id)>0)
                    {
                        //Upload audio
                        /*ByteBuffer byteBuffer = ByteBuffer.allocate(buffer.length*2);
                        for(short s : buffer)
                            byteBuffer.putShort(s);*/
                        byte[] out = wave.getOutput();
                        if(out!=null)
                        {
                            if (out.length > 0)
                            {
                                String at_event = "audio_at_event";
                                String meta = "filename=public_res|events|" + fileName + ".wav;"
                                        + at_event + "=" + ev_id + ";"
                                        + "username=" + SharedPreference.getUsername(context);
                                RemoteComms.imageUploadWithMeta(out, meta);
                            }else Log.d(TAG,"Empty byte buffer.");
                        }else Log.d(TAG,"Empty byte buffer.");
                    }else
                    {
                        Toast.makeText(context,"You are not signed in to a valid Icebreak Event.",Toast.LENGTH_SHORT).show();
                        Log.d(TAG,"You are not signed in to a valid Icebreak Event. Recording & images saved to local storage.");
                    }
                }else
                {
                    Toast.makeText(context,"You are not signed in to a valid Icebreak Event.",Toast.LENGTH_SHORT).show();
                    Log.d(TAG,"You are not signed in to a valid Icebreak Event.");
                }
            }else
            {
                Toast.makeText(context,"You are not signed in to a valid Icebreak Event.",Toast.LENGTH_SHORT).show();
                Log.d(TAG,"You are not signed in to a valid Icebreak Event.");
            }
        } catch (IOException e)
        {
            LocalComms.logException(e);
        }
        //start pitch capture
        pitch(5000,0);

        if(recorder!=null)
        {
            recorder.stop();
            recorder.release();
            recorder = null;
            //Stop
            ////////////// Now Play/////////////
            /*MediaPlayer player = new MediaPlayer();
            try {
                player.setDataSource(fileName);
                player.prepare();

                player.start();
            } catch (Exception e) {
                Log.e("Player Exception", "prepare() failed");
            }*/

            /*float tempoFactor = 0.8f;
            float pitchFactor = 1.0f;
            AudioDispatcher adp =  AudioDispatcherFactory.fromPipe(fileName, 44100, 4096, 0);
            TarsosDSPAudioFormat format = adp.getFormat();
            rbs = new RubberBandAudioProcessor(44100, tempoFactor, pitchFactor);
            adp.addAudioProcessor(rbs);
            adp.addAudioProcessor(new AudioPlayer(JVMAudioInputStream.toAudioFormat(format)));
            new Thread(adp).start();*/

            //File externalStorage = Environment.getExternalStorageDirectory();
            //File mp3 = new File(fileName);
            //AudioDispatcher adp;
            //new AndroidFFMPEGLocator(context);
            //adp = AudioDispatcherFactory.fromPipe(mp3.getPath(),44100,5000,2500);
            //adp.addAudioProcessor(new AndroidAudioPlayer(adp.getFormat(),15000, AudioManager.STREAM_MUSIC));
            //adp.run();
        }
    }
}
