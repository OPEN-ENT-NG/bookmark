package net.atos.entng.bookmark.services;

import org.entcore.common.service.CrudService;
import org.entcore.common.user.UserInfos;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;

import fr.wseduc.webutils.Either;

public interface BookmarkService extends CrudService {

	public String newObjectId();

	public void createBookmark(UserInfos user, String newBookmarkId, JsonObject data,
			Handler<Either<String, JsonObject>> handler);

	public void updateBookmark(UserInfos user, String bookmarkId, JsonObject data,
			Handler<Either<String, JsonObject>> handler);

	public void deleteBookmark(UserInfos user, String bookmarkId,
			Handler<Either<String, JsonObject>> handler);

	public void getBookmarks(UserInfos user, Handler<Either<String, JsonObject>> handler);
}
