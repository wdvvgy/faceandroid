package org.kjk.skuniv.project;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class Administrator extends AppCompatActivity {

    private static final String TAG = "Administrator";
    private DrawerLayout mDrawerLayout;

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
    }
    private void setupViewPager(ViewPager viewPager) {
        Adapter adapter = new Adapter(getSupportFragmentManager());
        //adapter.addFragment(wl, "작업목록");
        viewPager.setAdapter(adapter);
    }

    static class Adapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public Adapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
    private void Init() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.client_toolbar);
        setSupportActionBar(toolbar);
        // Setting ViewPager for each Tabs
        ViewPager viewPager = (ViewPager) findViewById(R.id.client_viewpager);
        setupViewPager(viewPager);
        // Set Tabs inside Toolbar
        TabLayout tabs = (TabLayout) findViewById(R.id.client_tabs);
        tabs.setupWithViewPager(viewPager);

        // Create Navigation drawer and inlfate layout
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer);

        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            VectorDrawableCompat indicator
                    = VectorDrawableCompat.create(getResources(), R.drawable.ic_menu, getTheme());
            indicator.setTint(ResourcesCompat.getColor(getResources(), R.color.white, getTheme()));
            supportActionBar.setHomeAsUpIndicator(indicator);
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Set behavior of Navigation drawer
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    // This method will trigger on item Click of navigation menu
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        // Set item in checked state
                        menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                Log.d("TEST", "hi");
                                class Logout extends AsyncTask<Void, Void, Boolean> {

                                    private static final String TAG = "Logout";

                                    private InputStream in;
                                    private OutputStream out;

                                    @Override
                                    protected void onPreExecute() {
                                        Log.d(TAG, "onPreExecute");
                                        super.onPreExecute();
                                    }

                                    @Override
                                    protected void onPostExecute(Boolean value) {
                                        Log.d(TAG, "onPostExecute");
                                        super.onPostExecute(value);

//                                        if(value) {
//                                            Intent intent = new Intent(AClientMainActivity.this, MainActivity.class);
//                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                                            startActivity(intent);
//                                            mDrawerLayout.closeDrawers();
//                                        }
                                    }

                                    @Override
                                    protected Boolean doInBackground(Void... params) {
                                        Log.d(TAG, "doInBackground");

                                        try{
                                            in = SocketClient.getInstance().getInputStream();
                                            out = SocketClient.getInstance().getOutputStream();

                                            if(sendMessage(HSP.HSP_HSP_USEHSP, HSP.HSP_DEVICE_MOBILE, HSP.HSP_SERVICEID_USER, HSP.HSP_ID_SERVER, HSP.HSP_MESSAGE_LOGOUT))
                                                Log.d("MESSAGE SEND", "LOGOUT");

                                            if(getMessage() == HSP.HSP_MESSAGE_OK)
                                                Log.d("MESSAGE RECEIVE", "OK");
                                        }
                                        catch(Exception e) {
                                            e.printStackTrace();
                                            return false;
                                        }
                                        return true;
                                    }

                                    public boolean sendMessage(final int... args) {

                                        byte[] messages = new byte[SocketClient.getBytelen()];
                                        int messages_index = 0;

                                        for (final int t : args)
                                            messages[messages_index++] = (byte) t;

                                        if (out != null) {
                                            try {
                                                out.write(messages);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            return true;
                                        }

                                        return false;
                                    }

                                    public int getMessage() {
                                        if (in != null) {
                                            byte[] arr = new byte[SocketClient.getBytelen()];
                                            try {
                                                in.read(arr);
                                                return arr[4];
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                        return -1;
                                    }

                                }
                                new Logout().execute();
                                return true;
                            }
                        });
                        // TODO: handle navigation

                        // Closing drawer on item click
                        mDrawerLayout.closeDrawers();
                        return true;
                    }
                });
    }
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_administrator);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
        return super.onOptionsItemSelected(item);
    }
}

