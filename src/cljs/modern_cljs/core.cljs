
(ns modern-cljs.core
  (:require [foo.bar] [m]))

(enable-console-print!)
(println "Hello All!!!")
(js/foo)
(.render js/m
  (.getElementById js/document "app")
  (js/m "div" "jei!"))
