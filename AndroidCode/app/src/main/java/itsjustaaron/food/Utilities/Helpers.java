package itsjustaaron.food.Utilities;

import itsjustaaron.food.R;

/**
 * Created by Aaron-Work on 5/1/2017.
 */

public class Helpers {
    public static int getTagDrawable(int tag) {
        switch (tag) {
            case 1:
                return R.drawable.tag_one;
            case 2:
                return R.drawable.tag_two;
            case 3:
                return R.drawable.tag_three;
            case 4:
                return R.drawable.tag_four;
            case 5:
                return R.drawable.tag_five;
            case 6:
                return R.drawable.tag_six;
//            case 7:
//                return R.drawable.tag_seven;
//            case 8:
//                return R.drawable.tag_eight;
//            case 9:
//                return R.drawable.tag_nine;
//            case 10:
//                return R.drawable.tag_ten;
//            default:
//                return R.drawable.tag_eleven;
            default:
                return 0;
        }
    }
}
