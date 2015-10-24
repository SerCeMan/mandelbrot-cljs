(ns mandelbrot.core
  (:require [cljs-webgl.context :as context]
            [cljs-webgl.shaders :as shaders]
            [buf2]
            [re-com.core :refer [slider v-box h-box label]]
            [reagent.core :refer [render atom]]
            [cljs-webgl.constants.draw-mode :as draw-mode :refer [triangle-strip]]
            [cljs-webgl.constants.data-type :as data-type]
            [cljs-webgl.constants.buffer-object :as buffer-object :refer [array-buffer static-draw]]
            [cljs-webgl.constants.shader :as shader]
            [cljs-webgl.buffers :as buffers]
            [cljs-webgl.typed-arrays :as ta]))

(enable-console-print!)


(def vertex-shader-source
  "
    attribute vec3 verPos;
    varying vec2 coord;

    void main(void)
    {
      gl_Position = vec4 ( verPos, 1.0 );
      coord = gl_Position.xy;
    }
    ")

(def fragment-shader-source
  " #ifdef GL_ES
        precision highp float;
    #else
        precision mediump float;
    #endif

    varying vec2 coord;
    uniform float iters, radius, x_c, y_c, zoom;
    uniform sampler2D u_texture;

    void main() {
       float x = coord.x,  y = coord.y, refir = x_c + (gl_FragCoord.x / 500.0) * zoom;
       float imfir = y_c + (1.0 - gl_FragCoord.y / 500.0) * zoom,
             re0, im0, re=0.0, im = 0.0, r2 = 0.0, iter = 0.0;
       for (float i = 0.0; i < 1000.0; i += 1.0) {
           if (i >= iters) break;
           if (r2 >= radius) break;
           re0 = re;
           im0 = im;
           re = re0 * re0 - im0 * im0 + refir;
           im = 2.0 * re0 * im0 + imfir;
           r2 = (re * re) + (im * im);
           iter = i;
       }

       vec4 color;
       if (r2 >= radius) {
           gl_FragColor = vec4(1.0, 1.0, 1.0, 1.0);
       } else {
           float idx = iter / iters;
           color = texture2D(u_texture, vec2(idx, idx));
           gl_FragColor = color;
       }
    }
  ")

(def gl (context/get-context (.getElementById js/document "canvas")))
(def posBuffer
  (buffers/create-buffer gl (ta/float32 [1.0 1.0 1.0
                                         -1.0 1.0 1.0
                                         1.0 -1.0 1.0
                                         -1.0 -1.0 1.0])
                         array-buffer static-draw 3))

(def shader
  (shaders/create-program gl
                          (shaders/create-shader gl shader/vertex-shader vertex-shader-source)
                          (shaders/create-shader gl shader/fragment-shader fragment-shader-source)))



(defonce iter-count (atom 20.0))
(defonce radius (atom 5.0))
(defonce zoom (atom 2))
(defonce x (atom (* -0.75 @zoom)))
(defonce y (atom (* -0.5 @zoom)))
(defonce mouse_pos (atom [0 0]))
(defonce image (atom nil))

(defn load-image
  [url callback-fn]
  (let [img (js/Image.)]
    (set! (.-onload img) (fn [] (callback-fn img)))
    (set! (.-crossOrigin img) "anonymous")
    (set! (.-src img) url)))
(load-image "resources/logo.jpg" #(reset! image %))

(reagent.ratom/run!
  (when @image
    (tex/create-texture gl
                        :image @image
                        :parameters [[10241 9729]
                                     [10242 33071]
                                     [10243 33071]])))

(defonce canvas-mouse
         (do (.addEventListener
               (js/document.getElementById "canvas")
               "mousemove"
               (fn [e]
                 (reset! mouse_pos [(.-clientX e) (.-clientY e)]))
               nil)
             :default))

(defonce canvas-wheel
         (do (.addEventListener
               (js/document.getElementById "canvas")
               "wheel"
               (fn [e]
                 (let [delta (.-wheelDelta e)
                       [mx my] @mouse_pos
                       ml (if (pos? delta) (/ 3 4)
                                           (/ 4 3))]
                   (swap! x #(+ % (* (/ mx 600) @zoom (- 1 ml))))
                   (swap! y #(+ % (* (/ my 600) @zoom (- 1 ml))))
                   (swap! zoom #(* ml %))))
               nil)
             :default))

(defn drawgl []
  (fn []
    @image
    (-> gl
        (buffers/clear-color-buffer 0 0 0 0)
        (buf2/draw! :shader shader
                    :draw-mode triangle-strip
                    :count 4

                    :attributes
                    [{:buffer                posBuffer
                      :location              (shaders/get-attrib-location gl shader "verPos")
                      :components-per-vertex 3
                      :type                  data-type/float}]

                    :uniforms
                    [{:name "iters" :type :vfloat :values @iter-count}
                     {:name "radius" :type :vfloat :values @radius}
                     {:name "zoom" :type :vfloat :values @zoom}
                     {:name "x_c" :type :vfloat :values @x}
                     {:name "y_c" :type :vfloat :values @y}]))
    [:div]))

(defn root []
  [v-box
   :children
   [[h-box
     :children [[slider
                 :model iter-count
                 :min 1
                 :max 600
                 :on-change #(reset! iter-count (float %))]
                [label
                 :label (str "Iterations " @iter-count)]]]
    [h-box
     :children [[slider
                 :model radius
                 :min 1
                 :max 20
                 :on-change #(reset! radius (float %))]
                [label
                 :label (str "Radius " @radius)]]]
    [drawgl]]])

(defn ^:export start []
  (render [root] (js/document.getElementById "app")))