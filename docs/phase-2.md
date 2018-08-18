# Introducción
En la fase 1 vimos como crear un proyecto de ClojureScript desde 0 y compilarlo con `lein-cljsbuild`. Al final se dijo que con eso es suficiente para comenzar a programar en ClojureScript. Sin embargo es muy tedioso tener que recompilar y recargar la página cada vez que se haga un cambio.

En la fase 2 vamos a ver cómo podemos hacer este proceso mucho más ágil e interactivo, agregando la capacidad de recargar nuestro código de manera dinámica (*hot-reload*), sin necesidad de recompilar manualmente, y sin necesidad de recargar la página en el navegador. En otras palabras, magia.

Bienvenido a la Fase 2. Manos a la obra.

## Fase 2 - Figwheel
La magia la proporciona [Figwheel](https://figwheel.org). ClojureScript tiene muchas características que lo hacen muy atractivo, pero Figwheel es un candidato serio al título de *killer application*. Como dice en su página web: Figwheel compila tu código ClojureScript y lo recarga en el navegador, al tiempo que escribes el código.

>Figwheel compila tu código ClojureScript y lo recarga en el navegador, al tiempo que escribes el código.

Cabe mencionar que recientemente se lanzó una nueva versión de Figwheel, llamada Figwheel Main. Es una reescritura completa de Figwheel, y se encuentra en desarrollo constante. Como se podrán imaginar, aún tiene camino por recorrer pero ya funciona bastante bien y tiene algunas características que lo hacen más atractivo que la versión anterior de Figwheel llamada lein-figwheel.

Originalmente este artículo se basaba en lein-figwheel, pero decidí cambiar a Figwheel Main ya que eventualmente reemplazará a lein-figwheel, y porque quería probar esta versión de Figwheel. Es completamente válido usar lein-fighweel, incluso es la opción más estable por ahora, pero su configuración es un poco más engorrosa que Figwheel Main.

Primero vamos a hacer algunos cambios sobre el `project.clj` de la Fase 1. Antes de continuar, vamos a agregar lo siguiente a nuestro `project.clj`: `:pedantic? :abort`. Además incrementamos la versión de Clojure de la `1.8.0` a la `1.9.0` ya que Figwheel Main depende de ella:

```
(defproject clojurescript-hard-way "0.1.0-SNAPSHOT"
  :plugins [[lein-cljsbuild "1.1.7"]]
  :source-paths ["src/clj"]

  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/clojurescript "1.10.339"]]

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
  :profiles {:uberjar {:aot :all}})
```

Lo que hace `:pedantic? :abort` es detener la ejecución en caso de encontrarse con un conflicto en las versiones de las librerías en las que depende nuestro proyecto. En un proyecto tan simple como el nuestro seguramente no tendremos conflictos, pero a medida que agregamos más dependencias esto es inevitable. Ahora ejecutamos `lein run` y debería de mostrarnos el famoso `Hello, World!`. Hasta aquí todo bien.

Ahora vamos a empezar a configurar Figwheel Main. Pero antes de hacer esto, necesitamos agrear distintos perfiles a nuestro `project.clj` muy similar a lo que hicimos en la Fase 1 con los perfiles de `cljsbuild`.

### Perfiles
Los perfiles funcionan básicamente igual que los *builds* de `cljsbuild`. Es decir, podemos tener un perfil "dev", uno "test" y uno "prod", y cada uno declarando distintas dependencias y configuraciones. Esto es necesario ya que Figwheel es una herramienta que queremos usar en desarrollo, pero definitivamente no la queremos en producción.

La manera en la que se definien perfiles es por medio de la llave `:profiles` en la raíz de nuestro `project.clj`. De hecho si se fijan ya tenemos uno declarado: `:profiles {:uberjar {:aot :all}}`.

El perfil `:uberjar`, como su nombre lo indica, nos permite crear un *uberjar* de nuestro proyecto, que contiene todas las dependencias incluídas en el mismo `.jar`. En este caso le estamos especificando a `leiningen` que cuando hagamos un *uberjar*, compile nuestro código _al momento_ (*ahead of time*), en vez de hacerlo _al vuelo_ (*on-the-fly*). Esto es necesario por ejemplo si se desea que nuestro *uberjar* no incluya el código fuente.

Podemos tener múltiples perfiles, pero hay algunos perfiles que son especiales y se activan en momentos específicos. Ya vimos el primero de ellos: `:uberjar`. Este perfil se activa en el momento que usamos el comando `lein uberjar`. Así como `:uberjar` hay otros perfiles:

- `:user`: se utiliza para especificar dependencias o plugins utilizados durante el desarrollo, pero para _todos_ nuestros proyectos. Este perfil _no_ se declara en el `project.clj` ya que ese archivo es por proyecto. En vez de esto, se utiliza un archivo `~/.lein/profiles.clj~. `leiningen` automáticamente lee ese archivo y lo combina con el resto de los perfiles.
- `:dev`: se utiliza para especificar dependencias y configuración que solo se utilizan para compilar o para ejecutar pruebas.
- `:default`: especifica los perfiles que están activos por defecto cuando se ejecutan tareas con `leiningen`. Si no se configura manualmente entonces se crea una lista de perfiles con la siguiente prioridad:
 - `:base`
 - `:system`
 - `:user`
 - `:provided`
 - `:dev`

Como vemos en la lista anterior hay otros perfiles que no explicamos aquí, pero quedan fuera del alcance de este artículo. Lo importante de los perfiles es que pueden cambiar la configuración de nuestro proyecto, dependiendo del perfil que esté activo y la tarea que se esté ejecutando (como `:test` y `:uberjar`).

### Continuando con Figwheel Main
Volviendo a la tarea de configuración de Figwheel Main, vamos a crear un perfil `:dev` y a especificar las dependencias que necesitamos:

```
:profiles {:uberjar {:aot :all}
           :dev {:dependencies [[com.bhauman/figwheel-main "0.1.7"]
                                [com.bhauman/rebel-readline-cljs "0.1.4"]
                                [org.clojure/clojurescript "1.10.339"]]}}
```

Como se explicó anteriormente, el perfil `:dev` es para especificar dependencias y configuración que solo se utiliza para compilar o para ejecutar pruebas. En nuestro caso `figwheel-main` y `clojurescript` solo los necesitamos para correr el repl, o para compilar el programa final. Echando un vistazo a nuestro `project.clj`:

```
(defproject clojurescript-hard-way "0.1.0-SNAPSHOT"
  :plugins [[lein-cljsbuild "1.1.7"]]

  :source-paths ["src/clj" "src/cljs"] ;; se agregó "src/cljs"
  :resource-paths ["resources"] 

  :dependencies [[org.clojure/clojure "1.9.0"]] ;; se incrementó a "1.9.0" y quitamos ClojureScript de aquí para pasarlo al perfil :dev

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
   :dev {:dependencies [[com.bhauman/figwheel-main "0.1.7"] ;; se agregó el pefil :dev con las dependencias de Figwheel Main
                        [com.bhauman/rebel-readline-cljs "0.1.4"]
                        [org.clojure/clojurescript "1.10.339"]]}})
```

Ya estamos cerca de terminar la configuración de Figwheel Main. Lo que nos restan son 2 archivos más: `figwheel-main.edn` y `dev.cljs.edn`. Ambos archivos se complementan. El primero `figwheel-main.edn` especifica opciones generales de Figwheel Main, mientras que el segundo incluye configuración adicional para un perfil específico, en este caso `dev`. Es posible sobreescribir configuración que está especificada en `figwheel-main.edn` desde el archivo de perfil `dev.cljs.edn`.

Primero veamos la estructura de nuestro directorio hasta ahora:

```
├── dev.cljs.edn
├── figwheel-main.edn
├── project.clj
├── resources
│   └── public
│       ├── index.html
│       └── js
├── src
    ├── clj
    │   └── clojurescript_hard_way
    │       └── core.clj
    └── cljs
        └── clojurescript_hard_way
            └── core.cljs
```

Truncado para resaltar lo importante. Tenemos entonces `figwheel-main.edn` y `dev.cljs.edn` en la raíz de nuestro proyecto. Veamos primero el contenido de `figwheel-main.edn`:

```
{:target-dir "resources"
 :watch-dirs ["src/cljs"]
 :open-url false}
```

Hay muchas más opciones disponibles que las pueden consultar en [la documentación oficial](https://github.com/bhauman/figwheel-main/blob/master/doc/figwheel-main-options.md). Por ahora lo importante es resaltar lo siguiente:
- `:target-dir` es el directorio donde Figwheel Main va a guardar los archivos intermedios generados por la compilación.
- `:watch-dirs` contiene una lista de directorios que Figwheel Main va a monitorear y en caso de detectar cambios, recompilar y recargar el código.
- `:open-url` está como `false` porque generalmente tengo mi navegador de desarrollo abierto, y me desagrada que se abran programas sin mi explícito consentimiento.

Por último, el contenido de `dev.cljs.edn`:

```
{:main clojurescript-hard-way.core}
```

Simplemente especificamos el punto de entrada. Esa es toda la configuración necesaria.

### Ejecutando Figwheel Main
Para ejecutar Figwheel Main, necesitamos hacer uso de un comando de `leiningen` llamado `trampoline`. Para entender lo que hace `trampoline` es necesario saber algo del funcionamiento interno de `leiningen`, y aunque es muy interesante, por ahora nos vamos a tener que conformar con saber que nos permite ejecutar el punto de entrada de Figwheel Main. El comando completo es:

    lein trampoline run -m figwheel.main

Pero si lo ejecutamos así, no va a tomar nuestro perfil `:dev` que tanto trabajo nos costó implementar. Para decirle el perfil:

    lein trampoline run -m figwheel.main -- -b dev

Si ya el comando anterior era algo engorroso, este ha sido la gota que derramó el vaso. Entonces para simplificarlo vamos a usar un alias. En nuestro `project.clj` a nivel raíz:

```
:aliases {"fig" ["trampoline" "run" "-m" "figwheel.main"]
          "fig-dev" ["trampoline" "run" "-m" "figwheel.main" "-b" "dev" "-r]}
```

El primer alias `fig` ejecuta Figwheel Main en su punto de entrada. El segundo alias `fig-dev` igual ejecuta Figwheel Main en su punto de entrada, pero además especifica el perfil `:dev` y finalmente `-r` para ejecutar un REPL de ClojureScript. Con estos aliases podemos ejecutar Figwheel Main de la siguiente manera:

    lein build-dev

Mucho mejor.

Revisemos lo que hemos hecho hasta ahora:

1. Creamos un perfil `:dev`.
2. Movimos la dependencia de ClojureScript al nuevo perfil, y agregamos las dependencias de Figwheel Main.
3. Incrementamos la versión de Clojure a la `1.9.0` ya que Figwheel Main depende de ello.
3. Configuramos Figwheel Main creando `figwheel-main.edn` y `dev.cljs.edn`.
4. Creamos _aliases_ para ejecutar Figwheel Main.

Y demos un último vistazo al `project.clj` completo:

```
(defproject clojurescript-hard-way "0.1.0-SNAPSHOT"
  :plugins [[lein-cljsbuild "1.1.7"]]

  :source-paths ["src/clj" "src/cljs"]
  :resource-paths ["resources" "target"]

  :dependencies [[org.clojure/clojure "1.9.0"]]

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
                        [org.clojure/clojurescript "1.10.339"]]}})

```

Es hora de ejecutar Figwheel Main:

    lein fig-dev

Y nos debe responder con algo similar a esto:

```
[Figwheel] Validating figwheel-main.edn
[Figwheel] figwheel-main.edn is valid!
[Figwheel] Compiling build dev to "resources/public/cljs-out/dev-main.js"
[Figwheel] Successfully compiled build dev to "resources/public/cljs-out/dev-main.js" in 0.504 seconds.
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
2018-08-17 18:14:03.002:INFO::main: Logging initialized @6840ms
```

Es bastante explícito. Algo importante a notar es que Figwheel Main tiene su propia ruta en donde genera el código JavaScript compilado:

    [Figwheel] Successfully compiled build dev to "resources/public/cljs-out/dev-main.js" in 0.504 seconds.

Recordando la Fase 1, el código JavaScript se encontraba en `resources/public/cljs-out/compiled/cshard-dev.js`. Entonces si intentamos cargar la página `index.html` como la tenemos de la Fase 1, esta no va a funcionar correctamente. Necesitamos actualizar la referencia a la nueva ruta:

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

### Resultado
Finalmente podemos ver el fruto de nuestro trabajo. En la salida de Figwheel menciona lo siguiente:

    [Figwheel] Starting Server at http://localhost:9500

Figwheel incluye su propio servidor Web. Lo que queda es apuntar nuestro navegador a `http://localhost:9500` y verificar que nuestro sitio carga correctamente:

<<insertar imagen>>

Pero hasta aquí ya habíamos llegado en la Fase 1. La diferencia viene a continuación. Como se dice coloquialmente "una imagen dice más que mil palabras":

<<insertar gif>>

Ahora podemos hacer cambios a nuestro código ClojureScript, y Figwheel Main se encarga de compilarlo, notificarle al navegador que hay cambios, y finalmente recargar el código. Todo sin salir del editor y sin necesidad de recargar la página. Y no solo funciona con cambios a ClojureScript, sino también recarga cambios a HTML, JavaScript y CSS.

## Palabras Finales
Con esta configuración tendremos una experiencia de desarrollo muy superior a lo logrado en la Fase 1. Recordando, en la Fase 1 vimos cómo crear un proyecto básico de ClojureScript. En la Fase 2 acabamos de ver cómo configurar Figwheel Main para dinámicamente compilar y recargar el código. Hasta aquí podríamos dejarlo y ser felices con nuestro _devenv_, pero como nos gusta la mala vida vamos por más.

En la Fase 3 veremos cómo conectar nuestro editor (ya sea Emacs o... Emacs) a un REPL de ClojureScript para poder evaluar código en el _runtime_ del navegador, directamente desde nuestro editor.