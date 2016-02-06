
(ns modern-cljs.core
  (:require [foo.bar]
            [m]
            [ajax.core :refer [GET POST]]
            [crud]
            [client]
            [setup_dropbox]
            [goog.i18n.DateTimeFormat :as dtf]))

;
; Constants
;

(def ^:const PAGESIZE 10)
(def ^:const DB "/data.json")

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

(defn route_param [param]
  "Get param from route params"
  (.param (.-route js/m) param))

;
; Closure helpers
;

(defn format_time [d]
  (let [format (new goog.i18n.DateTimeFormat "d.M.yyyy H:mm")]
    (.format format d)))


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

(defn text [id value size]
  (m "input[type=text]"
     {:value value :placeholder id :size size
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
           [(text "title" title 35)
            (text "url" url 15)
            (text "referer" referer 15)
            (text "time" time 15)
            (text "comment" comment 50)
            (m "button.pure-button.pure-button-primary"
               {:onclick #(save)} "Update")
            (m "button.pure-button" {:onclick #(clear)} "Clear")])])))

(defn notes [data start]
  "Returns 10 items from data"
  (m "table.datatable.pure-table.pure-table-horizontal" nil
     [(m "tr" nil
         [(m "th" nil "Title")
          (m "th" nil "Url")
          (m "th" nil "Referer")
          (m "th" nil "Time")
          (m "th" nil "Comment")
          (m "th" nil "")])
      (map-indexed
        (fn [i item]
          (let [title (get item "title" "")
                url (get item "url" "")
                referer (get item "referer" "")
                comment (get item "comment" "")
                time (get item "time" "")
                pos (+ start i)]
              (m "tr" nil [(m "td.itemtitle" nil title)
                           (m "td" nil (m ".sizer" nil url))
                           (m "td" nil (m ".sizer" nil referer))
                           (m "td" nil (format_time (new js/Date time)))
                           (m "td" nil comment)
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
    (m "#pages" nil ["Count: " count " " (page start - count) " " (page start + count)])))

(defn ctrl []
  (if (not (:data db nil))
    (do (println "Calling major Tom")
        (data_from_db DB))))

(defn viewer [c]
  (m "div" nil
     [(m "div" nil [(m "h1" {:style {:color "green"}} "NoteTapp")])
      (m "#nav" nil
         [(m "div" nil
             (m "button.pure-button.button-success" {:onclick #(data_to_db DB)} "Save"))])
      (m "#editor" nil [(editor (:editing db))])
      (pages)
      (m "div" {} [(notes (:data db) (:start db))])]))

(def app {:controller ctrl :view viewer})

(enable-console-print!)

(js/setup_dropbox)

(defn setup []
  (do
    (set! (.-mode (.-route js/m)) "hash")
    (.route js/m
            (.getElementById js/document "app")
            "/"
            (clj->js {"/" app}))))

(if (and (not (nil? js/client)) (.isAuthenticated js/client))
  (do
    (setup)
    (let [params {"title" (route_param "title")
                  "url" (route_param "url")
                  "referer" (route_param "referer")}]
      (if (not(nil? params))
        (do (println "initial param was:" params)
            (updatedb [:editing] params)
            (.route js/m "/")))))
  (println "No auth!"))

;; (require '[modern-cljs.core :as c] :reload)
;; alt cmd e -> search repl history
