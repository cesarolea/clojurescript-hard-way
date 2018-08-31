# WebJars
[WebJars](https://www.webjars.org) en sus propias palabras son librerías para hacer desarrollo del lado del cliente (_frontend_ o _client side_, como jQuery o Bootstrap) empaquetadas en formato `jar`.

¿Que por qué querríamos algo así?

El programador de _frontend_ promedio tiene que utilizar una sopa de utilerías que parece cambiar junto con el ciclo de la luna. Además la calidad de cada una de estas herramientas deja mucho que desear. Parte de la experiencia que compramos al cambiarnos a ClojureScript es precisamente escapar de ese mundo, y utilizar una sola herramienta (leiningen en nuestro caso) para manejar nuestro proyecto.

Si ahora les dijera que tienen que instalar `node`, `npm`, instalar `grunt`, `bower`, ah no ahora es `yarn`... supongo que estarían algo desilucionados.

Por eso queremos WebJars.

>Si ahora les dijera que tienen que instalar `node`, `npm`, instalar `grunt`, `bower`, ah no ahora es `yarn`... supongo que estarían algo desilucionados.

Básicamente WebJars nos permite manejar las dependencias de nuestras librerías _client side_ con leiningen. Utilizar WebJars en realidad es muy sencillo si estamos haciendo una aplicación Web tradicional (generando el HTML en el servidor). La cosa se complica un poco más cuando agregamos ClojureScript y Figwheel a la mezcla.

## ClojureScript, Figwheel y WebJars
Para propósito de este ejemplo se va a utilizar [Bulma](https://bulma.io), que es un framework CSS para nosotros que tenemos una aversión particular a CSS y 0 imaginación.

Primero revisamos el sitio de WebJars en [www.webjars.org](https://www.webjars.org) y buscamos "Bulma" y efectivamente:

    [org.webjars.npm/bulma "0.7.1"]

Ya sabemos como agregar dependencias a nuestro proyecto. Mis perfil `:dev` queda de la siguiente manera:

```
{:dev {:dependencies [[com.bhauman/figwheel-main "0.1.7"]
                        [com.bhauman/rebel-readline-cljs "0.1.4"]
                        [org.clojure/clojurescript "1.10.339"]
                        [org.clojure/tools.nrepl "0.2.13"]
                        [cider/piggieback "0.3.8"]
                        [reagent "0.8.1"]
                        [re-frame "0.10.5"]
                        [org.webjars.npm/bulma "0.7.1"]]
         :source-paths ["env/dev/clj"]
         :repl-options {:init-ns user
                        :nrepl-middleware [cider.piggieback/wrap-cljs-repl]}}}
```

En la documentación de Bulma hay un ejemplo base que explica cómo cargar Bulma en nuestra página:

```
<!DOCTYPE html>
<html>
  <head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Hello Bulma!</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/bulma/0.7.1/css/bulma.min.css">
    <script defer src="https://use.fontawesome.com/releases/v5.1.0/js/all.js"></script>
  </head>
  <body>
  <section class="section">
    <div class="container">
      <h1 class="title">
        Hello World
      </h1>
      <p class="subtitle">
        My first website with <strong>Bulma</strong>!
      </p>
    </div>
  </section>
  </body>
</html>
```

Podemos modificar nuestro `index.html` para agregar la referencia a Bulma, pero estaría cargando la librería de un sitio externo y no la que acabamos de declarar como dependencia.

Antes de poner manos a la obra, necesitamos entender cual es el problema. Leiningen va a descargar un archivo `jar` que contiene la librería, en este caso un archivo `css` minificado. Sin embargo la página `index.html` no sabe cómo cargar recursos que están dentro de un archivo `jar`. Entonces necesitamos una manera de especificar rutas relativas a recursos que se encuentran dentro de un `jar`. Eso es precisamente lo que vamos a hacer a continuación.

Aquí nos vamos a adentrar a territorio de Clojure. Específicamente desarrollo Web en Clojure. A riesgo de quedarme corto con la explicación, esto es lo que vamos a hacer:

1. Crear un servidor HTTP con `ring`.
2. Utilizar [ring-webjars](https://github.com/weavejester/ring-webjars), un _middleware_ para ring que facilita el uso de WebJars. Esto no es estrictamente necesario, pero lo hace más placentero.
3. Modificar `index.html` para hacer referencia al CSS de Bulma.

Primero las dependencias:

```
:dependencies [[org.clojure/clojure "1.9.0"]
               [mount "0.1.13"]
               [ring/ring-core "1.6.3"]
               [ring-webjars "0.2.0"]]
```

Después creamos un nuevo `namespace` que va a definir nuestro servidor HTTP con `ring`, recordando que todo esto es en **Clojure** y no **ClojureScript**.

```
(ns clojurescript-hard-way.figwheel
  (:require [ring.middleware.webjars :refer [wrap-webjars]]))

(defn handler [request]
  (if (and (= :get (:request-method request))
           (= "/" (:uri request)))
    {:status 200 :headers {"Content-Type" "text/html"} :body (slurp "resources/public/index.html")}
    {:status 404 :headers {"Content-Type" "text/plain"} :body "Not Found"}))

(def app (-> handler wrap-webjars))
```

Lo que hace nuestro servidor HTTP es declarar una sola ruta: `get /` y regresar como respuesta el contenido de `resources/public/index.html` (la página que sirve de _host_ para el código ClojureScript). Finalmente agrega el middleware `wrap-webjars`:

    (def app (-> handler wrap-webjars))

`app` va a ser una función que recibe un `request` de entrada y ejecuta una serie de funciones que forman una cadena (el _middleware_). En este caso solo hay uno: `wrap-webjars`.

La razón por la que hacemos esto es porque Figwheel tiene la capacidad de ejecutar este servidor HTTP en vez de su servidor interno, y luego modificar el HTML para hacer referencia a los _assets_ en nuestros WebJars.

El HTML nos quedaría así:

```
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <link rel="stylesheet" href="/assets/bulma/css/bulma.min.css">
    </head>
    <body>
        <div id="app"></div>
        <script src="cljs-out/dev-main.js" type="text/javascript"></script>
        <script type="text/javascript">clojurescript_hard_way.core.main()</script>
    </body>
</html>
```

La clave está en `<link rel="stylesheet" href="/assets/bulma/css/bulma.min.css">`. Esa ruta no existe físicamente, pero `ring-webjars` se encarga de leer los recursos dentro de los distintos WebJars que tengamos, de manera que podamos hacer referencia a ellos desde HTML.

Lo último es modificar Figwheel para que en vez de cargar `index.html`, cargue nuestro servidor HTTP. En `core.clj`:

```
(defstate ^{:on-reload :noop} figwheel
  :start (fw-api/start {:id "dev"
                        :options {:main 'clojurescript-hard-way.core}
                        :config {:target-dir "resources"
                                 :watch-dirs ["src/cljs"]
                                 :css-dirs []
                                 :open-url false
                                 :mode :serve
                                 :ring-handler clojurescript-hard-way.figwheel/app}})
  :stop (fw-api/stop "dev"))
```

Aquí la clave está en `:ring-handler clojurescript-hard-way.figwheel/app`. Así le decimos a Figwheel que use nuestro servidor HTTP. Esto es necesario porque si cargáramos `index.html` directamente, la ruta `/assets/bulma/css/bulma.min.css` marcaría un error.

Para comprobar lo anterior, modifiquemos `core.cljs` con lo siguiente:

```
(ns ^:figwheel-hooks clojurescript-hard-way.core
  (:require [reagent.core :as r]))

(defn bulma-example []
  [:div {:class "columns"}
   [:div {:class "column"} "First column"]
   [:div {:class "column"} "Second column"]
   [:div {:class "column"} "Third column"]
   [:div {:class "column"} "Fourth column"]])

(defn banner []
  [:div
   [:section {:class "hero"}
    [:div {:class "hero-body"}
     [:h1 {:class "title"} "ClojureScript <3 WebJars"]
     [:h2 {:class "subtitle"} "Using bulma as an example"]]]
   [bulma-example]])

(defn ^:after-load mount-root []
  (r/render [banner]
            (.getElementById js/document "app")))

(defn ^:export main []
  ;; do some other init
  (mount-root))
```

Las clases `columns`, `column`, `hero`, etc vienen definidas en el CSS de Bulma. Entonces si nuestra integración con WebJars funciona correctamente, no debemos de ver ningún error en la consola de JavaScript al hacer la petición del CSS. Veamos:

<<insertar imagen error>>

Un momento, ¡no lo encuentra!

La razón es muy sencilla: Figwheel al encontrarse con un `index.html` va a darle prioridad a entregarnos ese archivo directamente, en vez de utilizar nuestro servidor HTTP. Lo podemos comprobar renombrando `index.html`

    mv resources/public/index.html resources/public/figwheel.html

Y modificando el código del servidor HTTP para leer el contenido del archivo renombrado:

```
(ns clojurescript-hard-way.figwheel
  (:require [ring.middleware.webjars :refer [wrap-webjars]]))

(defn handler [request]
  (if (and (= :get (:request-method request))
           (= "/" (:uri request)))
    {:status 200 :headers {"Content-Type" "text/html"} :body (slurp "resources/public/figwheel.html")}
    {:status 404 :headers {"Content-Type" "text/plain"} :body "Not Found"}))

(def app (-> handler wrap-webjars))
```

Reiniciamos Figwheel y:

<<insertar imagen ok>>

_Bliss_

## Recapitulando
No hay que perder de vista la razón por la que hacemos toda esta configuración. Podrá parecer muy complicado, después de todo obtendríamos el mismo resultado modificando `index.html` directamente y agregando como fuente un CDN, y efectivamente funciona.

La cuestión es que en producción, por lo general queremos un solo `jar` que contenga todo nuestro código y las dependencias (tanto del _frontend_ como del _backend_). En este caso eliminamos la necesidad de depender de procesos y herramientas externos, simplificamos muchísimo la instalación en producción y en general la administración de la aplicación como tal. Instalarla es literalmente copiar un `jar` y ejecutarlo.

# CLJSJS
No es un trabalenguas. 