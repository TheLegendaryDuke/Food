package itsjustaaron.food.Model;

import com.backendless.BackendlessUser;

import java.util.Map;

/**
 * Created by aozhang on 3/17/2017.
 */

public class Offerer {
    public String objectId;
    public String tags;
    public double score;
    public String userID;
    public String description;

    public Offerer(Map map) {
        this.objectId = map.get("objectId").toString();
        this.tags = map.get("tags").toString();
        this.score = Double.parseDouble(map.get("score").toString());
        this.userID = map.get("userID").toString();
        this.description = map.get("description").toString();
    }
}
