package com.codcodes.icebreaker.auxilary;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Casper on 2016/10/31.
 */
public class Wave
{
    private final int LONGINT = 4;
    private final int SMALLINT = 2;
    private final int INTEGER = 4;
    private final int ID_STRING_SIZE = 4;
    private final int WAV_RIFF_SIZE = LONGINT+ID_STRING_SIZE;
    private final int WAV_FMT_SIZE = (4*SMALLINT)+(INTEGER*2)+LONGINT+ID_STRING_SIZE;
    private final int WAV_DATA_SIZE = ID_STRING_SIZE+LONGINT;
    private final int WAV_HDR_SIZE = WAV_RIFF_SIZE+ID_STRING_SIZE+WAV_FMT_SIZE+WAV_DATA_SIZE;
    private final short PCM = 1;
    private final int SAMPLE_SIZE = 2;
    int cursor, nSamples;
    private static final String TAG = "IB/Wave";
    byte[] output;

    public Wave(int sampleRate, short nChannels, short[] data, int start, int end)
    {
        nSamples=end-start+1;
        cursor=0;
        output=new byte[nSamples*SMALLINT+WAV_HDR_SIZE];
        buildHeader(sampleRate,nChannels);
        writeData(data,start,end);
    }
    // ------------------------------------------------------------
    private void buildHeader(int sampleRate, short nChannels)
    {
        write("RIFF");
        write(output.length);
        write("WAVE");
        writeFormat(sampleRate, nChannels);
    }
    // ------------------------------------------------------------
    public void writeFormat(int sampleRate, short nChannels)
    {
        write("fmt ");
        write(WAV_FMT_SIZE-WAV_DATA_SIZE);
        write(PCM);
        write(nChannels);
        write(sampleRate);
        write(nChannels * sampleRate * SAMPLE_SIZE);
        write((short)(nChannels * SAMPLE_SIZE));
        write((short)16);
    }
    // ------------------------------------------------------------
    public void writeData(short[] data, int start, int end)
    {
        write("data");
        write(nSamples*SMALLINT);
        for(int i=start; i<=end; write(data[i++]));
    }
    // ------------------------------------------------------------
    private void write(byte b)
    {
        output[cursor++]=b;
    }
    // ------------------------------------------------------------
    private void write(String id)
    {
        if(id.length()!=ID_STRING_SIZE) Log.d(TAG,"String " + id + " must have four characters.");
        else {
            for(int i=0; i<ID_STRING_SIZE; ++i) write((byte)id.charAt(i));
        }
    }
    // ------------------------------------------------------------
    private void write(int i)
    {
        write((byte) (i&0xFF)); i>>=8;
        write((byte) (i&0xFF)); i>>=8;
        write((byte) (i&0xFF)); i>>=8;
        write((byte) (i&0xFF));
    }
    // ------------------------------------------------------------
    private void write(short i)
    {
        write((byte) (i&0xFF)); i>>=8;
        write((byte) (i&0xFF));
    }

    public byte[] getOutput(){return output;}
    // ------------------------------------------------------------
    public boolean writeToFile(Context context, String filename)
    {
        boolean ok=false;

        //try
        {
            /*File path=new File(filename);
            FileOutputStream outFile = new FileOutputStream(path);
            outFile.write(output);
            outFile.close();*/
            WritersAndReaders.saveFile(context,output, filename);
            ok=true;
        }/* catch (FileNotFoundException e)
        {
            ok=false;
            LocalComms.logException(e);
        } catch (IOException e)
        {
            ok=false;
            LocalComms.logException(e);
        }*/
        return ok;
    }
}
