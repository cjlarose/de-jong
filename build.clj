(require 'cljs.closure)

(cljs.closure/build "src"
    {:main 'de-jong.core
     :output-to "out/main.js"})
