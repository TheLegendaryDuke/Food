package itsjustaaron.food.FoodActivities;

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
import itsjustaaron.food.Model.Craving;
import itsjustaaron.food.Model.Food;
import itsjustaaron.food.R;
import itsjustaaron.food.Utilities.EndlessScroll;

/**
 * Created by Aaron-Work on 8/7/2016.
 */
public class CravingFragment extends Fragment {

    public View rootView;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private MyAdapter mAdapter;
    public SwipeRefreshLayout swipeRefreshLayout;
    ProgressDialog wait;

    private class start extends AsyncTask<Void, Void, Void> {

        @Override
        public void onPreExecute() {
            if(wait != null) {
                wait.show();
            }
            swipeRefreshLayout.setRefreshing(true);
        }

        @Override
        public Void doInBackground(Void... voids) {
                Data.cravings.clear();
                ArrayList<Map> temp = new ArrayList<>(Back.getAll(Back.object.craving).getCurPage());
                for (int i = 0; i < temp.size(); i++) {
                    Data.cravings.add(new Craving(temp.get(i)));
                }
            return null;
        }

        @Override
        public void onPostExecute(Void v) {
            mAdapter.notifyDataSetChanged();
            if(wait != null) {
                wait.dismiss();
                wait = null;
            }
            swipeRefreshLayout.setRefreshing(false);
        }
    };

    public void refresh(final SwipeRefreshLayout s) {
        if(s != null) {
            s.setRefreshing(true);
        }
        if (Data.foodOffers.size() == 0 || Data.cSearchCriteria.size() == 0) {
            new start().execute(new Void[]{});
        }else {
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Data.cravings = new ArrayList<Craving>();
        wait = new ProgressDialog(getActivity());
        wait.setMessage("Please wait...");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.tab_craving, container, false);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.cravingList);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new MyAdapter<Craving>(Data.cravings, 'c', getActivity());
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

        new start().execute(new Void[]{});

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
                }.execute(new Void[]{});
            }
        };
        mRecyclerView.addOnScrollListener(endlessScroll);

        return rootView;
    }

    public void notifyChanges() {
        mAdapter.notifyDataSetChanged();
    }
}
