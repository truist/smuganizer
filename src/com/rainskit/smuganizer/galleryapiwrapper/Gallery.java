package com.rainskit.smuganizer.galleryapiwrapper;

import com.rainskit.smuganizer.GallerySettings;
import com.rainskit.smuganizer.tree.TreeableGalleryItem;
import java.awt.Desktop;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import javax.swing.Icon;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;

public class Gallery implements TreeableGalleryItem {
	public static final String TYPE = "MenaltoGallery";
	
	static final String ARG_ALBUMS_TOO = "albums_too";
	static final String ARG_COMMAND = "cmd";
	static final String ARG_G2_CONTROLLER = "g2_controller";
	static final String ARG_PASSWORD = "password";
	static final String ARG_PROTOCOL_VER = "protocol_version";
	static final String ARG_SET_ALBUMNAME = "set_albumName";
	static final String ARG_USERNAME = "uname";
	static final String ARGVAL_G2_CONTROLLER = "remote.GalleryRemote";
	static final String ARGVAL_NO = "no";
	static final String ARGVAL_YES = "yes";
	static final String COMMAND_ALBUM_PROPERTIES = "album-properties";
	static final String COMMAND_FETCH_ALBUM_IMAGES = "fetch-album-images";
	static final String COMMAND_FETCH_ALBUMS = "fetch-albums";
	static final String COMMAND_FETCH_ALBUMS_PRUNE = "fetch-albums-prune";
	static final String COMMAND_G2_AUTH_TOKEN = "g2_authToken";
	static final String COMMAND_LOGIN = "login";
	static final String G1_BASE = "gallery_remote2.php";
	static final String G1_NOOP = "";
	static final String G2_BASE = "main.php";
	static final String G2_NOOP = "?g2_controller=remote.GalleryRemote&g2_form[cmd]=no-op";
	static final String PROTOCOL_VERSION = "2.3";
	static final String RESPONSE_ALBUM_COUNT = "album_count";
	static final String RESPONSE_ALBUM_NAME_INDEXED = "album.name.";
	static final String RESPONSE_ALBUM_PARENT_INDEXED = "album.parent.";
	static final String RESPONSE_ALBUM_SUMMARY_INDEXED = "album.summary.";
	static final String RESPONSE_ALBUM_TITLE_INDEXED = "album.title.";
	static final String RESPONSE_AUTH_TOKEN = "auth_token";
	static final String RESPONSE_BASEURL = "baseurl";
	static final String RESPONSE_IMAGE_CAPTION_INDEXED = "image.caption.";
	static final String RESPONSE_IMAGE_COUNT = "image_count";
	static final String RESPONSE_IMAGE_HIDDEN_INDEXED = "image.hidden.";
	static final String RESPONSE_IMAGE_NAME_INDEXED = "image.name.";
	static final String RESPONSE_IMAGE_RESIZED_NAME_INDEXED = "image.resizedName.";
	static final String RESPONSE_IMAGE_TITLE_INDEXED = "image.title.";
	static final String RESPONSE_STATUS_CODE = "status";
	static final String RESPONSE_STATUS_TEXT = "status_text";
	static final String USER_AGENT = "Smuganizer";

    private HttpClient loginHttpClient;
	
	private GallerySettings settings;
    private String baseURL;
    private String completeURL;
    private int galleryVersion;
    private String lastAuthToken;

    public Gallery(GallerySettings settings) throws IOException {
		this.settings = settings;
		String settingsURL = settings.getURL().toExternalForm();
		this.baseURL = settingsURL + (settingsURL.endsWith("/") ? "" : "/");
		
		loginHttpClient = new HttpClient();
		loginHttpClient.setHttpConnectionManager(new MultiThreadedHttpConnectionManager());
//		loginHttpClient.getHostConfiguration().setProxy("127.0.0.1", 8888);
		
		this.galleryVersion = determineGalleryVersion(baseURL);
		login();
    }
	
	private int determineGalleryVersion(String baseURL) throws IOException {
        GetMethod get = new GetMethod();
		//need to pass 'false' so it encodes the query
		get.setURI(new org.apache.commons.httpclient.URI(baseURL + G2_BASE + G2_NOOP, false));
        try {
            int statusCode = loginHttpClient.executeMethod(get);
            if(statusCode != HttpStatus.SC_OK) {
                get.releaseConnection();
                get = new GetMethod();
				get.setURI(new org.apache.commons.httpclient.URI(baseURL + G1_BASE + G1_NOOP, false));
                statusCode = loginHttpClient.executeMethod(get);
                if(statusCode != HttpStatus.SC_OK) {
                    throw new IOException("Error contacting gallery - probably an invalid Gallery URL");
                }
                this.completeURL = baseURL + G1_BASE;
				return 1;
            }
            else {
				this.completeURL = baseURL + G2_BASE;
                return 2;
            }
        }
        finally {
            get.releaseConnection();
        }
	}

	private NameValuePair newArgument(String argument, String value) {
		return newArgument(argument, value, true);
	}
	
	private NameValuePair newArgument(String argument, String value, boolean adjustArgument) {
		if (adjustArgument) {
			argument = (galleryVersion == 1 ? argument : "g2_form[" + argument + "]");
		}
		return new NameValuePair(argument, value);
	}

	private ArrayList<NameValuePair> newBaseArguments(String command) {
		ArrayList<NameValuePair> arguments = new ArrayList<NameValuePair>();
		arguments.add(newArgument(ARG_COMMAND, command));
		arguments.add(newArgument(ARG_PROTOCOL_VER, PROTOCOL_VERSION));
		if (galleryVersion > 1) {
			arguments.add(newArgument(ARG_G2_CONTROLLER, ARGVAL_G2_CONTROLLER, false));
			if (lastAuthToken != null) {
				arguments.add(newArgument(COMMAND_G2_AUTH_TOKEN, lastAuthToken));
			}
		}
		return arguments;
	}
	
	private Properties executePost(ArrayList<NameValuePair> arguments, HttpClient httpClient) throws IOException {
		PostMethod post = new PostMethod(completeURL);
		post.setRequestHeader("User-Agent", USER_AGENT);
		post.setRequestBody(arguments.toArray(new NameValuePair[arguments.size()]));
		try {
			int statusCode = httpClient.executeMethod(post);
			if(statusCode != HttpStatus.SC_OK) {
				throw new IOException(post.getStatusLine().toString());
			}
			Properties response = new Properties();
			response.load(post.getResponseBodyAsStream());
			String responseCode = response.getProperty(RESPONSE_STATUS_CODE);
			if(responseCode == null || Integer.parseInt(responseCode) != 0) {
				throw new IOException(response.getProperty(RESPONSE_STATUS_TEXT));
			}
			if (galleryVersion > 1) {
				this.lastAuthToken = response.getProperty(RESPONSE_AUTH_TOKEN);
			}
			return response;
        }
        finally {
            post.releaseConnection();
        }
	}
	
    private void login() throws IOException {
		ArrayList<NameValuePair> arguments = newBaseArguments(COMMAND_LOGIN);
		arguments.add(newArgument(ARG_USERNAME, settings.getUsername()));
		arguments.add(newArgument(ARG_PASSWORD, String.valueOf(settings.getPassword())));
		executePost(arguments, loginHttpClient);
    }

    public List<? extends TreeableGalleryItem> loadChildren() throws IOException {
		Properties response = executePost(newBaseArguments(COMMAND_FETCH_ALBUMS_PRUNE), loginHttpClient);
		
        List<GalleryAlbum> rootAlbums = new ArrayList<GalleryAlbum>();
		
		Map<String, GalleryAlbum> nameAlbumMap = new HashMap<String, GalleryAlbum>();
		Map<GalleryAlbum, String> albumParentMap = new HashMap<GalleryAlbum, String>();

		int albumCount = Integer.parseInt(response.getProperty(RESPONSE_ALBUM_COUNT));
		for(int albumRefNum = 1; albumRefNum <= albumCount; albumRefNum++) {
			String albumName = response.getProperty(RESPONSE_ALBUM_NAME_INDEXED + albumRefNum);
			String albumTitle = response.getProperty(RESPONSE_ALBUM_TITLE_INDEXED + albumRefNum);
			String albumSummary = response.getProperty(RESPONSE_ALBUM_SUMMARY_INDEXED + albumRefNum);
			
			GalleryAlbum album = new GalleryAlbum(this, albumName, albumTitle, albumSummary);
			rootAlbums.add(album);
			
			nameAlbumMap.put(albumName, album);
			String parentName = response.getProperty(RESPONSE_ALBUM_PARENT_INDEXED + albumRefNum);
			if(parentName != null && parentName.length() > 0 && !parentName.equals("0")) {
				albumParentMap.put(album, parentName);
			}
		}
		
		//now that we're sure we've seen all the albums, it's safe to hook them up
		for (GalleryAlbum each : albumParentMap.keySet()) {
			GalleryAlbum parentAlbum = nameAlbumMap.get(albumParentMap.get(each));
			each.setParent(parentAlbum);
			if (parentAlbum != null) {
				parentAlbum.addSubAlbum(each);
			}
		}
		
		//trim it down to just the top-level albums
		rootAlbums.removeAll(albumParentMap.keySet());
		
        return rootAlbums;
    }
	
    ArrayList<GalleryImage> loadImagesFor(GalleryAlbum album) throws IOException {
        ArrayList<GalleryImage> images = new ArrayList<GalleryImage>();

		ArrayList<NameValuePair> arguments = newBaseArguments(COMMAND_FETCH_ALBUM_IMAGES);
		arguments.add(newArgument(ARG_ALBUMS_TOO, ARGVAL_NO));
        arguments.add(newArgument(ARG_SET_ALBUMNAME, album.getUrlName()));
		
		Properties response = executePost(arguments, loginHttpClient);
		int numImages = Integer.parseInt(response.getProperty(RESPONSE_IMAGE_COUNT));
		for(int imageRefNum = 1; imageRefNum <= numImages; imageRefNum++) {
			images.add(new GalleryImage(album, response, imageRefNum));
		}
		
		return images;
    }
	
	public HttpClient getHttpClient() {
		return loginHttpClient;
	}
	
	@Override
	public String toString() {
		return getLabel();
	}

	public String getFullPathLabel() {
		return "";
	}

	public int compareTo(TreeableGalleryItem o) {
		return 0;
	}

	public Icon getIcon() {
		return null;
	}

	public String getLabel() {
		return getBaseURL();
	}

	public boolean canBeRelabeled() {
		return false;
	}

	public void reLabel(String answer) {
		throw new UnsupportedOperationException("Not supported");
	}

	public boolean canBeDeleted() {
		return false;
	}

	public void delete() {
		throw new UnsupportedOperationException("Not supported");
	}

	public boolean canBeLaunched() {
		return true;
	}

	public void launch() throws IOException, URISyntaxException {
		Desktop.getDesktop().browse(new java.net.URI("http", getBaseURL(), "/", null));
	}
	
	public String getBaseURL() {
		int offset = baseURL.indexOf("//");
		if (offset < 0) {
			return baseURL;
		} else {
			if (baseURL.endsWith("/")) {
				return baseURL.substring(offset + 2, baseURL.length() - 1);
			} else {
				return baseURL.substring(offset + 2);
			}
		}
	}

	public URL getPreviewURL() throws MalformedURLException {
		return null;
	}

	public String getType() {
		return TYPE;
	}

	public TreeableGalleryItem getParent() {
		return null;
	}

	public boolean canAccept(TreeableGalleryItem childItem, int childIndex) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void receiveChild(TreeableGalleryItem childItem, int childIndex) {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
