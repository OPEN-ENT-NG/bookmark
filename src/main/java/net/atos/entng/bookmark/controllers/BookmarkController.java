package net.atos.entng.bookmark.controllers;

import static org.entcore.common.http.response.DefaultResponseHandler.defaultResponseHandler;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import net.atos.entng.bookmark.services.BookmarkService;
import net.atos.entng.bookmark.services.BookmarkServiceMongoImpl;

import org.entcore.common.mongodb.MongoDbControllerHelper;
import org.entcore.common.user.UserInfos;
import org.entcore.common.user.UserUtils;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;

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

	private final BookmarkService bookmarkService;
	private static final I18n i18n = I18n.getInstance();

	public BookmarkController(String collection) {
		super(collection);
		bookmarkService = new BookmarkServiceMongoImpl(collection);
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
										result.putString("_id", newBookmarkId);
										renderJson(request, result);
									} else {
										JsonObject error = new JsonObject().putString(
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
	@SecuredAction(value = "bookmark.update", type = ActionType.RESOURCE)
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
	@SecuredAction(value = "bookmark.delete", type = ActionType.RESOURCE)
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
