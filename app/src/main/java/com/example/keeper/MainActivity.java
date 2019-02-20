package com.example.keeper;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    public static final String TAG = "TEST";
    private Toolbar toolbar;
    private BottomNavigationView navigation;
    private FloatingActionButton fab;
    private Fragment homeFragment, statusFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        welcome();
        initView();
    }

    public void welcome() {
        SharedPreferences sp = getSharedPreferences("isFirstOpen", MODE_PRIVATE);
        boolean isFirstOpen = sp.getBoolean("isFirstOpen", true);
        if (isFirstOpen) {
            Bill welcome = new Bill();
            welcome.setPrice(0);
            welcome.setType(Bill.INCOME);
            welcome.setCategory("欢迎!");
            welcome.setRemark("点击加号创建一条新记录");
            welcome.save();
            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean("isFirstOpen", false);
            editor.commit();
        }
    }

    private void initView() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        fab = findViewById(R.id.fab);
        if (homeFragment == null) homeFragment = new HomeFragment();
        if (statusFragment == null) statusFragment = new StatusFragment();
        getFragmentManager()
                .beginTransaction()
                .add(R.id.home_fragment_holder, homeFragment)
                .add(R.id.home_fragment_holder, statusFragment)
                .hide(statusFragment)
                .commit();
        navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(this);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.navigation_home:
                if (homeFragment == null) {
                    homeFragment = new HomeFragment();
                }
                fab.show();
                getSupportActionBar().setTitle("Keeper");
                getFragmentManager()
                        .beginTransaction().hide(statusFragment).show(homeFragment).commit();
                return true;
            case R.id.navigation_status:
                if (statusFragment == null) {
                    statusFragment = new StatusFragment();
                }
                fab.hide();
                getSupportActionBar().setTitle("Status");
                getFragmentManager()
                        .beginTransaction().hide(homeFragment).show(statusFragment).commit();
                return true;
        }
        return false;
    }
}
