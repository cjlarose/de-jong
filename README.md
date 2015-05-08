build

    lein cljsbuild once dev

watch

    lein cljsbuild auto dev

start a REPL

    rlwrap lein figwheel
    > (require '[de-jong.core :as de-jong])

To Do:

* [x] Get the REPL working again
* [ ] Get some nice circular sliders (like http://www.toolitup.com/circular-slider.html)
* [x] Smooth animations from one IFS to another -- hopefully get rid of the "draw" button
* [ ] Integrate with fancy WebGL or canvas library for better faster rendering
* [ ] Update URL with de jong paramters for sharing
