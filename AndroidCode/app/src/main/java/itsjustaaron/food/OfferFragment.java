package itsjustaaron.food;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Map;

import itsjustaaron.food.Back.Back;
import itsjustaaron.food.Back.Data;

/**
 * Created by Aaron-Work on 8/7/2016.
 */
public class OfferFragment extends Fragment {
    MyAdapter<FoodOffer> myAdapter;
    boolean started = false;
    LinearLayoutManager layoutManager;
    View rootView;
    RecyclerView recyclerView;
    ProgressDialog wait;

    private class Start extends AsyncTask<Void, Void, Void> {

        @Override
        public void onPreExecute() {
            wait.show();
        }

        @Override
        public Void doInBackground(Void... voids) {
            Data.foodOffers.clear();
            ArrayList<Map> temp = new ArrayList<>(Back.getAll(Back.object.foodoffer).getCurPage());
            for (int i = 0; i < temp.size(); i++) {
                Data.foodOffers.add(new FoodOffer(temp.get(i)));
            }
            return null;
        }

        @Override
        public void onPostExecute(Void v) {
            myAdapter.notifyDataSetChanged();
            if(wait != null) {
                wait.dismiss();
            }
        }
    };

    public void refresh(final SwipeRefreshLayout s) {
        s.setRefreshing(false);
        if (Data.foodOffers.size() == 0 || Data.oSearchCriteria.size() == 0) {
            new Start().execute(new Void[]{});
        } else {
            new AsyncTask<Void, Void, Void>() {

                @Override
                public void onPreExecute() {
                    wait.show();
                }

                @Override
                public Void doInBackground(Void... voids) {

                    Data.foodOffers.clear();
                    String query = Food.listToCsv(Data.oSearchCriteria);
                    Intent search = new Intent(getActivity(), Searchable.class);
                    search.putExtra(SearchManager.QUERY, query);
                    startActivity(search);
                    return null;
                }

                @Override
                public void onPostExecute(Void v) {
                    myAdapter.notifyDataSetChanged();
                    wait.dismiss();
                }
            }.execute(new Void[]{});
        }
    }

    public void notifyChanges() {myAdapter.notifyDataSetChanged();}

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
        recyclerView = (RecyclerView)rootView.findViewById(R.id.offerList);
        recyclerView.setAdapter(myAdapter);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        return rootView;
    }

    public void start() {
        if(!started) {
            started = true;
            final SwipeRefreshLayout srl = (SwipeRefreshLayout) rootView.findViewById(R.id.oSwipeRefresh);
            srl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    Data.foodOffers.clear();
                    refresh(srl);
                }
            });

            new Start().execute(new Void[]{});

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
                    }.execute(new Void[]{});
                }
            };
            recyclerView.addOnScrollListener(endlessScroll);
        }
    }
}
