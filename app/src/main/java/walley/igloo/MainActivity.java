package walley.igloo;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.DownloadListener;
import com.androidnetworking.interfaces.StringRequestListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ImageView imageView;
    WallpaperManager wallpaperManager;
   //String url="https://s3-us-west-2.amazonaws.com/flx-editorial-wordpress/wp-content/uploads/2020/03/09162047/EWKA_The_Batman-Ben_Affleck-Justice_League.jpg";
   // String url="https://fsa.zobj.net/crop.php?r=swm9yPQWKOOYd-ONRTPvkeTZw07rm4mmcaXiFH4HA8Jb2lMsH2H1C1fIh3DgitBDVtp7R1ERETAy8159JHt6qf0XnDZkQBy7_l2DfReqPkIomm5_XH-oIdDVitl49Pckkr8phGif0QfGCz08";
   String url="";
    Bitmap bitmap;
    File file;
    String dirPath, fileName;

    private static final int STORAGE_PERMISSION_CODE = 1;
    Bitmap myBitmap;
    Bitmap b = null;
    int WRITE_EXTERNAL_REQUEST_CODE=1;

    BitmapDrawable bitmapDrawable ;
    Bitmap bitmap1, bitmap2 ;
    DisplayMetrics displayMetrics ;
    int width, height;

    String status="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("set-lockscreen"));

      /*  Button crashButton = new Button(this);
        crashButton.setText("Crash!");
        crashButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                throw new RuntimeException("Test Crash"); // Force a crash
            }
        });

        addContentView(crashButton, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));*/


        checkPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                STORAGE_PERMISSION_CODE);


        imageView=findViewById(R.id.imageView);


        wallpaperManager  = WallpaperManager.getInstance(getApplicationContext());
      //  bitmapDrawable = ((BitmapDrawable) imageView.getDrawable());


        // Initialization Of DownLoad Button
        AndroidNetworking.initialize(getApplicationContext());

        //Folder Creating Into Phone Storage
        dirPath = Environment.getExternalStorageDirectory() + "/Image";

        fileName = "image.jpeg";

        //file Creating With Folder & Fle Name
        file = new File(dirPath, fileName);

        callApi();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Setting Lock Screen Wallpaper...", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();


                if (!isMyServiceRunning(MyService.class)) {

                    startService(new Intent(MainActivity.this, MyService.class));
                }

                if (status.equals("1")) {

                    setWallpaper();
                }
            }


        });


    }

    private void setWallpaper() {
        //Folder Creating Into Phone Storage
        dirPath = Environment.getExternalStorageDirectory() + "/Image";

        fileName = "image.jpeg";

        //file Creating With Folder & Fle Name
        file = new File(dirPath, fileName);

        callApi();

        Uri imageUri = Uri.fromFile(file);
        // imageView.setImageURI(imageUri);


        //Convering Uri to Bitmap
        try {
            InputStream is = getContentResolver().openInputStream(imageUri);
            myBitmap = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


        GetScreenWidthHeight();


        wallpaperManager = WallpaperManager.getInstance(MainActivity.this);

        //setting wallpaper
             /*   try {

                    wallpaperManager.setBitmap(myBitmap);

                    wallpaperManager.suggestDesiredDimensions(width, height);


                } catch (IOException e) {
                    e.printStackTrace();
                }
*/
        //lock screen set wallpaper
        try {
            // wallpaperManager.setBitmap(myBitmap, null, true, WallpaperManager.FLAG_LOCK);
            InputStream is = getContentResolver().openInputStream(imageUri);
            WallpaperManager.getInstance(MainActivity.this).setStream(is, null, true, WallpaperManager.FLAG_LOCK);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void GetScreenWidthHeight(){

        displayMetrics = new DisplayMetrics();

        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        width = displayMetrics.widthPixels;

        height = displayMetrics.heightPixels;

    }


    // Function to check and request permission
    public void checkPermission(String permission, int requestCode)
    {

        // Checking if permission is not granted
        if (ContextCompat.checkSelfPermission(
                MainActivity.this,
                permission)
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat
                    .requestPermissions(
                            MainActivity.this,
                            new String[] { permission },
                            requestCode);
        }
        else {
         /*   Toast
                    .makeText(MainActivity.this,
                            "Permission already granted",
                            Toast.LENGTH_SHORT)
                    .show();*/
        }
    }

    // This function is called when user accept or decline the permission.
    // Request Code is used to check which permission called this function.
    // This request code is provided when user is prompt for permission.

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode,
                permissions,
                grantResults);

        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this,
                        "Storage Permission Granted",
                        Toast.LENGTH_SHORT)
                        .show();
            } else {
                Toast.makeText(MainActivity.this,
                        "Storage Permission Denied",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }
        @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.stop_service) {
            stopService(new Intent(this, MyService.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void downloadImage(String url){
        AndroidNetworking.download(url, dirPath, fileName)
                .build()
                .startDownload(new DownloadListener() {
                    @Override
                    public void onDownloadComplete() {

                        //Toast.makeText(MainActivity.this, "DownLoad Complete", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(ANError anError) {

                    }
                });
    }


    public void callApi(){


        //"https://api.pexels.com/v1/photos/"+"1099"
        AndroidNetworking.get(" https://api.unsplash.com/photos/random?client_id=W5mI7Fq_FXx-EnAGNCcmNpIbmfPQKBNBqQ9L4jxivcA")
                .setTag("pexel")
                .setPriority(Priority.LOW)
                .build()
                .getAsString(new StringRequestListener() {

                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject jsonObject =new JSONObject(response);
                            String urls=jsonObject.optString("urls");
                            JSONObject jsonObject1=new JSONObject(urls);
                            String regular=jsonObject1.optString("regular");

                            Picasso.with(MainActivity.this).load(regular)
                                    .into(imageView);

                            //Downloading image to file
                            downloadImage(regular);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError error) {
                        // handle error
                    }
                });
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
             status = intent.getStringExtra("Status");
             if(isMyServiceRunning(MyService.class)) {
                 if (status.equals("1")) {
                     setWallpaper();
                 }
             }
            // Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    };

  /*  @Override
    protected void onDestroy() {
        // Unregister since the activity is about to be closed.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onDestroy();
    }*/

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
