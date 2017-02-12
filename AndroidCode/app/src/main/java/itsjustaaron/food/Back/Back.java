package itsjustaaron.food.Back;

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.persistence.BackendlessDataQuery;

import java.util.Map;

import itsjustaaron.food.Craving;
import itsjustaaron.food.Food;
import itsjustaaron.food.FoodOffer;
import itsjustaaron.food.Offer;

/**
 * Created by Aaron-Home on 2017-02-11.
 */

public class Back {
    public static enum object {
        food, foodoffer, offer, craving, cravingfollower
    }

    public static Object getObjectByID(String id, object object) {
        switch (object) {
            case food:
                return new Food(Backendless.Persistence.of("foods").findById(id));
        }
        return null;
    }

    public static BackendlessCollection<Map> findObjectByWhere(String where, object object) {
        BackendlessDataQuery dataQuery = new BackendlessDataQuery();
        dataQuery.setWhereClause(where);
        switch (object) {
            case cravingfollower:
                return Backendless.Persistence.of("cravingFollowers").find(dataQuery);
        }
        return null;
    }
}
