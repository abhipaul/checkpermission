package payment.card.io.checkpermission;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.konylabs.android.KonyMain;

public class MainActivity extends Activity {
    private static ICaptureCall capturecallback;
    private static final int PERMISSION_CALLBACK_CONSTANT = 100;
    private static final int REQUEST_PERMISSION_SETTING = 101;
    String[] permissionsRequired = new String[]{Manifest.permission.CAMERA,
            Manifest.permission.READ_PHONE_STATE,Manifest.permission.ACCESS_FINE_LOCATION};

    private android.support.v7.app.AlertDialog.Builder builder;

    private SharedPreferences permissionStatus;
    private boolean sentToSettings = false;

    private static int ALL_APPROVED=1;
    private static int KILL_APP=0;

    @Override
    protected void onResume() {
        super.onResume();
        if(sentToSettings) {
            try {
//                checkAllPermission();
            }catch(Exception e)
            {

            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(sentToSettings) {
            capturecallback.getResponse(KILL_APP);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        permissionStatus = getSharedPreferences("permissionStatus",MODE_PRIVATE);
        try {
            checkAllPermission();
        }catch(Exception e)
        {

        }
    }
    public static String getApplicationName(Context context) {
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        return stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : context.getString(stringId);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

    public void checkAllPermission() throws Exception
    {
            if(ActivityCompat.checkSelfPermission(MainActivity.this, permissionsRequired[0]) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(MainActivity.this, permissionsRequired[1]) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(MainActivity.this, permissionsRequired[2]) != PackageManager.PERMISSION_GRANTED
                    ){
                if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,permissionsRequired[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,permissionsRequired[1])
                        || ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,permissionsRequired[2])
                        ){
                    //Show Information about why you need the permission
                    builder = new android.support.v7.app.AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Need Multiple Permissions");
                    builder.setCancelable(false);
                    builder.setMessage(getApplicationName(this)+" needs Camera and Phone state permissions.");
                    builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            ActivityCompat.requestPermissions(MainActivity.this,permissionsRequired,PERMISSION_CALLBACK_CONSTANT);
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            if(capturecallback!=null)
                            capturecallback.getResponse(KILL_APP);
                            finish();
                        }
                    });
                    builder.show();
                } else if (permissionStatus.getBoolean(permissionsRequired[0],false)) {
                    //Previously Permission Request was cancelled with 'Dont Ask Again',
                    // Redirect to Settings after showing Information about why you need the permission
                    builder = new android.support.v7.app.AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Need Multiple Permissions");
                    builder.setCancelable(false);
                    builder.setMessage(getApplicationName(this)+" needs Camera and Phone state permissions.");
                    builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            sentToSettings = true;
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", getPackageNameCustom(), null);
                            intent.setData(uri);
                            startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
                            Toast.makeText(getBaseContext(), "Go to Permissions to Grant Camera and Phone State", Toast.LENGTH_LONG).show();
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            if(capturecallback!=null)
                            capturecallback.getResponse(KILL_APP);
                            finish();
                        }
                    });
                    builder.show();
                }  else {
                    //just request the permission
                    ActivityCompat.requestPermissions(MainActivity.this,permissionsRequired,PERMISSION_CALLBACK_CONSTANT);
                }

//                Toast.makeText(this,"Permissions Required",Toast.LENGTH_LONG).show();

                SharedPreferences.Editor editor = permissionStatus.edit();
                editor.putBoolean(permissionsRequired[0],true);
                editor.commit();
            } else {
                //You already have the permission, just go ahead.
                proceedAfterPermission();
            }

    }

    private String getPackageNameCustom() {
        if(KonyMain.getActContext()!=null)
        return KonyMain.getActContext().getPackageName();
        else return "";
//       return getPackageName();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == PERMISSION_CALLBACK_CONSTANT){
            //check if all permissions are granted
            boolean allgranted = false;
            for(int i=0;i<grantResults.length;i++){
                if(grantResults[i]==PackageManager.PERMISSION_GRANTED){
                    allgranted = true;
                } else {
                    allgranted = false;
                    break;
                }
            }

            if(allgranted){
                proceedAfterPermission();
            } else if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,permissionsRequired[0])
                    || ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,permissionsRequired[1])
                    || ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,permissionsRequired[2])
                    ){
//                Toast.makeText(getBaseContext(),"Permissions Required",Toast.LENGTH_LONG).show();
                builder = new android.support.v7.app.AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Need Multiple Permissions");
                builder.setCancelable(false);
                builder.setMessage(getApplicationName(this)+" needs Camera and Phone state permissions.");
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(MainActivity.this,permissionsRequired,PERMISSION_CALLBACK_CONSTANT);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        if(capturecallback!=null)
                        capturecallback.getResponse(KILL_APP);
                        finish();
                    }
                });
                builder.show();
            } else {
//                Toast.makeText(getBaseContext(),"Unable to get Permission",Toast.LENGTH_LONG).show();
                sentToSettings = true;
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageNameCustom(), null);
                intent.setData(uri);
                startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
                Toast.makeText(getBaseContext(), "Go to Permissions to Grant  Camera and Location", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PERMISSION_SETTING) {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, permissionsRequired[0]) == PackageManager.PERMISSION_GRANTED) {
                //Got Permission
                proceedAfterPermission();
            }
        }
    }

    private void proceedAfterPermission() {
        if(capturecallback!=null)
        capturecallback.getResponse(ALL_APPROVED);
        finish();
//        Toast.makeText(getBaseContext(), "We got All Permissions", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (sentToSettings) {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, permissionsRequired[0]) == PackageManager.PERMISSION_GRANTED) {
                //Got Permission
                proceedAfterPermission();
            }
        }
    }

    public static void registerResponseCallback(ICaptureCall capturecallbackk) {
        capturecallback=capturecallbackk;
    }
}
