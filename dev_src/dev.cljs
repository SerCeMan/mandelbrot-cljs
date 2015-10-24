(ns mandelbrot.dev
  (:require
    [mandelbrot.core]
    [figwheel.client :as fw]))

(fw/start {:websocket-url "ws://localhost:4449/figwheel-ws"
           :build-id      "dev"
           :on-jsload     mandelbrot.core/start})