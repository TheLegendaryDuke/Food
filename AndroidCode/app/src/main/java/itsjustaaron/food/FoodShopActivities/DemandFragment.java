package itsjustaaron.food.FoodShopActivities;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Map;

import itsjustaaron.food.Back.Back;
import itsjustaaron.food.Back.Data;
import itsjustaaron.food.FoodActivities.Main;
import itsjustaaron.food.Model.Craving;
import itsjustaaron.food.Model.Food;
import itsjustaaron.food.R;
import itsjustaaron.food.Utilities.EndlessScroll;
import itsjustaaron.food.Utilities.MainAdapter;
import itsjustaaron.food.Utilities.SimpleDividerItemDecoration;

/**
 * Created by aozhang on 3/20/2017.
 */

public class DemandFragment extends Fragment {
    ProgressDialog wait;
    SwipeRefreshLayout swipeRefreshLayout;
    MainAdapter mAdapter;

    public void refresh(final SwipeRefreshLayout s) {
        if(s != null) {
            s.setRefreshing(true);
        }
        if (Data.cravings.size() == 0 || Data.cSearchCriteria.size() == 0) {
            new start().execute();
        } else {
            new AsyncTask<Void, Void, Void>() {

                @Override
                public Void doInBackground(Void... voids) {

                    Data.cravings.clear();
                    String query = Food.listToCsv(Data.cSearchCriteria);
                    ((Main)getActivity()).doMySearch(query);
                    return null;
                }

                @Override
                public void onPostExecute(Void v) {
                    mAdapter.notifyDataSetChanged();
                    swipeRefreshLayout.setRefreshing(false);
                }
            }.execute(new Void[]{});
        }
    }

    public void updateData() {
        mAdapter.notifyDataSetChanged();
    }

    private class start extends AsyncTask<Void, Void, Void> {

        @Override
        public void onPreExecute() {
            if (wait != null) {
                wait.show();
            }else {
                swipeRefreshLayout.setRefreshing(true);
            }
        }

        @Override
        public Void doInBackground(Void... voids) {
            Data.cravings.clear();
            Data.cravingPaged = Back.getAll(Back.object.craving);
            ArrayList<Map> temp = new ArrayList<>(Data.cravingPaged.getCurPage());
            for (int i = 0; i < temp.size(); i++) {
                Data.cravings.add(new Craving(temp.get(i)));
            }
            return null;
        }

        @Override
        public void onPostExecute(Void v) {
            mAdapter.notifyDataSetChanged();
            if (wait != null) {
                wait.dismiss();
                wait = null;
            }
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Data.cravings = new ArrayList<>();
        wait = new ProgressDialog(getActivity());
        wait.setMessage("Please wait...");
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab_demand, container, false);
        RecyclerView mRecyclerView = (RecyclerView) rootView.findViewById(R.id.cravingList);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new MainAdapter<>(Data.cravings, 'd', getActivity());
        mRecyclerView.setAdapter(mAdapter);

        final SwipeRefreshLayout srl = (SwipeRefreshLayout) rootView.findViewById(R.id.cSwipeRefresh);
        swipeRefreshLayout = srl;
        srl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Data.cravings.clear();
                refresh(srl);
            }
        });

        new start().execute();

        final EndlessScroll endlessScroll = new EndlessScroll(mLayoutManager) {
            @Override
            public void onLoadMore(int page, final RecyclerView view) {
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    public Void doInBackground(Void... voids) {
                        Data.cravingPaged.nextPage();
                        ArrayList<Map> temp = new ArrayList<Map>(Data.cravingPaged.getCurPage());
                        for (int i = 0; i < temp.size(); i++) {
                            Map obj = temp.get(i);
                            Craving craving = new Craving(obj);
                            Data.cravings.add(craving);
                        }
                        return null;
                    }

                    @Override
                    public void onPostExecute(Void v) {
                        mAdapter.notifyDataSetChanged();
                    }
                }.execute();
            }
        };
        mRecyclerView.addOnScrollListener(endlessScroll);

        return rootView;
    }
}
