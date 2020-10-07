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

package net.atos.entng.bookmark.controllers;

import static org.entcore.common.http.response.DefaultResponseHandler.defaultResponseHandler;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import net.atos.entng.bookmark.Bookmark;
import net.atos.entng.bookmark.services.BookmarkService;
import net.atos.entng.bookmark.services.BookmarkServiceMongoImpl;

import org.entcore.common.events.EventHelper;
import org.entcore.common.events.EventStore;
import org.entcore.common.events.EventStoreFactory;
import org.entcore.common.mongodb.MongoDbControllerHelper;
import org.entcore.common.user.UserInfos;
import org.entcore.common.user.UserUtils;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;

import fr.wseduc.rs.ApiDoc;
import fr.wseduc.rs.Delete;
import fr.wseduc.rs.Get;
import fr.wseduc.rs.Post;
import fr.wseduc.rs.Put;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import fr.wseduc.webutils.Either;
import fr.wseduc.webutils.I18n;
import fr.wseduc.webutils.request.RequestUtils;

public class BookmarkController extends MongoDbControllerHelper {
	static final String RESOURCE_NAME = "bookmark";
	private final BookmarkService bookmarkService;
	private static final I18n i18n = I18n.getInstance();
	private final EventHelper eventHelper;

	public BookmarkController(String collection) {
		super(collection);
		bookmarkService = new BookmarkServiceMongoImpl(collection);
		final EventStore eventStore = EventStoreFactory.getFactory().getEventStore(Bookmark.class.getSimpleName());
		this.eventHelper = new EventHelper(eventStore);
	}

	/* NB : actionType "AUTHENTICATED" is used instead of "WORKFLOW", so that
	 * application "bookmark" does not appear in the application list
	 */

	@Get("")
	@ApiDoc("Get user's bookmarks")
	@SecuredAction(value = "bookmark.list", type = ActionType.AUTHENTICATED)
	public void listBookmarks(final HttpServerRequest request) {
		UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
			@Override
			public void handle(final UserInfos user) {
				if (user != null) {
					bookmarkService.getBookmarks(user, defaultResponseHandler(request));
				}
			}
		});
	}

	@Post("")
	@ApiDoc("Add a bookmark")
	@SecuredAction(value = "bookmark.create", type = ActionType.AUTHENTICATED)
	public void createBookmark(final HttpServerRequest request) {
		UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
			@Override
			public void handle(final UserInfos user) {
				if (user != null) {
					RequestUtils.bodyToJson(request, pathPrefix + "createOrUpdateBookmark", new Handler<JsonObject>() {
						@Override
						public void handle(JsonObject data) {
							final String newBookmarkId =  bookmarkService.newObjectId();

							String url = data.getString("url");
							if(!isValidURL(url)) {
								String errorMessage = i18n.translate(
										"bookmark.widget.bad.request.invalid.url",
										getHost(request),
										I18n.acceptLanguage(request));
								badRequest(request, errorMessage);
								return;
							}

							bookmarkService.createBookmark(user, newBookmarkId, data, new Handler<Either<String, JsonObject>>() {
								@Override
								public void handle(Either<String, JsonObject> event) {
									if (event.isRight()) {
										// return id of created bookmark
										JsonObject result = new JsonObject();
										result.put("_id", newBookmarkId);
										renderJson(request, result);
										eventHelper.onCreateResource(request, RESOURCE_NAME);
									} else {
										JsonObject error = new JsonObject().put(
												"error", event.left().getValue());
										renderError(request, error);
									}
								}
							});
						}
					});
				}
			}
		});
	}

	@Put("/:id")
	@ApiDoc("Update a bookmark")
	@SecuredAction(value = "", type = ActionType.RESOURCE)
	public void updateBookmark(final HttpServerRequest request) {
		UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
			@Override
			public void handle(final UserInfos user) {
				if (user != null) {
					final String id = request.params().get("id");

					RequestUtils.bodyToJson(request, pathPrefix + "createOrUpdateBookmark", new Handler<JsonObject>() {
						@Override
						public void handle(JsonObject data) {
							String url = data.getString("url");
							if(!isValidURL(url)) {
								String errorMessage = i18n.translate(
										"bookmark.widget.bad.request.invalid.url",
										getHost(request),
										I18n.acceptLanguage(request));
								badRequest(request, errorMessage);
								return;
							}

							bookmarkService.updateBookmark(user, id, data, defaultResponseHandler(request));
						}
					});
				}
			}
		});
	}

	@Delete("/:id")
	@ApiDoc("Delete a bookmark")
	@SecuredAction(value = "", type = ActionType.RESOURCE)
	public void deleteBookmark(final HttpServerRequest request) {
		UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
			@Override
			public void handle(final UserInfos user) {
				if (user != null) {
					final String id = request.params().get("id");
					bookmarkService.deleteBookmark(user, id, defaultResponseHandler(request));
				}
			}
		});
	}

	private static boolean isValidURL(String url) {
		try {
			URL aURL = new URL(url);
			aURL.toURI();
			return true;
		} catch (MalformedURLException e) {
			return false;
		}
		catch (URISyntaxException e) {
			return false;
		}
	}

}
