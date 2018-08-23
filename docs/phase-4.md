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

Son puras funciones. Vamos a explicar paso a paso cada una, de abajo hacia arriba (y con la mano en la cintura si así lo desean). Primero la función `main`:

```
(defn ^:export main []
  (-> js/document
      (.getElementsByTagName "head")
      (aget 0)
      .-innerHTML
      (set! "<style>body{color:#FF0000; background-color:#1B1B1B;}</style>"))
  (mount-root))
```

Este es el punto de entrada a la aplicación, y se ejecuta cuando se carga la página que a su vez contiene nuestro código ClojureScript compilado. Aquí hay varias cosas que resaltar; primero está el hecho de que es una función, y así nomas porque si no va a ser ejecutada. Recordemos que todo esto se traduce (_transpila_) a JavaScript, entonces imaginemos que tenemos lo siguiente:

```
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
    </head>
    <body>
        <div id="app"></div>
        <script src="cljs-out/dev-main.js" type="text/javascript"></script>
    </body>
</html>
```

Al cargar el código JavaScript, lo que va a suceder es que se van a cargar varias funciones:

```
function doomguy-component() {...};
function title-component() {...};
function mount-root() {...};
function main() {...};
```

Pero no se va a ejecutar ninguna. Eso lo arreglamos fácilmente:

```
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
    </head>
    <body>
        <div id="app"></div>
        <script src="cljs-out/dev-main.js" type="text/javascript"></script>
        <script type="text/javascript">clojurescript_hard_way.core.main()</script>
    </body>
</html>
```

Simplemente le pedimos ejecutar la función `main`. Por eso la definición completa de 'main` incluye el metadato `^:export` que le dice al compilador que cuando haga sus optimizaciones respete el nombre de la función, porque si no lo hace entonces nuestro punto de entrada no va a funcionar. Después lo que viene es simplemente obtener el elemento `head` del documento y le asignamos una etiqueta `style` para que se vea bien profesional. Hasta aquí no hemos desplegado nada en pantalla, eso viene al final con la ejecución de `(mount-root)`.

```
(defn mount-root []
  (r/render [title-component]
            (.getElementById js/document "app")))
```

`mount-root` renderea un componente (que es otra función, `title-component`) en el elemento `app` `(.getElementById js/document "app")`. La función `r/render` ya es propia de Reagent. A su vez, `title-component` es una función que regresa el _markup_ del elemento que vamos a renderear:

```
(defn title-component []
  [:div "Activating God-Mode!"
   [:p [doomguy-component]]])
```

Eso es equivalente a:

```
<div>"Activating God-Mode!"<p><!-- Aqui va otro componente --></p></div>
```

Y por último el componente `doomguy-component` que igual es una función que regresa el _markup_ del componente:

```
(defn doomguy-component []
  [:img {:id "doomguy"
         :src "https://vignette.wikia.nocookie.net/wadguia/images/6/62/Godmode_face.png/revision/latest?cb=20141012222849"}])
```

Entonces programar una interfaz gráfica con Reagent es cuestión de ir armando componentes como bloques lego. Cada componente es una función. Y aquí es donde está el principal atractivo de Reagent: manipular la estructura de tu interfaz gráfica haciendo uso de [colecciones](https://blog.devz.mx/colecciones-en-clojure/) que ya conoces.

Pero Reagent es mucho más que sólo crear componentes con vectores y mapas. En realidad Reagent expone la mayoría de los eventos de React, además de ofrecer un mecanismo para que los componentes mantengan valores (estado) local, actualizándose automáticamente cuando este estado cambie. Por ejemplo el siguiente componente:

```
(defn doomguy-animation []
  (let [click-count (r/atom 0)]
    (fn []
      [:img {:id "doomguy"
             :src (str "images/doomguy-frame-"
                       (if (even? @click-count) "even" "odd")
                       ".png")
             :on-click #(swap! click-count inc)}])))
```

Este componente mantiene estado local en `click-count (r/atom 0)`. Pero no es un `atom` cualquiera, es uno de Reagent, y lo que hace es causar que cualquier componente de dependa de el se vuelva a pintar cuando el valor del `atom` cambie.

En nuestro caso el valor inicia en `0` y con cada click a la imagen vamos incrementando su valor, luego verificamos si es par o impar para generar la ruta completa del `sprite`:

- Par: `images/doomguy-frame-even.png`
- Impar: `images/doomguy-frame-odd.png`

El código completo nos quedaría así:

```
(ns clojurescript-hard-way.core
  (:require [reagent.core :as r]))

(defn doomguy-animation
  "Changes the image on click"
  []
  (let [click-count (r/atom 0)]
    (fn []
      [:img {:id "doomguy"
             :src (str "images/doomguy-frame-"
                       (if (even? @click-count) "even" "odd")
                       ".png")
             :on-click #(swap! click-count inc)}])))

(defn title-component []
  [:div "Activating God-Mode!"
   [:p [doomguy-animation]]])

(defn mount-root []
  (r/render [title-component]
            (.getElementById js/document "app")))

(defn ^:export main []
  ;; do some other init

  (-> js/document
      (.getElementsByTagName "head")
      (aget 0)
      .-innerHTML
      (set! "<style>body{color:#FF0000; background-color:#1B1B1B;}</style>"))

  (mount-root))
```

Lo único que hice fue agregar el componente `doomguy-animation` y modificar `title-component` para que lo cargue. Hay que verlo en funcionamiento:

<<insertar gif>>

Pues funciona, pero perdimos el _hot reload_. ¿Qué pasó?

Como comentábamos antes, Figwheel se encarga de compilar y recargar nuestro código en el navegador. Y si, efectivamente lo recarga, pero no hay nada que cause que Reagent vuelva a dibujar el componente `title-component` que modificamos. Al dar click modificamos el estado de `click-count` lo que causa que cambie la imagen, pero `title-component` no tienen ningún estado interno que cambie, entonces Reagent simplemente nunca ejecuta la función que representa el componente, y por lo tanto nunca se actualiza en la pantalla. En realidad es más complicado que eso, pero una discusión del ciclo de vida de los componentes de React queda fuera del alcance de este artículo.

La única razón de la existencia de la Fase 4 es precisamente cómo resolver este problema. Si lo que quieren es aprender a utilizar Reagent y/o Re-Frame, la documentación oficial de ambos repositorios es excelente (incluyendo la de Re-Frame, que poco a poco han bajándole 2 rayitas y centrándose en lo que es realmente importante). Ese no es nuestro objetivo. El objetivo es dejar listo nuestro ambiente de desarrollo, y ustedes como yo se van a topar con el mismo problema. Así que veamos como resolverlo.

>La única razón de la existencia de la Fase 4 es precisamente cómo resolver este problema.

¿Listos?

Figwheel Main contiene [mecanismos](https://figwheel.org/docs/reloadable_code.html#setup-and-teardown-pattern) específicamente para cuando estamos alterando el DOM directamente, pero queremos mantener la recarga de código. Su uso es muy sencillo: solo agregamos algo de metadatos que serán leídos por Figwheel Main y ya. Primero marcamos nuestro _namespace_ para indicarle a Figwheel que hay _hooks_ en ese _namespace_:

```
(ns ^:figwheel-hooks clojurescript-hard-way.core
  (:require [reagent.core :as r]))
```

Y luego le decimos qué queremos que ejecute cuando termine de cargar la página:

```
(defn ^:after-load mount-root []
  (r/render [title-component]
            (.getElementById js/document "app")))
```

Como se podrán imaginar también hay un `^:before-load` pero en este caso no hacemos uso de él. Es un patrón de _setup_ y _teardon_, similar a lo que seguramente han usado en sus pruebas unitarias que seguro hacen siempre y sin falta, ¿verdad?

Entonces agregamos estos metadatos y:

<<insertar gif>>

Como diría un célebre maestro de la carrera _así si baila 'mija con el señor_. Tenemos Reagent y _hot reload_ juntos en plena armonía.

## Re-Frame
Re-Frame construye sobre lo que nos deja Reagent, es decir _markup_ para componentes usando vectores y mapas, `atom`s reactivos que actualizan los componentes que dependen de ellos, y agrega un ciclo de eventos (_event loop_) y algo de estructura. Re-Agent nos da las herramientas y suficiente cuerda para ahorcarnos con ella, y Re-Frame viene a poner orden con sus ideas estrictas sobre cómo y donde.

Ya con la estructura que tenemos en el `project.clj` agregar soporte para Re-Frame es solo cuestión de agregar la dependencia `[re-frame "0.10.5"]`:

```
(defproject clojurescript-hard-way "0.1.0-SNAPSHOT"
  :plugins [[lein-cljsbuild "1.1.7"]]

  :source-paths ["src/clj" "src/cljs"]
  :resource-paths ["resources" "target"]

  :dependencies [[org.clojure/clojure "1.9.0"]
                 [mount "0.1.13"]]

  :aliases {"fig" ["trampoline" "run" "-m" "figwheel.main"]
            "fig-dev" ["trampoline" "run" "-m" "figwheel.main" "-b" "dev" "-r"]}

  :cljsbuild {:builds
              [{:id "min"
                :source-paths ["src/cljs"]
                :compiler {:main "clojurescript-hard-way.core"
                           :output-to "resources/public/js/compiled/cshard-prod.js"
                           :closure-defines {goog.DEBUG false}
                           :optimizations :advanced
                           :pretty-print false}}]}

  :main ^:skip-aot clojurescript-hard-way.core
  :pedantic? :abort
  :target-path "target/%s"
  :profiles
  {:uberjar {:aot :all}
   :dev {:dependencies [[com.bhauman/figwheel-main "0.1.7"]
                        [com.bhauman/rebel-readline-cljs "0.1.4"]
                        [org.clojure/clojurescript "1.10.339"]
                        [org.clojure/tools.nrepl "0.2.13"]
                        [cider/piggieback "0.3.8" :exclusions [org.clojure/tools.logging]]
                        [reagent "0.8.1"]
                        [re-frame "0.10.5"]]
         :source-paths ["env/dev/clj"]
         :repl-options {:init-ns user
                        :nrepl-middleware [cider.piggieback/wrap-cljs-repl]}}})
```

Hay que resaltar dos cosas:
1. Re-Frame, al igual que Reagent, es una dependencia de tiempo de compilación y por lo tanto va en el perfil `dev`.
2. ¿Se acuerdan del modo `:pedantic? :abort`? En mi caso cuando agregué Re-Frame, leiningen me advirtió que había un conflicto entre `tools.logging` proporcionado por `piggieback`, y la misma librería en Re-Frame.

```
Possibly confusing dependencies found:
[re-frame "0.10.5"] -> [org.clojure/tools.logging "0.3.1"]
 overrides
[cider/piggieback "0.3.8"] -> [nrepl "0.4.3"] -> [org.clojure/tools.logging "0.4.1"]

Consider using these exclusions:
[cider/piggieback "0.3.8" :exclusions [org.clojure/tools.logging]]

Aborting due to :pedantic? :abort
```

En mi caso prefiero este comportamiento que toparme después con errores en tiempo de ejecución por versiones incompatibles. En este caso tomo la sugerencia de leiningen y agrego la exclusión a `piggieback`. En lo general se recomienda mantener la versión más antigua y si se fijan `piggieback` incluye la versión `0.4.1` mientras que Re-Frame la versión `0.3.1` de la misma librería.

Re-Frame es lo suficientemente grande como para merecer su propio artículo. De hecho Reagent igual, aquí solo tocamos la superficie. Por esta razón doy por concluida la Fase 4.

# Palabras Finales
El propósito principal de la Fase 4 fue mostrar cómo arreglar el _hot reload_ con Figwheel cuando agregamos Reagent y/o Re-Frame a nuestro proyecto. La mayoría de los que vienen a ClojureScript lo hacen para hacer _frontend_ y la popularidad de las librerías y _frameworks_ que utilizan React, me hizo considerar que este sería un punto de frustración para los que están iniciando este viaje.

La Fase 4 da por concluida la configuración del ambiente de desarrollo. La Fase 5 será una colección de trucos y consejos, principalmente sobre cómo tomar todo lo que hemos visto y montarlo detrás de una máquina virtual o contenedor. Porque por ahí alguien dijo:

>Jamás voy a volver a instalar un ambiente de desarrollo en mi computadora.

# Enlaces
[Fase 1](https://blog.devz.mx/clojurescript-sin-atajos-fase-1/)
[Fase 2](https://blog.devz.mx/clojurescript-sin-atajos-fase-2/)
[Fase 3](https://blog.devz.mx/clojurescript-sin-atajos-fase-3/)