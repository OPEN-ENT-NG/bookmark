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

package net.atos.entng.bookmark;


import net.atos.entng.bookmark.controllers.BookmarkController;
import net.atos.entng.bookmark.filters.BookmarkOwnerOnly;
import net.atos.entng.bookmark.services.BookmarkRepositoryEvents;

import org.entcore.common.http.BaseServer;
import org.entcore.common.mongodb.MongoDbConf;

import io.vertx.core.Promise;

public class Bookmark extends BaseServer {

	public final static String BOOKMARK_COLLECTION = "bookmark";

	@Override
	public void start(Promise<Void> startPromise) throws Exception {
		super.start(startPromise);

		// Set RepositoryEvents implementation used to process events published for transition
		setRepositoryEvents(new BookmarkRepositoryEvents());

		addController(new BookmarkController(BOOKMARK_COLLECTION));
		MongoDbConf.getInstance().setCollection(BOOKMARK_COLLECTION);
		setDefaultResourceFilter(new BookmarkOwnerOnly());
		startPromise.tryComplete();
	}

}
