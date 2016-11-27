(defproject de_jong "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-3149"]
                 [org.omcljs/om "0.8.8"]
                 [cljsjs/three "0.0.70-0"]]
  :plugins [[lein-cljsbuild "1.0.5"]
            [lein-figwheel "0.2.9"]]
  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]
  :cljsbuild {
    :builds [{:id "dev"
              :source-paths ["src"]
              :figwheel true
              :compiler {
                :main de-jong.core
                :asset-path "js/compiled/out"
                :output-to "resources/public/js/compiled/de_jong.js"
                :output-dir "resources/public/js/compiled/out"
                :optimizations :none
                :source-map true
                :source-map-timestamp true
                :cache-analysis true}}
             {:id "release"
              :source-paths ["src"]
              :compiler {
                :main de-jong.core
                :output-to "resources/public/js/compiled/de_jong.js"
                :optimizations :simple
                :pretty-print false}}]}
  :figwheel {
    :css-dirs ["resources/public/css"] ;; watch and update CSS
  })
