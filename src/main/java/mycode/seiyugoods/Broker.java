package mycode.seiyugoods;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import mycode.seiyugoods.source.callable.AmiamiItems;
import mycode.seiyugoods.source.callable.AmiamiTitles;
import mycode.seiyugoods.source.callable.CategoryAndTemplateSeiyu;
import mycode.seiyugoods.source.callable.PersistentAmiamiTitles;
import mycode.seiyugoods.source.callable.PersistentSeiyu;
import mycode.seiyugoods.source.polling.Amiami;
import mycode.seiyugoods.source.polling.SeiyuCategoryMembers;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Broker extends RouteBuilder {
    
    @Autowired
    BrokerBuilder builder;
    public static final Map<Class, Long> allSourceTimeStamp = new HashMap<>();
    @Autowired
    FileRoute fileRoute;
    
    @Override
    public void configure() throws Exception {
        init();
        String port = System.getenv("PORT");
        if (port == null) {
            port = "80";
        }
        String siteTopUrl = "http://0.0.0.0:" + port;        
        from("jetty:" + siteTopUrl + "/api/seiyu_name").to("direct:allSeiyuName");
        from("jetty:" + siteTopUrl + "/api/search").to("direct:search");
        from("jetty:" + siteTopUrl + "/").process((exchange)->exchange.getIn().setBody(fileRoute.getBody()));
        
        from("seda:broker.notate")
                .process((exchange) -> {
                    Class body = exchange.getIn().getBody(Class.class);
                    System.out.println("<<< " + body.getSimpleName() + " updated.");
                    Set<Class> needFor = builder.getNeedFor(body);
                    if (needFor != null) {
                        List<String> collect = needFor.stream()
                        .map((clazz) -> {
                            System.out.println(">>> try to " + clazz.getSimpleName());
                            return clazz;
                        })
                        .map((clazz) -> "seda:" + clazz.getName())
                        .collect(Collectors.toList());
                        exchange.getIn().setBody(collect);
                    } else {
                        exchange.getIn().setBody(new ArrayList<>());
                    }
                }).split().body(List.class)
                .process((exchange) -> {
                    exchange.getIn().setHeader("slip", exchange.getIn().getBody(String.class));
                    exchange.getIn().setBody(allSourceTimeStamp);
                })
                .routingSlip(simple("${header.slip}"));
        
    }
    
    public void init() {
        builder.from(SeiyuCategoryMembers.class)
                .to(CategoryAndTemplateSeiyu.class);
        builder.from(CategoryAndTemplateSeiyu.class)
                .to(PersistentSeiyu.class);
        builder.from(Amiami.class)
                .to(AmiamiTitles.class)
                .to(AmiamiItems.class);
        builder.from(AmiamiTitles.class)
                .to(PersistentAmiamiTitles.class);
    }
    
    public static boolean isUpToDate(Map<Class, Long> sourceMap) {
        return sourceMap.isEmpty() || sourceMap.keySet().stream()
                .allMatch((key)
                        -> Objects.equals(sourceMap.get(key), allSourceTimeStamp.get(key)));
    }
}
