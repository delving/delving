package eu.delving.metarepo;

import com.mongodb.DB;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * The controller for the metadata repository
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

@Controller
public class MetaRepoController {

    @Value("#{metarepo}")
    private DB metaRepo;

    @RequestMapping("index.html")
    public @ResponseBody String list() {
        StringBuilder out = new StringBuilder("MetaRepo Collections:\n");
        for (String name : metaRepo.getCollectionNames()) {
            out.append(name).append('\n');
        }
        return out.toString();
    }

    @RequestMapping("/submit/{collectionId}")
    public void submit(
        @PathVariable String collectionId   
    ) {

    }

}
