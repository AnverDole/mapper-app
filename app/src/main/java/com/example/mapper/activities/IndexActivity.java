package com.example.mapper.activities;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.TextView;

import com.example.mapper.R;
import com.example.mapper.activities.dialogs.LogoutDialog;
import com.example.mapper.apis.Mapper;
import com.example.mapper.databinding.ActivityIndexBinding;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;

public class IndexActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityIndexBinding binding;
    NavigationView navigationView;

    private TextView authNameTxtView;
    private TextView authEmailTxtView;
    private static HashMap<Integer, HashMap<Integer, OptionMenuItemClickEvent>> optionMenuItemClickEventObservers = new HashMap<>();

    private int optionMenuRef = R.menu.activity_index_menu;
    private static LeftMenuUpdatedEventListener leftMenuUpdatedEventListener;
    private static OptionMenu optionMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityIndexBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarIndex.toolbar);

        optionMenu = menuId -> {
            optionMenuRef = menuId;
            this.invalidateOptionsMenu();
        };

        DrawerLayout drawer = binding.drawerLayout;
        navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home,
                R.id.nav_subject,
                R.id.nav_schedule,
                R.id.nav_settings,
                R.id.nav_account_management)
                .setOpenableLayout(drawer)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_index);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        View headerView = navigationView.getHeaderView(0);
        authNameTxtView = headerView.findViewById(R.id.auth_user_name_text);
        authEmailTxtView = headerView.findViewById(R.id.auth_user_email_text);



        leftMenuUpdatedEventListener = () -> {
            authEmailTxtView.setText(Mapper.GetAuthenticatedUser().email);
            authNameTxtView.setText(Mapper.GetAuthenticatedUser().firstname + " " + Mapper.GetAuthenticatedUser().lastname);
        };

        leftMenuUpdatedEventListener.onUpdated();

        LogoutDialog logoutDialog = new LogoutDialog();

        navigationView.setNavigationItemSelectedListener(item -> {

            if(item.getItemId() == R.id.nav_logout){
                logoutDialog.show(this);
            }else{
                NavigationUI.onNavDestinationSelected(item, navController);
                new Handler().postDelayed(() -> drawer.close(), 100);
            }

            return true;
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(optionMenuRef, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(triggerOptionMenuItemClickEvent(navigationView.getCheckedItem().getItemId(), item)){
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_index);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public static void refreshLeftMenu(){
        if(leftMenuUpdatedEventListener != null){
            leftMenuUpdatedEventListener.onUpdated();
        }
    }

    public static void registerOptionMenuItemClickEventObserver(Integer navItemId, Integer menuItemId, OptionMenuItemClickEvent optionMenuItemClickEvent){
        if(optionMenuItemClickEventObservers.containsKey(navItemId)){
            optionMenuItemClickEventObservers.get(navItemId).put(menuItemId, optionMenuItemClickEvent);
        }else{
            HashMap<Integer, OptionMenuItemClickEvent> menuItems = new HashMap<>();
            menuItems.put(menuItemId, optionMenuItemClickEvent);
            optionMenuItemClickEventObservers.put(navItemId, menuItems);
        }

    }
    public static boolean triggerOptionMenuItemClickEvent(Integer pageId, MenuItem menuItem){
        if(!optionMenuItemClickEventObservers.containsKey(pageId)) return false;
        if(!optionMenuItemClickEventObservers.get(pageId).containsKey(menuItem.getItemId())) return false;

        optionMenuItemClickEventObservers.get(pageId).get(menuItem.getItemId()).onClick();
        return true;
    }
    public static void registerOptionMenu(Integer menuRef){
        if(optionMenu != null){
            optionMenu.setMenu(menuRef);
        }
    }

    private interface LeftMenuUpdatedEventListener{
        void onUpdated();
    }

    public static interface OptionMenuItemClickEvent{
        void onClick();
    }

    public static interface OptionMenu {
        void setMenu(int menuId);
    }
}