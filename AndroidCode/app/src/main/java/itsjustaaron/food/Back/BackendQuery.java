package itsjustaaron.food.Back;

import com.backendless.BackendlessCollection;

import java.util.ArrayList;

/**
 * Created by aozhang on 2/13/2017.
 */

//TODO: use this to replace all BackendlessCollections
public class BackendQuery<T> {
    private BackendlessCollection<T> data;
    public BackendQuery(BackendlessCollection<T> data){
        this.data = data;
    }

    public ArrayList getCurPage() {
        return new ArrayList(data.getCurrentPage());
    }
}
