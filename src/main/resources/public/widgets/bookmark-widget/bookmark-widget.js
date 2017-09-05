var bookmarkWidget = model.widgets.findWidget('bookmark-widget');

// Model
function Bookmark(data){
	for(var prop in data){
		this[prop] = data[prop];
	}
}

var getBookmarks = function() {
	http().get('/bookmark').done(function(response){
		if(response && response.bookmarks) {
			bookmarkWidget.bookmarks = _.map(response.bookmarks, function(b){ return new Bookmark(b) });
		}else{
            bookmarkWidget.bookmarks = [];
		}
		model.widgets.apply('bookmarks');
	});
};

Bookmark.prototype.createBookmark = function() {
	if (isEmpty(this.name)) {
		notify.error('bookmark.widget.form.name.is.empty');
		return;
	}
	if (isEmpty(this.url)) {
		notify.error('bookmark.widget.form.url.is.empty');
		return;
	}

	http().postJson('/bookmark', this).done(function(response){
		for(var prop in response){
			this[prop] = response[prop];
		}
		bookmarkWidget.bookmarks.push(this);
		bookmarkWidget.display.newBookmark = false;
		model.widgets.apply('bookmarks');
	}.bind(this))
	.e400(function(response){
		notify.error(JSON.parse(response.responseText).error);
	});
};

Bookmark.prototype.updateBookmark = function() {
	if (isEmpty(this.name)) {
		notify.error('bookmark.widget.form.name.is.empty');
		return;
	}
	if (isEmpty(this.url)) {
		notify.error('bookmark.widget.form.url.is.empty');
		return;
	}

	var that = this;
	http().putJson('/bookmark/' + this._id, this).done(function(){
		var aBookmark = bookmarkWidget.bookmarks.find(function(pBookmark) {
			return pBookmark._id === that._id;
		});
		for(var prop in that){
			aBookmark[prop] = that[prop];
		}
		model.widgets.apply('bookmarks');
		bookmarkWidget.editedBookmark = new Bookmark();
	}).e400(function(response){
		notify.error(JSON.parse(response.responseText).error);
	});
};

Bookmark.prototype.deleteBookmark = function() {
	http().delete('/bookmark/' + this._id).done(function(){
		var index = bookmarkWidget.bookmarks.indexOf(this);
		bookmarkWidget.bookmarks.splice(index, 1);
		model.widgets.apply('bookmarks');
	}.bind(this));
};

Bookmark.prototype.toJSON = function() {
	var json = {
			name : this.name,
			url : this.url
		};
	return json;
};


// Controller
bookmarkWidget.newBookmark = function() {
	if(!bookmarkWidget.display) {
		bookmarkWidget.display = {};
	}
	bookmarkWidget.bookmark = new Bookmark( { url : "http://" } );
	bookmarkWidget.display.newBookmark = true;
};

bookmarkWidget.manageBookmarks = function() {
	if(!bookmarkWidget.display) {
		bookmarkWidget.display = {};
	}
	bookmarkWidget.display.manage = true;
};

bookmarkWidget.editBookmark = function(bookmark) {
	bookmarkWidget.editedBookmark = angular.copy(bookmark);
};

bookmarkWidget.cancelEdit = function() {
	bookmarkWidget.editedBookmark = new Bookmark();
};

bookmarkWidget.stopPropagation = function(event) {
	if(event && event.stopPropagation) {
		event.stopPropagation();
	}
};

var isEmpty = function(string){
	return (!string || string.trim().length === 0);
};

getBookmarks();
