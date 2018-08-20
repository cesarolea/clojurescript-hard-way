# Introducción
Esta es la Fase 3 de la serie ClojureScript, sin atajos. El objetivo de la serie es aprender cómo se configura un proyecto ClojureScript con `leiningen`, y como interactúan todas las partes para crear un ambiente de desarrollo ágil y dinámico. Pero lo más importante es aprender a configurarlo para adaptarlo a nuestras necesidades.

## Recapitulando
Al final de la [Fase 2](https://blog.devz.mx/clojurescript-sin-atajos-fase-2/) nos quedamos con un proyecto ClojureScript y Figwheel Main configurado. Si bien esto es más que suficiente para ser productivos en ClojureScript, en la Fase 3 lo vamos a llevar un paso adelante.

En la Fase 3 vamos a entender cómo conectar nuestro editor a un REPL de ClojureScript, para poder enviar código ClojureScript desde nuestro editor para ser evaluado por el _runtime_ de JavaScript en el navegador.

Bienvenido a la Fase 3. Manos a la obra.

# Fase 3 - REPL REPL REPL
El lema de Clojure debería ser "ven por los paréntesis, quédate por el REPL" o algo por el estilo. Recuerdo mi primer acercamiento a _Common Lisp_ que, gracias a [SLIME](https://common-lisp.net/project/slime/), programar en el se sentía como tener superpoderes. Era un concepto nuevo, extraño y fascinante. En vez de escribir un programa, compilarlo e instalar el binario en la computadora, el programa va tomando forma, **evolucionando** con cada función que evalúas en el REPL, y al final guardas la imagen que contiene el programa y eso es lo que instalas. En producción tienes acceso a la imagen, al REPL, y puedes evaluar nuevo código, por ejemplo para investigar o arreglar errores críticos.

>Era un concepto nuevo, extraño y fascinante.

El REPL de Clojure, aunque tiene algunas desventajas con respecto al de Common Lisp (SBCL en particular es el que llegué a utilizar), se vuelve igual una herramienta indispensable. Lo mismo para ClojureScript. Hasta ahora no hemos visto nada de REPL en nuestro proyecto, pero eso va a cambiar aquí y ahora.

En realidad si tenemos un REPL, o varios REPLs:
1. En el navegador tenemos un REPL: la consola de JavaScript. Aquí podemos introducir código JavaScript, el navegador lee (Read) el comando, Evalúa el comando (Eval), imprime el resultado (Print) y regresa el ciclo al estado inicial esperando otro comando (Loop). El problema es que solo acepta JavaScript y no ClojureScript.
2. Figwheel Main incluye su propio REPL. Este pequeño detalle lo dejé pasar en la Fase 2, pero lo vamos a ver a continuación. El problema de este REPL supone que se está ejecutando en una interfaz de teletipo, o sea que la única manera de evaluar código es copiando y pegando trozos de nuestro programa en el REPL. Está bien para una o dos instrucciones simples, pero se vuelve tedioso y propenso a errores.

## El REPL de Figwheel Main
Fighweel Main contiene un REPL. Recordemos el comando que usamos para lanzar Figwheel:

    lein trampoline run -m figwheel.main -- -b dev -r

Ese `-r` es para indicarle a Figwheel que queremos un REPL. Y de hecho la salida de Figwheel Main incluye lo siguiente:

```
[Figwheel] Starting REPL
Prompt will show when REPL connects to evaluation environment (i.e. a REPL hosting webpage)
```

Entonces aunque no lo haya mencionado explícitamente, el REPL ya estaba ahí desde la Fase 2. Al compilar nuestro código, Figwheel incluye código que abre una conexión por _websocket_ hacia nuestro servidor. Cuando el proceso de Figwheel Main detecta esta conexión, nos muestra un _prompt_ en donde podemos evaluar ClojureScript. Es decir, el REPL. Veamos:

```
lein fig-dev
[Figwheel] Validating figwheel-main.edn
[Figwheel] figwheel-main.edn is valid!
[Figwheel] Compiling build dev to "resources/public/cljs-out/dev-main.js"
[Figwheel] Successfully compiled build dev to "resources/public/cljs-out/dev-main.js" in 0.621 seconds.
[Figwheel] Watching and compiling paths: ("src/cljs") for build - dev
[Figwheel] Starting Server at http://localhost:9500
[Figwheel] Starting REPL
Prompt will show when REPL connects to evaluation environment (i.e. a REPL hosting webpage)
Figwheel Main Controls:
          (figwheel.main/stop-builds id ...)  ;; stops Figwheel autobuilder for ids
          (figwheel.main/start-builds id ...) ;; starts autobuilder focused on ids
          (figwheel.main/reset)               ;; stops, cleans, reloads config, and starts autobuilder
          (figwheel.main/build-once id ...)   ;; builds source one time
          (figwheel.main/clean id ...)        ;; deletes compiled cljs target files
          (figwheel.main/status)              ;; displays current state of system
Figwheel REPL Controls:
          (figwheel.repl/conns)               ;; displays the current connections
          (figwheel.repl/focus session-name)  ;; choose which session name to focus on
In the cljs.user ns, controls can be called without ns ie. (conns) instead of (figwheel.repl/conns)
    Docs: (doc function-name-here)
    Exit: :cljs/quit
 Results: Stored in vars *1, *2, *3, *e holds last exception object
[Rebel readline] Type :repl/help for online help info
2018-08-18 19:41:19.807:INFO::main: Logging initialized @7800ms
ClojureScript 1.10.339
cljs.user=> ;; aquí podemos evaluar ClojureScript
```

Es importante recalcar que el _prompt_ sólo va a aparecer hasta que nuestra página se conecte con el proceso de Figwheel Main. Este es uno de los problemas más frecuentes que tienen los principiantes.

>el _prompt_ sólo va a aparecer hasta que nuestra página se conecte con el proceso de Figwheel Main

## nREPL
Pero en el mundo de Clojure tenemos `nREPL`. Este es un _network REPL_ con una arquitectura cliente/servidor que, en pocas palabras, nos permite evaluar código Clojure en ambientes remotos. nREPL es lo que usan la mayoría de los ambientes de desarrollo para Clojure, como cider, Atom, fireplace (para vim), Cursive e incluso el mismísimo leiningen.

Y nREPL es lo que queremos usar nosotros.

Vale la pena aclarar de una vez que en este artículo vamos a usar Emacs con cider como cliente nREPL, pero la configuración es la misma para cualquier ambiente.

En la Fase 1 hacía el comentario que una de las ventajas de hacer la configuración desde 0 es que podemos adaptar el proyecto a nuestras necesidades únicas. Dicho de otra manera, hacer que las herramientas se adapten a nuestro flujo de trabajo, y no a revés. También comentaba que es mucho más sencillo utilizar alguna de las recetas existentes de leiningen para ClojureScript, como por ejemplo [Chestnut](https://github.com/plexus/chestnut). Si bien no tiene nada de malo utilizarlas (y de hecho constantemente hago uso de ellas para aprender cómo hacer cierta configuración que quiero incluir en mis proyectos), si tratamos de desviarnos un poco del camino trazado por la receta, nos metemos en problemas por no saber cómo funcionan.

Uno de esos "caminos trazados" por la gran mayoría de las recetas para ClojureScript es, por coincidencia, la conexión con nREPL. Al parecer lo más común es lanzar el REPL desde Emacs, y dejar que cider se conecte automáticamente al proceso. Pero yo prefiero lanzar mis REPLs desde una terminal separada de Emacs y mantener esa separación en todo momento. Eso es lo que vamos a hacer a continuación.

Primero lanzamos un REPL desde leiningen. Este REPL es de **Clojure** y no **ClojureScript**. Es muy importante tener en cuenta que no es lo mismo y no son intercambiables. No puedes evaluar ClojureScript en un REPL de Clojure.

>No puedes evaluar ClojureScript en un REPL de Clojure.

    lein repl :headless :port 31337

Con el comando anterior iniciamos un REPL de Clojure en el puerto 31337. El parámetro `:headless` le dice a leiningen que no queremos un _prompt_, ya que el prompt lo vamos a tener una vez que conectemos nuestro cliente nREPL (cider en este caso) al servidor nREPL que acabamos de iniciar.

```
lein repl :headless :port 31337
nREPL server started on port 31337 on host 127.0.0.1 - nrepl://127.0.0.1:31337
```

<<insertar gif>>

Así de sencillo es conectarnos a nREPL. Pero este REPL es de Clojure, no ClojureScript. ¿Qué sucede si intentamos evaluar ClojureScript?

<<insertar gif>>

Cider en este caso nos advierte que si intentamos evaluar ClojureScript, necesitamos estar conectados a un REPL de ClojureScript. Figwheel tiene un REPL de ClojureScript, pero este REPL no es compatible con nREPL entonces cider no se puede conectar. Demonios. Si tan solo existiera algo que "convirtiera" una sesión de nREPL en una sesión de REPL de ClojureScript...

Les presento a [Piggieback](https://github.com/nrepl/piggieback). Este _middleware_ nos permite utilizar el REPL de ClojureScript desde una sesión de nREPL, permitiendo a cider (nuestro cliente nREPL) conectarse como siempre, pero evaluar código ClojureScript. Afortunadamente instalarlo es muy sencillo. vamos a modificar el perfil `:dev` y agregar dos cosas: la dependencia de Piggieback, y una configuración especial que le dice a leiningen que agregue el _middleware_ cuando lanzamos un REPL con `lein repl`. Nuestros perfiles en `project.clj` nos quedan así:

```
:profiles
{:uberjar {:aot :all}
 :dev {:dependencies [[com.bhauman/figwheel-main "0.1.7"]
                      [com.bhauman/rebel-readline-cljs "0.1.4"]
                      [org.clojure/clojurescript "1.10.339"]
                      [cider/piggieback "0.3.8"] ;; agregamos la dependencia a Piggieback
                     ]
       :repl-options {:nrepl-middleware [cider.piggieback/wrap-cljs-repl]}}} ;; decimos a lein repl que agregue el middleware
```

Lanzamos el REPL como antes:

    lein repl

Y nos conectamos con nuestro cliente nREPL. Hasta aquí sin novedad. Pero en el fondo, leiningen descargó la dependencia y agregó el middleware a la sesión de nREPL. Esto quiere decir que la sesión de nREPL a la que estamos conectados tiene un _je ne se quoi_ que la hace diferente. Veamos la siguiente sesión de nREPL (el cliente es cider en Emacs):

```
;; Connected to nREPL server - nrepl://localhost:31337
;; CIDER 0.17.0 (Andalucía), nREPL 0.2.13
;; Clojure 1.9.0, Java 1.8.0_111
;;     Docs: (doc function-name)
;;           (find-doc part-of-name)
;;   Source: (source function-name)
;;  Javadoc: (javadoc java-object-or-class)
;;     Exit: <C-c C-q>
;;  Results: Stored in vars *1, *2, *3, an exception in *e;
clojurescript-hard-way.core> (require 'figwheel.main.api)
nil
clojurescript-hard-way.core> (figwheel.main.api/start "dev")
[Figwheel] Validating figwheel-main.edn
[Figwheel] figwheel-main.edn is valid!
[Figwheel] Compiling build dev to "resources/public/cljs-out/dev-main.js"
[Figwheel] Successfully compiled build dev to "resources/public/cljs-out/dev-main.js" in 0.488 seconds.
[Figwheel] Watching and compiling paths: ("src/cljs") for build - dev
[Figwheel] Starting Server at http://localhost:9500
[Figwheel] Starting REPL
Prompt will show when REPL connects to evaluation environment (i.e. a REPL hosting webpage)
Figwheel Main Controls:
          (figwheel.main/stop-builds id ...)  ;; stops Figwheel autobuilder for ids
          (figwheel.main/start-builds id ...) ;; starts autobuilder focused on ids
          (figwheel.main/reset)               ;; stops, cleans, reloads config, and starts autobuilder
          (figwheel.main/build-once id ...)   ;; builds source one time
          (figwheel.main/clean id ...)        ;; deletes compiled cljs target files
          (figwheel.main/status)              ;; displays current state of system
Figwheel REPL Controls:
          (figwheel.repl/conns)               ;; displays the current connections
          (figwheel.repl/focus session-name)  ;; choose which session name to focus on
In the cljs.user ns, controls can be called without ns ie. (conns) instead of (figwheel.repl/conns)
    Docs: (doc function-name-here)
    Exit: :cljs/quit
 Results: Stored in vars *1, *2, *3, *e holds last exception object
2018-08-19 08:43:48.673:INFO::nREPL-worker-0: Logging initialized @26055ms
To quit, type: :cljs/quit
nil
cljs.user> js/document
#object[HTMLDocument [object HTMLDocument]]
cljs.user> :cljs/quit
nil
clojurescript-hard-way.core> js/document
CompilerException java.lang.RuntimeException: No such namespace: js, compiling:(*cider-repl localhost*:1:8045) 
clojurescript-hard-way.core>
```

Lo que sucedió en esta sesión de nREPL es lo siguiente:
1. Primero lanzamos un servidor nREPL con `lein repl` (esta parte no se ve en el listado anterior).
2. Nos conectamos al servidor nREPL con el cliente nREPL cider.
3. Incluímos la dependencia a `figwheel.main.api` con `(require 'figwheel.main.api)`.
4. Iniciamos el proceso de Figwheel con `(figwheel.main.api/start "dev")`.
5. En el browser, cargamos el sitio `localhost:9500`, lo que causa que Figwheel nos muestre el prompt del REPL de ClojureScript. En este punto nuestro REPL ya no es un REPL de Clojure, sino de ClojureScript.
6. Evaluamos `js/document` solo para comprobar que estamos en un REPL de ClojureScript.
7. Terminamos la sesión de REPL de ClojureScript con `:cljs/quit`. Nuestro REPL se convierte en un REPL de Clojure.
8. Intentamos evaluar `js/document` de nuevo para comprobar que estamos de vuelta en un REPL de Clojure.

También podemos lanzar el proceso de Figwheel **sin el REPL** en caso de que queramos manejarlos por separado:

```
(figwheel.main/start {:mode :serve} "dev") ;; {:mode :serve} le dice a figwheel que no inicie el REPL
(figwheel.main.api/cljs-repl "dev")
```

Es importante recalcar que Figwheel Main lanza su proceso de compilación en el fondo y no depende de ninguna manera del REPL de ClojureScript. Puedes entrar y salir (con `:cljs/quit`) del REPL de ClojureScript sin afectar a Figwheel de ninguna manera.

El resultado final es que podemos evaluar código ClojureScript directamente desde nuestro editor. Cómo evaluarlo ya depende del editor que estemos usando; para el caso de cider `C-c C-c` evalúa la forma que se encuentre debajo del cursor, y `C-c C-k` evalúa el buffer completo.

# Flujo de Trabajo
Con esta configuración, lo que normalmente hago es:
1. Iniciar un servidor nREPL con `lein repl :headless :port 31337`.
2. Conectarme a este nREPL con cider `C-c M-c`.
3. Iniciar el proceso de Figwheel Main. Mi _namespace_ por defecto ya incluye la referencia a `figwheel.main.api`, pero si no entonces `(require 'figwheel.main.api)` y después `(figwheel.main.api/start "dev")`.
4. Cargar la página en el navegador, que se encuentra en `localhost:9500`.

En mi caso no trabajo con ClojureScript muy seguido, así que el proceso no es muy engorroso y me permite iniciar Figwheel y el REPL de ClojureScript únicamente cuando los voy a necesitar. Sin embargo, si vamos a estar programando en ClojureScript diariamente entonces lo podemos simplificar aún más. Lo que vamos a hacer a continuación es utilizar [mount](https://github.com/tolitius/mount), una librería que nos permite definir ciertos estados (_states_) de nuestro programa, y después combinar (_compose_) un sistema completo a partir de estos estados. El ejemplo por excelencia es un _pool_ de conexiones a base de datos; creas un estado que mantiene el _pool_ de conexiones, y lo combinas con otros estados (como por ejemplo un servidor HTTP) para generar un sistema completo.

Primero creamos un directorio nuevo que va a contener un _namespace_ cuyo único propósito es contener código de soporte, es decir que no forma parte del sistema como tal, pero nos ayuda con tareas como iniciar y detener el sistema:

    mkdir -p env/dev/clj
    touch env/dev/clj/user.clj

Antes de mostrar el contenido de `user.clj`, vamos a agregar la librería mount como dependencia, y de una vez modificamos el perfil `:dev` para incluir el _path_ donde se va a guardar `user.clj`. En nuestro `project.clj`:

```
(defproject clojurescript-hard-way "0.1.0-SNAPSHOT"
  :plugins [[lein-cljsbuild "1.1.7"]]

  :source-paths ["src/clj" "src/cljs"]
  :resource-paths ["resources" "target"]

  :dependencies [[org.clojure/clojure "1.9.0"]
                 [mount "0.1.13"] ;; dependencia a mount
                ]

  :aliases {"fig" ["trampoline" "run" "-m" "figwheel.main"]
            "fig-dev" ["trampoline" "run" "-m" "figwheel.main" "-b" "dev" "-r"]}

  :cljsbuild {:builds
              [{:id "dev"
                :source-paths ["src/cljs"]
                :compiler {:main "clojurescript-hard-way.core"
                           :output-dir "resources/public/js/compiled/out"
                           :output-to "resources/public/js/compiled/cshard-dev.js"
                           :asset-path "js/compiled/out"
                           :optimizations :none}}
               {:id "min"
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
                        [cider/piggieback "0.3.8"]]
         :source-paths ["env/dev/clj"] ;; path a user.clj
         :repl-options {:init-ns user  ;; le indicamos al REPL que inicie en el nuevo namespace
                        :nrepl-middleware [cider.piggieback/wrap-cljs-repl]}}})
```

Lo siguiente es definir los estados. Esto lo vamos a hacer en `core.clj` que contiene nuestro _namespace_ principal:

```
(ns clojurescript-hard-way.core
  (:require [figwheel.main.api :as fw-api]
            [mount.core :refer [defstate]])
  (:gen-class))

(defstate ^{:on-reload :noop} figwheel
  :start (fw-api/start {:id "dev"
                        :options {:main 'clojurescript-hard-way.core}
                        :config {:target-dir "resources"
                                 :watch-dirs ["src/cljs"]
                                 :css-dirs []
                                 :open-url false
                                 :mode :serve}})
  :stop (fw-api/stop "dev"))

(defstate ^{:on-reload :noop} cljs-repl
  :start (fw-api/cljs-repl "dev"))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, I mean, Clojure!"))
```

Requerimos `defstate` de la librería `mount`. Con `defstate` definimos dos estados: uno para el proceso de Figwheel, y otro para el REPL de ClojureScript.

El lector astuto se dará cuenta que estamos duplicando la configuración que tenemos en `figwheel-main.edn` y `dev.cljs.edn`. En teoría no debería ser necesario, pero en mis pruebas no me fue posible lanzar Figwheel especificando `{:mode :serve}` y el perfil `dev`. Es decir, si trato de lanzar Figwheel de la siguiente manera:

   (fw-api/start {:mode :serve} "dev")

Figwheel ignora la configuración del perfil "dev". Tal vez sea un error en Figwheel Main, o tal vez sea un malentendido por mi parte sobre cómo se supone que funcione, pero por el momento resolvemos el problema especificando la configuración directamente en la llamada a Figwheel.

Finalmente nuestro `user.clj`:

```
(ns user
  (:require [figwheel.main.api :as fw-api]
            [mount.core :as mount]
            [clojurescript-hard-way.core]))

(defn start []
  (mount/start-without #'clojurescript-hard-way.core/cljs-repl))

(defn stop []
  (mount/stop-except))
```

Aquí simplemente creamos dos funciones: `start` y `stop`. De esta manera cuando iniciemos un REPL podemos ejecutar `(start)` y esto iniciará nuestro sistema completo (en este caso nuestro sistema solo incluye el proceso de Figwheel).

`start` específicamente evita iniciar `cljs-repl`. Esto no es estrictamente necesario, y pueden simplemente ejecutar `(mount/start)` en caso de querer siempre ejecutar tanto Figwheel como el REPL de ClojureScript. En mi caso prefiero tener el control de cada proceso por separado.

# Palabras Finales
En la Fase 3 explicamos cómo conectar nuestro editor (en realidad, un cliente nREPL) a un REPL de ClojureScript. Este es un paso más en la configuración de un ambiente de desarrollo que considero _ideal_. Como se ha repetido en múltiples ocasiones, lo importante no es replicar la configuración, sino entender el cómo para poder adaptarlo a nuestras necesidades.

Recordando, en la Fase 1 vimos cómo crear un proyecto básico de ClojureScript. En la Fase 2 vimos cómo configurar Figwheel Main para dinámicamente compilar y recargar el código. En la Fase 3 acabamos de ver cómo agregar soporte a nuestro proyecto para conectarnos al REPL de ClojureScript (proporcionado por Figwheel Main) por medio de un cliente nREPL (cider en nuestro caso).

En la Fase 4 vamos a tomar una ligera desviación. Hasta ahora las Fases se han centrado en configuración, mientras que la programación en ClojureScript queda del lado. En la Fase 4 vamos a ver cómo agregar soporte para [Reagent](https://reagent-project.github.io/) y [Re-Frame](https://github.com/Day8/re-frame). Reagent es una interfaz minimalista entre ClojureScript y [React](http://facebook.github.io/react/). Re-Frame nos ayuda proporcionando un ciclo de eventos (_event loop_) y algo de estructura para nuestro proyecto.

Cerramos con una demostración del poder infinito que adquirimos al configurar nuestro cliente nREPL.

<<insertar gif>>

# Enlaces
[Fase 1](https://blog.devz.mx/clojurescript-sin-atajos-fase-1/): Projecto básico y compilación con `lein-cljsbuild`.
[Fase 2](https://blog.devz.mx/clojurescript-sin-atajos-fase-2/): Figwheel.