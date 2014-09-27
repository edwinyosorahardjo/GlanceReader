package pro.dbro.glance.activities;

import android.app.ActionBar;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

import com.astuetz.PagerSlidingTabStrip;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.UpdateManager;

import pro.dbro.glance.PrefsManager;
import pro.dbro.glance.R;
import pro.dbro.glance.SECRETS;
import pro.dbro.glance.adapters.ReaderSectionAdapter;

public class CommunityActivity extends FragmentActivity {

    /** Intent Code */
    private static final int SELECT_MEDIA = 42;


    /** Theme Codes */
    private static final int THEME_LIGHT = 0;
    private static final int THEME_DARK = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int theme = PrefsManager.getTheme(this);
        switch (theme) {
            case THEME_LIGHT:
                setTheme(R.style.Light);
                break;
            case THEME_DARK:
                setTheme(R.style.Dark);
                break;
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community);
        setupActionBar();

        // Initialize the ViewPager and set an adapter
        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(new ReaderSectionAdapter(getSupportFragmentManager()));

        // Bind the tabs to the ViewPager
        PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        tabs.setViewPager(pager);

        checkForUpdates();
    }

    @Override
    public void onResume() {
        super.onResume();
        checkForCrashes();
    }

    private void setupActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_open) {
            chooseMedia();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Fires an intent to spin up the "file chooser" UI and select an image.
     */
    public void chooseMedia() {

        // ACTION_OPEN_DOCUMENT is the new API 19 action for the Android file manager
        Intent intent;
        if (Build.VERSION.SDK_INT >= 19) {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        } else {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        }

        // Filter to only show results that can be "opened", such as a
        // file (as opposed to a list of contacts or timezones)
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Currently no recognized epub MIME type
        intent.setType("*/*");

        startActivityForResult(intent, SELECT_MEDIA);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.community, menu);
        return true;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SELECT_MEDIA && data != null) {
            Uri uri = data.getData();
            if (Build.VERSION.SDK_INT >= 19) {
                final int takeFlags = data.getFlags()
                        & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                getContentResolver().takePersistableUriPermission(uri, takeFlags);
            }
            Intent spritzIntent = new Intent(this, MainActivity.class);
            spritzIntent.setAction(Intent.ACTION_VIEW);
            spritzIntent.setData(uri);
            startActivity(spritzIntent);
        }
    }

    private void checkForCrashes() {
        CrashManager.register(this, SECRETS.getHockeyAppId());
    }

    private void checkForUpdates() {
        // Remove this for store builds!
        UpdateManager.register(this, SECRETS.getHockeyAppId());
    }
}

