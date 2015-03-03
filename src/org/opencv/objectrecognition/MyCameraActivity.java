
package org.opencv.objectrecognition;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.objectrecognition.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import org.opencv.objectrecognition.ImgSearchEngine;

public class MyCameraActivity<Stopwatch> extends Activity {
    private static final int CAMERA_REQUEST = 1888; 
    private static final int GALLERY_REQUEST = 1889;
    private ImageView imageView;

    private static Bitmap cropBmp;
    private Uri mImageCaptureUri; // This needs to be initialized.
    static final int CAMERA_PIC_REQUEST = 1337; 
    
    private static final String    TAG = "OCVSample::Activity";
	private static Context mContext;

    private File mEngineFile;
	String filePath;
    File mediaFile;
    
    private static ImgSearchEngine engine= new ImgSearchEngine(); 
    
    public static Context getContext(){
        return mContext;
    } 
    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(getContext()) {
 	   @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");

                    // Load native library after(!) OpenCV initialization
                    System.loadLibrary("objengine");
                    Log.i(TAG, "loaded objengine library");
                    
                    try {
                        // load cascade file from application resources
                        InputStream is = getResources().openRawResource(R.raw.pack_db);
                        File dataDir = getDir("objectrecognition", Context.MODE_PRIVATE);
                        mEngineFile = new File(dataDir, "pack_db.data");
                        FileOutputStream os = new FileOutputStream(mEngineFile);

                        byte[] buffer = new byte[3200000];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                        is.close();
                        os.close();
                        
                        String filepath=mEngineFile.getAbsolutePath();
                        boolean fload=engine.loadDB(filepath);

                        dataDir.delete();
                        Log.i(TAG, "succeed to create engine : count = "+fload);
                    } catch (IOException e) {
                        e.printStackTrace();
                    	Log.e(TAG, "failed to create engine"+e);
                    }
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.imgfilepath);
        setContentView(R.layout.activity_my_camera);
        this.imageView = (ImageView)this.findViewById(R.id.imageView1);
        mediaFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Temp1.jpg");
        filePath = mediaFile.getAbsolutePath();
        Button cameraButton = (Button) this.findViewById(R.id.buttonCamera1);
        
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                cameraIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, Uri.fromFile(mediaFile));
                startActivityForResult(cameraIntent, CAMERA_PIC_REQUEST);        
            }
        });
        
        Button galleryButton = (Button) this.findViewById(R.id.buttonGallery1);
        
        galleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
 				Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Complete action using"), GALLERY_REQUEST);
            }
        });
        
        // opencv loading
    	mContext = this;
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_8, this, mLoaderCallback);

        Button cropButton = (Button) this.findViewById(R.id.buttonCrop1);
        cropButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	cropImage();
            }
        });
    }
    void cropImage()
	{
    	if (filePath == "")
    		return;
    	long startTime = System.currentTimeMillis();
    	int[] objectidxs=new int[1];
    	double[] dists=new double[1];
    	boolean frecog=engine.recognize(filePath, objectidxs, dists);
    	int objectidx=objectidxs[0];
    	double dist=dists[0];
    	Log.i(TAG, ""+frecog);
    	
    	boolean fprocess=false;
		long stopTime = System.currentTimeMillis();
	    long elapsedTime = stopTime - startTime;
	    System.out.println(elapsedTime);
    	if (frecog)
    	{
    		AlertDialog alertDialog = new AlertDialog.Builder(MyCameraActivity.this).create();
    		alertDialog.setTitle("Alert Dialog");
    		alertDialog.setMessage("Succeed to recognize object" + objectidx);
    		alertDialog.setIcon(R.drawable.tick);
    		alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
    			public void onClick(DialogInterface dialog, int which){}
    		});
    		alertDialog.show();
    	}
    	else
    	{
    		AlertDialog alertDialog = new AlertDialog.Builder(MyCameraActivity.this).create();
    		alertDialog.setTitle("Alert Dialog");
    		alertDialog.setMessage("Failed to recognize object" + objectidx);
    		alertDialog.setIcon(R.drawable.tick);
    		alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
    			public void onClick(DialogInterface dialog, int which){}
    		});
    		alertDialog.show();
    	}
	}

    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        CursorLoader loader = new CursorLoader(mContext, contentUri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {  
    	if (requestCode == CAMERA_PIC_REQUEST && resultCode == RESULT_OK) {
    		if(!filePath.isEmpty()){
    			Uri  uriFromPath = Uri.fromFile(new File(filePath));
    			Bitmap bmp = this.getBitmap(uriFromPath);		    
    			Bitmap alteredBitmap = bmp;
    			imageView.setImageBitmap(alteredBitmap);
    		}
    	}
      
    	
    	if(requestCode == GALLERY_REQUEST && resultCode == RESULT_OK)
    	{
			Uri contentURI = data.getData();
		    String tempPath;
		    Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
		    if (cursor == null) { // Source is Dropbox or other similar local file path
		    	tempPath = contentURI.getPath();
		    } else { 
		        cursor.moveToFirst(); 
		        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA); 
		        tempPath = cursor.getString(idx);
		        cursor.close();
		    }
		    
			filePath = tempPath;
			Bitmap bmp = this.getBitmap(contentURI);		    
			Bitmap alteredBitmap = bmp;
			imageView.setImageBitmap(alteredBitmap);
        }
    }
 
    private Bitmap getBitmap(Uri uri) {
    	InputStream in = null;
    	try {
    	    final int IMAGE_MAX_SIZE = 1000000; // 1.2MP
    	    in = this.getContentResolver().openInputStream(uri);

    	    // Decode image size
    	    BitmapFactory.Options o = new BitmapFactory.Options();
    	    o.inJustDecodeBounds = true;
    	    BitmapFactory.decodeStream(in, null, o);
    	    in.close();



    	    int scale = 1;
    	    while ((o.outWidth * o.outHeight) * (1 / Math.pow(scale, 2)) > 
    	          IMAGE_MAX_SIZE) {
    	       scale++;
    	    }

    	    Bitmap b = null;
    	    in = this.getContentResolver().openInputStream(uri);
    	    if (scale > 1) {
    	        scale--;
    	        // scale to max possible inSampleSize that still yields an image
    	        // larger than target
    	        o = new BitmapFactory.Options();
    	        o.inSampleSize = scale;
    	        b = BitmapFactory.decodeStream(in, null, o);

    	        // resize to desired dimensions
    	        int height = b.getHeight();
    	        int width = b.getWidth();

    	        double y = Math.sqrt(IMAGE_MAX_SIZE
    	                / (((double) width) / height));
    	        double x = (y / height) * width;

    	        Bitmap scaledBitmap = Bitmap.createScaledBitmap(b, (int) x, 
    	           (int) y, true);
    	        b.recycle();
    	        b = scaledBitmap;

    	        System.gc();
    	    } else {
    	        b = BitmapFactory.decodeStream(in);
    	    }
    	    Bitmap b1 = b.copy(Bitmap.Config.ARGB_8888, true);
    	    b.recycle();
    	    System.gc();
    	    in.close();
    	    return b1;
    	} catch (IOException e) {
    	    return null;
    	}    
    }
    
    protected boolean saveBitmap(Bitmap bitmap)
    {
	    File file = new File("sdcard/objimage.jpg");

	    FileOutputStream fOutputStream = null;

        try {
            fOutputStream = new FileOutputStream(file);

            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOutputStream);

            fOutputStream.flush();
            fOutputStream.close();

            MediaStore.Images.Media.insertImage(getContentResolver(), file.getAbsolutePath(), file.getName(), file.getName());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this, "Save Failed", Toast.LENGTH_SHORT).show();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Save Failed", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
   	
    }
    
}