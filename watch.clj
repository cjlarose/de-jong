(require 'cljs.closure)

(cljs.closure/watch "src"
  {:main 'hello-world.core
   :output-to "out/main.js"})
