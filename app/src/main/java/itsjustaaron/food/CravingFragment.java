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
public class CravingFragment extends Fragment {

    public View rootView;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private MyAdapter mAdapter;

    private AsyncTask start;

    public void refresh(final SwipeRefreshLayout s) {
        if (Data.cravings.size() == 0) {
            start.execute(new Void[]{});
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

                    Data.cravings.clear();
                    try {
                        Data.collection = Data.collection.getPage(Data.loadCount, 0);
                        ArrayList<Map> temp = new ArrayList<Map>(Data.collection.getCurrentPage());
                        //TODO: reorganize retrieving logic here
                        for (int i = 0; i < temp.size(); i++) {
                            Map obj = temp.get(i);
                            Craving craving = new Craving(obj);
                            Data.cravings.add(craving);
                        }
                    } catch (BackendlessException e) {
                        Log.d("backendless", e.toString());
                    }
                    return null;
                }

                @Override
                public void onPostExecute(Void v) {
                    mAdapter.notifyDataSetChanged();
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
        Data.cravings = new ArrayList<Craving>();
        start = new AsyncTask<Void, Void, Void>() {
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
                    Data.cravings.clear();
                    BackendlessDataQuery backendlessDataQuery = new BackendlessDataQuery();
                    QueryOptions queryOptions = new QueryOptions();
                    queryOptions.setOffset(0);
                    queryOptions.setPageSize(Data.loadCount);
                    backendlessDataQuery.setQueryOptions(queryOptions);
                    Data.collection = Backendless.Data.of("Craving").find(backendlessDataQuery);
                    ArrayList<Map> temp = new ArrayList<>(Data.collection.getCurrentPage());
                    for (int i = 0; i < temp.size(); i++) {
                        Data.cravings.add(new Craving(temp.get(i)));
                    }
                } catch (BackendlessException e) {
                    Log.d("backgroundless", e.toString());
                }
                return null;
            }

            @Override
            public void onPostExecute(Void v) {
                mAdapter.notifyDataSetChanged();
                wait.dismiss();
            }
        };
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
        srl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh(srl);
            }
        });

        start.execute(new Void[]{});

        final EndlessScroll endlessScroll = new EndlessScroll(mLayoutManager) {
            @Override
            public void onLoadMore(int page, final RecyclerView view) {
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    public Void doInBackground(Void... voids) {
                        try {
                            Data.collection = Data.collection.nextPage();
                            ArrayList<Map> temp = new ArrayList<Map>(Data.collection.getCurrentPage());
                            //TODO: reorganize retrieving logic here
                            for (int i = 0; i < temp.size(); i++) {
                                Map obj = temp.get(i);
                                Craving craving = new Craving(obj);
                                Data.cravings.add(craving);
                            }
                        } catch (BackendlessException e) {
                            Log.d("backendless", e.toString());
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
