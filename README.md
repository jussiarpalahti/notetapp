
# NoteTapp note taking application with ClojureScript and Mithril

You need to have [Clojure](https://clojure.org) and [Boot](http://boot-clj.com/) installed. Build script is based on [Modern ClojureScript](https://github.com/magomimmo/modern-cljs).

## Regular dev environment

  boot dev

## Just Clojurescript build

  boot cljs target -d target

## REPL connection

  boot repl -c

## For production use advanced optimization

  boot cljs -O advanced target -d target

App now consists of target/index.html, target/js/main.js and target/css/styles.css.
