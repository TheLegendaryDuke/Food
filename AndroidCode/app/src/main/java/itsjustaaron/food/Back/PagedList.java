package itsjustaaron.food.Back;

import com.backendless.BackendlessCollection;

import java.util.ArrayList;

/**
 * Created by aozhang on 2/13/2017.
 */

//TODO: use this to replace all BackendlessCollections
public class PagedList<T> {
    private BackendlessCollection<T> data;
    public PagedList(BackendlessCollection<T> data){
        this.data = data;
    }

    public ArrayList<T> getCurPage() {
        return new ArrayList(data.getCurrentPage());
    }
}
