
(ns modern-cljs.core
  (:require [foo.bar]
            [m]
            [ajax.core :refer [GET POST]]))

;
; Constants
;

(def ^:const PAGESIZE 10)

;
; Mithril helpers
;

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

(defn setpage [next]
  (updatedb [:start] next))

;
; View
;

(defn notes [data start]
  "Returns 10 items from data"
  (map
    (fn [item]
      (let [title (get item "title")]
          (m "li" nil title)))
    (take PAGESIZE (drop start data))))

(defn page [start direction count]
  (let [next (direction start PAGESIZE)
        pagenum (if (= next 0) 1 next)
        end (+ next PAGESIZE)
        stop (if (not (or (< next 0) (< count next))) true false)]
    (if stop
      (m "span" nil
       [(m "a"
           {:onclick #(setpage next) :href "#"}
           (str "Items: " pagenum "-" end))])
      nil)))

(defn pages []
  (let [start (:start db)
        count (count (:data db))]
    (m "div" nil ["Count: " count " " (page start - count) " " (page start + count)])))

(defn ctrl []
  (println "Calling major Tom")
  (fetch "/data.json"))

(defn viewer [c]
  (m "div" nil
     [(m "h1" {:style {:color "green"}} (:var db))
      (m "ol" nil (notes (:data db) (:start db)))
      (pages)]))

(def app {:controller ctrl :view viewer})

(enable-console-print!)
(println "Hello All!!!")
(js/foo)
(.mount js/m
  (.getElementById js/document "app")
  (clj->js app))

;; (require '[modern-cljs.core :as c] :reload)
