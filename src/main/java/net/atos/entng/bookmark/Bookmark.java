package net.atos.entng.bookmark;


import net.atos.entng.bookmark.controllers.BookmarkController;
import net.atos.entng.bookmark.filters.BookmarkOwnerOnly;

import org.entcore.common.http.BaseServer;
import org.entcore.common.mongodb.MongoDbConf;

public class Bookmark extends BaseServer {

	public final static String BOOKMARK_COLLECTION = "bookmark";

	@Override
	public void start() {
		super.start();
		addController(new BookmarkController(BOOKMARK_COLLECTION));
		MongoDbConf.getInstance().setCollection(BOOKMARK_COLLECTION);
		setDefaultResourceFilter(new BookmarkOwnerOnly());
	}

}
