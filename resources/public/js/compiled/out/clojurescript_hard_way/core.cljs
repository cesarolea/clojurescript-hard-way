(ns clojurescript-hard-way.core)
(js/console.log "dev")

(-> js/document
    (.getElementById "app")
    (.-innerHTML)
    (set! "Hola ClojureScript!"))
