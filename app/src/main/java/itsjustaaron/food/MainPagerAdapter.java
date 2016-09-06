package itsjustaaron.food;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by Aaron-Work on 8/7/2016.
 */
public class MainPagerAdapter extends FragmentPagerAdapter{
    public MainPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                CravingFragment cravingFragment = new CravingFragment();
                Data.cravingFragment = cravingFragment;
                return cravingFragment;
            case 1:
                OfferFragment offerFragment = new OfferFragment();
                Data.offerFragment = offerFragment;
                return offerFragment;
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
                return "Cravings";
            case 1:
                return "Offers";
            default:
                return null;
        }
    }
}
