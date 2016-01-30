
(ns modern-cljs.core
  (:require [foo.bar] [m]))

(defn m [tag attrs values]
  (js/m tag (clj->js attrs) (clj->js values)))

(def db
  {:var (str "tosi:" (rand-int 10))})

(defn ^:export ctrl []
  (println "Calling major Tom"))
(defn ^:export viewer [c]
  (println "Seeing things")
  (m "div" {:style {:color "green"}} (:var db)))

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
