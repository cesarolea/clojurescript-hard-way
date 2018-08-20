(ns clojurescript-hard-way.dev
  (:require [figwheel.client :as figwheel]
            [clojurescript-hard-way.core :as core]))

(figwheel/watch-and-reload
  :websocket-url "ws://localhost:3449/figwheel-ws"
  :jsload-callback (fn [] (core/mount-root)))
