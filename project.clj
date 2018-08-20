(defproject clojurescript-hard-way "0.1.0-SNAPSHOT"
  :plugins [[lein-cljsbuild "1.1.7"]]

  :source-paths ["src/clj" "src/cljs"]
  :resource-paths ["resources" "target"]

  :dependencies [[org.clojure/clojure "1.9.0"]
                 [mount "0.1.13"]]

  :aliases {"fig" ["trampoline" "run" "-m" "figwheel.main"]
            "fig-dev" ["trampoline" "run" "-m" "figwheel.main" "-b" "dev" "-r"]}

  :cljsbuild {:builds
              [{:id "dev"
                :source-paths ["src/cljs"]
                :compiler {:main "clojurescript-hard-way.core"
                           :output-dir "resources/public/js/compiled/out"
                           :output-to "resources/public/js/compiled/cshard-dev.js"
                           :asset-path "js/compiled/out"
                           :optimizations :none}}
               {:id "min"
                :source-paths ["src/cljs"]
                :compiler {:main "clojurescript-hard-way.core"
                           :output-to "resources/public/js/compiled/cshard-prod.js"
                           :closure-defines {goog.DEBUG false}
                           :optimizations :advanced
                           :pretty-print false}}]}

  :main ^:skip-aot clojurescript-hard-way.core
  :pedantic? :abort
  :target-path "target/%s"
  :profiles
  {:uberjar {:aot :all}
   :dev {:dependencies [[com.bhauman/figwheel-main "0.1.7"]
                        [com.bhauman/rebel-readline-cljs "0.1.4"]
                        [org.clojure/clojurescript "1.10.339"]
                        [cider/piggieback "0.3.8"]]
         :source-paths ["env/dev/clj"]
         :repl-options {:init-ns user
                        :nrepl-middleware [cider.piggieback/wrap-cljs-repl]}}})
