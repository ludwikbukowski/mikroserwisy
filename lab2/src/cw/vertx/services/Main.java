package cw.vertx.services;


import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import tools.LoaderAnalyzer;
import java.util.*;

public class Main extends AbstractVerticle {

	@Override
	public void start() throws Exception {

		Router router = Router.router(vertx);
		HttpClient client = vertx.createHttpClient();

		router.route("/vertx/main/:yr/:mt/:dy").handler(routingContext -> {
			String year = routingContext.request().getParam("yr");
			String month = routingContext.request().getParam("mt");
			String day = routingContext.request().getParam("dy");

			process(year, month, day, client, routingContext);

		});
		vertx.createHttpServer().requestHandler(router::accept).listen(8081);
	}

	private void process(String y, String m, String d, HttpClient client, RoutingContext routingContext){
		String date = y+"/"+m+"/"+d;
		client.getNow(8080, "localhost", "/cw-netkernel/nasa/" + date,
				response -> {
					response.bodyHandler( buffer-> {
						String result = buffer.toString(); // odpowiedź z mikroserwisu
						applyWikipedia(result, client, routingContext);

					});
				});
	}


	private void replaceLinks(String text, StringBuilder str, ArrayList<int[]> indexes,
								HttpClient client, RoutingContext routingContext){

		if(indexes.size() == 0){
			routingContext
					.response()
					.putHeader("content-type", "text/html")
					.end( str.toString());
		}else {
			String with_spaces = text.substring(indexes.get(0)[0], indexes.get(0)[1]);
			String word = with_spaces.replace(' ', '_');

			client.getNow(8080, "localhost", "/cw-netkernel/wikimedia/" + word,
					response -> {
						response.bodyHandler(buffer -> {
							String wikires = buffer.toString(); // odpowiedź z mikroserwisu
							str.replace(indexes.get(0)[0],indexes.get(0)[1], "<a href=\"" + wikires + "\">" + with_spaces + "</a>");
							indexes.remove(0);
							replaceLinks(text, str, indexes, client, routingContext);
						});
					});

		}
	}



	private void applyWikipedia(String text, HttpClient client, RoutingContext routingContext){
		ArrayList<int[]> tabs = LoaderAnalyzer.findProperNames(text);
		StringBuilder str = new StringBuilder(text);
		replaceLinks(text, str, tabs, client, routingContext);
	}
}