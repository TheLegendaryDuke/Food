package itsjustaaron.food.FoodShopActivities;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import itsjustaaron.food.FoodActivities.CravingFragment;
import itsjustaaron.food.FoodActivities.OfferFragment;

/**
 * Created by aozhang on 3/20/2017.
 */

public class FoodShopPagerAdapter extends FragmentPagerAdapter {
    private FoodShopMain main;

    public FoodShopPagerAdapter(FragmentManager fm, FoodShopMain main) {
        super(fm);
        this.main = main;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                DemandFragment demandFragment = new DemandFragment();
                main.demandFragment = demandFragment;
                return demandFragment;
            case 1:
                MenuFragment menuFragment = new MenuFragment();
                main.menuFragment = menuFragment;
                return menuFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Demands";
            case 1:
                return "Menu";
            default:
                return null;
        }
    }
}
