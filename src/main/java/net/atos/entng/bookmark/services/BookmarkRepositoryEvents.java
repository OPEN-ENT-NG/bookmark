package net.atos.entng.bookmark.services;

import static net.atos.entng.bookmark.Bookmark.BOOKMARK_COLLECTION;

import org.entcore.common.mongodb.MongoDbResult;
import org.entcore.common.user.RepositoryEvents;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;

import com.mongodb.QueryBuilder;

import fr.wseduc.mongodb.MongoDb;
import fr.wseduc.mongodb.MongoQueryBuilder;
import fr.wseduc.webutils.Either;

public class BookmarkRepositoryEvents implements RepositoryEvents {

	private static final Logger log = LoggerFactory.getLogger(BookmarkRepositoryEvents.class);
	private final MongoDb mongo = MongoDb.getInstance();

	@Override
	public void exportResources(String exportId, String userId,
			JsonArray groups, String exportPath, String locale, String host, final Handler<Boolean> handler) {
		log.warn("Method exportResources is not implemented in BookmarkRepositoryEvents");
	}

	@Override
	public void deleteGroups(JsonArray groups) {
		log.warn("Method deleteGroups is not implemented in BookmarkRepositoryEvents");
	}

	@Override
	public void deleteUsers(JsonArray users) {

		if(users == null || users.size() == 0) {
			log.warn("JsonArray users is null or empty");
			return;
		}

		final String [] userIds = new String[users.size()];
		for (int i = 0; i < users.size(); i++) {
			JsonObject j = users.get(i);
			userIds[i] = j.getString("id");
		}

		final JsonObject criteria = MongoQueryBuilder.build(QueryBuilder.start("owner.userId").in(userIds));

		mongo.delete(BOOKMARK_COLLECTION, criteria,
				MongoDbResult.validActionResultHandler(new Handler<Either<String,JsonObject>>() {
					@Override
					public void handle(Either<String, JsonObject> event) {
						if(event.isLeft()) {
							log.error("Error when deleting bookmarks : "+ event.left());
						}
						else {
							String message = "Delete bookmarks successful : ";
							if(event.right().getValue() != null) {
								message += event.right().getValue().toString();
							}
							log.info(message);
						}
					}
				})
		);

	}

}
