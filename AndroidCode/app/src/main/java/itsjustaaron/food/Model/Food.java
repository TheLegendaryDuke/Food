package itsjustaaron.food.Model;

import java.util.ArrayList;
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

    public Food() {
    }

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
}
