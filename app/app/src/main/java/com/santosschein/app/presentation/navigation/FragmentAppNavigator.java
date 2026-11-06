package com.santosschein.app.presentation.navigation;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.santosschein.app.presentation.auth.login.LoginFragment;
import com.santosschein.app.presentation.auth.register.RegisterFragment;
import com.santosschein.app.presentation.home.HomeFragment;
import com.santosschein.app.presentation.profile.ProfileFragment;
import com.santosschein.app.presentation.reservation.create.CreateReservationFragment;
import com.santosschein.app.presentation.reservation.list.ReservationListFragment;

public final class FragmentAppNavigator implements AppNavigator {

    private final FragmentManager fragmentManager;
    private final int containerId;

    public FragmentAppNavigator(FragmentManager fragmentManager, int containerId) {
        this.fragmentManager = fragmentManager;
        this.containerId = containerId;
    }

    @Override
    public void navigateTo(AppDestination destination) {
        if (destination == AppDestination.LOGIN || isTopLevelDestination(destination)) {
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }

        FragmentTransaction transaction = fragmentManager.beginTransaction()
                .replace(containerId, createFragment(destination));
        if (destination != AppDestination.LOGIN && !isTopLevelDestination(destination)) {
            transaction.addToBackStack(destination.name());
        }
        transaction.commit();
    }

    private Fragment createFragment(AppDestination destination) {
        switch (destination) {
            case REGISTER:
                return new RegisterFragment();
            case HOME:
                return new HomeFragment();
            case RESERVATION_CREATE:
                return new CreateReservationFragment();
            case RESERVATIONS:
                return ReservationListFragment.newInstance(ReservationListFragment.MODE_UPCOMING, true);
            case RESERVATION_UPCOMING:
                return ReservationListFragment.newInstance(ReservationListFragment.MODE_UPCOMING, false);
            case RESERVATION_PAST:
                return ReservationListFragment.newInstance(ReservationListFragment.MODE_PAST, false);
            case PROFILE:
                return new ProfileFragment();
            case LOGIN:
            default:
                return new LoginFragment();
        }
    }

    private boolean isTopLevelDestination(AppDestination destination) {
        return destination == AppDestination.HOME
                || destination == AppDestination.RESERVATION_CREATE
                || destination == AppDestination.RESERVATIONS
                || destination == AppDestination.PROFILE;
    }
}
