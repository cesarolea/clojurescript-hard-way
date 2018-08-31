(ns ^:figwheel-hooks clojurescript-hard-way.core
  (:require [reagent.core :as r]))

(defn bulma-example []
  [:div {:class "columns"}
   [:div {:class "column"} "First column"]
   [:div {:class "column"} "Second column"]
   [:div {:class "column"} "Third column"]
   [:div {:class "column"} "Fourth column"]])

(defn banner []
  [:div
   [:section {:class "hero"}
    [:div {:class "hero-body"}
     [:h1 {:class "title"} "ClojureScript <3 WebJars"]
     [:h2 {:class "subtitle"} "Using bulma as an example"]]]
   [bulma-example]])

(defn ^:after-load mount-root []
  (r/render [banner]
            (.getElementById js/document "app")))

(defn ^:export main []
  ;; do some other init
  (mount-root))
