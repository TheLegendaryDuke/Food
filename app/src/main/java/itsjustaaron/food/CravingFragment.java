package itsjustaaron.food;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.BackendlessDataQuery;
import com.backendless.persistence.QueryOptions;

import java.io.ByteArrayOutputStream;
import java.io.Console;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Created by Aaron-Work on 8/7/2016.
 */
public class CravingFragment extends Fragment {

    public class EndlessScrollListener implements AbsListView.OnScrollListener {

        private int visibleThreshold = 5;
        private int currentPage = 0;
        private int previousTotal = 0;
        private boolean loading = true;

        public EndlessScrollListener() {
        }
        public EndlessScrollListener(int visibleThreshold) {
            this.visibleThreshold = visibleThreshold;
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                             int visibleItemCount, int totalItemCount) {
            if (loading) {
                if (totalItemCount > previousTotal) {
                    loading = false;
                    previousTotal = totalItemCount;
                    currentPage++;
                }
            }
            if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
                updateCravings(false);
                loading = true;
            }
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
        }
    }

    public View rootView;

    public class CravingAdapter extends ArrayAdapter<Craving> {
        private final Context context;
        public final List<Craving> values;

        private View.OnClickListener listener;

        public CravingAdapter(Context context, List<Craving> values) {
            super(context, -1, values);
            if(values == null) {
                values = new ArrayList<Craving>();
            }
            this.context = context;
            this.values = values;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.craving_list_item, parent, false);
            final ImageView image = (ImageView)rowView.findViewById(R.id.cravingItemImage);
            TextView description = (TextView)rowView.findViewById(R.id.cravingItemDescription);
            TextView tags = (TextView)rowView.findViewById(R.id.cravingItemTags);
            final ImageView likeOrNot = (ImageView)rowView.findViewById(R.id.cravingFollowingOrNot);
            final TextView count = (TextView)rowView.findViewById(R.id.cravingFollowerCount);
            final Craving craving = values.get(position);
            final String imagePath = getActivity().getFilesDir() + "/foods/" + craving.food.image;
            final File file = new File(imagePath);
            if(file.exists()) {
                image.setImageBitmap(BitmapFactory.decodeFile(imagePath));
            }else {
                File dir = new File(getActivity().getFilesDir() + "/foods/");
                if(!dir.exists()) {
                    dir.mkdir();
                }
                final String path = "https://api.backendless.com/0020F1DC-E584-AD36-FF74-6D3E9E917400/v1/files/foods/" + craving.food.image;
                new Thread() {
                    public void run() {
                        try {
                            file.createNewFile();
                            URL url = new URL(path);
                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                            conn.setDoInput(true);
                            conn.connect();
                            InputStream is = conn.getInputStream();
                            Bitmap bm = BitmapFactory.decodeStream(is);
                            FileOutputStream fos = new FileOutputStream(imagePath);
                            ByteArrayOutputStream outstream = new ByteArrayOutputStream();
                            bm.compress(Bitmap.CompressFormat.PNG, 100, outstream);
                            byte[] byteArray = outstream.toByteArray();
                            fos.write(byteArray);
                            fos.close();
                        }
                        catch (Exception e) {
                        }
                    }
                }.start();
            }
            description.setText(craving.food.name);
            tags.setText(craving.food.tags);
            if(craving.following) {
                likeOrNot.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_favorite_black_48dp, null));
            }else {
                likeOrNot.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_favorite_border_black_48dp, null));
            }
            likeOrNot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    craving.followSwitch();
                    if(craving.following) {
                        likeOrNot.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_favorite_black_48dp, null));
                        count.setText(String.valueOf(Integer.parseInt(count.getText().toString()) + 1));
                    }else {
                        likeOrNot.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_favorite_border_black_48dp, null));
                        count.setText(String.valueOf(Integer.parseInt(count.getText().toString()) - 1));
                    }
                }
            });
            listener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent next = new Intent(getActivity().getBaseContext(), CravingDetails.class);
                    next.putExtra("cravingID", craving.objectId);
                    startActivity(next);
                }
            };
            image.setOnClickListener(listener);
            description.setOnClickListener(listener);
            count.setText(String.valueOf(craving.numFollowers));
            image.setImageBitmap(BitmapFactory.decodeFile(imagePath));
            return rowView;
        }
    }

    public CravingAdapter adapter;

    public void updateCravings(boolean reload) {
        Main.showWait();
        final List<Map> temp = new ArrayList<>();
        if (reload) {
            final BackendlessDataQuery backendlessDataQuery = new BackendlessDataQuery();
            QueryOptions queryOptions = new QueryOptions();
            queryOptions.setOffset(0);
            queryOptions.setPageSize(Data.loadCount);
            backendlessDataQuery.setQueryOptions(queryOptions);
            new Thread() {
                public void run() {
                    Backendless.Data.of("Craving").find(backendlessDataQuery, new AsyncCallback<BackendlessCollection<Map>>() {
                        @Override
                        public void handleResponse(BackendlessCollection<Map> mapBackendlessCollection) {
                            Data.collection = mapBackendlessCollection;
                            temp.addAll(mapBackendlessCollection.getCurrentPage());
                            if(temp.size() != 0) {
                                for (int i = 0; i < temp.size(); i++) {
                                    Map obj = temp.get(i);
                                    final Craving craving = new Craving(obj, true);
                                }
                            }else {
                                adapter.notifyDataSetChanged();
                                Main.hideWait();
                            }
                        }

                        @Override
                        public void handleFault(BackendlessFault backendlessFault) {
                            Main.hideWait();
                        }
                    });
                }
            }.run();
        } else {
            if(Data.cravings == null) {
                Data.cravings = new ArrayList<Craving>();
            }else {
                Data.cravings.clear();
            }
            Data.collection.nextPage(new AsyncCallback<BackendlessCollection<Map>>() {
                @Override
                public void handleResponse(BackendlessCollection<Map> backendlessCollection) {
                    Data.collection = backendlessCollection;
                    temp.addAll(backendlessCollection.getCurrentPage());
                    if(temp.size() == 0) {
                        Main.hideWait();
                    }
                    for (int i = 0; i < temp.size(); i++) {
                        Map obj = temp.get(i);
                        final Craving craving = new Craving(obj, true);
                    }
                }

                @Override
                public void handleFault(BackendlessFault backendlessFault) {
                    Main.hideWait();
                }
            });
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(Data.cravings == null) {
            Data.cravings = new ArrayList<Craving>();
        }
        rootView = inflater.inflate(R.layout.tab_craving, container, false);
        updateList(false);
        return rootView;
    }

    public void updateList(boolean dataReady) {
        ListView listView = (ListView) rootView.findViewById(R.id.cravingListView);
        adapter = new CravingAdapter(getActivity().getBaseContext(), Data.cravings);
        listView.setAdapter(adapter);
        listView.setOnScrollListener(new EndlessScrollListener(5));
        updateCravings(!dataReady);
    }
    public void notifyChanges() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
    }
}
