package itsjustaaron.food;

import android.app.ProgressDialog;
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

import com.backendless.Backendless;
import com.backendless.exceptions.BackendlessException;
import com.backendless.persistence.BackendlessDataQuery;
import com.backendless.persistence.QueryOptions;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Aaron-Work on 8/7/2016.
 */
public class OfferFragment extends Fragment {
    MyAdapter<FoodOffer> myAdapter;
    boolean started = false;
    LinearLayoutManager layoutManager;
    View rootView;
    RecyclerView recyclerView;

    private class Start extends AsyncTask<Void, Void, Void> {
        ProgressDialog wait;

        @Override
        public void onPreExecute() {
            wait = new ProgressDialog(getActivity());
            wait.setMessage("Please wait...");
            wait.show();
        }

        @Override
        public Void doInBackground(Void... voids) {
            try {
                Data.foodOffers.clear();
                BackendlessDataQuery backendlessDataQuery = new BackendlessDataQuery();
                QueryOptions queryOptions = new QueryOptions();
                queryOptions.setOffset(0);
                queryOptions.setPageSize(Data.loadCount);
                backendlessDataQuery.setQueryOptions(queryOptions);
                Data.offerCollection = Backendless.Data.of("foodOffers").find(backendlessDataQuery);
                ArrayList<Map> temp = new ArrayList<>(Data.offerCollection.getCurrentPage());
                for (int i = 0; i < temp.size(); i++) {
                    Data.foodOffers.add(new FoodOffer(temp.get(i)));
                }
            } catch (BackendlessException e) {
                Log.d("backgroundless", e.toString());
            }
            return null;
        }

        @Override
        public void onPostExecute(Void v) {
            myAdapter.notifyDataSetChanged();
            wait.dismiss();
        }
    };

    public void refresh(final SwipeRefreshLayout s) {
        if (Data.cravings.size() == 0) {
            s.setRefreshing(false);
            new Start().execute(new Void[]{});
        } else {
            new AsyncTask<Void, Void, Void>() {
                ProgressDialog wait;

                @Override
                public void onPreExecute() {
                    if (s == null) {
                        wait = new ProgressDialog(getActivity());
                        wait.setMessage("Please wait...");
                        wait.show();
                    }
                }

                @Override
                public Void doInBackground(Void... voids) {

                    Data.foodOffers.clear();
                    try {
                        Data.offerCollection = Data.offerCollection.getPage(Data.loadCount, 0);
                        ArrayList<Map> temp = new ArrayList<Map>(Data.offerCollection.getCurrentPage());
                        for (int i = 0; i < temp.size(); i++) {
                            Map obj = temp.get(i);
                            FoodOffer craving = new FoodOffer(obj);
                            Data.foodOffers.add(craving);
                        }
                    } catch (BackendlessException e) {
                        Log.d("backendless", e.toString());
                    }
                    return null;
                }

                @Override
                public void onPostExecute(Void v) {
                    myAdapter.notifyDataSetChanged();
                    if (s == null) {
                        wait.dismiss();
                    } else {
                        s.setRefreshing(false);
                    }
                }
            }.execute(new Void[]{});
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                            try {
                                Data.offerCollection = Data.offerCollection.nextPage();
                                ArrayList<Map> temp = new ArrayList<Map>(Data.offerCollection.getCurrentPage());
                                for (int i = 0; i < temp.size(); i++) {
                                    Map obj = temp.get(i);
                                    FoodOffer offer = new FoodOffer(obj);
                                    Data.foodOffers.add(offer);
                                }
                            } catch (BackendlessException e) {
                                Log.d("backendless", e.toString());
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
