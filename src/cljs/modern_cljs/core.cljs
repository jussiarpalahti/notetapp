
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
(def ^:const DB "/data2.json")
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
  {:start 0
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

(defn data_from_db [url]
  "Getting data from Dropbox"
  (.read js/crud url
         (fn [resp]
           (println "Fetched data from DB")
           (let [data (.parse js/JSON resp)]
           (updatedb [:data] (js->clj data))))))

(defn data_to_db [url]
  "Send data to db"
  (.write js/crud url (.stringify js/JSON (clj->js (:data db)))))

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
     {:value value :placeholder id
      :onchange #(update_field id (-> % .-target .-value))}
     nil))

(defn editor [data]
  (let [title (get data "title" "")
        url (get data "url" "")
        referer (get data "referer" "")
        comment (get data "comment" "")
        time (get data "time" "")]
    (m "form.pure-form" nil
       [(m "fieldset" nil
           [(text "title" title)
            (text "url" url)
            (text "referer" referer)
            (text "comment" comment)
            (text "time" time)
            (m "button.pure-button.pure-button-primary"
               {:onclick #(save)} "Update")
            " "
            (m "button.pure-button" {:onclick #(clear)} "Clear")])])))

(defn notes [data start]
  "Returns 10 items from data"
  (m "table.datatable.pure-table.pure-table-horizontal" nil
     [(m "tr" nil
         [(m "td" nil "Title")
          (m "td" nil "Url")
          (m "td" nil "Referer")
          (m "td" nil "Comment")
          (m "td" nil "Time")
          (m "td" nil "")])
      (map-indexed
        (fn [i item]
          (let [title (get item "title" "")
                url (get item "url" "")
                referer (get item "referer" "")
                comment (get item "comment" "")
                time (get item "time" "")
                pos (+ start i)]
              (m "tr" nil [(m "td" nil title)
                           (m "td" nil url)
                           (m "td" nil referer)
                           (m "td" nil comment)
                           (m "td" nil time)
                           (m "td" nil
                              [(m "button.pure-button" {:onclick #(edit item pos)} "Edit")])])))
        (if (< PAGESIZE (count data))
          (subvec data start (+ start PAGESIZE))
          data))]))

(defn page [start direction count]
  (let [next (direction start PAGESIZE)
        pagenum (if (= next 0) 1 next)
        end (+ next PAGESIZE)
        stop (if (not (or (< next 0) (< count next))) true false)]
    (if stop
      (m "span" nil
       [(m "a"
           {:onclick (fn [e]
                       (.preventDefault e)
                       (setpage next))
            :href ""}
           (str "Items: " pagenum "-" end))])
      nil)))

(defn pages []
  (let [start (:start db)
        count (count (:data db))]
    (m "div" nil ["Count: " count " " (page start - count) " " (page start + count)])))

(defn ctrl []
  (if (not (:data db nil))
    (do (println "Calling major Tom")
        (data_from_db DB))))

(defn viewer [c]
  (m "div" nil
     [(m "h1" {:style {:color "green"}} "NoteTapp")
      (m "#nav" nil
         [(m "div" nil (m "button.pure-button.button-success" {:onclick #(data_to_db DB)} "Save"))
          (pages)])
      (m "div" nil [(editor (:editing db))])
      (m "div" {} [(notes (:data db) (:start db))])]))

(def app {:controller ctrl :view viewer})

(enable-console-print!)
(println "Hello All!!!")
(js/foo)

(if (nil? js/client) (js/setup_dropbox))
(if (.isAuthenticated js/client)
  (do
    (set! (.-mode (.-route js/m)) "hash")
    (.route js/m
      (.getElementById js/document "app")
      "/"
      (clj->js {"/" app})))
  (println "No auth!"))

(let [par (.param (.-route js/m) "hih")]
  (if (not(nil? par))
    (println "initial param was:" par)
    (.route js/m "/")))

;; (require '[modern-cljs.core :as c] :reload)
;; alt cmd e -> search repl history
