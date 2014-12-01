package net.atos.entng.bookmark.filters;

import com.mongodb.BasicDBObject;
import com.mongodb.QueryBuilder;

import fr.wseduc.mongodb.MongoQueryBuilder;
import fr.wseduc.webutils.http.Binding;

import org.entcore.common.http.filter.MongoAppFilter;
import org.entcore.common.http.filter.ResourcesProvider;
import org.entcore.common.mongodb.MongoDbConf;
import org.entcore.common.user.UserInfos;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;

public class BookmarkOwnerOnly implements ResourcesProvider  {

	private MongoDbConf conf = MongoDbConf.getInstance();

	@Override
	public void authorize(HttpServerRequest request, Binding binding, UserInfos user, Handler<Boolean> handler) {

		String id = request.params().get("id");
		if (id != null && !id.trim().isEmpty()) {
			BasicDBObject idDBO = new BasicDBObject("_id", id);
			QueryBuilder query = QueryBuilder.start("owner.userId").is(user.getUserId())
					.put("bookmarks").elemMatch(idDBO);
			MongoAppFilter.executeCountQuery(request, conf.getCollection(), MongoQueryBuilder.build(query), 1, handler);
		} else {
			handler.handle(false);
		}

	}

}
