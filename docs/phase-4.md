# Introducción
Esta es la Fase 4 de la serie ClojureScript, sin atajos. El objetivo de la serie es aprender a configurar un ambiente de ClojureScript con `leiningen`, y adaptarlo a nuestras necesidades.

En la Fase 4 vamos a expandir nuestro proyecto para incluir no uno, sino dos librerías para _frontend_. Por un lado tenemos a [Reagent](https://reagent-project.github.io), una librería que proporciona una interfaz entre ClojureScript y [React](http://facebook.github.io/react/), y por otro lado tenemos a [Re-Frame](https://github.com/Day8/re-frame), un _framework_ que agrega un ciclo de mensajes (_event loop_) y algo de estructura a nuestro proyecto.

La configuración de ambas es muy sencilla. Basta con agregar la dependencias correctas al `project.clj` y listo. Sin embargo voy a compartir algunos consejos para mejorar nuestra experiencia programando con _Reagent_ y _Re-Frame_.

## Recapitulando
Al final de la [Fase 3](https://blog.devz.mx/clojurescript-sin-atajos-fase-3/) nos quedamos con un proyecto con Figwheel Main, y un REPL de ClojureScript completamente funcional. Además configuramos un cliente nREPL (cider en Emacs) para conectarse al REPL de ClojureScript, y poder evaluar código directamente en el navegador.

Uno de los usos más comunes de ClojureScript (de acuerdo a mi perspectiva, en realidad no tengo un estudio que lo respalde), es el de programar aplicaciones Web de las llamadas _Single Page Applications_ o SPA. A lo largo de los años he visto pasar múltiples librerías y _frameworks_ para programar SPAs, desde ExtJS con su patrón MVC y sus _widgets_ listos para usarse, hasta React con su DOM virtual, pasando por AngularJS, Meteor, Knockout, etc.

En ClojureScript podemos hacer uso de cualquiera de las librerías existentes, o podemos incluso inventar la nuestra...

O podemos dejarnos de cosas y usar React.

Bienvenido a la Fase 4. Manos a la obra.

# Fase 4 - Siendo Productivos
Hasta aquí hemos encontrado bastantes excusas para hacer todo excepto ponernos a trabajar. Si yo fuera su jefe, ya los hubiera corrido por procrastinar jugando con su ambiente de desarrollo, en vez de ponerse a programar la maldita aplicación de una buena vez.

Pero eso cambia en la Fase 4.

La selección de un _framework_ es una elección que no se debe tomar a la ligera, ya que afecta de manera fundamental la estructura de nuestro código y la cómo se implementará el proyecto completo. Como podrán imaginarse existe una variedad de _frameworks_ para ClojureScript, pero en general la comunidad de Clojure gravita hacia el uso de React con la convicción de que la programación funcional y las estructuras de datos inmutables propuestas por ClojureScript y React, mas las optimizaciones del compilador de Google dan por resultado aplicaciones con mayor rendimiento que si se utilizara React "puro".

En realidad yo no he comprobado esa cuestión del rendimiento. Mis razones para utilizar React son distintas: el paradigma de construcción de componentes de la interfaz gráfica que promueve React me es mucho más familiar a las aplicaciones con interfaz gráfica de antaño (AWT!), esto aunado al concepto de _data binding_, que básicamente te permite que tus componentes _reaccionen_ (_get it?_) ante cambios en su fuente de datos y se actualicen para representar estos nuevos datos. Cualquiera que haya sufrido lo suficiente bajo el yugo de JQuery me entenderá.

Si ya nos decidimos por el uso de React, ahora queda tomar la decisión sobre cómo usar React en ClojureScript. Aquí es donde entra Reagent y su primo Re-Frame.

Finalmente mi decisión de usar el combo de Reagent y Re-Frame se limita a mi propio entendimiento de ambos. Tras leer y experimentar con varios otros _frameworks_ (te estoy viendo a ti [Om Next](https://github.com/omcljs/om)), las ideas de Reagent y Re-Frame simplemente hicieron el proverbial _click_ en mi cerebro más rápido.

>las ideas de Reagent y Re-Frame simplemente hicieron el proverbial _click_ en mi cerebro más rápido.

Por mi parte haré lo posible por replicar ese _click_ en el cerebro de ustedes también.

## Reagent
En términos prácticos, Reagent es una envoltura de ClojureScript sobre React. Nos permite pasar de esto:

```
ReactDOM.render(
  <h1>Hola ClojureScript!</h1>,
  document.getElementById('app')
);
```

A esto:

```
(defn greeting-component []
  [:h1 "Hola ClojureScript!"])

(r/render [greeting-component]
          (.getElementById js/document "app"))
```

Lo se, no es ni más sencillo, ni más corto, pero no se trata de eso. Se trata de poder crear componentes de React usando funciones de ClojureScript. Las funciones utilizan vectores para representar elementos HTML. Lo interesante de esto es que, al ser estructuras de datos nativas de ClojureScript, podemos usar el mismo lenguaje para crearlas, manipularlas, y componerlas hasta construir toda nuestra interfaz gráfica a partir de componentes... que son vectores... que son estructuras de datos nativas de ClojureScript y por lo tanto el lenguaje ofrece todo lo necesario para trabajar con ellas.

Agregar soporte para Reagent a nuestro proyecto es tan sencillo como agregar la dependencia en nuestro `project.clj`:

```
:profiles {:uberjar {:aot :all}
           :dev {:dependencies [[com.bhauman/figwheel-main "0.1.7"]
                                [com.bhauman/rebel-readline-cljs "0.1.4"]
                                [org.clojure/clojurescript "1.10.339"]
                                [org.clojure/tools.nrepl "0.2.13"]
                                [cider/piggieback "0.3.8"]
                                [reagent "0.8.1"]] ;; agregamos reagent
                 :source-paths ["env/dev/clj"]
                 :repl-options {:init-ns user
                                :nrepl-middleware [cider.piggieback/wrap-cljs-repl]}}}
```

Estamos listos para usar Reagent. En `core.cljs`:

```
(ns clojurescript-hard-way.core
  (:require [reagent.core :as r]))

(defn doomguy-component []
  [:img {:id "doomguy"
         :src "https://vignette.wikia.nocookie.net/wadguia/images/6/62/Godmode_face.png/revision/latest?cb=20141012222849"}])

(defn title-component []
  [:div "Activating God-Mode!"
   [:p [doomguy-component]]])

(defn mount-root []
  (r/render [title-component]
            (.getElementById js/document "app")))

(defn ^:export main []
  (-> js/document
      (.getElementsByTagName "head")
      (aget 0)
      .-innerHTML
      (set! "<style>body{color:#FF0000; background-color:#1B1B1B;}</style>"))
  (mount-root))
```



# Enlaces
[Fase 1](https://blog.devz.mx/clojurescript-sin-atajos-fase-1/)
[Fase 2](https://blog.devz.mx/clojurescript-sin-atajos-fase-2/)
[Fase 3](https://blog.devz.mx/clojurescript-sin-atajos-fase-3/)