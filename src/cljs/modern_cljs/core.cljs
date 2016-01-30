
(ns modern-cljs.core
  (:require [foo.bar] [m]))

(def db
  {:var (str "tosi:" (rand-int 10))})

(defn ^:export ctrl []
  (println "Calling major Tom"))
(defn ^:export viewer [c]
  (println "Seeing things")
  (js/m "div" (.-var (clj->js db))))

(def app {:controller ctrl :view viewer})

(defn ^:export updatedb [fields value]
  (set! db (assoc-in db fields value))
  (.redraw js/m true))

(enable-console-print!)
(println "Hello All!!!")
(js/foo)
(.mount js/m
  (.getElementById js/document "app")
  (clj->js app))

