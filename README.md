# The de Jong Attractor

The de Jong Attractor is a system paramerterized by four reals a, b, c, d ∈ [-π, π). For a given choice of these parameters and an initial point ⟨x<sub>0</sub>, y<sub>0</sub>⟩, the attractor yields a sequence of points where each point is defined in terms of the previous: f(x, y) = ⟨sin(ay) - cos(bx), sin(cx) - cos(dy)⟩.

The formula is quite simple, but plots of the de Jong Attractor can be quite [beautiful][bourke]. This project explores this beauty by presenting an interface that enables the exploration of different plots of the attractor. It animates between plots by linear interpolation on the four parameters.

It is implemented in ClojureScript with [Om][om] and [THREE.js][three], using custom GLSL vertex shaders for the calculation of points.

[bourke]: http://paulbourke.net/fractals/peterdejong/
[om]: https://github.com/omcljs/om
[three]: http://threejs.org/

## Usage

build

    lein cljsbuild once dev

watch

    lein cljsbuild auto dev

start a REPL

    rlwrap lein figwheel
    > (require '[de-jong.core :as de-jong])
