
(ns notetapp.core
  (:require [foo.bar]
            [m]
            [ajax.core :refer [GET POST]]
            [crud]
            [client]
            [setup_dropbox]
            [goog.i18n.DateTimeFormat :as dtf]))

(enable-console-print!)

;
; Constants
;

(def ^:const PAGESIZE 10)
(def ^:const DB "/data.json")
(def ^:const DATEFORMAT "d.M.yyyy H:mm")

;
; Mithril helpers
;

(defn m [tag attrs values]
  "Mithril in Cljs"
  (js/m tag (clj->js attrs) (clj->js values)))

(defn nm
  "Mithril in Cljs supporting different call methods"
  ([tag]
   (m tag nil nil))
  ([tag par2]
   (if (= (type par2) (type []))
     (m tag nil par2)
     (if (= (type par2) (type {}))
       (m tag par2 nil)
       (m tag nil par2))))
  ([tag par2 par3]
   (m tag par2 par3)))

(defn route_param [param]
  "Get param from route params"
  ((aget (.-route js/m) "param") param))

;
; Closure helpers
;

(defn format_time [d format]
  "Render instance of js/Date according to format"
  (let [format (new goog.i18n.DateTimeFormat format)]
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
  (let [d (:editing db)
        doc (if (nil? (get d "time"))
              (assoc d "time" (str (new js/Date)))
              d)
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

(defn delete [pos]
  (let [data (:data db)]
    (updatedb [:data]
              (vec (concat (subvec data 0 pos) (subvec data (inc pos)))))))
;
; View
;

(defn text [id value size]
  (nm "input[type=text]"
     {:value value :placeholder id :size size
      :onchange #(update_field id (-> % .-target .-value))}))

(defn editor [data]
  (let [title (get data "title" "")
        url (get data "url" "")
        referer (get data "referer" "")
        comment (get data "comment" "")
        time (get data "time" "")]
    (m "form.pure-form"
       {:obsubmit (fn [e]
           (.preventDefault e)
           (println "Shouldn't be submitting")
            false)}
       [(m "fieldset" nil
           [(text "title" title 35)
            (text "url" url 15)
            (text "referer" referer 15)
            (text "time" time 15)
            (text "comment" comment 50)
            (m "button.pure-button.pure-button-primary[type=button]"
               {:onclick #(save)}
               "Update")
            (m "button.pure-button[type=button]"
               {:onclick #(clear)} "Clear")])])))

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
              (nm "tr" [(m "td.itemtitle" nil title)
                           (nm "td" (m "a.link" {:href url} "Link"))
                           (nm "td" (if (not(= "" referer)) (m "a.link" {:href referer} "Referer")))
                           (nm "td" (format_time (new js/Date time) DATEFORMAT))
                           (nm "td" comment)
                           (nm "td"
                              [(nm "button.pure-button" {:onclick #(edit item pos)} "Edit")
                               (nm "button.pure-button" {:onclick #(delete pos)} "Remove")])])))
        (if (< PAGESIZE (count data))
          (subvec data start (+ start PAGESIZE))
          data))]))

(defn page [start direction count]
  (let [next (direction start PAGESIZE)
        pagenum (if (= next 0) 1 next)
        end (+ next PAGESIZE)
        stop (if (not (or (< next 0) (< count next))) true false)]
    (if stop
      (nm "span.pagelink"
       [(m "a"
           {:onclick (fn [e]
                       (.preventDefault e)
                       (setpage next))
            :href ""}
           (str pagenum " - " end))])
      (nm "span.pagelink.emptypage" "______"))))

(defn pages []
  (let [start (:start db)
        count (count (:data db))]
    (nm "#pages" ["Count: " count " " (page start - count) " " (page start + count)])))

(defn ctrl []
  (if (not (:data db nil))
    (do (println "Calling major Tom")
        (data_from_db DB))))

(defn viewer [c]
  (nm "div"
     [(nm "div" [(nm "h1" {:style {:color "green"}} "NoteTapp")])
      (nm "#nav"
         [(nm "div"
             (nm "button.pure-button.button-success" {:onclick #(data_to_db DB)} "Save"))])
      (nm "#editor" [(editor (:editing db))])
      (pages)
      (nm "div" [(notes (:data db) (:start db))])]))

(def app {:controller ctrl :view viewer})

;; Routing mode
(aset (.-route js/m) "mode" "hash")

;; Dropbox OAuth process
(js/setup_dropbox)

(defn setup []
  "Mount app"
  (do
    (.route js/m
            (.getElementById js/document "app")
            "/"
            (clj->js {"/" app}))))

;; If authentication has been successful, mount app and use query params if any
(if (and (not (nil? js/client)) (.isAuthenticated js/client))
  (do
    (setup)
    (let [params {"title" (route_param "title")
                  "url" (route_param "url")
                  "referer" (route_param "referer")
                  "time" (str (new js/Date))}]
      (if (not(nil? (get params "url")))
        (do (println "initial param was:" params)
            (.route js/m "/")
            (updatedb [:editing] params))
        (println "No params!" params))))
  (println "No auth!"))

;; (require '[notetapp.core :as c] :reload)
;; alt cmd e -> search repl history
;; (.route js/m "/")
