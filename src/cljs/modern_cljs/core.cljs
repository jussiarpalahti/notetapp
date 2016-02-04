
(ns modern-cljs.core
  (:require [foo.bar]
            [m]
            [ajax.core :refer [GET POST]]
            [crud]
            [client]
            [setup_dropbox]))

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

(defn nm
  "Mithril in Cljs supporting different arguments amounts"
  ([x]
   '("Lone tag"))
  ([x y]
   (if (= (type y) (type []))
     '("Tag with children")
     (if (= (type y) (type {}))
       '("Lone tag with attrs")
       '("Tag with text worthy content, I hope"))))
  ([x y z]
   (if (= (type z) (type []))
     '("Tag with attrs and children")
     '("Tag with attrs and text worthy content, I hope"))))

;
;  Data handlers
;

(def db
  {:var (str "tosi:" (rand-int 10))
   :start 0
   :editing {}})

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

(defn update_field [id value]
  (set! db (assoc-in db [:editing id] value)))

(defn clear []
  (updatedb [:editing] {})
  (println "Cleared!"))

(defn edit [item pos]
  (println "editing" pos item)
  (updatedb [:editing] (merge {:pos pos} item)))

(defn save []
  (let [doc (:editing db)
        data (:data db)
        pos (:pos doc)]
    (if (not (empty? doc))
      (do
        (if (not (nil? pos))
          (updatedb [:data] (assoc data pos (dissoc doc :pos)))
          (do
            (updatedb [:data] (into [doc] data))
            (updatedb [:start] 0)))
        (clear)
        (println "Saved!" (str doc))))))

;
; View
;

(defn text [id value]
  (m "input[type=text]"
     {:value value :onchange #(update_field id (-> % .-target .-value))}
     nil))

(defn editor [data]
  (let [title (get data "title" "")
        url (get data "url" "")
        referer (get data "referer" "")
        comment (get data "comment" "")
        time (get data "time" "")]
    (m "table#editor" nil
       [(m "tr" nil
           [(m "td" nil "Title")
            (m "td" nil "Url")
            (m "td" nil "Referer")
            (m "td" nil "Comment")
            (m "td" nil "Time")
            (m "td" nil "")])
        (m "tr" nil [(m "td" nil (text "title" title))
                     (m "td" nil (text "url" url))
                     (m "td" nil (text "referer" referer))
                     (m "td" nil (text "comment" comment))
                     (m "td" nil (text "time" time))
                     (m "td#edit" nil [(m "button" {:onclick #(save)} "Save")
                                  (m "button" {:onclick #(clear)} "Clear")])])])))

(defn notes [data start]
  "Returns 10 items from data"
  (map-indexed
    (fn [i item]
      (let [title (get item "title")
            pos (+ start i)]
          (m "li" nil [(m "button" {:onclick #(edit item pos)} "Edit")
                       title])))
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
  (let [par (.param (.-route js/m) "hih")]
    (println "initial param was:" par))
  (if (not (:data db nil))
    (do (println "Calling major Tom")
        (fetch "/data.json"))))

(defn viewer [c]
  (m "div" nil
     [(m "h1" {:style {:color "green"}} (:var db))
      (editor (:editing db))
      (m "div" {} [(m "ol" nil (notes (:data db) (:start db)))])
      (pages)]))

(def app {:controller ctrl :view viewer})

(enable-console-print!)
(println "Hello All!!!")
(js/foo)

(js/setup_dropbox)
(if (and (not(nil? js/client)) (.isAuthenticated js/client))
  (do
    (set! (.-mode (.-route js/m)) "hash")
    (.route js/m
            (.getElementById js/document "app")
            "/"
            (clj->js {"/" app})))
  (println "No auth!"))

;; (require '[modern-cljs.core :as c] :reload)
;; alt cmd e -> search repl history
