(ns user
  (:require [figwheel.main.api :as fw-api]
            [mount.core :as mount]
            [clojurescript-hard-way.core]))

(defn start []
  (mount/start-without #'clojurescript-hard-way.core/cljs-repl))

(defn stop []
  (mount/stop-except))
