
(ns modern-cljs.core
  (:require [foo.bar] [m]))

(defn ^:export ctrl []
  (println "Calling major Tom")
  (js-obj "var" (str "tosi: " (rand-int 10))))
(defn ^:export viewer [arg]
  (println "Seeing things")
  (js/m "div" (.-var arg)))
(def app (js-obj "controller" ctrl "view" viewer))

(enable-console-print!)
(println "Hello All!!!")
(js/foo)
(.mount js/m
  (.getElementById js/document "app")
  app)
