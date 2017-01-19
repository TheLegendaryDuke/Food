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

//    public class EndlessScrollListener implements AbsListView.OnScrollListener {
//
//        private int visibleThreshold = 5;
//        private int currentPage = 0;
//        private int previousTotal = 0;
//        private boolean loading = true;
//
//        public EndlessScrollListener() {
//        }
//        public EndlessScrollListener(int visibleThreshold) {
//            this.visibleThreshold = visibleThreshold;
//        }
//
//        @Override
//        public void onScroll(AbsListView view, int firstVisibleItem,
//                             int visibleItemCount, int totalItemCount) {
//            if (loading) {
//                if (totalItemCount > previousTotal) {
//                    loading = false;
//                    previousTotal = totalItemCount;
//                    currentPage++;
//                }
//            }
//            if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
//                updateCravings(false);
//                loading = true;
//            }
//        }
//
//        @Override
//        public void onScrollStateChanged(AbsListView view, int scrollState) {
//        }
//    }

    public View rootView;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private MyAdapter mAdapter;

    private AsyncTask start;


//    public class CravingAdapter extends ArrayAdapter<Craving> {
//        private final Context context;
//        public final List<Craving> values;
//
//        private View.OnClickListener listener;
//
//        public CravingAdapter(Context context, List<Craving> values) {
//            super(context, -1, values);
//            if(values == null) {
//                values = new ArrayList<Craving>();
//            }
//            this.context = context;
//            this.values = values;
//        }
//
//        @Override
//        public View getView(int position, View convertView, ViewGroup parent) {
//            LayoutInflater inflater = (LayoutInflater) context
//                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//            View rowView = inflater.inflate(R.layout.craving_list_item, parent, false);
//            final ImageView image = (ImageView)rowView.findViewById(R.id.cravingItemImage);
//            TextView description = (TextView)rowView.findViewById(R.id.cravingItemDescription);
//            TextView tags = (TextView)rowView.findViewById(R.id.cravingItemTags);
//            final ImageView likeOrNot = (ImageView)rowView.findViewById(R.id.cravingFollowingOrNot);
//            final TextView count = (TextView)rowView.findViewById(R.id.cravingFollowerCount);
//            final Craving craving = values.get(position);
//            final String imagePath = getActivity().getFilesDir() + "/foods/" + craving.food.image;
//            final File file = new File(imagePath);
//            if(file.exists()) {
//                image.setImageBitmap(BitmapFactory.decodeFile(imagePath));
//            }else {
//                File dir = new File(getActivity().getFilesDir() + "/foods/");
//                if(!dir.exists()) {
//                    dir.mkdir();
//                }
//                final String path = "https://api.backendless.com/0020F1DC-E584-AD36-FF74-6D3E9E917400/v1/files/foods/" + craving.food.image;
//                new Thread() {
//                    public void run() {
//                        try {
//                            file.createNewFile();
//                            URL url = new URL(path);
//                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//                            conn.setDoInput(true);
//                            conn.connect();
//                            InputStream is = conn.getInputStream();
//                            Bitmap bm = BitmapFactory.decodeStream(is);
//                            FileOutputStream fos = new FileOutputStream(imagePath);
//                            ByteArrayOutputStream outstream = new ByteArrayOutputStream();
//                            bm.compress(Bitmap.CompressFormat.PNG, 100, outstream);
//                            byte[] byteArray = outstream.toByteArray();
//                            fos.write(byteArray);
//                            fos.close();
//                        }
//                        catch (Exception e) {
//                        }
//                    }
//                }.start();
//            }
//            description.setText(craving.food.name);
//            tags.setText(craving.food.tags);
//            if(craving.following) {
//                likeOrNot.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_favorite_black_48dp, null));
//            }else {
//                likeOrNot.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_favorite_border_black_48dp, null));
//            }
//            likeOrNot.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    craving.followSwitch();
//                    if(craving.following) {
//                        likeOrNot.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_favorite_black_48dp, null));
//                        count.setText(String.valueOf(Integer.parseInt(count.getText().toString()) + 1));
//                    }else {
//                        likeOrNot.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_favorite_border_black_48dp, null));
//                        count.setText(String.valueOf(Integer.parseInt(count.getText().toString()) - 1));
//                    }
//                }
//            });
//            listener = new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    Intent next = new Intent(getActivity().getBaseContext(), CravingDetails.class);
//                    next.putExtra("cravingID", craving.objectId);
//                    startActivity(next);
//                }
//            };
//            image.setOnClickListener(listener);
//            description.setOnClickListener(listener);
//            count.setText(String.valueOf(craving.numFollowers));
//            image.setImageBitmap(BitmapFactory.decodeFile(imagePath));
//            return rowView;
//        }
//    }



//    public void updateCravings(boolean reload) {
//        Main.showWait();
//        final List<Map> temp = new ArrayList<>();
//        if (reload) {
//            final BackendlessDataQuery backendlessDataQuery = new BackendlessDataQuery();
//            QueryOptions queryOptions = new QueryOptions();
//            queryOptions.setOffset(0);
//            queryOptions.setPageSize(Data.loadCount);
//            backendlessDataQuery.setQueryOptions(queryOptions);
//            new Thread() {
//                public void run() {
//                    Backendless.Data.of("Craving").find(backendlessDataQuery, new AsyncCallback<BackendlessCollection<Map>>() {
//                        @Override
//                        public void handleResponse(BackendlessCollection<Map> mapBackendlessCollection) {
//                            Data.collection = mapBackendlessCollection;
//                            temp.addAll(mapBackendlessCollection.getCurrentPage());
//                            if(temp.size() != 0) {
//                                for (int i = 0; i < temp.size(); i++) {
//                                    Map obj = temp.get(i);
//                                    final Craving craving = new Craving(obj, true);
//                                }
//                            }else {
//                                adapter.notifyDataSetChanged();
//                                Main.hideWait();
//                            }
//                        }
//
//                        @Override
//                        public void handleFault(BackendlessFault backendlessFault) {
//                            Main.hideWait();
//                        }
//                    });
//                }
//            }.run();
//        } else {
//            if(Data.cravings == null) {
//                Data.cravings = new ArrayList<Craving>();
//            }
//            Data.collection.nextPage(new AsyncCallback<BackendlessCollection<Map>>() {
//                @Override
//                public void handleResponse(BackendlessCollection<Map> backendlessCollection) {
//                    Data.collection = backendlessCollection;
//                    temp.addAll(backendlessCollection.getCurrentPage());
//                    if(temp.size() == 0) {
//                        Main.hideWait();
//                    }
//                    for (int i = 0; i < temp.size(); i++) {
//                        Map obj = temp.get(i);
//                        final Craving craving = new Craving(obj, true);
//                    }
//                }
//
//                @Override
//                public void handleFault(BackendlessFault backendlessFault) {
//                    Main.hideWait();
//                }
//            });
//        }
//    }

    public void refresh(final SwipeRefreshLayout s) {
        if(Data.cravings.size() == 0) {
            start.execute(new Void[]{});
        }else {
            new AsyncTask<Void, Void, Void>() {
                ProgressDialog wait;

                @Override
                public void onPreExecute() {
                    if(s == null) {
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
                    if(s == null) {
                        wait.dismiss();
                    }else {
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
                    for(int i = 0; i < temp.size(); i++) {
                        Data.cravings.add(new Craving(temp.get(i)));
                    }
                }catch (BackendlessException e) {
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
        mRecyclerView = (RecyclerView)rootView.findViewById(R.id.cravingList);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new MyAdapter<Craving>(Data.cravings, 'c', getActivity());
        mRecyclerView.setAdapter(mAdapter);

        final SwipeRefreshLayout srl = (SwipeRefreshLayout)rootView.findViewById(R.id.cSwipeRefresh);
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
                        }catch (BackendlessException e) {
                            Log.d("backendless", e.toString());
                        }
                        return null;
                    }

                    @Override
                    public void onPostExecute(Void v) {
                        mAdapter.notifyDataSetChanged();
                    }
                    //TODO: look up if this will work
                }.execute(new Void[]{});
            }
        };
        mRecyclerView.addOnScrollListener(endlessScroll);

        return rootView;
    }

//    public void updateList(boolean dataReady) {
//        ListView listView = (ListView) rootView.findViewById(R.id.cravingListView);
//        adapter = new CravingAdapter(getActivity().getBaseContext(), Data.cravings);
//        listView.setAdapter(adapter);
//        listView.setOnScrollListener(new EndlessScrollListener(5));
//        updateCravings(!dataReady);
//    }
    public void notifyChanges() {
        mAdapter.notifyDataSetChanged();
    }
}
