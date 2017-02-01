package itsjustaaron.food;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
                    Log.d("date", String.valueOf(Data.foodOffers.get(i).expire.getTime()));
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


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab_offer, container, false);
        Data.foodOffers = new ArrayList<>();
        myAdapter = new MyAdapter<>(Data.foodOffers, 'o', getActivity());
        RecyclerView recyclerView = (RecyclerView)rootView.findViewById(R.id.offerList);
        recyclerView.setAdapter(myAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        return rootView;
    }

    public void start() {
        if(!started) {
            started = true;
            new Start().execute(new Void[]{});
        }
    }
}
