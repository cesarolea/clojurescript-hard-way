(ns ^:figwheel-hooks clojurescript-hard-way.core
  (:require [reagent.core :as r]))

(defn doomguy-component []
  [:img {:id "doomguy"
         :src "https://vignette.wikia.nocookie.net/wadguia/images/6/62/Godmode_face.png/revision/latest?cb=20141012222849"}])

(defn doomguy-animation []
  (let [click-count (r/atom 0)]
    (fn []
      [:img {:id "doomguy"
             :src (str "images/doomguy-frame-"
                       (if (even? @click-count) "even" "odd")
                       ".png")
             :on-click #(swap! click-count inc)}])))

(defn title-component []
  [:div "Activating God-Mode!"
   [:p [doomguy-animation]]])

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
