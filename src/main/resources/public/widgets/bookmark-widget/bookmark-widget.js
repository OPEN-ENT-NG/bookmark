var bookmarkWidget = model.widgets.findWidget('bookmark-widget');

// Model
function Bookmark(){}

var getBookmarks = function() {
	http().get('/bookmark').done(function(response){
		if(response && response.bookmarks) {
			model.bookmarks.load(response.bookmarks);
		}
		bookmarkWidget.bookmarks = model.bookmarks;
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
		this.updateData(response);
		model.bookmarks.push(this);
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
		var aBookmark = model.bookmarks.find(function(pBookmark) {
			return pBookmark._id === that._id;
		});
		aBookmark.updateData(that);
		model.widgets.apply('bookmarks');
		bookmarkWidget.editedBookmark = new Bookmark();
	}).e400(function(response){
		notify.error(JSON.parse(response.responseText).error);
	});
};

Bookmark.prototype.deleteBookmark = function() {
	http().delete('/bookmark/' + this._id).done(function(){
		model.bookmarks.remove(this);
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


// Init
model.makeModel(Bookmark);
model.collection(Bookmark);
getBookmarks();
