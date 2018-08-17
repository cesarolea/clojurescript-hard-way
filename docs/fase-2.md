# Introducción
En la fase 1 vimos como crear un proyecto de ClojureScript desde 0 y compilarlo con `lein-cljsbuild`. Al final se dijo que con eso es suficiente para comenzar a programar en ClojureScript. Sin embargo es muy tedioso tener que recompilar y recargar la página cada vez que se haga un cambio.

En la fase 2 vamos a ver cómo podemos hacer este proceso mucho más ágil e interactivo, agregando la capacidad de recargar nuestro código de manera dinámica (*hot-reload*), sin necesidad de recompilar manualmente, y sin necesidad de recargar la página en el navegador. En otras palabras, magia.

Bienvenido a la Fase 2. Manos a la obra.

## Fase 2 - Figwheel
La magia la proporciona [Figwheel](https://figwheel.org). ClojureScript tiene muchas características que lo hacen muy atractivo, pero Figwheel es un candidato serio al título de *killer application*. Como dice en su página web: Figwheel compila tu código ClojureScript y lo recarga en el navegador, al tiempo que escribes el código.

>Figwheel compila tu código ClojureScript y lo recarga en el navegador, al tiempo que escribes el código.

Cabe mencionar que recientemente se lanzó una nueva versión de Figwheel, llamada Figwheel Main. Es una reescritura completa de Figwheel, y se encuentra en desarrollo constante. Como se podrán imaginar, aún tiene camino por recorrer pero ya funciona bastante bien y tiene algunas características que lo hacen más atractivo que la versión anterior de Figwheel llamada lein-figwheel.

Originalmente este artículo se basaba en lein-figwheel, pero decidí cambiar a Figwheel Main ya que eventualmente reemplazará a lein-figwheel, y porque quería probar esta versión de Figwheel. Es completamente válido usar lein-fighweel, incluso es la opción más estable por ahora, pero su configuración es un poco más engorrosa que Figwheel Main.

Primero vamos a hacer algunos cambios sobre el `project.clj` de la Fase 1. Antes de continuar, vamos a agregar lo siguiente a nuestro `project.clj`: `:pedantic? :abort` de tal manera que nos queda así:

```
(defproject clojurescript-hard-way "0.1.0-SNAPSHOT"
  :plugins [[lein-cljsbuild "1.1.7"]]
  :source-paths ["src/clj"]

  :dependencies [[org.clojure/clojure "1.8.0"]
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

Volviendo a la tarea de configuración de Figwheel Main, vamos a crear un perfil `:dev` y a especificar las dependencias que necesitamos:

```

```