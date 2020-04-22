package com.dennohpeter.renewdata;

import android.Manifest;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements UpdateHelper.OnUpdateCheckListener, EasyPermissions.PermissionCallbacks {
    private static final String TAG = "MainActivity";
    private static final int WRITE_REQUEST_CODE = 300;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TabLayout tabLayout = findViewById(R.id.tabs);

        tabLayout.addTab(tabLayout.newTab().setText("HOME"));
        tabLayout.addTab(tabLayout.newTab().setText("OPTIONS"));
        tabLayout.addTab(tabLayout.newTab().setText("LOGS"));

        final ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(new PageAdapter(getSupportFragmentManager(), tabLayout.getTabCount()));
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        // check update
        check_update();
    }

    public void check_update() {
        UpdateHelper.with(MainActivity.this)
                .onUpdateCheck(this)
                .check();
    }

    private void downloadApk(String url) {
        //Check if SD card is present or not
        if (UpdateHelper.CheckForSDCard.isSDCardPresent()) {

            //check if app has permission to write to the external storage.
            if (EasyPermissions.hasPermissions(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                new DownloadFileHelper(MainActivity.this).execute(url);

            } else {
                //If permission is not present request for it.
                EasyPermissions.requestPermissions(MainActivity.this, getString(R.string.write_file), WRITE_REQUEST_CODE, Manifest.permission.READ_EXTERNAL_STORAGE);
            }

        }
    }


    @Override
    public void onUpdateCheckListener(String url) {
        // Create an update alert dialog
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.update_title))
                .setMessage(getString(R.string.update_message))
                .setPositiveButton(getString(R.string.update_option), (dialog, which) -> {
                    // setting update url to retrieve later on permission grant listener
                    UpdateHelper.setKeyUpdateUrl(url);
                    downloadApk(url);
                }).setNegativeButton(getString(R.string.dismiss_text), (dialog, which) -> dialog.dismiss()).create();
        alertDialog.show();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, MainActivity.this);

    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        Log.d(TAG, "onPermissionsGranted: " + requestCode + perms);
        //Download the file once permission to write to external storage is granted
        if (requestCode == WRITE_REQUEST_CODE) {
            String url = UpdateHelper.getKeyUpdateUrl();
            downloadApk(url);
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        Log.d(TAG, "Permission has been denied");
    }
}
