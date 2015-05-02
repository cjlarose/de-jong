(require 'cljs.closure)

(cljs.closure/watch "src"
  {:main 'de-jong.core
   :output-to "out/main.js"})
