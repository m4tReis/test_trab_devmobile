package com.santosschein.app;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.santosschein.app.core.config.AppContainer;
import com.santosschein.app.domain.model.InitialDestination;
import com.santosschein.app.presentation.navigation.AppDestination;
import com.santosschein.app.presentation.navigation.FragmentAppNavigator;

public class MainActivity extends AppCompatActivity {

    private static final String KEY_CURRENT_DESTINATION = "current_destination";

    private AppContainer appContainer;
    private FragmentAppNavigator navigator;
    private BottomNavigationView bottomNavigationView;
    private AppDestination currentDestination;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        appContainer = ((SantosScheinApplication) getApplication()).getAppContainer();
        navigator = new FragmentAppNavigator(getSupportFragmentManager(), R.id.fragment_container);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                navigateTo(AppDestination.HOME);
                return true;
            }
            if (itemId == R.id.navigation_create_reservation) {
                navigateTo(AppDestination.RESERVATION_CREATE);
                return true;
            }
            if (itemId == R.id.navigation_reservations) {
                navigateTo(AppDestination.RESERVATIONS);
                return true;
            }
            if (itemId == R.id.navigation_profile) {
                navigateTo(AppDestination.PROFILE);
                return true;
            }
            return false;
        });

        if (savedInstanceState == null) {
            InitialDestination initialDestination = appContainer.getInitialAppDestinationUseCase().execute();
            navigateTo(toAppDestination(initialDestination));
        } else {
            String destinationName = savedInstanceState.getString(KEY_CURRENT_DESTINATION);
            if (destinationName != null) {
                currentDestination = AppDestination.valueOf(destinationName);
                updateBottomNavigation(currentDestination);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (currentDestination != null) {
            outState.putString(KEY_CURRENT_DESTINATION, currentDestination.name());
        }
    }

    public AppContainer getAppContainer() {
        return appContainer;
    }

    public void navigateTo(AppDestination destination) {
        navigator.navigateTo(destination);
        currentDestination = destination;
        updateBottomNavigation(destination);
    }

    public void navigateBack() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            navigateTo(AppDestination.HOME);
        }
    }

    private AppDestination toAppDestination(InitialDestination initialDestination) {
        if (initialDestination == InitialDestination.HOME) {
            return AppDestination.HOME;
        }
        return AppDestination.LOGIN;
    }

    private void updateBottomNavigation(AppDestination destination) {
        boolean authenticatedArea = destination != AppDestination.LOGIN && destination != AppDestination.REGISTER;
        bottomNavigationView.setVisibility(authenticatedArea ? android.view.View.VISIBLE : android.view.View.GONE);
        int menuItemId = menuItemIdFor(destination);
        if (menuItemId != 0) {
            bottomNavigationView.getMenu().findItem(menuItemId).setChecked(true);
        }
    }

    private int menuItemIdFor(AppDestination destination) {
        switch (destination) {
            case HOME:
                return R.id.navigation_home;
            case RESERVATION_CREATE:
                return R.id.navigation_create_reservation;
            case RESERVATIONS:
            case RESERVATION_UPCOMING:
            case RESERVATION_PAST:
                return R.id.navigation_reservations;
            case PROFILE:
                return R.id.navigation_profile;
            case LOGIN:
            case REGISTER:
            default:
                return 0;
        }
    }
}
