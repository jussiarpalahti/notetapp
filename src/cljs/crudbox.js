
var client, crud, setup_dropbox;

var decodeUtf8 = function (arrayBuffer) {
	var result = "";
	var i = 0;
	var c = 0;
	var c1 = 0;
	var c2 = 0;

	var data = new Uint8Array(arrayBuffer);

	// If we have a BOM skip it
	if (data.length >= 3 && data[0] === 0xef && data[1] === 0xbb && data[2] === 0xbf) {
		i = 3;
	}

	while (i < data.length) {
		c = data[i];

		if (c < 128) {
			result += String.fromCharCode(c);
			i++;
		} else if (c > 191 && c < 224) {
			if( i+1 >= data.length ) {
				throw "UTF-8 Decode failed. Two byte character was truncated.";
			}
			c2 = data[i+1];
			result += String.fromCharCode( ((c&31)<<6) | (c2&63) );
			i += 2;
		} else {
			if (i+2 >= data.length) {
				throw "UTF-8 Decode failed. Multi byte character was truncated.";
			}
			c2 = data[i+1];
			c3 = data[i+2];
			result += String.fromCharCode( ((c&15)<<12) | ((c2&63)<<6) | (c3&63) );
			i += 3;
		}
	}
	return result;
}

setup_dropbox = function () {
	var showError = function(error) {
	  switch (error.status) {
		  case Dropbox.ApiError.INVALID_TOKEN:
		    // If you're using dropbox.js, the only cause behind this error is that
		    // the user token expired.
		    // Get the user through the authentication flow again.
		    console.log("INVALID_TOKEN");
		    break;

		  case Dropbox.ApiError.NOT_FOUND:
		    // The file or folder you tried to access is not in the user's Dropbox.
		    // Handling this error is specific to your application.
		    console.log("NOT_FOUND");
		    break;

		  case Dropbox.ApiError.OVER_QUOTA:
		    // The user is over their Dropbox quota.
		    // Tell them their Dropbox is full. Refreshing the page won't help.
		    console.log("OVER_QUOTA");
		    break;

		  case Dropbox.ApiError.RATE_LIMITED:
		    // Too many API requests. Tell the user to try again later.
		    // Long-term, optimize your code to use fewer API calls.
		    console.log("RATE_LIMITED");
		    break;

		  case Dropbox.ApiError.NETWORK_ERROR:
		    // An error occurred at the XMLHttpRequest layer.
		    // Most likely, the user's network connection is down.
		    // API calls will not succeed until the user gets back online.
		    console.log("NETWORK_ERROR");
		    break;

		  case Dropbox.ApiError.INVALID_PARAM:
		  	console.log("INVALID_PARAM");
		  	break;
		  case Dropbox.ApiError.OAUTH_ERROR:
		  	console.log("OAUTH_ERROR");
		  	break;
		  case Dropbox.ApiError.INVALID_METHOD:
		  	console.log("INVALID_METHOD");
		  	break;
		  case Dropbox.AuthError.ACCESS_DENIED:
		  	console.log("ACCESS_DENIED");
		  	break;
		  case Dropbox.AuthError.INVALID_REQUEST:
		  	console.log("INVALID_REQUEST");
		  	break;
		  case Dropbox.AuthError.UNAUTHORIZED_CLIENT:
		  	console.log("UNAUTHORIZED_CLIENT");
		  	break;
		  case Dropbox.AuthError.INVALID_GRANT:
		  	console.log("INVALID_GRANT");
		  	break;
		  case Dropbox.AuthError.SERVER_ERROR:
		  	console.log("SERVER_ERROR");
		  	break;
		  case Dropbox.AuthError.UNSUPPORTED_RESPONSE_TYPE:
		  	console.log("UNSUPPORTED_RESPONSE_TYPE");
		  	break;
		  case Dropbox.AuthError.TEMPORARILY_UNAVAILABLE:
		  	console.log("TEMPORARILY_UNAVAILABLE");
		  	break;
		  default:
		    // Caused by a bug in dropbox.js, in your application, or in Dropbox.
		    // Tell the user an error occurred, ask them to refresh the page.
		    console.log("Something went wrong, no idea what");
	  }
	};

	// create a client object with app's public key
	client = new Dropbox.Client({ key: DROPBOX_APP_KEY });
	
	// try to authenticate with redirect to dropbox where oauth token gets set somewhere somehow
	client.authenticate(function(error, client) {
    if (error) {
		// Replace with a call to your own error-handling code.
		//
		// Don't forget to return from the callback, so you don't execute the code
		// that assumes everything went well.
		showError(error);
		console.log("error ", error);

	  } else {
		// Replace with a call to your own application code.
		//
		// The user authorized your app, and everything went well.
		// client is a Dropbox.Client instance that you can use to make API calls.
		console.log("jei, authenticated");
	  }
	});
}

// namespace for crud ops
crud = {}

// write file with contents
crud.write = function (doc, contents) {

	// writing file
	client.writeFile(doc, contents, function(error, stat) {
	if (error) {
		console.log("error ", error);
		return false;
	}

	console.log("File saved as revision " + stat.revisionTag);
	});
	return true;
}

// read file, return contents
crud.read = function (doc, data_handler) {
	client.readFile(doc, { arrayBuffer: true }, function(error, data) {
	if (error) {
		console.log("error ", error);  // Something went wrong.
	}

	console.log("data", data);  // data has the file's contents
	data = decodeUtf8(data);
	data_handler(data);
	});
}

// read file, write file with old and new contens concatenated
crud.update = function (doc, contents) {
	read(doc, function (data) {
		console.log("update handler", data, contents);
		write(doc, data + contents);
	});
	
}
