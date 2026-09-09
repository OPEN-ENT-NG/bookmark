/*
 * Copyright © Région Nord Pas de Calais-Picardie,  Département 91, Région Aquitaine-Limousin-Poitou-Charentes, 2016.
 *
 * This file is part of OPEN ENT NG. OPEN ENT NG is a versatile ENT Project based on the JVM and ENT Core Project.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation (version 3 of the License).
 *
 * For the sake of explanation, any module that communicate over native
 * Web protocols, such as HTTP, with OPEN ENT NG is outside the scope of this
 * license and could be license under its own terms. This is merely considered
 * normal use of OPEN ENT NG, and does not fall under the heading of "covered work".
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package net.atos.entng.bookmark.services;

import static org.entcore.common.mongodb.MongoDbResult.validActionResultHandler;
import static org.entcore.common.mongodb.MongoDbResult.validResultHandler;

import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.entcore.common.service.impl.MongoDbCrudService;
import org.entcore.common.user.UserInfos;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import com.mongodb.BasicDBObject;

import fr.wseduc.mongodb.MongoDb;
import fr.wseduc.mongodb.MongoQueryBuilder;
import fr.wseduc.mongodb.MongoUpdateBuilder;
import fr.wseduc.webutils.Either;

import static com.mongodb.client.model.Filters.*;

public class BookmarkServiceMongoImpl extends MongoDbCrudService implements BookmarkService {

	public BookmarkServiceMongoImpl(String collection) {
		super(collection);
	}

	@Override
	public String newObjectId() {
		return new ObjectId().toString();
	}

	@Override
	public void createBookmark(UserInfos user, String newBookmarkId, JsonObject data,
			Handler<Either<String, JsonObject>> handler) {

		// Only one document per user, containing all user's bookmarks
		Bson query = eq("owner.userId", user.getUserId());

		// Create document with array "bookmarks", or add bookmark to the array if it already exists
		JsonObject newBookmark = new JsonObject();
		newBookmark.put("_id", newBookmarkId)
			.put("name", data.getString("name"))
			.put("url", data.getString("url"));

		MongoUpdateBuilder modifier = new MongoUpdateBuilder();
		modifier.push("bookmarks", newBookmark);

		JsonObject now = MongoDb.now();
		modifier.set("modified", now);

		JsonObject owner = new JsonObject()
			.put("userId", user.getUserId())
			.put("displayName", user.getUsername());

		JsonObject update = modifier.build();
		update.put("$setOnInsert",
				new JsonObject().put("created", now)
					.put("owner", owner));

		mongo.update(collection, MongoQueryBuilder.build(query),
				update, true, false,
				validActionResultHandler(handler));

	}

	@Override
	public void createBookmarkWithLimit(UserInfos user, String newBookmarkId, JsonObject data,
			Handler<Either<String, JsonObject>> handler) {

		// Only one document per user, containing all user's bookmarks
		Bson query = eq("owner.userId", user.getUserId());

		JsonObject now = MongoDb.now();
		JsonObject owner = new JsonObject()
			.put("userId", user.getUserId())
			.put("displayName", user.getUsername());

		// Create the user's document if needed, so that adding the bookmark never needs an upsert
		JsonObject create = new JsonObject().put("$setOnInsert",
				new JsonObject().put("created", now)
					.put("owner", owner)
					.put("bookmarks", new JsonArray()));

		mongo.update(collection, MongoQueryBuilder.build(query),
				create, true, false,
				validActionResultHandler(event -> {
					if (event.isLeft()) {
						handler.handle(event);
						return;
					}
					pushBookmark(user, newBookmarkId, data, now, handler);
				}));
	}

	/*
	 * Add the bookmark to the user's document, unless the limit is already reached.
	 * The limit is part of the update query so that the check and the insertion are a single
	 * atomic operation : "bookmarks.<MAX-1>" exists as soon as the array holds MAX elements,
	 * hence concurrent creations cannot exceed the limit.
	 */
	private void pushBookmark(UserInfos user, String newBookmarkId, JsonObject data, JsonObject now,
			Handler<Either<String, JsonObject>> handler) {

		Bson query = and(eq("owner.userId", user.getUserId()),
				exists("bookmarks." + (MAX_BOOKMARKS_PER_USER - 1), false));

		JsonObject newBookmark = new JsonObject();
		newBookmark.put("_id", newBookmarkId)
			.put("name", data.getString("name"))
			.put("url", data.getString("url"));

		MongoUpdateBuilder modifier = new MongoUpdateBuilder();
		modifier.push("bookmarks", newBookmark)
				.set("modified", now);

		mongo.update(collection, MongoQueryBuilder.build(query),
				modifier.build(), false, false,
				validActionResultHandler(event -> {
					if (event.isRight() && event.right().getValue().getInteger("number", 0) == 0) {
						// No document updated : the user already reached the limit
						handler.handle(new Either.Left<>(LIMIT_REACHED_ERROR));
						return;
					}
					handler.handle(event);
				}));
	}

	@Override
	public void updateBookmark(UserInfos user, String bookmarkId, JsonObject data,
			Handler<Either<String, JsonObject>> handler) {

		// Query
		BasicDBObject idDBO = new BasicDBObject("_id", bookmarkId);
		Bson query = and(eq("owner.userId", user.getUserId()),
				elemMatch("bookmarks", idDBO));

		// Update
		MongoUpdateBuilder modifier = new MongoUpdateBuilder();
		JsonObject now = MongoDb.now();
		modifier.set("bookmarks.$.name", data.getString("name"))
				.set("bookmarks.$.url", data.getString("url"))
				.set("modified", now);

		mongo.update(collection, MongoQueryBuilder.build(query),
				modifier.build(),
				validActionResultHandler(handler));
	}


	@Override
	public void deleteBookmark(UserInfos user, String bookmarkId,
			Handler<Either<String, JsonObject>> handler) {

		// Query
		BasicDBObject idDBO = new BasicDBObject("_id", bookmarkId);
		Bson query = and(eq("owner.userId", user.getUserId()),
				elemMatch("bookmarks", idDBO));

		// Update
		MongoUpdateBuilder modifier = new MongoUpdateBuilder();
		JsonObject now = MongoDb.now();

		modifier.pull("bookmarks", new JsonObject().put("_id", bookmarkId))
				.set("modified", now);

		mongo.update(collection, MongoQueryBuilder.build(query),
				modifier.build(),
				validActionResultHandler(handler));
	}

	@Override
	public void getBookmarks(UserInfos user, Handler<Either<String, JsonObject>> handler) {
		Bson query = eq("owner.userId", user.getUserId());

		mongo.findOne(collection, MongoQueryBuilder.build(query),
				validResultHandler(handler));
	}

}
