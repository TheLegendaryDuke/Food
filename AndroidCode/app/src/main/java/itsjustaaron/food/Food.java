package itsjustaaron.food;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Aaron-Work on 8/17/2016.
 */
public class Food {
    public String objectId;
    public String image;
    public String name;
    public String tags;
    public String description;

    public Food(){}

    public Food(Map map) {
        this.objectId = map.get("objectId").toString();
        this.image = map.get("image").toString();
        this.name = map.get("name").toString();
        this.tags = map.get("tags").toString();
        this.description = map.get("description").toString();
    }

    public static List<String> csvToList(String csv) {
        String[] values = csv.split(",");
        List<String> tags = new ArrayList<String>();
        for (int i = 0; i < values.length; i++) {
            String strip = values[i].replaceAll(" ", "");
            tags.add(strip);
        }
        return tags;
    }

    public static String listToCsv(List<String> tags) {
        StringBuilder csv = new StringBuilder();
        for (int i = 0; i < tags.size(); i++) {
            csv.append(tags.get(i));
            if (i != tags.size() - 1) {
                csv.append(",");
            }
        }
        return csv.toString();
    }

    public void save() {
        HashMap map = new HashMap();
        map.put("objectId", objectId);
        map.put("image", image);
        map.put("name", name);
        map.put("tags", tags);
        map.put("description", description);
        Backendless.Persistence.of("foods").save(map, new AsyncCallback<Map>() {
            @Override
            public void handleResponse(Map map) {

            }

            @Override
            public void handleFault(BackendlessFault backendlessFault) {

            }
        });
    }
}
