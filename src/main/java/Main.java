
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;

public class Main {

    static String wao = "wao";

    public static void main(String[] args) throws Exception {
        org.apache.camel.main.Main main = new org.apache.camel.main.Main();
        main.addRouteBuilder(new RouteBuilder() {

            @Override
            public void configure() throws Exception {
                final String env = System.getenv("PORT");

                from("jetty:http://0.0.0.0:" + env).process(new Processor() {

                    public void process(Exchange exchange) throws Exception {
                        exchange.getIn().setBody(wao);
                    }
                });
                from("timer:foo").setBody(constant("wao")).to("websocket://0.0.0.0:25255/page/?sendToAll=true");
                from("timer:foo?period=3s").process(new Processor() {

                    public void process(Exchange exchange) throws Exception {
                        wao += "o\n";
                    }
                });
                from("jetty:http://0.0.0.0:" + env + "/websocket").process(new Processor() {

                    public void process(Exchange exchange) throws Exception {
                        exchange.getIn().setBody("<script>  var ws = new WebSocket('ws://limitless-ocean-7504.herokuapp.com:25255/page/'); ws.onopen =function(){ console.log('wao');} </script>");
                    }
                });
            }
        });
        main.run();
    }
}
