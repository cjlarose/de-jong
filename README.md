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
* [ ] look into changing the `globalCompositeOperation` to avoid manual overlay blending (https://developer.mozilla.org/en-US/docs/Web/API/CanvasRenderingContext2D/globalCompositeOperation)
* [ ] Smooth animations from one IFS to another -- hopefully get rid of the "draw" button
