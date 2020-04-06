package com.dennohpeter.renewdata;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.michaelflisar.changelog.ChangelogBuilder;
import com.michaelflisar.changelog.classes.DefaultAutoVersionNameFormatter;
import com.michaelflisar.changelog.classes.ImportanceChangelogSorter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AboutActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        Toolbar toolbar = findViewById(R.id.about_toolbar);
        setSupportActionBar(toolbar);
        // setting back button on toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        // setting app version_textView
        TextView version_textView = findViewById(R.id.app_version);
        String version_number = getString(R.string.app_version, appVersion(AboutActivity.this));
        version_textView.setText(version_number);
    }

    public void showCredit(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.credits);

        View popupLayout = getLayoutInflater().inflate(R.layout.contributor_list_view, null);
        HashMap<String, String> contributor_roles = new HashMap<>();
        contributor_roles.put(getString(R.string.creator), getString(R.string.creator_roles));
        contributor_roles.put(getString(R.string.dev1), getString(R.string.idealist));
        contributor_roles.put(getString(R.string.dev2), getString(R.string.contributor));

        List<HashMap<String, String>> listItem = new ArrayList<>();

        ListView listView = popupLayout.findViewById(R.id.list);

        SimpleAdapter adapter = new SimpleAdapter(this, listItem, R.layout.contributor_list_item,
                new String[]{"name", "role"},
                new int[]{R.id.contributor_name, R.id.developer_role});
        for (Map.Entry<String, String> pair : contributor_roles.entrySet()) {
            HashMap<String, String> resultMap = new HashMap<>();
            resultMap.put("name", ((Map.Entry) pair).getKey().toString());
            resultMap.put("role", ((Map.Entry) pair).getValue().toString());
            listItem.add(resultMap);
        }
        listView.setAdapter(adapter);

        builder.setView(popupLayout);
        builder.setNegativeButton(R.string.ok, (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    public void showChangelog(View view) {
        ChangelogBuilder builder = new ChangelogBuilder()
                .withUseBulletList(true)
                .withManagedShowOnStart(false)
                .withTitle(getString(R.string.app_name) +  " Changelog")
                .withSorter(new ImportanceChangelogSorter())
                .withVersionNameFormatter(new DefaultAutoVersionNameFormatter(DefaultAutoVersionNameFormatter.Type.MajorMinor, "b"));
        builder.buildAndShowDialog(this, false);
    }

    public void rateApp(View view) {
        Toast.makeText(this, "Rate this app", Toast.LENGTH_SHORT).show();
    }
    /*
     * Takes Activity Context and returns a String of the App Version e.g 1.0
     */
    static String appVersion(Context context) {
        String result = "";
        try {
            result = context.getPackageManager().getPackageInfo(context.getPackageName(), 0)
                    .versionName;
            result = result.replaceAll("[a-zA-Z]|-", "");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return result;

    }
}
