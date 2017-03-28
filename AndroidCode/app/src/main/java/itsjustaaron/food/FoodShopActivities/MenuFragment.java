package itsjustaaron.food.FoodShopActivities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import itsjustaaron.food.Back.Back;
import itsjustaaron.food.Back.Data;
import itsjustaaron.food.FoodActivities.NewFood;
import itsjustaaron.food.FoodActivities.Welcome;
import itsjustaaron.food.Model.Offer;
import itsjustaaron.food.Model.Offerer;
import itsjustaaron.food.R;

/**
 * Created by aozhang on 3/20/2017.
 */

public class MenuFragment extends Fragment {
    ArrayList<Offer> menu;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.tab_menu, container, false);
        menu = new ArrayList<>();
        new AsyncTask<Void, Void, Void>() {
            @Override
            public Void doInBackground(Void... voids) {
                Offerer offerer = (Offerer) Back.getObjectByID(Data.user.getProperty("offerer").toString(), Back.object.offerer);
                List<Map> temp = Back.findObjectByWhere("offerer='" + offerer.objectId + "'", Back.object.offer).getCurPage();
                for (Map m : temp) {
                    menu.add(new Offer(m));
                }
                return null;
            }

            @Override
            public void onPostExecute(Void v) {
                if(menu.size() == 0) {
                    rootView.findViewById(R.id.emptyPrompt).setVisibility(View.VISIBLE);
                }
                rootView.findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (Data.user != null) {
                            Intent next = new Intent(getActivity(), NewFood.class);
                            next.putExtra("onCraving", true);
                            startActivity(next);
                        }
                    }
                });
            }
        }.execute();

        return rootView;
    }


}
