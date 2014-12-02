var bookmarkWidget = model.widgets.findWidget('bookmark');

// Model
function Bookmark(){}

bookmarkWidget.getBookmarks = function() {
	http().get('/bookmark').done(function(response){
		bookmarkWidget.bookmarks = response.bookmarks;
		model.widgets.apply();
	});
};

Bookmark.prototype.createBookmark = function() {
	http().postJson('/bookmark', this).done(function(response){
		this.updateData(response);
		bookmarkWidget.bookmarks.push(this);
		bookmarkWidget.display.newBookmark = false;
		model.widgets.apply('bookmarks');
	}.bind(this));
};

Bookmark.prototype.updateBookmark = function() {
	http().putJson('/bookmark/' + this._id, this).done(function(){
		
	});
};

Bookmark.prototype.deleteBookmark = function() {
	http().delete('/bookmark/' + this._id).done(function(){
		bookmarkWidget.bookmarks.remove(this);
	}.bind(this));
};


// Controller
bookmarkWidget.newBookmark = function() {
	if(!bookmarkWidget.display) {
		bookmarkWidget.display = {};
	}
	bookmarkWidget.display.newBookmark = true;
	bookmarkWidget.bookmark = new Bookmark();
};

bookmarkWidget.manageBookmarks = function() {
	if(!bookmarkWidget.display) {
		bookmarkWidget.display = {};
	}
	bookmarkWidget.display.manage = true;	
};



// Init
model.makeModel(Bookmark);
bookmarkWidget.getBookmarks();