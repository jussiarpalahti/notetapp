
(ns modern-cljs.core
  (:require [foo.bar] [m]))

(def db
  (js-obj "var" (str "tosi: " (rand-int 10))))

(defn ^:export ctrl []
  (println "Calling major Tom"))
(defn ^:export viewer [arg]
  (println "Seeing things")
  (js/m "div" (.-var db)))
(def app (js-obj "controller" ctrl "view" viewer))

(defn ^:export updatedb [arg]
  (set! (.-var db) arg)
  (.redraw js/m true))

(enable-console-print!)
(println "Hello All!!!")
(js/foo)
(.mount js/m
  (.getElementById js/document "app")
  app)
