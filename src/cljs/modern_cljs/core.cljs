
(ns modern-cljs.core
  (:require [foo.bar]
            [m]
            [ajax.core :refer [GET POST]]))

; Contants

(def ^:const PAGESIZE 10)

; Mithril helpers

(defn m [tag attrs values]
  "Mithril in Cljs"
  (js/m tag (clj->js attrs) (clj->js values)))

;
;  Data handlers
;

(def db
  {:var (str "tosi:" (rand-int 10))
   :start 10})

(defn ^:export updatedb [fields value]
  (set! db (assoc-in db fields value))
  (.redraw js/m true))

(defn handler [resp]
  "Data request handler"
  (.log js/console "Request successful")
  (updatedb [:data] resp))

(defn error-handler [{:keys [status status-text]}]
  "Data request error handler"
  (.log js/console (str "something went wrong: " status " " status-text)))


(defn fetch [url]
  "Get data from URL"
  (GET url {:handler handler
            :error-handler error-handler
            :format "json"}))
;
; View
;

(defn notes [start]
  "Returns 10 items from data"
  (map
    (fn [item]
      (let [title (get item "title")]
          (m "li" nil title)))
    (take PAGESIZE (drop start (:data db)))))

(defn ctrl []
  (println "Calling major Tom")
  (fetch "/data.json"))

(defn viewer [c]
  (println "Seeing things")
  (m "div" nil
     [(m "h1" {:style {:color "green"}} (:var db))
      (m "ol" nil (notes (:start db)))]))

(def app {:controller ctrl :view viewer})

(enable-console-print!)
(println "Hello All!!!")
(js/foo)
(.mount js/m
  (.getElementById js/document "app")
  (clj->js app))

;; (require '[modern-cljs.core :as c] :reload)
