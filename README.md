start a REPL

    rlwrap java -cp cljs.jar:src clojure.main repl.clj

build

    java -cp cljs.jar:src clojure.main build.clj

watch

    java -cp cljs.jar:src clojure.main watch.clj

To Do:

* [ ] Get the REPL working again
* [ ] Get some nice circular slides (like http://www.toolitup.com/circular-slider.html)
* [ ] look into changing the `globalCompositeOperation` to avoid manual overlay blending (https://developer.mozilla.org/en-US/docs/Web/API/CanvasRenderingContext2D/globalCompositeOperation)
* [ ] Smooth animations from one IFS to another -- hopefully get rid of the "draw" button
