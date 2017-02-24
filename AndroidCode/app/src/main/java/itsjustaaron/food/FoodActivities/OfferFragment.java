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

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import itsjustaaron.food.Back.Back;
import itsjustaaron.food.Back.Data;
import itsjustaaron.food.Model.Food;
import itsjustaaron.food.Model.FoodOffer;
import itsjustaaron.food.R;
import itsjustaaron.food.Utilities.EndlessScroll;

/**
 * Created by Aaron-Work on 8/7/2016.
 */
public class OfferFragment extends Fragment {
    public SwipeRefreshLayout swipeRefreshLayout;
    MyAdapter<FoodOffer> myAdapter;
    boolean started = false;
    LinearLayoutManager layoutManager;
    View rootView;
    RecyclerView recyclerView;
    ProgressDialog wait;

    public void refresh(final SwipeRefreshLayout s) {
        if (s != null) {
            s.setRefreshing(true);
        }
        if (Data.foodOffers.size() == 0 || Data.oSearchCriteria.size() == 0) {
            new Start().execute();
        } else {
            new AsyncTask<Void, Void, Void>() {

                @Override
                public Void doInBackground(Void... voids) {

                    Data.foodOffers.clear();
                    String query = Food.listToCsv(Data.oSearchCriteria);
                    ((Main) getActivity()).doMySearch(query);
                    return null;
                }

                @Override
                public void onPostExecute(Void v) {
                    myAdapter.notifyDataSetChanged();
                    s.setRefreshing(false);
                }
            }.execute();
        }
    }

    public void notifyChanges() {
        myAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        wait = new ProgressDialog(getActivity());
        wait.setMessage("Please wait...");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.tab_offer, container, false);
        Data.foodOffers = new ArrayList<>();
        myAdapter = new MyAdapter<>(Data.foodOffers, 'o', getActivity());
        recyclerView = (RecyclerView) rootView.findViewById(R.id.offerList);
        recyclerView.setAdapter(myAdapter);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        return rootView;
    }

    public void start() {
        if (!started) {
            final SwipeRefreshLayout srl = (SwipeRefreshLayout) rootView.findViewById(R.id.oSwipeRefresh);
            swipeRefreshLayout = srl;
            srl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    Data.foodOffers.clear();
                    refresh(srl);
                }
            });

            new Start().execute();

            final EndlessScroll endlessScroll = new EndlessScroll(layoutManager) {
                @Override
                public void onLoadMore(int page, final RecyclerView view) {
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        public Void doInBackground(Void... voids) {
                            Data.offerPaged.nextPage();
                            ArrayList<Map> temp = new ArrayList<Map>(Data.offerPaged.getCurPage());
                            for (int i = 0; i < temp.size(); i++) {
                                Map obj = temp.get(i);
                                FoodOffer offer = new FoodOffer(obj);
                                Data.foodOffers.add(offer);
                            }
                            return null;
                        }

                        @Override
                        public void onPostExecute(Void v) {
                            myAdapter.notifyDataSetChanged();
                        }
                    }.execute();
                }
            };
            recyclerView.addOnScrollListener(endlessScroll);
        }
    }

    private class Start extends AsyncTask<Void, Void, Void> {

        @Override
        public void onPreExecute() {
            if (started) {
                swipeRefreshLayout.setRefreshing(true);
            }
        }

        @Override
        public Void doInBackground(Void... voids) {
            Data.foodOffers.clear();
            File offerers = new File(Data.fileDir + "/offers/offerers/");
            for (File f : offerers.listFiles()) {
                f.delete();
            }
            Data.offerPaged = Back.findObjectByWhere("city='" + Data.user.getProperty("city") + "'", Back.object.foodoffer);
            ArrayList<Map> temp = new ArrayList<>(Data.offerPaged.getCurPage());
            for (int i = 0; i < temp.size(); i++) {
                Data.foodOffers.add(new FoodOffer(temp.get(i)));
            }
            return null;
        }

        @Override
        public void onPostExecute(Void v) {
            if (!started) {
                getActivity().findViewById(R.id.findNear).setVisibility(View.GONE);
                swipeRefreshLayout.setVisibility(View.VISIBLE);
                started = true;
            } else {
                myAdapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);
            }
        }
    }
}
