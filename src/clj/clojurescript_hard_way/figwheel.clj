(ns clojurescript-hard-way.figwheel
  (:require [ring.middleware.webjars :refer [wrap-webjars]]))

(defn handler [request]
  (if (and (= :get (:request-method request))
           (= "/" (:uri request)))
    {:status 200 :headers {"Content-Type" "text/html"} :body (slurp "resources/public/figwheel.html")}
    {:status 404 :headers {"Content-Type" "text/plain"} :body "Not Found"}))

(def app (-> handler wrap-webjars))
