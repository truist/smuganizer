package com.rainskit.smuganizer.smugmugapiwrapper;

import com.rainskit.smuganizer.Main;

public interface SmugAPIConstants {
	static final String API_VERSION = "1.2.0";
	static final String API_URL = "https://api.smugmug.com/hack/json/" + API_VERSION + "/";
	static final String API_UPLOAD_URL = "http://upload.smugmug.com/";
	static final String API_KEY = "aR8ks0WWmboWAcclI9poAboELIqNj3wW";
	static final String API_TYPE = "JSON";

	static final String USER_AGENT = "Smuganizer/" + Main.VERSION;
	
	static final String LOGIN_WITH_PASSWORD = "smugmug.login.withPassword";
	static final String USER_NAME = "EmailAddress";
	static final String PASSWORD = "Password";

	static final String GET_TREE = "smugmug.users.getTree";
	static final String GET_CATEGORIES = "smugmug.categories.get";
	static final String CATEGORIES_ARRAY = "Categories";

	static final String GET_SUBCATEGORIES = "smugmug.subcategories.get";
	static final String SUBCATEGORIES_ARRAY = "SubCategories";
	static final String SUBCATEGORY_OBJECT = "SubCategory";
	static final String SUBCATEGORY_ID = "id";

	static final String CREATE_CATEGORY = "smugmug.categories.create";
	static final String CATEGORY_OBJECT = "Category";
	static final String CATEGORY_NAME = "Name";
	static final String CATEGORY_TITLE = "Title";
	static final String CATEGORY_ID = "id";
	static final String RENAME_CATEGORY = "smugmug.categories.rename";
	static final String CATEGORY_ACTION_ID = "CategoryID";
	static final String DELETE_CATEGORY = "smugmug.categories.delete";

	static final String CREATE_SUBCATEGORY = "smugmug.subcategories.create";
	static final String SUBCATEGORY_ACTION_ID = "SubCategoryID";
	static final String SUBCATEGORY_NAME = "Name";

	static final String CREATE_ALBUM = "smugmug.albums.create";
	static final String ALBUM_TITLE = "Title";
	static final String ALBUM_DESCRIPTION = "Description";
	static final String ALBUM_ID = "id";
	static final String ALBUM_POSITION = "Position";
	static final String ALBUM_PASSWORD = "Password";
	static final String ALBUM_PASSWORD_HINT = "PasswordHint";

	static final String GET_ALBUM_INFO = "smugmug.albums.getInfo";
	static final String ALBUM_OBJECT = "Album";
	static final String ALBUM_ACTION_ID = "AlbumID";
	static final String ALBUM_ACTION_KEY = "AlbumKey";
	static final String ALBUM_KEY = "Key";
	
	static final String CHANGE_ALBUM_SETTINGS = "smugmug.albums.changeSettings";
	static final String ALBUM_NAME = "Title";

	static final String DELETE_ALBUM = "smugmug.albums.delete";

	static final String GET_IMAGES = "smugmug.images.get";
	static final String IMAGE_ID = "id";
	static final String IMAGE_KEY = "Key";

	static final String GET_IMAGE_INFO = "smugmug.images.getInfo";
	static final String IMAGE_OBJECT = "Image";
	static final String IMAGE_MEDIUM_URL = "MediumURL";
	static final String IMAGE_ORIGINAL_URL = "OriginalURL";
	static final String IMAGE_CAPTION = "Caption";
	static final String IMAGE_FILENAME = "FileName";
	static final String IMAGE_ALBUM_URL = "AlbumURL";
	static final String IMAGE_HIDDEN = "Hidden";

	static final String CHANGE_IMAGE_SETTINGS = "smugmug.images.changeSettings";
	static final String IMAGE_ACTION_ID = "ImageID";
	static final String IMAGE_ACTION_KEY = "ImageKey";

	static final String CHANGE_IMAGE_POSITION = "smugmug.images.changePosition";
	static final String IMAGE_POSITION = "Position";

	static final String UPLOAD_IMAGE = "smugmug.images.upload";
	static final String UPLOAD_ALBUM_ID = "X-Smug-AlbumID";
	static final String UPLOAD_FILENAME = "X-Smug-FileName";
	static final String UPLOAD_CAPTION = "X-Smug-Caption";
	static final String UPLOAD_RESPONSE_ID = "id";
	static final String UPLOAD_RESPONSE_KEY = "Key";

	static final String IMAGE_DELETE = "smugmug.images.delete";

	static final String JSON_LOGIN = "Login";
	static final String JSON_LOGIN_SESSION = "Session";
	static final String JSON_LOGIN_SESSION_ID = "id";
	static final String JSON_LOGIN_USER = "User";
	static final String JSON_LOGIN_USER_NICKNAME = "NickName";

	static final String JSON_CATEGORIES = "Categories";
	static final String JSON_ALBUMS = "Albums";
	static final String JSON_SUBCATEGORIES = "SubCategories";
}
