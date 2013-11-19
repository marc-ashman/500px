package com.ashman.fivehundredpx;


import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.ashman.fivehundredpx.enums.GalleryCategory;

public class GalleryActivity extends BaseFragmentActivity {
    private GalleryFragment galleryFragment;
    private ActionBarDrawerToggle drawerToggle;
    private ActionBar actionBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        final DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.gallery_drawer_layout);
        ListView drawerList = (ListView) findViewById(R.id.gallery_drawer_listview);

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.drawable.ic_drawer_light,
                R.string.drawer_open, R.string.drawer_close);

        drawerLayout.setDrawerListener(new GalleryDrawerListener());
        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        drawerList.setAdapter(new DrawerListAdapter(this));
        drawerList.setScrollingCacheEnabled(false);
        drawerList.setScrollContainer(false);
        drawerList.setFastScrollEnabled(true);
        drawerList.setSmoothScrollbarEnabled(true);

        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle(FiveHundredApplication.DEFAULT_CATEGORY.getName());

        galleryFragment = (GalleryFragment) getSupportFragmentManager().
                findFragmentById(R.id.gallery_fragment);

        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView,
                                    View view, int position, long id) {
                GalleryCategory category = (GalleryCategory)
                        adapterView.getAdapter().getItem(position);
                drawerLayout.closeDrawers();
                galleryFragment.setCurrentCategory(category);
                actionBar.setTitle(category.getName());
            }
        });

        if(!isNetworkAvailable())
        {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            Fragment oldFragment = getSupportFragmentManager().findFragmentByTag(
                    "needData");
            if(oldFragment != null)
                transaction.remove(oldFragment);
            SimpleDialogFragment dialog = SimpleDialogFragment.newInstance(
                    getString(R.string.error_needData));
            dialog.show(transaction, "needData");
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
     public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_gallery, menu);
        if(FiveHundredApplication.get().isLoggedIn())
            menu.removeItem(R.id.menu_login);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        Intent intent;
        switch (item.getItemId()) {
            case android.R.id.home:
                return true;
            case R.id.menu_login:
                intent = new Intent(this, LoginActivity.class);
                startActivityForResult(intent, 0);
                return true;
            case R.id.menu_settings:
                intent = new Intent(this, SettingsActivity.class);
                startActivityForResult(intent, 0);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        supportInvalidateOptionsMenu();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }


    private class GalleryDrawerListener implements DrawerLayout.DrawerListener {
        private CharSequence originalTitle;

        @Override
        public void onDrawerOpened(View drawerView) {
            drawerToggle.onDrawerOpened(drawerView);
            originalTitle = actionBar.getTitle();
            actionBar.setTitle(R.string.drawer_open_title);
        }

        @Override
        public void onDrawerClosed(View drawerView) {
            drawerToggle.onDrawerClosed(drawerView);
            if(actionBar.getTitle().equals(getString(R.string.drawer_open_title)))
                actionBar.setTitle(originalTitle);
        }

        @Override
        public void onDrawerSlide(View drawerView, float slideOffset) {
            drawerToggle.onDrawerSlide(drawerView, slideOffset);
        }

        @Override
        public void onDrawerStateChanged(int newState) {
            drawerToggle.onDrawerStateChanged(newState);
        }
    }
}
