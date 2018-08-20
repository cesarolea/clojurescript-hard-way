(ns clojurescript-hard-way.core)

(defn toggle-doomguy
  "toggles the doomguy img"
  []
  (let [doomguy (-> js/document (.getElementById "doomguy"))
        visibility (-> doomguy .-style .-visibility)]
    (condp = visibility
      "hidden" (set! (-> doomguy .-style .-visibility) "visible")
      "visible" (set! (-> doomguy .-style .-visibility) "hidden")
      (set! (-> doomguy .-style .-visibility) "visible"))))

(-> js/document
    (.getElementsByTagName "head")
    (aget 0)
    .-innerHTML
    (set! "<style>body{color:#FF0000; background-color:#1B1B1B;}</style>"))

(-> js/document
    (.getElementById "app")
    (.-innerHTML)
    (set! "Activating God-Mode!</p><img id='doomguy' src='https://vignette.wikia.nocookie.net/wadguia/images/6/62/Godmode_face.png/revision/latest?cb=20141012222849' />"))

(defonce do-timer (js/setInterval toggle-doomguy 1000))
