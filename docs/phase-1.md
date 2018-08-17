# Introducción
La promesa de ClojureScript es simplemente Clojure... en el navegador. Demasiado bueno para ser verdad, ¿O no?

En realidad ClojureScript resuelve muchos de los problemas de JavaScript, como las inconsistencias de tipos, de valores booleanos, de precedencia de operadores, y reemplaza todo eso por un lenguaje con sintaxis mínima que es "compilado" a JavaScript. Además al igual que con Clojure y Java, ClojureScript permite llamar métodos de JavaScript, así como también exportar funciones de ClojureScript para que puedan ser llamadas directamente desde JavaScript.

Lo que puede llegar a ser bastante confuso y complicado es la configuración del ambiente de desarrollo. Si se va a iniciar un proyecto nuevo no hay tanto problema: basta con utilizar una de las recetas de `leiningen` que producen un proyecto listo para usarse. El problema está cuando necesitamos agregar ClojureScript a un proyecto existente.

Para remediar esto les presento una guía sobre cómo configurar el ambiente de desarrollo de ClojureScript, desde 0, sin atajos ni recetas. A la manera difícil. Lo vamos a hacer por fases, iniciando con simplemente compilar ClojureScript, y finalizando con tener un ambiente con recarga automática y ejecución de código directamente en la VM del navegador, todo esto en una VM con Vagrant o Docker. Si suena complicado es porque lo es.

En cada fase vamos a entender exactamente qué estamos utilizando y por qué es necesaria la configuración que estamos haciendo. Al final, lo importante es entender nuestro ambiente de desarrollo, para poder adaptarlo a nuestras necesidades.

Una cosa más, durante todo el ejercicio uso `leiningen` ya que es lo que uso en mis proyectos, y goza de bastante popularidad en la comunidad de Clojure y ClojureScript, por lo que es más sencillo encontrar documentación y ayuda si es necesario. Habiendo dicho esto, no es un requisito estricto el uso de `leiningen`, pero nada de la configuración que veremos a continuación funciona para otros *toolchains*.

Bienvenido a la Fase 1. Manos a la obra.

## Fase 1 - Proyecto Básico
En esta fase vamos a crear un proyecto con `leiningen`, agregar algo de ClojureScript, una página HTML que incluya el código ClojureScript compilado, y compilar el proyecto.

Para crear el proyecto con `leiningen`

    lein new app clojurescript-hard-way

Lo anterior nos crea la siguiente estructura de directorios:

    ├── CHANGELOG.md
    ├── LICENSE
    ├── README.md
    ├── doc
    │   └── intro.md
    ├── project.clj
    ├── resources
    ├── src
    │   └── clojurescript_hard_way
    │       └── core.clj
    └── test
        └── clojurescript_hard_way
            └── core_test.clj

Podemos borrar todo lo que no nos sirve:

    rm -r CHANGELOG.md LICENSE README.md doc/ test/

Lo que nos deja con lo siguiente:

    ├── project.clj
    ├── resources
    └── src
        └── clojurescript_hard_way
            └── core.clj

Ahora echemos un vistazo a `project.clj`

    (defproject clojurescript-hard-way "0.1.0-SNAPSHOT"
      :description "FIXME: write description"
      :url "http://example.com/FIXME"
      :license {:name "Eclipse Public License"
                :url "http://www.eclipse.org/legal/epl-v10.html"}
      :dependencies [[org.clojure/clojure "1.8.0"]]
      :main ^:skip-aot clojurescript-hard-way.core
      :target-path "target/%s"
      :profiles {:uberjar {:aot :all}})

Lo primero que vamos a hacer es agregar un *plugin* para `leiningen`. Este plugin nos va a permitir compilar ClojureScript a JavaScript ejecutando un comando de `leiningen`. El plugin es `[lein-cljsbuild]`(https://github.com/emezeske/lein-cljsbuild) y en realidad puede hacer mucho más, pero nosotros solo lo vamos a utilizar para esto.

Para instalar el plugin:

    :plugins [[lein-cljsbuild "1.1.7"]]

Entonces nuestro `project.clj` se ve así (eliminando todo lo que no es estrictamente necesario):

    (defproject clojurescript-hard-way "0.1.0-SNAPSHOT"
      :plugins [[lein-cljsbuild "1.1.7"]]
      :dependencies [[org.clojure/clojure "1.8.0"]]
      :main ^:skip-aot clojurescript-hard-way.core
      :target-path "target/%s"
      :profiles {:uberjar {:aot :all}})

Hasta aquí instalamos el plugin, pero en realidad no hace nada ya que necesita dos cosas más: código ClojureScript que compilar, y configuración para decirle en donde encontrarlo.

Si corremos el proyecto con `lein run` podemos ver que imprime `Hello, World!`. Este código está en el archivo `core.clj`. Pero esto es Clojure, no ClojureScript. Aún no hemos agregado ClojureScript, lo cual es lo que vamos a hacer a continuación.

Como nuestro proyecto fue creado con una receta genérica, en realidad no está preparado para compilar Clojure y ClojureScript. Lo que vamos a hacer a continuación es crear directorios separados donde guardar el código Clojure y ClojureScript:

    mkdir src/clj src/cljs
    mv src/clojurescript_hard_way/ src/clj/

Movemos el código existente al directorio `scr/clj`. Esto nos deja con la siguiente estructura:

    ├── project.clj
    ├── resources
    ├── src
    │   ├── clj
    │   │   └── clojurescript_hard_way
    │   │       └── core.clj
    │   └── cljs

Pero si intentamos correr el proyecto con `lein run` ahora falla. La razón es porque `leiningen` supone que el código fuente se encuentra bajo `src` y nosotros lo movimos a `src/clj`. Entonces le tenemos que indicar del cambio:

    (defproject clojurescript-hard-way "0.1.0-SNAPSHOT"
      :plugins [[lein-cljsbuild "1.1.7"]]
      :source-paths ["src/clj"]
      :dependencies [[org.clojure/clojure "1.8.0"]]
      :main ^:skip-aot clojurescript-hard-way.core
      :target-path "target/%s"
      :profiles {:uberjar {:aot :all}})

Ahora si, podemos volver a ejecutar `lein run` y sigue funcionando. Lo que es mejor, ya tenemos un lugar en donde guardar nuestro código ClojureScript. Ahora agregamos el código ClojureScript que vamos a compilar:

    mkdir src/cljs/clojurescript_hard_way
    touch src/cljs/clojurescript_hard_way/core.cljs

El contenido de `core.cljs`

    (ns clojurescript-hard-way.core)
    (js/console.log "dev")
    
    (-> js/document
        (.getElementById "app")
        (.-innerHTML)
        (set! "Hola ClojureScript!"))

Lo que hicimos fue crear el archivo `src/cljs/clojurescript_hard_way/core.cljs`. Es importante resaltar la extensión es `cljs` para ClojureScript y no `clj` para Clojure.

Nuestro programa es extremadamente sencillo. Supone que se está ejecutando como parte de una página Web. Lo que hace es obtener el `div` con id `app` y asignarle el valor "Hola ClojureScript!". Antes de eso imprime a la consola para asegurarnos que se está ejecutando nuestro código.

Eso finaliza la parte del código, ahora sigue la configuración de `cljsbuild` para indicarle en donde puede encontrar el código. Vamos a crear 2 versiones del código, una de desarrollo y una de producción. La diferencia entre ambas son principalmente las optimizaciones del compilador.

Para entender las optimizaciones, es necesario saber cómo se convierte nuestro código ClojureScript en JavaScript. ClojureScript en parte depende del compilador Closure Compiler de Google. Este compilador ofrece distintos niveles de optimización, desde ninguna optimización hasta optimizaciones avanzadas, donde se elimina todo el espacio en blanco, comentarios, signos de puntuacion extra, renombramiento de variables para minimizar el tamaño del código, eliminación de codigo muerto, entre otras.

Aunque nuestra versión de desarrollo pudiera funcionar sin problema en producción, en fases siguientes vamos a incluir código que definitivamente no queremos en nuestra aplicación en producción y por ello es bueno que hagamos esta separación de una vez por todas.

Toda la configuración la hacemos en el `project.clj`. Básicamente vamos a agregar la dependencia de ClojureScript, y a configurar el plugin `lein-cljsbuild` para poder compilar el código:

    (defproject clojurescript-hard-way "0.1.0-SNAPSHOT"
      :plugins [[lein-cljsbuild "1.1.7"]]
      :source-paths ["src/clj"]
    
      :dependencies [[org.clojure/clojure "1.8.0"]
                     [org.clojure/clojurescript "1.10.339"]]
    
      :cljsbuild {:builds
                  [{:id "dev"
                    :source-paths ["src/cljs"]
                    :compiler {:main "clojurescript-hard-way.core"
                               :output-to "cshard-dev.js"
                               :optimizations :none}}
                   {:id "min"
                    :source-paths ["src/cljs"]
                    :compiler {:main "clojurescript-hard-way.core"
                               :output-to "cshard-prod.js"
                               :closure-defines {goog.DEBUG false}
                               :optimizations :advanced
                               :pretty-print false}}]}
    
      :main ^:skip-aot clojurescript-hard-way.core
      :target-path "target/%s"
      :profiles {:uberjar {:aot :all}})

Como vemos, la novedad es la nueva dependencia de ClojureScript, y la configuración de `cljsbuild` con los dos perfiles que comentamos anteriormente: `dev` para desarrollo y `min` para producción. Incluso podríamos agregar más perfiles en caso de necesitarlo.

Ya con esta configuración podemos compilar ambos perfiles con `lein cljsbuild once` o uno en particular con `lein cljsbuild once dev`. Al hacer esto nos debe generar dos archivos: `cshard-dev.js` y `cshard-prod.js` según sea el perfil que se haya compilado.

Lo último es ejecutar el código que acabamos de compilar. Como es JavaScript, necesitamos una página Web que nos sirva de contenedor para el código. Creamos una en `resources/public`

    mkdir resources/public
    touch resources/public/index.html

Y le agregamos lo siguiente:

    <!DOCTYPE html>
    <html>
        <head>
            <meta charset="UTF-8">
        </head>
        <body>
            <div id="app"></div>
            <script src="../../cshard-dev.js" type="text/javascript"></script>
        </body>
    </html>

Si intentamos cargar `index.html` en el navegador no va a funcionar. La razón es que `cshard-dev.js` depende de código externo que no se encuentra en el mismo directorio. En Firefox el error es el siguiente:

```
Loading failed for the <script> with source “file:///Users/cesarolea/workspace/clojurescript-hard-way/resources/public/target/base+system+user+dev/cljsbuild-compiler-0/goog/base.js”.
ClojureScript could not load :main, did you forget to specify :asset-path?
```

La ruta `resources/public/target` no existe. ¿Por qué `cshard-dev.js` está tratando de cargar otros archivos desde esta ruta?

La respuesta es muy sencilla. Al compilar sin optimizaciones, el compilador genera archivos intermedios que necesitan ser cargados por nuestro código. Sin embargo nunca le indicamos al compilador en donde se encuentran estos archivos. De hecho el mismo mensaje de error tiene la respuesta:

>ClojureScript could not load :main, did you forget to specify :asset-path?

Para arreglar este problema tenemos que especificar más información al compilador:

    {:id "dev"
     :source-paths ["src/cljs"]
     :compiler {:main "clojurescript-hard-way.core"
                :output-dir "resources/public/js/compiled/out"
                :asset-path "js/compiled/out"
                :output-to "resources/public/js/compiled/cshard-dev.js"
                :optimizations :none}}

- `:main` es el nombre del *namespace* de entrada como aparece en `core.cljs`.
- `:output-dir` es la ruta de directorio en donde se van a colocar los archivos intermedios generados por el compilador.
- `:asset-path` es una ruta relativa al punto de entrada que controla de donde se van a cargar otros scripts. Esto es lo que nos hacía falta. Es importante notar que esta es una ruta relativa, no una ruta de nuestro sistema de archivos.
- `:output-to` es la ruta de directorio y nombre del archivo que va a contener nuestro código ClojureScript compilado. Este es el que se tiene que incluir en `index.html`.
- `:optimizations` es el nivel de optimizaciones (`:none` para el caso del perfil "dev")

Compilamos una vez mas:

    lein cljsbuild once dev

Ahora dentro de `resources` debemos tener la ruta `public/js/compiled/cshard-dev.js`. Finalmente cambiamos nuestro `index.html` para agregar este archivo:

    <!DOCTYPE html>
    <html>
        <head>
            <meta charset="UTF-8">
        </head>
        <body>
            <div id="app"></div>
            <script src="js/compiled/cshard-dev.js" type="text/javascript"></script>
        </body>
    </html>

Y podemos ver el resultado terminado:
    
![fase-1](/content/images/2018/08/fase-1.png)

Como último detalle, la compilación de la versión de producción en este caso no necesita de especificar `:output-dir` y `:asset-path` ya que no genera archivos intermedios y no carga otros scripts externos.

Esto concluye la Fase 1. Con esto es suficiente para empezar a programar en ClojureScript. Como se dijo en la introducción, no es la manera más sencilla de hacerlo pero si es la manera que nos va a permitir entender y extender nuestro ambiente de desarrollo, así como también adaptarlo a un proyecto Clojure existente.

En la Fase 2 veremos cómo podemos configurar nuestro proyecto para evitar tener que compilar y recargar el navegador en cada modificación al código.