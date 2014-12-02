var bookmarkWidget = model.widgets.findWidget('bookmark');

// Model
function Bookmark(){}

bookmarkWidget.getBookmarks = function() {
	http().get('/bookmark').done(function(response){
		model.bookmarks.load(response.bookmarks);
		bookmarkWidget.bookmarks = model.bookmarks;
		model.widgets.apply('bookmarks');
	});
};

Bookmark.prototype.createBookmark = function() {
	http().postJson('/bookmark', this).done(function(response){
		this.updateData(response);
		model.bookmarks.push(this);
		bookmarkWidget.display.newBookmark = false;
		model.widgets.apply('bookmarks');
	}.bind(this));
};

Bookmark.prototype.updateBookmark = function() {
	var that = this;
	http().putJson('/bookmark/' + this._id, this).done(function(){
		var aBookmark = model.bookmarks.find(function(pBookmark) {
			return pBookmark._id === that._id;
		});
		aBookmark.updateData(that);
		model.widgets.apply('bookmarks');
		bookmarkWidget.editedBookmark = new Bookmark();
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
	bookmarkWidget.display.newBookmark = true;
	bookmarkWidget.bookmark = new Bookmark( { name : "http://" } );
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


// Init
model.makeModel(Bookmark);
model.collection(Bookmark);
bookmarkWidget.getBookmarks();
