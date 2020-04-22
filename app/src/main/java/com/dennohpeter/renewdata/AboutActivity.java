package com.dennohpeter.renewdata;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.michaelflisar.changelog.ChangelogBuilder;
import com.michaelflisar.changelog.ChangelogSetup;
import com.michaelflisar.changelog.classes.DefaultAutoVersionNameFormatter;

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
        String version_number = getString(R.string.app_version, Utils.getAppVersion(AboutActivity.this));
        version_textView.setText(version_number);

        //Enable open in browser popup onclick
        TextView copyright = findViewById(R.id.copyright);
        copyright.setMovementMethod(LinkMovementMethod.getInstance());

        // Feedback and suggestions
        RelativeLayout feedback = findViewById(R.id.feedback);
        feedback.setOnClickListener(v -> {
            String subject = "Feedback For " + getString(R.string.app_name) + " v" + Utils.getAppVersion(this);
            //Add extras and launch intent to send email
            Intent feedbackEmailIntent = new Intent(Intent.ACTION_SENDTO,
                    Uri.fromParts("mailto", getString(R.string.email), null))
                    .putExtra(Intent.EXTRA_SUBJECT, subject);
            startActivity(Intent.createChooser(feedbackEmailIntent, subject));
        });

    }

    public void showCredit(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.acknowledgements);

        View popupLayout = getLayoutInflater().inflate(R.layout.contributor_list_view, null);
        HashMap<String, String> contributor_roles = new HashMap<>();
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
        // registering custom tags
        String misc_prefix = "<font color=\"#6AC90C\"><b>Misc: </b></font>";
        ChangelogSetup.get().registerTag(new CustomTags("misc", misc_prefix));
        String todo_prefix = "<font color=\"#CC0F00\"><b>Todo: </b></font>";
        ChangelogSetup.get().registerTag(new CustomTags("todo", todo_prefix));
        // prepare changelog
        ChangelogBuilder builder = new ChangelogBuilder()
                .withUseBulletList(true)
                .withManagedShowOnStart(false)
                .withTitle(getString(R.string.changelog))
                .withOkButtonLabel(getString(R.string.close))
                .withVersionNameFormatter(new DefaultAutoVersionNameFormatter(DefaultAutoVersionNameFormatter.Type.MajorMinor, "b"));
        // build and show changelog
        builder.buildAndShowDialog(this, false);
    }

    public void rateApp(View view) {
        Toast.makeText(this, "Rate this app", Toast.LENGTH_SHORT).show();
    }
}
