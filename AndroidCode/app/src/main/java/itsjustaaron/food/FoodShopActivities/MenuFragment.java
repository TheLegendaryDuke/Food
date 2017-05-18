package itsjustaaron.food.FoodShopActivities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import itsjustaaron.food.Back.Back;
import itsjustaaron.food.Back.Data;
import itsjustaaron.food.FoodActivities.NewFood;
import itsjustaaron.food.Model.Offer;
import itsjustaaron.food.R;
import itsjustaaron.food.Utilities.MainAdapter;
import itsjustaaron.food.Utilities.SimpleDividerItemDecoration;

/**
 * Created by aozhang on 3/20/2017.
 */

public class MenuFragment extends Fragment {
    private View root;
    private MainAdapter<Offer> mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // TODO: add an "end offer" option
        final View rootView = inflater.inflate(R.layout.tab_menu, container, false);
        root = rootView;
        Data.menu = new ArrayList<>();
        new AsyncTask<Void, Void, Void>() {
            @Override
            public Void doInBackground(Void... voids) {
                List<Map> temp = Back.findObjectByWhere("ownerId='" + Data.user.getObjectId() + "'", Back.object.offer).getCurPage();
                for (Map m : temp) {
                    Data.menu.add(new Offer(m));
                }
                return null;
            }

            @Override
            public void onPostExecute(Void v) {
                rootView.findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (Data.user != null) {
                            Intent next = new Intent(getActivity(), NewFood.class);
                            next.putExtra("onCraving", false);
                            startActivityForResult(next, 0);
                        }
                    }
                });

                RecyclerView mRecyclerView = (RecyclerView) root.findViewById(R.id.menuRecycler);
                mRecyclerView.setHasFixedSize(true);
                LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
                mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));
                mRecyclerView.setHasFixedSize(true);
                mRecyclerView.setLayoutManager(mLayoutManager);
                mAdapter = new MainAdapter<>(Data.menu, 'm', getActivity());
                mRecyclerView.setAdapter(mAdapter);

                update();
            }
        }.execute();
        return rootView;
    }

    public void update() {
        if(Data.menu.size() == 0) {
            root.findViewById(R.id.emptyPrompt).setVisibility(View.VISIBLE);
        }else {
            root.findViewById(R.id.emptyPrompt).setVisibility(View.GONE);
            mAdapter.notifyDataSetChanged();
        }
    }
}
