
(ns modern-cljs.core
  (:require [foo.bar]
            [m]
            [ajax.core :refer [GET POST]]))

(defn m [tag attrs values]
  "Mithril in Cljs"
  (js/m tag (clj->js attrs) (clj->js values)))


(def db
  {:var (str "tosi:" (rand-int 10))})

(defn handler [resp]
  "Data request handler"
  (.log js/console "Request successful")
  (set! db (assoc db :response resp)))


(defn error-handler [{:keys [status status-text]}]
  "Data request error handler"
  (.log js/console (str "something bad happened: " status " " status-text)))


(defn fetch [url]
  "Get data from URL"
  (GET url {:handler handler
            :error-handler error-handler
            :format "json"}))

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

;; (require '[modern-cljs.core :as c] :reload)
