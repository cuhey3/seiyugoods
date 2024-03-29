package mycode.seiyugoods.source.polling;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mycode.seiyugoods.source.PollingSource;
import mycode.seiyugoods.util.WikiParse;
import org.springframework.stereotype.Component;

@Component
public class SeiyuCategoryMembers extends PollingSource {

    public SeiyuCategoryMembers() {
        period = 60 * 60;
    }

    @Override
    public Map<String, Object> poll() throws Exception {
        Map<String, Object> sourceFields = new HashMap<>();
        WikiParse parse = new WikiParse();
        parse.setParam("action=query&list=categorymembers&cmtitle=Category:%E6%97%A5%E6%9C%AC%E3%81%AE%E5%A5%B3%E6%80%A7%E5%A3%B0%E5%84%AA&cmlimit=500&cmnamespace=0&format=xml&continue=&cmprop=title|ids|sortkeyprefix");
        parse.setList("categorymembers");
        parse.setMap("cm");
        parse.setContinueElement("cmcontinue");
        List<Map<String, String>> mapList = parse.getMapList();
        parse = new WikiParse();
        parse.setParam("action=query&list=categorymembers&cmtitle=Category:%E6%97%A5%E6%9C%AC%E3%81%AE%E7%94%B7%E6%80%A7%E5%A3%B0%E5%84%AA&&cmlimit=500&cmnamespace=0&format=xml&continue=&cmprop=title|ids|sortkeyprefix");
        parse.setList("categorymembers");
        parse.setMap("cm");
        parse.setContinueElement("cmcontinue");
        mapList.addAll(parse.getMapList());
        sourceFields.put("mapList", mapList);
        return sourceFields;
    }
}
