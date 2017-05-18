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
import itsjustaaron.food.Model.Offer;
import itsjustaaron.food.R;
import itsjustaaron.food.Utilities.EndlessScroll;
import itsjustaaron.food.Utilities.MainAdapter;
import itsjustaaron.food.Utilities.SimpleDividerItemDecoration;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

/**
 * Created by Aaron-Work on 8/7/2016.
 */
public class OfferFragment extends Fragment {
    public SwipeRefreshLayout swipeRefreshLayout;
    MainAdapter<Offer> mainAdapter;
    boolean started = false;
    LinearLayoutManager layoutManager;
    View rootView;
    RecyclerView recyclerView;
    ProgressDialog wait;

    public void refresh(final SwipeRefreshLayout s) {
        if(s != null) {
            s.setRefreshing(true);
        }
        if (Data.offers.size() == 0 || Data.oSearchCriteria.size() == 0) {
            new Start().execute();
        } else {
            new AsyncTask<Void, Void, Void>() {

                @Override
                public Void doInBackground(Void... voids) {

                    Data.offers.clear();
                    String query = Food.listToCsv(Data.oSearchCriteria);
                    ((Main)getActivity()).doMySearch(query);
                    return null;
                }

                @Override
                public void onPostExecute(Void v) {
                    mainAdapter.notifyDataSetChanged();
                    s.setRefreshing(false);
                }
            }.execute();
        }
    }

    public void notifyChanges() {
        if(started) {
            mainAdapter.notifyDataSetChanged();
        }else {
            started = true;
            final SwipeRefreshLayout srl = (SwipeRefreshLayout) rootView.findViewById(R.id.oSwipeRefresh);
            swipeRefreshLayout = srl;
            srl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    Data.offers.clear();
                    refresh(srl);
                }
            });

            getActivity().findViewById(R.id.sort).setVisibility(View.VISIBLE);

            final EndlessScroll endlessScroll = new EndlessScroll(layoutManager) {
                @Override
                public void onLoadMore(int page, final RecyclerView view) {
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        public Void doInBackground(Void... voids) {
                            Data.offerPaged.nextPage();
                            ArrayList<Map> temp = new ArrayList<>(Data.offerPaged.getCurPage());
                            for (int i = 0; i < temp.size(); i++) {
                                Map obj = temp.get(i);
                                Offer offer = new Offer(obj);
                                Data.offers.add(offer);
                            }
                            return null;
                        }

                        @Override
                        public void onPostExecute(Void v) {
                            mainAdapter.notifyDataSetChanged();
                        }
                    }.execute();
                }
            };
            recyclerView.addOnScrollListener(endlessScroll);
            getActivity().findViewById(R.id.findNear).setVisibility(View.GONE);
            swipeRefreshLayout.setVisibility(View.VISIBLE);
        }

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
        Data.offers = new ArrayList<>();
        mainAdapter = new MainAdapter<>(Data.offers, 'o', getActivity());
        recyclerView = (RecyclerView) rootView.findViewById(R.id.offerList);
        recyclerView.setAdapter(mainAdapter);
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        final GifDrawable gif = (GifDrawable) ((GifImageView)rootView.findViewById(R.id.radar)).getDrawable();
        gif.stop();
        rootView.findViewById(R.id.radar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gif.start();
                start();
            }
        });
        return rootView;
    }

    public void start() {
        if(!started) {
            final SwipeRefreshLayout srl = (SwipeRefreshLayout) rootView.findViewById(R.id.oSwipeRefresh);
            swipeRefreshLayout = srl;
            srl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    Data.offers.clear();
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
                            ArrayList<Map> temp = new ArrayList<>(Data.offerPaged.getCurPage());
                            for (int i = 0; i < temp.size(); i++) {
                                Map obj = temp.get(i);
                                Offer offer = new Offer(obj);
                                Data.offers.add(offer);
                            }
                            return null;
                        }

                        @Override
                        public void onPostExecute(Void v) {
                            mainAdapter.notifyDataSetChanged();
                        }
                    }.execute();
                }
            };
            recyclerView.addOnScrollListener(endlessScroll);
        }
    }

    public void notifySortChange() {
        new Start().execute();
    }

    public void showSort() {
        if(started) {
            getActivity().findViewById(R.id.sort).setVisibility(View.VISIBLE);
        }else {
            getActivity().findViewById(R.id.sort).setVisibility(View.GONE);
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
            Data.offers.clear();
            File offerers = new File(Data.fileDir + "/offers/offerers/");
            if(!offerers.exists()) {
                offerers.mkdirs();
            }else {
                for (File f : offerers.listFiles()) {
                    f.delete();
                }
            }
            Back.generateOffers();
            ArrayList<Map> temp = new ArrayList<>(Data.offerPaged.getCurPage());
            for (int i = 0; i < temp.size(); i++) {
                Data.offers.add(new Offer(temp.get(i)));
            }
            return null;
        }

        @Override
        public void onPostExecute(Void v) {
            if (!started) {
                getActivity().findViewById(R.id.findNear).setVisibility(View.GONE);
                swipeRefreshLayout.setVisibility(View.VISIBLE);
                started = true;
                if(Data.offers.size() == 0) {
                    getActivity().findViewById(R.id.nothingNear).setVisibility(View.VISIBLE);
                }
                getActivity().findViewById(R.id.sort).setVisibility(View.VISIBLE);
            } else {
                mainAdapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);
            }
        }
    }
}
