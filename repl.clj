(require 'cljs.repl)
(require 'cljs.closure)
(require 'cljs.repl.browser)

(cljs.closure/build "src"
  {:main 'de-jong.core
   :output-to "out/main.js"
   :verbose true})

(cljs.repl/repl (cljs.repl.browser/repl-env)
  :watch "src"
  :output-dir "out")
