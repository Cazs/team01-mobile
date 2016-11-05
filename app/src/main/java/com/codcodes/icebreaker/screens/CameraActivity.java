package com.codcodes.icebreaker.screens;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.codcodes.icebreaker.R;
import com.codcodes.icebreaker.auxilary.AudioMgr;
import com.codcodes.icebreaker.auxilary.Config;
import com.codcodes.icebreaker.auxilary.ImageUtils;
import com.codcodes.icebreaker.auxilary.LocalComms;
import com.codcodes.icebreaker.auxilary.RemoteComms;
import com.codcodes.icebreaker.auxilary.SharedPreference;
import com.codcodes.icebreaker.auxilary.WritersAndReaders;
import com.codcodes.icebreaker.auxilary.camera.CameraSource;
import com.codcodes.icebreaker.auxilary.camera.CameraSourcePreview;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.io.IOException;
import java.nio.ByteBuffer;

public class CameraActivity extends AppCompatActivity
{
    private final String TAG = "IB/CamActivity";
    private AudioMgr media_mgr;

    // intent request code to handle updating play services if needed.
    private static final int RC_HANDLE_GMS = 9001;

    // permission request codes need to be < 256
    private static final int RC_HANDLE_CAMERA_PERM = 2;

    // constants used to pass extra data in the intent
    public static final String AutoFocus = "AutoFocus";
    public static final String UseFlash = "UseFlash";

    private CameraSource mCameraSource;
    private CameraSourcePreview mPreview;

    private int start_ms=0,end_ms=0;
    private boolean shutter_down=false;
    private String fileName="";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_camera);

        final Button btnShutter = (Button)findViewById(R.id.btnShutter);

        media_mgr = new AudioMgr(this);

        Thread tShutter = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                Looper.prepare();
                boolean rec=false;
                while (true)
                {
                    if(btnShutter!=null)
                    {
                        if(btnShutter.isPressed())
                        {
                            fileName = String.valueOf(System.currentTimeMillis());
                            if(!rec)
                            {
                                try
                                {
                                    media_mgr.listen(1, fileName);//in seconds
                                }catch (IOException e)
                                {
                                    LocalComms.logException(e);
                                }
                                rec=true;
                            }
                            onShutter(fileName);
                            try
                            {
                                Thread.sleep(3000);
                            } catch (InterruptedException e)
                            {
                                LocalComms.logException(e);
                            }
                        }else
                        {
                            if(rec)
                            {
                                media_mgr.stop();
                                rec = false;
                            }
                        }
                    }
                }
            }
        });
        tShutter.start();

        /*btnShutter.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent)
            {
                if(motionEvent.getAction()==MotionEvent.ACTION_BUTTON_PRESS)
                {
                    shutter_down = true;
                }else if(motionEvent.getAction()==MotionEvent.ACTION_BUTTON_RELEASE)
                {
                    shutter_down = false;
                }
                return true;
            }
        });*/

        mPreview = (CameraSourcePreview) findViewById(R.id.preview);
        //mGraphicOverlay = (GraphicOverlay<BarcodeGraphic>) findViewById(R.id.graphicOverlay);

        //mPreview.setMinimumHeight(900);

        // read parameters from the intent used to launch the activity.
        boolean autoFocus = getIntent().getBooleanExtra(AutoFocus, false);
        boolean useFlash = getIntent().getBooleanExtra(UseFlash, false);

        // Check for the camera permission before accessing the camera.  If the
        // permission is not granted yet, request permission.
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED)
            createCameraSource(autoFocus, useFlash);
        else requestCameraPermission();

        //gestureDetector = new GestureDetector(this, new CaptureGestureListener());
        //scaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());

        Snackbar.make(mPreview, "Tap/Hold to capture image/s and audio. Pinch/Stretch to zoom in/out.",
                Snackbar.LENGTH_SHORT)
                .show();
    }

    /**
     * Handles the requesting of the camera permission.  This includes
     * showing a "Snackbar" message of why the permission is needed then
     * sending the request.
     */
    private void requestCameraPermission()
    {
        Log.w(TAG, "Camera permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }

        final Activity thisActivity = this;

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions,
                        RC_HANDLE_CAMERA_PERM);
            }
        };

        Snackbar.make(mPreview, R.string.permission_camera_rationale,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, listener)
                .show();
    }

    /**
     * Creates and starts the camera.  Note that this uses a higher resolution in comparison
     * to other detection examples to enable the barcode detector to detect small barcodes
     * at long distances.
     *
     * Suppressing InlinedApi since there is a check that the minimum version is met before using
     * the constant.
     */
    @SuppressLint("InlinedApi")
    private void createCameraSource(boolean autoFocus, boolean useFlash)
    {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        CameraSource.Builder builder = new CameraSource.Builder(getApplicationContext())
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(metrics.widthPixels, metrics.heightPixels)
                .setRequestedFps(30.0f);


        //TODO: Update deprecated code
        // make sure that auto focus is an available option
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
        {
            builder = builder.setFocusMode(autoFocus ? Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE : null);
        }
        //builder.setFlashMode(useFlash ? Camera.Parameters.FLASH_MODE_TORCH : null);

        mCameraSource = builder.build();
    }

    private Handler toastHandler(final String text)
    {
        Handler toastHandler = new Handler(Looper.getMainLooper())
        {
            public void handleMessage(Message msg)
            {
                super.handleMessage(msg);
                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
            }
        };
        return toastHandler;
    }

    private class PictureCallbackHandler implements CameraSource.PictureCallback
    {
        private String fileName;

        public PictureCallbackHandler(String fileName){this.fileName=fileName;}

        @Override
        public void onPictureTaken(final byte[] image_data)
        {
            /*Bitmap b = BitmapFactory.decodeByteArray(image_data,0,image_data.length);
            //final Bitmap bitmap = ImageUtils.getInstance().compressBitmapImage(new BitmapDrawable(CameraActivity.this.getResources(),b));
            final byte[] data;
            //if(bitmap!=null)
            if(b!=null)
            {
                ByteBuffer buffer = ByteBuffer.allocate(image_data.length);
                //bitmap.copyPixelsToBuffer(buffer);
                data = buffer.array();
            }else data = null;
            b.recycle();*/
            //bitmap.recycle();

            Thread tImgSave = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    Looper.prepare();
                    if(image_data!=null)
                    {
                        try
                        {
                            String ev_id = WritersAndReaders.readAttributeFromConfig(Config.EVENT_ID.getValue());
                            if (ev_id != null)
                            {
                                if (!ev_id.isEmpty())
                                {
                                    if (Long.parseLong(ev_id) > 0)
                                    {
                                        WritersAndReaders.saveFile(CameraActivity.this, image_data, "/media/" + ev_id + "/" + fileName + ".png");
                                        toastHandler("Image was saved image to local storage [~/Icebreak/media/" + ev_id + "/" + fileName + ".png]").obtainMessage().sendToTarget();
                                        //Upload image
                                        String at_event = "snap_at_event";
                                        String meta = "filename=public_res|events|" + fileName + ".png;"
                                                + at_event + "=" + ev_id + ";"
                                                + "username=" + SharedPreference.getUsername(getApplicationContext());
                                        RemoteComms.imageUploadWithMeta(image_data, meta);
                                    } else
                                    {
                                        WritersAndReaders.saveFile(CameraActivity.this, image_data, "/media/" + ev_id + "/" + fileName + ".png");
                                        toastHandler("Your image won't be uploaded anywhere because you're not signed into any event. Image was saved image to local storage [~/Icebreak/media/" + ev_id + "/" + fileName + ".png]").obtainMessage().sendToTarget();
                                    }
                                } else
                                {
                                    toastHandler("You are not signed in to a valid Icebreak Event.").obtainMessage().sendToTarget();
                                    Log.d(TAG, "You are not signed in to a valid Icebreak Event.");
                                }
                            } else
                            {
                                toastHandler("You are not signed in to a valid Icebreak Event.").obtainMessage().sendToTarget();
                                Log.d(TAG, "You are not signed in to a valid Icebreak Event.");
                            }
                        } catch (IOException e)
                        {
                            LocalComms.logException(e);
                            toastHandler("I/O Error.").obtainMessage().sendToTarget();
                        }
                    }else toastHandler("Got empty image from camera source. Try again.").obtainMessage().sendToTarget();
                }
            });
            tImgSave.start();

            Thread tImgUploader = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        RemoteComms.imageUpload(image_data,"public_res;events;"+fileName,".png");
                    } catch (IOException e)
                    {
                        LocalComms.logException(e);
                    }
                }
            });
            //tImgUploader.start();
        }
    }

    private void onShutter(final String fileName)
    {
        if(mCameraSource!=null)
        {
            CameraSource.ShutterCallback shutterCallbackHandler = new CameraSource.ShutterCallback()
            {
                @Override
                public void onShutter()
                {
                    //AudioManager mgr = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                    //mgr.playSoundEffect(AudioManager.FLAG_PLAY_SOUND);
                }
            };
            PictureCallbackHandler pictureCallbackHandler = new PictureCallbackHandler(fileName);
            mCameraSource.takePicture(shutterCallbackHandler, pictureCallbackHandler);

        }
    }

    private void startCameraSource() throws SecurityException
    {
        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getApplicationContext());
        if (code != ConnectionResult.SUCCESS)
        {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
            dlg.show();
        }

        if (mCameraSource != null)
        {
            try
            {
                mPreview.start(mCameraSource);
            } catch (IOException e)
            {
                Log.e(TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }

    /**
     * Restarts the camera.
     */
    @Override
    protected void onResume()
    {
        super.onResume();
        startCameraSource();
    }

    /**
     * Stops the camera.
     */
    @Override
    protected void onPause()
    {
        super.onPause();
        if (mPreview != null)
        {
            mPreview.stop();
        }
    }

    /**
     * Releases the resources associated with the camera source, the associated detectors, and the
     * rest of the processing pipeline.
     */
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if (mPreview != null)
        {
            mPreview.release();
        }
    }

    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on {@link #requestPermissions(String[], int)}.
     * <p>
     * <strong>Note:</strong> It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     * </p>
     *
     * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link PackageManager#PERMISSION_GRANTED}
     *                     or {@link PackageManager#PERMISSION_DENIED}. Never null.
     * @see #requestPermissions(String[], int)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        if (requestCode != RC_HANDLE_CAMERA_PERM)
        {
            Log.d(TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
            Log.d(TAG, "Camera permission granted - initialize the camera source");
            // we have permission, so create the camerasource
            boolean autoFocus = getIntent().getBooleanExtra(AutoFocus,false);
            boolean useFlash = getIntent().getBooleanExtra(UseFlash, false);
            createCameraSource(autoFocus, useFlash);
            return;
        }

        Log.e(TAG, "Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Icebreak")
                .setMessage(R.string.no_camera_permission)
                .setPositiveButton(R.string.ok, listener)
                .show();
    }
}