(ns ^:figwheel-hooks clojurescript-hard-way.core
  (:require [reagent.core :as r]))

(defn doomguy-component []
  [:img {:id "doomguy"
         :src "https://vignette.wikia.nocookie.net/wadguia/images/6/62/Godmode_face.png/revision/latest?cb=20141012222849"}])

(defn title-component []
  [:div "Activando God-Mode!"
   [:p [doomguy-component]]])

(defn ^:after-load mount-root []
  (r/render [title-component]
            (.getElementById js/document "app")))

(defn ^:export main []
  ;; do some other init

  (-> js/document
      (.getElementsByTagName "head")
      (aget 0)
      .-innerHTML
      (set! "<style>body{color:#FF0000; background-color:#1B1B1B;}</style>"))

  (mount-root))
