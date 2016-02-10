
# NoteTapp note taking application with ClojureScript and Mithril

Application reads and saves data on Dropbox, so [their account and developer key](https://www.dropbox.com/developers/apps) is required.

You need to have [Clojure](https://clojure.org) and [Boot](http://boot-clj.com/) installed. Build script is based on [Modern ClojureScript](https://github.com/magomimmo/modern-cljs).

## Regular dev environment

  boot dev

## Just Clojurescript build

  boot cljs target -d target

## REPL connection

  boot repl -c

## For production use advanced optimization

  boot cljs -O advanced target -d target

App is published by placing target/index.html, target/js/main.js and target/css/styles.css and to some web server.

## Dropbox configuration

You need to create a [Dropbox app](https://www.dropbox.com/developers/apps) to allow the app to use Dropbox as its database. Add your developer key to index.html where specified. Application will try to authenticate using dropbox.js and OAuth authentication.

You can run the app from localhost provided you set the app's settings accordingly (like host and port) so that authentication is allowed from Dropbox's end.

App can be served from public Dropbox folder which app creation should have set up for you in your account's Dropbox folder. Getting all parameters right can be bit involded. I've only done that once. Also, authentication is a bit fiddly, and there's essentially no support for dropbox.js. It has worked for me, though, for close to two years now.

Using Dropbox accounts users can get their own note database on their Dropbox folders (which is really a regular JSON file).
